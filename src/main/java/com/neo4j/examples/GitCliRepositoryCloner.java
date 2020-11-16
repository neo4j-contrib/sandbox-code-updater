package com.neo4j.examples;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class GitCliRepositoryCloner implements RepositoryCloner {

    @Override
    public void clone(String repositoryUri, Path destinationDirectory) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", repositoryUri, destinationDirectory.toString());
        Process process = processBuilder.start();
        try {
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                throw new RuntimeException(String.format("Could not clone %s into %s in less than 1 minute", repositoryUri, destinationDirectory));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(String.format("Interrupted while cloning %s into %s", repositoryUri, destinationDirectory));
        }
    }
}
