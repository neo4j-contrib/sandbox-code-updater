package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.updater.formatting.DefaultQueryIndenter;
import com.neo4j.sandbox.updater.formatting.IndentDetector;
import com.neo4j.sandbox.updater.formatting.JavaQueryIndenter;
import com.neo4j.sandbox.updater.formatting.QueryIndenter;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

import static java.nio.charset.StandardCharsets.UTF_8;

class TemplateEngine {

    private final MetadataReader reader;
    private final JavaQueryIndenter javaIndenter;
    private final DefaultQueryIndenter defaultIndenter;

    public TemplateEngine(MetadataReader reader) {
        this.reader = reader;
        IndentDetector indentDetector = new IndentDetector();
        defaultIndenter = new DefaultQueryIndenter(indentDetector);
        javaIndenter = new JavaQueryIndenter(indentDetector);
    }

    public String substituteValues(Path sandboxRoot,
                                   Path currentSourceCodeFile) throws IOException {

        String code = Files.readString(currentSourceCodeFile);
        String languageName = currentSourceCodeFile.getParent().toFile().getName();
        try (FileReader readmeReader = new FileReader(sandboxRoot.resolve("README.adoc").toFile())) {
            Metadata metadata = reader.read(readmeReader);
            String indentedQuery = queryIndenter(languageName).indent(code, metadata.getQuery());
            code = code.replaceFirst("[^\\S\\n]*MATCH \\(m:Movie.*", Matcher.quoteReplacement(indentedQuery));
            code = code.replaceFirst("(?:neo4j|bolt)(?:\\+.{1,3})?://.*:\\d+", "neo4j://<HOST>:<BOLTPORT>");
            code = code.replace("mUser", "<USERNAME>");
            code = code.replace("s3cr3t", "<PASSWORD>");
            code = code.replace("movies", "neo4j");
            code = code.replace("movieTitle", metadata.getParameterName());
            code = code.replace("The Matrix", metadata.getParameterValue());
            code = code.replace("actorName", metadata.getResultColumn());
            String schema = graphqlSchema(sandboxRoot);
            if (schema != null) {
                code = code.replace("let typeDefs;\n", schemaDeclaration(schema));
            }
        }
        return code;
    }

    private String graphqlSchema(Path sandboxRoot) throws IOException {
        Path schema = sandboxRoot.resolve("graphql").resolve("schema.graphql");
        if (!schema.toFile().exists()) {
            return null;
        }
        return Files.readString(schema, UTF_8);
    }

    private String schemaDeclaration(String schema) {
        return String.format("const typeDefs = /* GraphQL */ `\n%s\n`;\n", schema);
    }

    private QueryIndenter queryIndenter(String languageName) {
        if (languageName.equals("java")) {
            return javaIndenter;
        }
        return defaultIndenter;
    }
}
