package com.neo4j.examples;

import java.io.IOException;
import java.nio.file.Path;

public interface RepositoryCloner {

    void clone(String repositoryUri, Path destinationDirectory) throws IOException;
}
