package edu.utexas.libra.translator.block;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;

import java.util.*;
import java.util.stream.Collectors;

public class CodeBlockBuilder {

    private Map<Unit, Loop> loopMap;
    private MHGPostDominatorsFinder<Unit> pDomFinder;

    public CodeBlockBuilder(LoopNestTree loopNestTree, MHGPostDominatorsFinder<Unit>
            pDomFinder) {
        Map<Unit, Loop> map = new HashMap<>();
        for (Loop loop: loopNestTree)
            map.put(loop.getHead(), loop);

        this.loopMap = map;
        this.pDomFinder = pDomFinder;
    }

    private CompareCondition.Operator getCompareOperator(Value cond) {
        if (cond instanceof EqExpr)
            return CompareCondition.Operator.EQ;
       else if (cond instanceof NeExpr)
            return CompareCondition.Operator.NE;
        else if (cond instanceof GeExpr)
            return CompareCondition.Operator.GE;
        else if (cond instanceof GtExpr)
            return CompareCondition.Operator.GT;
        else if (cond instanceof LeExpr)
            return CompareCondition.Operator.LE;
        else if (cond instanceof LtExpr)
            return CompareCondition.Operator.LT;
        else
            return null;
    }

    private Condition conditionFromValue(Value cond) {
        if (cond instanceof BinopExpr) {
            BinopExpr binopExpr = (BinopExpr) cond;
            CompareCondition.Operator operator = getCompareOperator(binopExpr);
            if (operator != null)
                return new CompareCondition(operator, binopExpr.getOp1(), binopExpr.getOp2());
        } else if (cond instanceof NegExpr) {
            NegExpr negExpr = (NegExpr) cond;
            return new NegateCondition(negExpr.getOp());
        }
        return new ValueCondition(cond);
    }

