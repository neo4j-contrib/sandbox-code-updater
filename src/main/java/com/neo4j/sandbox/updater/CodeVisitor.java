package com.neo4j.sandbox.updater;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CodeVisitor extends SimpleFileVisitor<Path> {

    private static final Pattern EXAMPLE_FILE_REGEX = Pattern.compile("[a-z]xample\\.[a-z]+");

    private final List<Path> matchedFiles = new ArrayList<>(6);

    private final boolean sandboxHasGraphqlSchema;

    public CodeVisitor(Path sandboxPath) {
        this.sandboxHasGraphqlSchema = sandboxPath.resolve("graphql").resolve("schema.graphql").toFile().exists();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String lowerCaseFileName = file.toFile().getName().toLowerCase(Locale.ENGLISH);
        if (!isCodeSampleFile(lowerCaseFileName)) {
            return super.visitFile(file, attrs);
        }
        if (isGraphqlSample(file) && !sandboxHasGraphqlSchema) {
            return FileVisitResult.SKIP_SIBLINGS;
        }
        matchedFiles.add(file);
        return FileVisitResult.CONTINUE;
    }

    private boolean isCodeSampleFile(String lowerCaseFileName) {
        return EXAMPLE_FILE_REGEX.matcher(lowerCaseFileName).find();
    }

    private boolean isGraphqlSample(Path file) {
        return file.endsWith(Paths.get("graphql", "example.js"));
    }

    public List<Path> getMatchedFiles() {
        return matchedFiles;
    }
}
