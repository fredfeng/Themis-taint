package edu.utexas.libra.clients;

import soot.CompilationDeathException;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

/**
 * Created by yufeng on 6/16/17.
 */
public class PrintMethods {
    public static void main(String[] args) {
        if(args.length < 1) {
            System.exit(0);
        }
        String benchmark = args[0];
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
        System.out.println("Jar file:" + benchmark + " Application methods: ------------------------------------");
        for (SootClass clz:
             Scene.v().getApplicationClasses()) {
            for (SootMethod meth:
                 clz.getMethods()) {
                System.out.println(meth);
            }
        }


    }
}
