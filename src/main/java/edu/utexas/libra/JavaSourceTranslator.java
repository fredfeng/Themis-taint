package edu.utexas.libra;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import edu.utexas.libra.themis.ast.Program;
import edu.utexas.libra.themis.visitor.PrettyPrinter;
import edu.utexas.libra.translator.HotspotMethod;
import edu.utexas.libra.translator.SootMethodTranslator;
import edu.utexas.libra.utils.SootHelper;
import edu.utexas.libra.utils.SourceCompiler;
import edu.utexas.libra.utils.TempDirectory;
import soot.SootMethod;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Invoke Soot-to-Themis translator without going through taint analysis
 */
public class JavaSourceTranslator {

    private static class Args {
        @Parameter(description = "Filenames ", required = true)
        private List<String> fileNames;

        @Parameter(names = {"--class", "-c"}, description = "Hotspot method's declaring class name", required = true)
        private String className;

        @Parameter(names = {"--method", "-m"}, description = "Hotspot method's method name (overloading method currently not supported", required = true)
        private String methodName;

        @Parameter(names = {"--package", "-p"}, description = "Hotspot method's package name")
        private String pkgName = "";

        @Parameter(names = {"--model", "-d"}, description = "Location of the model jar file")
        private String modelJar = "model.jar";

        @Parameter(names = {"--high", "-h"}, description = "The high parameter indices.")
        private List<Integer> highIndices = Collections.emptyList();

        @Parameter(names= {"--output", "-o"}, description = "Path of the output file. Results are printed to stdout by default.")
        private String output = "";

        @Parameter(names = "--help", help = true, description = "Print usage")
        private boolean help = false;
    }

    public static void main(String[] argv) throws IOException {
        Args args = new Args();
        JCommander cmdParser = JCommander.newBuilder().addObject(args).build();
        try {
            cmdParser.parse(argv);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            System.err.println("To see usage information, use the --help flag");
            System.exit(-1);
        }

        if (args.help) {
            cmdParser.usage();
            System.exit(0);
        }

        run(args);
    }

    private static void run(Args args) throws IOException {
        TempDirectory tmpRoot = new TempDirectory("libraRoot");
        tmpRoot.deleteOnExit();

        Program prog = doTranslate(args, tmpRoot);
        PrintWriter pw;
        if (args.output.isEmpty())
            pw = new PrintWriter(System.out, true);
        else
            pw = new PrintWriter(args.output);
        PrettyPrinter.print(pw, prog);
    }

    private static  Program doTranslate(Args args, TempDirectory tmpRoot) {
        List<Path> filePaths = args.fileNames.stream()
                .map(f -> Paths.get(f)).collect(Collectors.toList());
        String fullClassName = args.pkgName.isEmpty() ? args.className : args.pkgName + "." + args.className;
        List<String> classNames = Collections.singletonList(fullClassName);
        String classPath = String.join(":", tmpRoot.getPath().toString(), args.modelJar);

        SourceCompiler.compileToDirectory(filePaths, tmpRoot.getPath().toString());
        SootHelper.loadClasses(classPath, classNames);
        SootMethod method = SootHelper.getMethodInClass(args.methodName, fullClassName);
        SootHelper.setEntryPoint(method);
        SootHelper.runSootPacks();

        HotspotMethod hsMethod = HotspotMethod.fromMethodWithHigh(method, args.highIndices);
        return SootMethodTranslator.translateFromHotspot(hsMethod, Collections.emptySet());
    }
}
