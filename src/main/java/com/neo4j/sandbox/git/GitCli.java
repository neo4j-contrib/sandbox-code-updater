package com.neo4j.sandbox.git;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

// @Service
public class GitCli implements GitOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitCli.class);

    private static final String GIT_LOCALE = "en_US";

    @Override
    public void clone(Path cloneLocation, String repositoryUri, String token) throws IOException {
        File workingDirectory = cloneLocation.toFile();
        workingDirectory.mkdirs();
        String tokenUri = withToken(repositoryUri, token);
        executeCommand(workingDirectory, "git", "clone", tokenUri, ".");
    }

    @Override
    public void checkoutNewBranch(Path cloneLocation, String branchName) throws IOException {
        if (branchExists(cloneLocation, branchName)) {
            throw new DuplicateBranchException();
        }
        executeCommand(cloneLocation.toFile(), "git", "checkout", "-b", branchName);
    }

    @Override
    public void commitAll(Path cloneLocation, String message) throws IOException {
        File workingDirectory = cloneLocation.toFile();
        executeCommand(workingDirectory, "git", "add", "--all");
        try {
            executeCommand(workingDirectory, "git", "commit", "-m", message);
        } catch (CommandIOException e) {
            if (e.commandOutputContains("nothing to commit")) {
                throw new BlankCommitException(e);
            }
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

    private boolean branchExists(Path cloneLocation, String branchName) throws IOException {
        String remoteUrl = executeCommand(cloneLocation.toFile(), "git", "remote", "get-url", "origin");
        String output = executeCommand(cloneLocation.toFile(), "git", "ls-remote", "--heads", remoteUrl.trim(), branchName);
        return !output.isBlank();
    }

    private static String executeCommand(File workingDirectory, String... commands) throws IOException {
        Process process = runProcess(workingDirectory, commands);
        assertSuccessfulExitCode(process, commands);
        return stdout(process);
    }

    private static Process runProcess(File workingDirectory, String... commands) throws IOException {
        String command = String.join(" ", commands);
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.environment().put("LC_ALL", GIT_LOCALE);
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
            throw new CommandIOException(command, exitStatus, stdout(process), stderr(process));
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
