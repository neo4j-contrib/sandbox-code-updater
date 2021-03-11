package com.neo4j.sandbox.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Service
public class JGit implements GitOperations {

    @Override
    public void clone(Path cloneLocation, String repositoryUri, String token) throws IOException {
        execute(() ->
                Git.cloneRepository()
                        .setCredentialsProvider(credentials(token))
                        .setDirectory(cloneLocation.toFile())
                        .setURI(repositoryUri)
                        .call());
    }

    @Override
    public void checkoutNewBranch(Path cloneLocation, String branchName) throws IOException {
        execute(() ->
                repository(cloneLocation)
                        .checkout()
                        .setCreateBranch(true)
                        .setName(branchName).call());
    }

    @Override
    public void commitAll(Path cloneLocation, String message) throws IOException {
        execute(() ->
                repository(cloneLocation)
                        .commit()
                        .setAllowEmpty(false)
                        .setAll(true)
                        .setMessage(message)
                        .call());
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

    private static <T> T execute(Callable<T> task) throws IOException {
        try {
            return task.call();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}


