package com.neo4j.sandbox.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DispatchContext {

    private final String sourceRepository;
    private final String commitReference;

    @JsonCreator
    public DispatchContext(@JsonProperty("source") String sourceRepository, @JsonProperty("commit") String commitReference) {
        this.sourceRepository = sourceRepository;
        this.commitReference = commitReference;
    }

    public String getSourceRepository() {
        return sourceRepository;
    }

    public String getCommitReference() {
        return commitReference;
    }

    @Override
    public String toString() {
        return "DispatchContext{" +
                "sourceRepository='" + sourceRepository + '\'' +
                ", commitReference='" + commitReference + '\'' +
                '}';
    }
}
