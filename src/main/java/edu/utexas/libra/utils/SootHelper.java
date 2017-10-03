package edu.utexas.libra.utils;

import soot.*;
import soot.options.Options;

import java.util.Collections;
import java.util.List;

public class SootHelper {
    public static void loadClasses(String classPath, List<String> classes) {
        Options sootOpt = Options.v();
        sootOpt.set_soot_classpath(classPath);
        sootOpt.set_keep_line_number(true);
        sootOpt.set_output_format(soot.options.Options.output_format_none);
        sootOpt.set_allow_phantom_refs(true);
        sootOpt.set_whole_program(true);
        sootOpt.set_output_dir("/dev/null");

        for (String s : classes) {
            Scene.v().addBasicClass(s, SootClass.BODIES);
        }
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Thread", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.ThreadGroup", SootClass.SIGNATURES);

        Scene.v().addBasicClass("java.lang.ClassLoader", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.security.PrivilegedActionException", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.ref.Finalizer", SootClass.SIGNATURES);

        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
    }

    public static SootMethod getMethodInClass(String methodName, String className) {
        SootClass cl = Scene.v().getSootClass(className);
        SootMethod method = null;
        for (SootMethod m: cl.getMethods()) {
            if (m.getName().equals(methodName)) {
                if (method == null)
                    method = m;
                else
                    throw new IllegalArgumentException("SootHelper.getMethodInClass() found overloaded methods!");
            }
        }
        return method;
    }

    public static void setEntryPoint(SootMethod entryPoint) {
        setEntryPoints(Collections.singletonList(entryPoint));
    }

    public static void setEntryPoints(List<SootMethod> entryPoints) {
        Scene.v().setEntryPoints(entryPoints);
    }

    public static void runSootPacks() {
        PhaseOptions.v().setPhaseOption("cg", "enabled:true");
        PhaseOptions.v().setPhaseOption("cg.cha", "enabled:false");
        PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
        Pack cgPack = PackManager.v().getPack("cg");
        cgPack.apply();
    }
}
