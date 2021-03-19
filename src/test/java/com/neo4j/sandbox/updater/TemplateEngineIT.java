package com.neo4j.sandbox.updater;

import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngineIT {

    TemplateEngine templateEngine = new TemplateEngine(new MetadataReader(Asciidoctor.Factory.create()));

    @Test
    void replaces_default_type_definitions_with_actual_schema(@TempDir Path sandboxCloneLocation) throws IOException {
        defineSandboxMetadata(sandboxCloneLocation);
        Path templateGraphqlExampleFile = TestPaths.templateRepositoryPath().resolve("code").resolve("graphql").resolve("example.js");

        String generatedGraphqlExample = templateEngine.substituteValues(sandboxCloneLocation, templateGraphqlExampleFile);

        assertThat(generatedGraphqlExample)
                .doesNotContain("let typeDefs;")
                .contains("  const typeDefs = /* GraphQL */ `\n" +
                        "type Movie {\n" +
                        "  budget: Int\n" +
                        "}\n" +
                        "`;\n"
                );
    }

    private void defineSandboxMetadata(Path sandboxRoot) throws IOException {
        writeFile(sandboxRoot.resolve(sandboxRoot).resolve("README.adoc"),
                ".Example Query:\n" +
                        "[source,cypher,role=query-example,param-name=favorite,param-value=\"The Matrix\",result-column=title,expected-result=\"Cloud Atlas\"]\n" +
                        "----\n" +
                        "MATCH (movie:Movie {title:$favorite})<-[:ACTED_IN]-(actor)-[:ACTED_IN]->(rec:Movie)\n" +
                        " RETURN distinct rec.title as title LIMIT 20\n" +
                        "----");
        writeFile(sandboxRoot.resolve("graphql").resolve("schema.graphql"), "type Movie {\n" +
                "  budget: Int\n" +
                "}");
    }

    private void writeFile(Path path, String contents) throws IOException {
        path.getParent().toFile().mkdirs();
        Files.write(path, contents.getBytes(StandardCharsets.UTF_8));
    }
}