package com.neo4j.sandbox.updater;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "sandbox")
public class BatchUpdaterSettings {

    private List<String> repositories;

    private Path codeSamplesPath;

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    public Path getCodeSamplesPath() {
        return codeSamplesPath;
    }

    public void setCodeSamplesPath(Path codeSamplesPath) {
        this.codeSamplesPath = codeSamplesPath;
    }
}
