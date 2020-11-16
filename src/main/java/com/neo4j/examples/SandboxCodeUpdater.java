package com.neo4j.examples;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

public class SandboxCodeUpdater {

    private final RepositoryCloner cloner;
    private final SandboxMetadataReader metadataReader;
    private final Path sourceCodePath;

    public SandboxCodeUpdater(RepositoryCloner cloner, SandboxMetadataReader metadataReader, Path sourceCodePath) {
        this.cloner = cloner;
        this.metadataReader = metadataReader;
        this.sourceCodePath = sourceCodePath;
    }

    public void update(String repositoryName, Path cloneLocation) throws IOException {
        String repositoryUri = String.format("https://github.com/neo4j-graph-examples/%s", repositoryName);
        Path sandboxRootFolder = cloneLocation.resolve(repositoryName);
        if (!sandboxRootFolder.toFile().exists()) {
            this.cloner.clone(repositoryUri, sandboxRootFolder);
        }

        Path sandboxCodeFolder = sandboxRootFolder.resolve("code");

        CodeExampleFileVisitor visitor = new CodeExampleFileVisitor();
        Files.walkFileTree(this.sourceCodePath.resolve("code"), visitor);
        for (Path sourceExample : visitor.getMatchedFiles()) {
            String languageName = sourceExample.getParent().toFile().getName();
            Path languageFolder = sandboxCodeFolder.resolve(languageName);
            languageFolder.toFile().mkdirs();
            String code = substituteValues(repositoryName, sandboxRootFolder, sourceExample, newQueryFormatter(languageName));
            Files.write(languageFolder.resolve(sourceExample.toFile().getName()), code.getBytes(StandardCharsets.UTF_8));
        }
    }

    private QueryFormatter newQueryFormatter(String languageName) {
        QueryFormatter queryFormatter;
        IndentDetector indentDetector = new IndentDetector();
        if (languageName.equals("java")) {
            queryFormatter = new JavaQueryFormatter(indentDetector);
        } else {
            queryFormatter = new DefaultQueryFormatter(indentDetector);
        }
        return queryFormatter;
    }

    private String substituteValues(String repositoryName,
                                    Path sandboxRepositoryRootFolder,
                                    Path sourceExample,
                                    QueryFormatter queryFormatter) throws IOException {

        String code = Files.readString(sourceExample);
        try (FileReader readmeReader = new FileReader(sandboxRepositoryRootFolder.resolve("README.adoc").toFile())) {
            SandboxMetadata metadata = metadataReader.readMetadata(readmeReader);
            String indentedQuery = queryFormatter.format(code, metadata.getQuery());
            code = code.replaceFirst("[^\\S\\n]*MATCH \\(m:Movie.*", Matcher.quoteReplacement(indentedQuery));
            code = code.replace("mUser", repositoryName);
            code = code.replace("s3cr3t", repositoryName);
            code = code.replace("movies", repositoryName);
            code = code.replace("movieTitle", metadata.getParameterName());
            code = code.replace("The Matrix", metadata.getParameterValue());
            code = code.replace("actorName", metadata.getResultColumn());
        }
        return code;
    }
}
