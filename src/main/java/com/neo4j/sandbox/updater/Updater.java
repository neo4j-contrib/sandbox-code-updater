package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.GitOperations;
import com.neo4j.sandbox.github.GithubSettings;
import com.neo4j.sandbox.updater.formatting.DefaultQueryIndenter;
import com.neo4j.sandbox.updater.formatting.IndentDetector;
import com.neo4j.sandbox.updater.formatting.JavaQueryIndenter;
import com.neo4j.sandbox.updater.formatting.QueryIndenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class Updater {

    private static final Logger LOGGER = LoggerFactory.getLogger(Updater.class);

    private final GitOperations cloner;

    private final TemplateEngine templateEngine;

    private final GithubSettings githubSettings;


    public Updater(GitOperations cloner,
                   MetadataReader metadataReader,
                   GithubSettings githubSettings) {

        this.cloner = cloner;
        this.templateEngine = new TemplateEngine(metadataReader);
        this.githubSettings = githubSettings;
    }

    /**
     * Updates every code sample of the provided sandbox repository.
     * <p>
     * The updater will clone the repository if {@code cloneLocation} does not denote an existing path.
     * <p>
     * The updater generates the code samples based on the given {@code templateLocation}. This location denotes the
     * path to the local clone of https://github.com/neo4j-graph-examples/template/, where the canonical examples live.
     *
     * @param templateLocation path to the local the template repository, where the source code samples are
     * @param cloneLocation    path to the local clone of the sandbox repository (will be created by `git clone` if it
     *                         does not exist)
     * @param repositoryUri    URI of the sandbox repository
     * @return the list of every code file of the cloned sandbox
     * @throws IOException if any of the underlying file operations fail
     */
    public List<Path> updateCodeExamples(Path templateLocation, Path cloneLocation, String repositoryUri) throws IOException {
        if (cloneLocation.toFile().exists()) {
            LOGGER.debug("Clone of {} already exists at location {}. Skipping git clone operation.", repositoryUri, cloneLocation);
        } else {
            LOGGER.trace("About to clone {} at {}.", repositoryUri, cloneLocation);
            this.cloner.clone(cloneLocation, repositoryUri, githubSettings.getToken());
        }

        LOGGER.trace("About to update samples based on {} code.", templateLocation);
        Path sandboxCodeFolder = cloneLocation.resolve("code");
        CodeVisitor visitor = new CodeVisitor(cloneLocation);
        Files.walkFileTree(templateLocation.resolve("code"), visitor);
        List<Path> templateCodeSamples = visitor.getMatchedFiles();
        List<Path> generatedFiles = new ArrayList<>(templateCodeSamples.size());
        for (Path sourceExample : templateCodeSamples) {
            String languageName = sourceExample.getParent().toFile().getName();
            LOGGER.trace("About to update {} sample of {}", languageName, repositoryUri);
            Path languageFolder = sandboxCodeFolder.resolve(languageName);
            languageFolder.toFile().mkdirs();
            String code = templateEngine.substituteValues(cloneLocation, sourceExample);
            Path sandboxExample = languageFolder.resolve(sourceExample.toFile().getName());
            Files.write(sandboxExample, code.getBytes(StandardCharsets.UTF_8));
            generatedFiles.add(sandboxExample);
        }
        return generatedFiles;
    }

}
