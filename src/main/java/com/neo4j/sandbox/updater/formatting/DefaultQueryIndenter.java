package com.neo4j.sandbox.updater.formatting;

public class DefaultQueryIndenter implements QueryIndenter {

    private final IndentDetector indentDetector;

    public DefaultQueryIndenter(IndentDetector indentDetector) {
        this.indentDetector = indentDetector;
    }

    @Override
    public String indent(String code, String query) {
        return indentDetector
                .detect("MATCH (m:Movie", code)
                .map(indentation -> indentation.indent(query))
                .orElse(query);
    }
}
