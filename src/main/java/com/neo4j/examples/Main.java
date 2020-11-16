package com.neo4j.examples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new RuntimeException("Expected at least 1 argument");
        }
        SandboxCodeUpdater codeUpdater = new SandboxCodeUpdater(
                new GitCliRepositoryCloner(),
                new SandboxMetadataReader(),
                getTemplateRootFolder(System.getenv("TEMPLATE_REPO_CLONE_DIR"))
        );
        codeUpdater.update(args[0], getCloneLocation(args));
    }

    private static Path getCloneLocation(String[] args) {
        if (args.length == 2) {
            return Path.of(args[1]);
        }
        String temporaryDirectory = System.getProperty("java.io.tmpdir");
        Path tempDirectory = new File(temporaryDirectory, randomize("sandbox-updater")).toPath();
        System.out.printf("Repository will be cloned here: %s%n", tempDirectory);
        return tempDirectory;
    }

    private static String randomize(String base) {
        return base + System.nanoTime();
    }

    private static Path getTemplateRootFolder(String templateDirectory) {
        Path result = Path.of(templateDirectory);
        if (!result.toFile().isDirectory()) {
            throw new RuntimeException(
                    String.format("Expected TEMPLATE_REPO_CLONE_DIR envvar to point to neo4j-graph-examples/template clone directory: %s is not a valid directory", result)
            );
        }
        return result;
    }
}