    private Condition getLoopConditionFromIf(Unit unit) {
        if (unit instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) unit;
            Condition cond = conditionFromValue(ifStmt.getCondition());
            return cond.negate();
        } else
            throw new RuntimeException("getLoopConditionFromIf() does not recognize: " + unit);
    }

    private Stmt getElseTarget(IfStmt ifStmt, List<Unit> succs) {
        if (succs.size() != 2)
            throw new RuntimeException("getElseTarget() only accepts stmts with 2 successors");

        Stmt tgt = ifStmt.getTarget();
        if (succs.get(0) == tgt)
            return (Stmt) succs.get(1);
        else
            return (Stmt) succs.get(0);
    }

    private void addUnitToBlockList(List<CodeBlock> blocks, Unit unit) {
        SeqCodeBlock lastSeqBlock = null;
        if (!blocks.isEmpty()) {
            CodeBlock lastBlock = blocks.get(blocks.size() - 1);
            if (lastBlock instanceof SeqCodeBlock)
                lastSeqBlock = (SeqCodeBlock)lastBlock;
        }
        if (lastSeqBlock == null) {
            lastSeqBlock = new SeqCodeBlock();
            blocks.add(lastSeqBlock);
        }
        lastSeqBlock.add(unit);
    }

    private static class BreakInfo {
        private Stmt breakTarget;
        private Stmt exitTarget;

        private BreakInfo(Stmt breakTarget, Stmt exitTarget) {
            this.breakTarget = breakTarget;
            this.exitTarget = exitTarget;
        }
    }

    private Stmt getMainLoopExit(Collection<Stmt> exits, UnitGraph cfg, Stmt backJumpStmt) {
        Stmt next = backJumpStmt;
        while (true) {
            if (exits.contains(next))
                return next;

            List<Unit> succs = cfg.getSuccsOf(next);
            if (succs.size() != 1)
                throw new RuntimeException("Main loop exit discovery should not see >1 successors");
            next = (Stmt) succs.get(0);
        }
    }

    private List<CodeBlock> buildFromGraphImpl(UnitGraph cfg, List<Unit> entries, Unit exit, Set<Unit> visitedSet, Map<Stmt, BreakInfo> breakMap) {
        Queue<Unit> workList = new ArrayDeque<>();
        List<CodeBlock> retList = new ArrayList<>();

        workList.addAll(entries);
        while (!workList.isEmpty()) {
            Unit currUnit = workList.poll();

            if (currUnit == exit)
                continue;

            if (visitedSet.contains(currUnit))
                continue;
            visitedSet.add(currUnit);

            // See if the given stmt is a loop header
            Loop loopBody = loopMap.get(currUnit);
            if (loopBody != null) {

                Collection<Stmt> loopExits = loopBody.getLoopExits();
                Stmt backJumpStmt = loopBody.getBackJumpStmt();
                Stmt loopExit = getMainLoopExit(loopExits, cfg, backJumpStmt);
                List<Stmt> otherExits = new ArrayList<>();
                for (Stmt stmt: loopExits) {
                    if (stmt != loopExit)
                        otherExits.add(stmt);
                }

                Collection<Stmt> loopExitTargets = loopBody.targetsOfLoopExit(loopExit);
                if (loopExitTargets.size() > 1)
                    throw new RuntimeException("Loop exit with >1 target not supported");
                Stmt loopExitTarget = loopExitTargets.iterator().next();

                Map<Stmt, BreakInfo> loopBreakMap = null;
                if (!otherExits.isEmpty()) {
                    loopBreakMap = new HashMap<>();
                    for (Stmt extraExit: otherExits) {
                        Collection<Stmt> targets = loopBody.targetsOfLoopExit(extraExit);
                        if (targets.size() > 1)
                            throw new RuntimeException("Loop exit with >1 target not supported");
                        Stmt extraTarget = targets.iterator().next();
                        loopBreakMap.put(extraExit, new BreakInfo(extraTarget, loopExitTarget));
                    }
                }

                if (currUnit == loopExit) {
                    List<Unit> loopStarts = cfg.getSuccsOf(currUnit).stream().filter(u -> !loopExitTargets.contains(u)).collect(Collectors.toList());
                    List<CodeBlock> bodyBlocks = buildFromGraphImpl(cfg, loopStarts, backJumpStmt, visitedSet, loopBreakMap);
                    LoopCodeBlock loopCodeBlock = new LoopCodeBlock(getLoopConditionFromIf(currUnit), bodyBlocks);
                    retList.add(loopCodeBlock);
                }
                else if (currUnit instanceof AssignStmt && loopExit instanceof IfStmt) {
                    AssignStmt assignStmt = (AssignStmt) currUnit;
                    IfStmt ifStmt = (IfStmt) loopExit;
                    Value assignTarget = assignStmt.getLeftOp();
                    Value ifCond = ifStmt.getCondition();
                    if (!(assignTarget instanceof Local && ifCond instanceof BinopExpr))
                        throw new RuntimeException("Loop format not supported yet");
                    Local local = (Local) assignTarget;
                    Value rhs = assignStmt.getRightOp();
                    BinopExpr binExpr = (BinopExpr) ifCond;
                    Value binLhs = binExpr.getOp1();
                    Value binRhs = binExpr.getOp2();
                    CompareCondition.Operator cmpOp = getCompareOperator(ifCond);
                    if (cmpOp == null)
                        throw new RuntimeException("Loop format not supported yet");
                    Condition cond = null;
                    if (binLhs instanceof Local) {
                        Local binLhsLocal = (Local) binLhs;
                        if (binLhsLocal.getName().equals(local.getName())) {
                            cond = new CompareCondition(CompareCondition
                                    .Operator.negate(cmpOp), rhs, binRhs);
                        }
                    }
                    if (cond == null && binRhs instanceof Local) {
                        Local binRhsLocal = (Local) binRhs;
                        if (binRhsLocal.getName().equals(local.getName())) {
                            cond = new CompareCondition(CompareCondition.Operator.negate(cmpOp), binLhs, rhs);
                        }

                    }
                    if (cond == null)
                        throw new RuntimeException("Loop format not supported yet");

                    List<Unit> loopStarts = cfg.getSuccsOf(loopExit).stream().filter(u -> !loopExitTargets.contains(u)).collect(Collectors.toList());
                    List<CodeBlock> bodyBlocks = buildFromGraphImpl(cfg, loopStarts, backJumpStmt, visitedSet, loopBreakMap);

                    LoopCodeBlock loopCodeBlock = new LoopCodeBlock(cond, bodyBlocks);
                    retList.add(loopCodeBlock);
                } else
                    throw new RuntimeException("Loop format not supported yet");

                workList.addAll(loopExitTargets);
                continue;
            }

            List<Unit> succs = cfg.getSuccsOf(currUnit);
            if (succs.size() > 1) {
                if (succs.size() > 2)
                    throw new RuntimeException("Jumping to more than 2 branches is not supported yet");

                IfStmt ifStmt = (IfStmt) currUnit;
                Unit pDom = pDomFinder.getImmediateDominator(ifStmt);
                Value condValue = ifStmt.getCondition();
                Stmt thenTarget = ifStmt.getTarget();
                Stmt elseTarget = getElseTarget(ifStmt, succs);
                BranchCodeBlock branchBlock = null;
                if (visitedSet.contains(thenTarget)) {
                    // This might be a continue stmt in the loop
                    List<CodeBlock> emptyBlock = Collections.emptyList();
                    List<CodeBlock> subBlock = buildFromGraphImpl(cfg, Collections.singletonList(elseTarget), pDom, visitedSet, null);
                    Condition negCond = conditionFromValue(condValue).negate();
                    branchBlock = new BranchCodeBlock(negCond, subBlock, emptyBlock);
                    pDom = null;
                } else if (breakMap != null && breakMap.containsKey(ifStmt)) {
                    // This might be a break stmt in the loop
                    BreakInfo breakInfo = breakMap.get(ifStmt);
                    List<CodeBlock> emptyBlock = Collections.emptyList();
                    List<CodeBlock> subBlock = buildFromGraphImpl(cfg, Collections.singletonList(breakInfo.breakTarget), breakInfo.exitTarget, visitedSet, null);
                    Condition cond = conditionFromValue(condValue);
                    if (breakInfo.breakTarget != thenTarget)
                        cond.negate();

                    if (subBlock.isEmpty())
                        throw new RuntimeException("Break block should not be empty");
                    CodeBlock lastBlock = subBlock.get(subBlock.size() - 1);
                    if (!(lastBlock instanceof SeqCodeBlock))
                        throw new RuntimeException("Break block should end with seqblock");
                    SeqCodeBlock lastSeqBlock = (SeqCodeBlock) lastBlock;
                    List<Unit> lastSeqBlockUnits = lastSeqBlock.getElements();
                    if (lastSeqBlockUnits.isEmpty())
                        throw new RuntimeException("Break block should not end with empty seqblock");
                    Unit lastUnit = lastSeqBlockUnits.get(lastSeqBlockUnits.size() - 1);
                    if (lastUnit instanceof GotoStmt)
                        lastSeqBlock.setTerminator(new BreakTerminator());
                    else if (lastUnit instanceof ReturnStmt) {
                        ReturnStmt returnStmt = (ReturnStmt) lastUnit;
                        lastSeqBlock.setTerminator(new ReturnTerminator(returnStmt.getOp()));
                    } else if (lastUnit instanceof ReturnVoidStmt) {
                        lastSeqBlock.setTerminator(new ReturnTerminator(null));
                    } else
                        throw new RuntimeException("Unrecognized terminator: " + lastUnit);
                    lastSeqBlock.dropLast();

                    branchBlock = new BranchCodeBlock(cond, subBlock, emptyBlock);
                } else {
                    // Ordinary if stmt

                    // Hack: Only change visitedSet on one branch to prevent mistransformation when nested if stmt share their branches
                    Set<Unit> clonedVisitedSet = new HashSet<>(visitedSet);
                    List<CodeBlock> subBlock0 = buildFromGraphImpl(cfg, Collections.singletonList(thenTarget), pDom, clonedVisitedSet, breakMap);
                    List<CodeBlock> subBlock1 = buildFromGraphImpl(cfg, Collections.singletonList(elseTarget), pDom, visitedSet, breakMap);
                    branchBlock = new BranchCodeBlock(conditionFromValue(condValue), subBlock0, subBlock1);
                }

                retList.add(branchBlock);
                if (pDom != null)
                    workList.add(pDom);
            } else {
                addUnitToBlockList(retList, currUnit);
                workList.addAll(succs);
            }
        }
        return retList;
    }

    public List<CodeBlock> buildFromGraph(UnitGraph cfg) {
        if (cfg == null)
            throw new IllegalArgumentException();
        if (cfg.size() == 0)
            return Collections.emptyList();

        List<Unit> entryUnit = Collections.singletonList(cfg.getBody().getUnits().getFirst());
        Set<Unit> visitedSet = new HashSet<>();
        return buildFromGraphImpl(cfg, entryUnit, null, visitedSet, null);
    }
}
