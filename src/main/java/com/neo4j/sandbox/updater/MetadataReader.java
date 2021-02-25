package com.neo4j.sandbox.updater;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetadataReader {

    private static final String ROLE_FILTER = "query-example";

    private final Asciidoctor parser;

    public MetadataReader(Asciidoctor parser) {
        this.parser = parser;
    }

    public Metadata read(Reader path) throws IOException {
        Document document = parser.load(readContents(path), Collections.emptyMap());
        List<StructuralNode> nodes = document.findBy(nodeFilters(":listing", ROLE_FILTER));
        int size = nodes.size();
        if (size != 1) {
            throw new IllegalArgumentException(
                    String.format("Expected exactly 1 Cypher source listing with \"query-example\" role: %d found.", size));
        }
        Block codeBlock = (Block) nodes.iterator().next();
        return new Metadata(
                String.join("\n", codeBlock.getLines()),
                (String) codeBlock.getAttribute("param-name"),
                (String) codeBlock.getAttribute("param-value"),
                (String) codeBlock.getAttribute("result-column"),
                (String) codeBlock.getAttribute("expected-result")
        );
    }

    private static String readContents(Reader path) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(path)) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static Map<Object, Object> nodeFilters(String context, String role) {
        Map<Object, Object> selectors = new HashMap<>(2);
        selectors.put("context", context);
        selectors.put("role", role);
        return selectors;
    }
}
