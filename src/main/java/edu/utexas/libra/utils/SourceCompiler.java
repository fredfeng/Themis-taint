package edu.utexas.libra.utils;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SourceCompiler {

    public static void compileToDirectory(Path filePath, String outputDir) {
        compileToDirectory(Collections.singletonList(filePath), outputDir);
    }

    public static void compileToDirectory(List<Path> filePaths, String outputDir) {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager sjfm = javaCompiler.getStandardFileManager(null, null, null);

        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(outputDir);
        File[] srcFiles = filePaths.stream()
                .map(p -> p.toFile())
                .toArray(size -> new File[filePaths.size()]);

        JavaCompiler.CompilationTask compilationTask = javaCompiler.getTask(null, null, null,
                options,
                null,
                sjfm.getJavaFileObjects(srcFiles)
        );

        compilationTask.call();
    }
}
