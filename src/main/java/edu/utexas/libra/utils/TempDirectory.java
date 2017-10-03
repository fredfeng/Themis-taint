package edu.utexas.libra.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TempDirectory {
    final Path path;

    public TempDirectory(String prefix) throws IOException {
        if (prefix != null)
            path = Files.createTempDirectory(prefix);
        else
            path = Files.createTempDirectory("");
    }

    public Path getPath() {
        return path;
    }

    public void deleteOnExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> delete()));
    }

    private void delete() {
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException e)
                        throws IOException {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                    // directory iteration failed
                    throw e;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete " + path, e);
        }
    }

}