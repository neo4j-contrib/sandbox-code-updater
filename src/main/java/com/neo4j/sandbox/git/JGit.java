package com.neo4j.sandbox.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.String.format;

@Service
public class JGit implements GitOperations {

    @Override
    public void clone(Path cloneLocation, String repositoryUri, String token) throws IOException {
        execute(() -> {
            File workingDir = cloneLocation.toFile();
            if (!workingDir.mkdirs()) {
                throw new IOException("mkdirs failed");
            }
            Process process = new ProcessBuilder("git", "clone", repositoryUri.replace("github.com", token + "@github.com"))
                    .directory(workingDir)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            int exitStatus = process.waitFor();
            if (exitStatus != 0) {
                throw new IOException(String.format("Exit status was %d", exitStatus));
            }
            return null;

//            return Git.cloneRepository()
//                    .setCredentialsProvider(credentials(token))
//                    .setDirectory(cloneLocation.toFile())
//                    .setURI(repositoryUri)
//                    .call();
        });
    }

    @Override
    public void checkoutNewBranch(Path cloneLocation, String branchName) throws IOException {
        if (branchExists(cloneLocation, branchName)) {
            throw new DuplicateBranchException();
        }
        execute(() ->
                repository(cloneLocation)
                        .checkout()
                        .setCreateBranch(true)
                        .setName(branchName).call());
    }

    @Override
    public void commitAll(Path cloneLocation, String message) throws IOException {
        try {
            execute(() ->
                    repository(cloneLocation)
                            .commit()
                            .setAllowEmpty(false)
                            .setAll(true)
                            .setMessage(message)
                            .call());
        } catch (IOException exception) {
            if (exception.getCause() instanceof EmptyCommitException) {
                throw new BlankCommitException(exception);
            }
            throw exception;
        }
    }

    @Override
    public void push(Path cloneLocation, String token, String remote, String branch) throws IOException {
        execute(() ->
                repository(cloneLocation)
                        .push()
                        .setCredentialsProvider(credentials(token))
                        .setRemote(remote)
                        .setRefSpecs(new RefSpec(branch))
                        .call());
    }

    @Override
    public String currentBranch(Path cloneLocation) throws IOException {
        return execute(() -> repository(cloneLocation)
                .getRepository()
                .getBranch()
        );

    }

    private static CredentialsProvider credentials(String token) {
        return token == null ?
                null :
                new UsernamePasswordCredentialsProvider(token, "");
    }

    private static Git repository(Path cloneLocation) throws IOException {
        return Git.open(cloneLocation.toFile());
    }

    private static boolean branchExists(Path cloneLocation, String branchName) throws IOException {
        return execute(() -> {
            List<Ref> refs = repository(cloneLocation)
                    .branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call();
            return refs.stream().anyMatch(ref -> ref.getName().endsWith(branchName));
        });
    }

    private static <T> T execute(Callable<T> task) throws IOException {
        try {
            return task.call();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}


