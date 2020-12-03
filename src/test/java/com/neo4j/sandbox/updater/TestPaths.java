package com.neo4j.sandbox.updater;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class TestPaths {

    public static Path templateRepositoryPath() {
        try {
            return classpathFile("/fake-template-repo");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path classpathFile(String name) throws URISyntaxException {
        URL resource = TestPaths.class.getResource(name);
        return new File(resource.toURI()).toPath();
    }
}
