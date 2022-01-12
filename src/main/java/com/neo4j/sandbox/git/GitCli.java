package com.neo4j.sandbox.git;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

// @Service
public class GitCli implements GitOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitCli.class);

    @Override
    public void clone(Path cloneLocation, String repositoryUri, String token) throws IOException {
        File workingDirectory = cloneLocation.toFile();
        workingDirectory.mkdirs();
        String tokenUri = withToken(repositoryUri, token);
        executeCommand(workingDirectory, "git", "clone", tokenUri, ".");
    }

    @Override
    public void checkoutNewBranch(Path cloneLocation, String branchName) throws IOException {
        executeCommand(cloneLocation.toFile(), "git", "checkout", "-b", branchName);
    }

    @Override
    public void commitAll(Path cloneLocation, String message) throws IOException {
        File workingDirectory = cloneLocation.toFile();
        executeCommand(workingDirectory, "git", "add", "--all");
        try {
            executeCommand(workingDirectory, "git", "commit", "-m", message);
        } catch (IOException e) {
            throw new CommitException(e);
        }
    }

    @Override
    public void push(Path cloneLocation, String token, String remote, String branch) throws IOException {
        try {
            executeCommand(cloneLocation.toFile(), "git", "push", remote, branch);
        } catch (IOException e) {
            throw new PushException(e);
        }
    }

    @Override
    public String currentBranch(Path cloneLocation) throws IOException {
        String[] command = new String[]{"git", "symbolic-ref", "HEAD"};
        Process process = runProcess(cloneLocation.toFile(), command);
        assertSuccessfulExitCode(process, command);
        String stdout = stdout(process).trim();
        return stdout.substring(stdout.lastIndexOf("/") + 1);
    }

    private static void executeCommand(File workingDirectory, String... commands) throws IOException {
        Process process = runProcess(workingDirectory, commands);
        assertSuccessfulExitCode(process, commands);
    }

    private static Process runProcess(File workingDirectory, String... commands) throws IOException {
        String command = String.join(" ", commands);
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(workingDirectory);
        Process process = processBuilder.start();
        waitForProcess(command, process);
        return process;
    }

    private static void assertSuccessfulExitCode(Process process, String[] commands) throws IOException {
        int exitStatus = process.exitValue();
        String command = String.join(" ", commands);
        LOGGER.trace("Git command {} exited with status code {}", command, exitStatus);
        if (exitStatus != 0) {
            String error = combinedOutputs(process);
            throw new IOException(
                    String.format("Expected Git operation %s to succeed but got exit status %d. See error below%n%s",
                            command, exitStatus, error));
        }
    }

    private static void waitForProcess(String command, Process process) throws IOException {
        try {
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                throw new IOException(String.format("Could not perform Git operation %s in less than 1 minute", command));
            }
        } catch (InterruptedException e) {
            throw new IOException(String.format("Interrupted while performing Git operation %s. See error below%n%s", command, e));
        }
    }

    private static String combinedOutputs(Process process) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Error stream%n"));
        builder.append(stderr(process));
        builder.append("\n");
        builder.append(String.format("Output stream%n"));
        builder.append(stdout(process));
        builder.append("\n");
        return builder.toString();
    }

    private static String stdout(Process process) throws IOException {
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String stderr(Process process) throws IOException {
        return new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String withToken(String repositoryUri, String token) {
        return repositoryUri.replaceFirst("(https?)://", String.format("$1://%s@", token));
    }
}
