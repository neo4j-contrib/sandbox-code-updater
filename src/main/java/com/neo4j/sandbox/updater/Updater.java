package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.Git;
import com.neo4j.sandbox.github.GithubSettings;
import com.neo4j.sandbox.updater.formatting.DefaultQueryIndenter;
import com.neo4j.sandbox.updater.formatting.IndentDetector;
import com.neo4j.sandbox.updater.formatting.JavaQueryIndenter;
import com.neo4j.sandbox.updater.formatting.QueryIndenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Component
public class Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(Updater.class);

    private final Git cloner;

    private final MetadataReader metadataReader;

    private final GithubSettings githubSettings;

    public Updater(Git cloner,
                   MetadataReader metadataReader,
                   GithubSettings githubSettings) {

        this.cloner = cloner;
        this.metadataReader = metadataReader;
        this.githubSettings = githubSettings;
    }

    /**
     * Updates every code sample of the provided sandbox repository.
     * <p>
     * The updater will clone the repository if {@code cloneLocation} does not denote an existing path.
     * <p>
     * The updater generates the code samples based on the given {@code templateLocation}. This
     * location denotes the path to the local clone of https://github.com/neo4j-graph-examples/template/, where the
     * canonical examples live.
     *
     * @param templateLocation path to the local the template repository, where the source code samples are
     * @param cloneLocation path to the local clone of the sandbox repository (will be created by `git clone` if it does
     *                      not exist)
     * @param repositoryUri URI of the sandbox repository
     * @return the list of every code file of the cloned sandbox
     * @throws IOException if any of the underlying file operations fail
     */
    public List<Path> updateCodeExamples(Path templateLocation, Path cloneLocation, String repositoryUri) throws IOException {
        if (cloneLocation.toFile().exists()) {
            LOGGER.debug("Clone of {} already exists at location {}. Skipping git clone operation.", repositoryUri, cloneLocation);
        } else {
            LOGGER.trace("About to clone {} at {}.", repositoryUri, cloneLocation);
            this.cloner.clone(cloneLocation, withToken(repositoryUri));
        }

        LOGGER.trace("About to update samples based on {} code.", templateLocation);
        Path sandboxCodeFolder = cloneLocation.resolve("code");
        CodeVisitor visitor = new CodeVisitor();
        Files.walkFileTree(templateLocation.resolve("code"), visitor);
        List<Path> templateCodeSamples = visitor.getMatchedFiles();
        List<Path> generatedFiles = new ArrayList<>(templateCodeSamples.size());
        for (Path sourceExample : templateCodeSamples) {
            String languageName = sourceExample.getParent().toFile().getName();
            LOGGER.trace("About to update {} sample of {}", languageName, repositoryUri);
            Path languageFolder = sandboxCodeFolder.resolve(languageName);
            languageFolder.toFile().mkdirs();
            String code = substituteValues(cloneLocation, sourceExample, newQueryFormatter(languageName));
            Path sandboxExample = languageFolder.resolve(sourceExample.toFile().getName());
            Files.write(sandboxExample, code.getBytes(StandardCharsets.UTF_8));
            generatedFiles.add(sandboxExample);
        }
        return generatedFiles;
    }

    // with this one weird trick, authentication works in Github Action
    private String withToken(String repositoryUri) {
        return repositoryUri.replaceFirst("(https?)://", String.format("$1://%s@", githubSettings.getToken()));
    }

    private QueryIndenter newQueryFormatter(String languageName) {
        IndentDetector indentDetector = new IndentDetector();
        if (languageName.equals("java")) {
            return new JavaQueryIndenter(indentDetector);
        }
        return new DefaultQueryIndenter(indentDetector);
    }

    private String substituteValues(Path sandboxRepositoryRootFolder,
                                    Path sourceExample,
                                    QueryIndenter queryIndenter) throws IOException {

        String code = Files.readString(sourceExample);
        try (FileReader readmeReader = new FileReader(sandboxRepositoryRootFolder.resolve("README.adoc").toFile())) {
            Metadata metadata = metadataReader.read(readmeReader);
            String indentedQuery = queryIndenter.indent(code, metadata.getQuery());
            code = code.replaceFirst("[^\\S\\n]*MATCH \\(m:Movie.*", Matcher.quoteReplacement(indentedQuery));
            code = code.replaceFirst("(?:neo4j|bolt)(?:\\+.{1,3})?://.*:\\d+", "bolt://<HOST>:<BOLTPORT>");
            code = code.replace("mUser", "<USERNAME>");
            code = code.replace("s3cr3t", "<PASSWORD>");
            code = code.replace("movies", "neo4j");
            code = code.replace("movieTitle", metadata.getParameterName());
            code = code.replace("The Matrix", metadata.getParameterValue());
            code = code.replace("actorName", metadata.getResultColumn());
        }
        return code;
    }

}
