package com.neo4j.sandbox.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CodeVisitorTest {

    @TempDir
    Path templateRepoPath;

    @TempDir
    Path sandboxRepoPath;


    @Test
    void includes_example_file() throws IOException {
        Path code = createFile(templateRepoPath, "code/java/Example.java");
        CodeVisitor visitor = new CodeVisitor(sandboxRepoPath);

        visitor.visitFile(code, mock(BasicFileAttributes.class));

        assertThat(visitor.getMatchedFiles()).isEqualTo(singletonList(code));
    }

    @Test
    void excludes_graphql_if_schema_is_absent_from_sandbox_repo() throws IOException {
        Path code = createFile(templateRepoPath, "code/graphql/example.js");
        CodeVisitor visitor = new CodeVisitor(sandboxRepoPath);

        visitor.visitFile(code, mock(BasicFileAttributes.class));

        assertThat(visitor.getMatchedFiles()).isEmpty();
    }

    @Test
    void includes_graphql_if_schema_is_present_in_sandbox_repo() throws IOException {
        Path code = createFile(templateRepoPath, "code/graphql/example.js");
        createFile(sandboxRepoPath, "graphql/schema.graphql");
        CodeVisitor visitor = new CodeVisitor(sandboxRepoPath);

        visitor.visitFile(code, mock(BasicFileAttributes.class));

        assertThat(visitor.getMatchedFiles()).isEqualTo(singletonList(code));
    }

    private Path createFile(Path basePath, String file) throws IOException {
        basePath = basePath.resolve(file);
        basePath.getParent().toFile().mkdirs();
        basePath.toFile().createNewFile();
        return basePath;
    }
}