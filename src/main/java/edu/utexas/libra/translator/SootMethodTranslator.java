package edu.utexas.libra.translator;

import edu.utexas.libra.themis.ast.Program;
import edu.utexas.libra.themis.ast.expr.BinaryOperator;
import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.func.Function;
import edu.utexas.libra.themis.ast.func.Param;
import edu.utexas.libra.themis.ast.func.ParamAnnotation;
import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.lvalue.Lvalue;
import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.ast.type.Type;
import edu.utexas.libra.themis.builder.AstBuilder;
import edu.utexas.libra.themis.builder.ThemisAstBuilder;
import edu.utexas.libra.translator.block.CodeBlock;
import edu.utexas.libra.translator.block.CodeBlockBuilder;
import edu.utexas.libra.translator.util.SootAllocSiteMap;
import edu.utexas.libra.translator.util.SootFieldMap;
import edu.utexas.libra.translator.util.SootParamMap;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class SootMethodTranslator {

    private static final String MAIN_FUNCTION_NAME = "main";

    private AstBuilder astBuilder;
    private SootTypeTranslator typeTranslator;
    private SootFieldMap fieldMap;
    private SootAllocSiteMap allocSiteMap;
    private Map<String, Function> functionMap;
    private SootMethodTranslator(AstBuilder astBuilder) {
        this.astBuilder = astBuilder;
        typeTranslator = new SootTypeTranslator(astBuilder);
        functionMap = new HashMap<>();
        fieldMap = new SootFieldMap();
        allocSiteMap = new SootAllocSiteMap();
    }

    private void addFunction(Function func) {
        String funcName = func.getName();
        if (!functionMap.containsKey(funcName))
            functionMap.put(funcName, func);
        else
            throw new RuntimeException("Functions with duplicate names detected: " + funcName);
    }

    private Program makeProgram(HotspotMethod hotspotMethod) {
        fabricateMainFunction(hotspotMethod);
        return astBuilder.makeProgram(functionMap.values());
    }

    public static String translateMethodName(SootMethod method) {
        return method.getBytecodeSignature()
                .replaceAll("[^\\w]", "_");
    }

    private void translateMethod(SootMethod method, List<CodeBlock> blocks) {
        String methodName = translateMethodName(method);
        SootParamMap paramMap = new SootParamMap();
        Stmt body = CodeBlockTranslator.translate(astBuilder, fieldMap, allocSiteMap, paramMap, method, blocks);
        List<Param> paramList = paramMap.getParamList();
        Type retType = typeTranslator.translateType(method.getReturnType());
        Function func = astBuilder.makeFunction(methodName, paramList, retType, body);
        addFunction(func);
    }

    private void fabricateMainFunction(HotspotMethod hotspotMethod) {
        String entryName = translateMethodName(hotspotMethod.getMethod());
        Function entryFunc = functionMap.get(entryName);
        if (entryFunc == null)
            throw new RuntimeException("Cannot find entry method in functionMap");

        List<Param> mainParams = new ArrayList<>();
        List<Expr> entryArgs = new ArrayList<>();

        List<Param> entryParams = entryFunc.getParameters();
        List<ParamAnnotation> hotspotAnnotations = hotspotMethod.getAnnotations();
        for (int i = 0; i < entryParams.size(); ++i) {
            VarDecl decl = entryParams.get(i).getDecl();
            ParamAnnotation annot = hotspotAnnotations.get(i);
            Param mainParam = astBuilder.makeParam(astBuilder.makeVariableDecl(decl.getType(), decl.getName()), annot);
            mainParams.add(mainParam);

            Lvalue argRef = astBuilder.makeVariableRef(decl.getName());
            entryArgs.add(astBuilder.makeValueFetchExpr(argRef));
        }

        Type entryRetType = entryFunc.getReturnType();
        String dummyName = "_";
        VarDecl dummyDecl = astBuilder.makeVariableDecl(entryRetType, dummyName);
        Expr callExpr = astBuilder.makeCallExpr(entryName, entryArgs);
        Stmt callStmt = astBuilder.makeDeclareStmt(dummyDecl, callExpr);

        Type mainRetType = astBuilder.makeUnitType();
        Function mainFunc = astBuilder.makeFunction(MAIN_FUNCTION_NAME, mainParams, mainRetType, callStmt);
        addFunction(mainFunc);
    }

    public static Program translateFromHotspot(HotspotMethod hotspotMethod, Set<SootMethod> modelMethodSet) {
        CallGraph cg = Scene.v().getCallGraph();
        SootMethod entryMethod = hotspotMethod.getMethod();
        Set<SootMethod> reachableMethods = getReachableMethods(cg, entryMethod, modelMethodSet);
        System.out.println("Number of reachable methods: " + reachableMethods.size());
//        for (SootMethod method: reachableMethods)
//            System.out.println(method.getSignature());

        SootMethodTranslator translator = new SootMethodTranslator(new ThemisAstBuilder());
        for (SootMethod method: reachableMethods) {
            Body body = method.retrieveActiveBody();
            if (body == null)
                throw new RuntimeException("Method " + method.getSignature() + " missing active body");
            UnitGraph cfg = new BriefUnitGraph(body);
            LoopNestTree loopNestTree = new LoopNestTree(body);
            MHGPostDominatorsFinder<Unit> postDominatorsFinder = new MHGPostDominatorsFinder<>(cfg);
            CodeBlockBuilder blockBuilder = new CodeBlockBuilder(loopNestTree, postDominatorsFinder);
            List<CodeBlock> blocks = blockBuilder.buildFromGraph(cfg);

            translator.translateMethod(method, blocks);
        }

        return translator.makeProgram(hotspotMethod);
    }

    private static Set<SootMethod> getReachableMethods(CallGraph cg, SootMethod entryMethod, Set<SootMethod> modelMethodSet) {
        Set<SootMethod> reachableSet = new HashSet<>();
        Queue<SootMethod> workList = new ArrayDeque<>();
        workList.add(entryMethod);

        while (!workList.isEmpty()) {
            SootMethod currMethod = workList.poll();
            if (reachableSet.contains(currMethod))
                continue;
            reachableSet.add(currMethod);

            for (Iterator<Edge> it = cg.edgesOutOf(currMethod); it.hasNext(); ) {
                Edge mmc = it.next();

                // Hack: ignore static initializers for now
                if (mmc.isClinit())
                    continue;

                SootMethod tgtMethod = mmc.tgt();
                if (tgtMethod.isPhantom())
                    continue;
                if (modelMethodSet.contains(tgtMethod))
                    continue;

                workList.add(tgtMethod);
            }
        }
        return reachableSet;
    }
}
