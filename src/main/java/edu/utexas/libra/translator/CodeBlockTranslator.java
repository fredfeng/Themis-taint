package edu.utexas.libra.translator;

import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.stmt.DeclareStmt;
import edu.utexas.libra.themis.ast.stmt.SkipStmt;
import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.ast.type.Type;
import edu.utexas.libra.themis.builder.AstBuilder;
import edu.utexas.libra.translator.block.*;
import edu.utexas.libra.translator.util.SootAllocSiteMap;
import edu.utexas.libra.translator.util.SootFieldMap;
import edu.utexas.libra.translator.util.SootLocalMap;
import edu.utexas.libra.translator.util.SootParamMap;
import soot.SootMethod;
import soot.Unit;
import soot.Value;

import java.util.*;
import java.util.stream.Collectors;

public class CodeBlockTranslator implements CodeBlockVisitor<Void> {
    private AstBuilder astBuilder;
    private SootFieldMap fieldMap;
    private SootAllocSiteMap allocSiteMap;
    private SootParamMap paramMap;
    private SootLocalMap localMap;
    private SootMethod method;
    private SootTypeTranslator typeTranslator;

    private List<Stmt> resultStmts;

    public CodeBlockTranslator(AstBuilder astBuilder,
                               SootFieldMap fieldMap,
                               SootAllocSiteMap allocSiteMap,
                               SootParamMap paramMap,
                               SootLocalMap localMap,
                               SootMethod method,
                               List<Stmt> resultStmts) {
        this.astBuilder = astBuilder;
        this.resultStmts = resultStmts;
        this.fieldMap = fieldMap;
        this.allocSiteMap = allocSiteMap;
        this.paramMap = paramMap;
        this.localMap = localMap;
        this.method = method;
        this.typeTranslator = new SootTypeTranslator(astBuilder);
    }

    @Override
    public Void visit(SeqCodeBlock seqBlock) {
        SootStmtTranslator stmtTranslator = new SootStmtTranslator(
                astBuilder,
                typeTranslator,
                fieldMap,
                allocSiteMap,
                paramMap,
                localMap,
                method,
                resultStmts
        );
        List<Unit> seqBody = seqBlock.getElements();
        for (Unit unit: seqBody) {
            stmtTranslator.translateUnit(unit);
        }

        Terminator terminator = seqBlock.getTerminator();
        if (terminator != null)
            stmtTranslator.translateTerminator(terminator);

        return null;
    }

    private List<Stmt> translateCodeBlocksImmediately(List<CodeBlock> blocks) {
        List<Stmt> stmts = new ArrayList<>();
        new CodeBlockTranslator(
                astBuilder,
                fieldMap,
                allocSiteMap,
                paramMap,
                localMap,
                method,
                stmts
        ).translateCodeBlocks(blocks);
        return stmts;
    }

    @Override
    public Void visit(BranchCodeBlock branchBlock) {
        Condition cond = branchBlock.getBranchCondition();

        Stmt trueBranch = compressStmt(astBuilder, translateCodeBlocksImmediately(branchBlock.getTrueBlocks()));
        Stmt falseBranch = compressStmt(astBuilder, translateCodeBlocksImmediately(branchBlock.getFalseBlocks()));

        SootStmtTranslator stmtTranslator = new SootStmtTranslator(
                astBuilder,
                typeTranslator,
                fieldMap,
                allocSiteMap,
                paramMap,
                localMap,
                method,
                resultStmts
        );
        stmtTranslator.translateBranch(cond, trueBranch, falseBranch);

        return null;
    }

    @Override
    public Void visit(LoopCodeBlock loopBlock) {
        Condition cond = loopBlock.getLoopCondition();

        Stmt body = compressStmt(astBuilder, translateCodeBlocksImmediately(loopBlock.getBodyBlocks()));

        SootStmtTranslator stmtTranslator = new SootStmtTranslator(
                astBuilder,
                typeTranslator,
                fieldMap,
                allocSiteMap,
                paramMap,
                localMap,
                method,
                resultStmts
        );
        stmtTranslator.translateLoop(cond, body);

        return null;
    }

    public void translateCodeBlocks(List<CodeBlock> codeBlocks) {
        for (CodeBlock block: codeBlocks) {
            block.accept(this);
        }
    }

    public List<Stmt> getResultStmts() {
        return resultStmts;
    }

    private List<Stmt> collectDeclareStmts() {
        Collection<SootLocalMap.LocalInfo> declares = localMap.declareEntries();
        List<Stmt> declStmts = new ArrayList<>(declares.size());
        if (!declares.isEmpty()) {
            for (SootLocalMap.LocalInfo info: declares) {
                VarDecl decl = astBuilder.makeVariableDecl(info.getType(), info.getName());
                if (info.getType().isInt()) {
                    Expr initExpr = astBuilder.makeIntConstantExpr(info.getId());
                    DeclareStmt declStmt = astBuilder.makeDeclareStmt(decl, initExpr);
                    declStmts.add(declStmt);
                } else {
                    DeclareStmt declStmt = astBuilder.makeDefaultDeclareStmt(decl);
                    declStmts.add(declStmt);
                }

            }
        }
        return declStmts;
    }

    public static Stmt translate(AstBuilder astBuilder,
                                 SootFieldMap fieldMap,
                                 SootAllocSiteMap allocSiteMap,
                                 SootParamMap paramMap,
                                 SootMethod method,
                                 List<CodeBlock> codeBlocks) {
        SootLocalMap localMap = new SootLocalMap();
        CodeBlockTranslator translator = new CodeBlockTranslator(
                astBuilder,
                fieldMap,
                allocSiteMap,
                paramMap,
                localMap,
                method,
                new ArrayList<>()
        );
        translator.translateCodeBlocks(codeBlocks);

        List<Stmt> stmts = translator.collectDeclareStmts();
        stmts.addAll(translator.getResultStmts());
        return compressStmt(astBuilder, stmts);
    }

    public static Stmt compressStmt(AstBuilder astBuilder, List<Stmt> stmts) {
        List<Stmt> filterStmts = stmts.stream()
                .filter(s -> !(s instanceof SkipStmt)).collect(Collectors.toList());
        if (filterStmts.isEmpty())
            return astBuilder.makeSkipStmt();
        if (filterStmts.size() == 1)
            return stmts.get(0);
        return astBuilder.makeBlockStmt(filterStmts);
    }
}
