package edu.utexas.libra.clients;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.internal.JAddExpr;
import soot.options.Options;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yufeng on 6/16/17.
 */
public class InstrumentMeth {
    public static void main(String[] args) {
        String benchmark = "/home/yufeng/research/Libra/exp2-ccs17/tourPlanner/tour_demo.jar";
        String targetMeth = "<com.graphhopper.util.shapes.Circle: boolean intersect(com.graphhopper.util.shapes.BBox)>";

        StringBuilder options = new StringBuilder();
        options.append("-prepend-classpath");
        options.append(" -full-resolver");
        options.append(" -allow-phantom-refs");
        StringBuilder cp = new StringBuilder();
        cp.append(benchmark);
        cp.append(":");
        options.append(" -process-dir " + benchmark);
        options.append(" -cp " + cp.toString());

        if (!Options.v().parse(options.toString().split(" ")))
            throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED,
                    "Option parse error");

        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

            @Override
            protected void internalTransform(Body body, String s, Map<String, String> map) {
                if(!body.getMethod().getSignature().equals(targetMeth)) return;

                //declaration.
                Local counter = Jimple.v().newLocal("myCounter", IntType.v());
                body.getLocals().add(counter);
                Chain<Unit> units = body.getUnits();
                Unit thisUnit = units.getFirst();
                AssignStmt init = Jimple.v().newAssignStmt(counter, IntConstant.v(0));
                System.out.println("init "+ init);
                List<Unit> copyList = new ArrayList<>();
                Iterator<Unit> iter = units.snapshotIterator();

                //init with 0;
                units.insertAfter(init, thisUnit);

                System.out.println(units.getFirst());
                int i = 0;
                copyList.add(units.getFirst());
                while(iter.hasNext()) {
                    Unit unit = iter.next();
                    if(i==0) continue;
                    AssignStmt assign = Jimple.v().newAssignStmt(counter, new JAddExpr(counter, IntConstant.v(1)));
                    units.insertAfter(assign, unit);
                    i++;
                }



                System.out.println(body.getMethod().getSignature() + " " + body);

            }
        }));

        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }
}
