package com.neo4j.sandbox.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CommitMessageFormatterTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("executionContexts")
    void produces_commit_message(String ignored, ExecutionContext context, String result) {
        CommitMessageFormatter formatter = new CommitMessageFormatter(
                new ObjectMapper(),
                context);

        assertThat(formatter.createMessage()).isEqualTo(result);
    }

    static Stream<Arguments> executionContexts() {
        String commitMessageTemplate = "Triggered by direct commit. Origin: https://github.com/neo4j-contrib/sandbox-code-updater/commit/%s";
        String repositoryDispatch = "Triggered by dispatch event. Origin: https://github.com/%s/commit/%s";
        return Stream.of(
                arguments(
                        "direct commit",
                        directCommit("some-sha"),
                        String.format(commitMessageTemplate, "some-sha")
                ),
                arguments(
                        "repository dispatch",
                        repositoryDispatch("{\"source\": \"foo/bar\", \"commit\": \"some-sha\"}"),
                        String.format(repositoryDispatch, "foo/bar", "some-sha")
                ),
                arguments(
                        "unknown trigger",
                        unknown(),
                        "Manually triggered"
                )
        );
    }

    private static ExecutionContext directCommit(String commit) {
        ExecutionContext context = new ExecutionContext();
        context.setCommit(commit);
        context.setDispatch("");
        return context;
    }

    private static ExecutionContext repositoryDispatch(String dispatch) {
        ExecutionContext context = new ExecutionContext();
        context.setCommit("");
        context.setDispatch(dispatch);
        return context;
    }

    private static ExecutionContext unknown() {
        ExecutionContext context = new ExecutionContext();
        context.setCommit("");
        context.setDispatch("");
        return context;
    }
}