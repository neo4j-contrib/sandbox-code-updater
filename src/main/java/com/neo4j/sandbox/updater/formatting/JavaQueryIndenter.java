package com.neo4j.sandbox.updater.formatting;

public class JavaQueryIndenter implements QueryIndenter {

    private final IndentDetector indentDetector;

    public JavaQueryIndenter(IndentDetector indentDetector) {
        this.indentDetector = indentDetector;
    }

    @Override
    public String indent(String initialCode, String newQuery) {
        String quotedString = quote(newQuery);
        int firstNewlineIndex = quotedString.indexOf("\n");
        if (firstNewlineIndex == -1) {
            return quotedString;
        }
        String firstLine = quotedString.substring(0, firstNewlineIndex + 1);
        String rest = quotedString.substring(firstNewlineIndex + 1);
        return indentDetector
                .detect("MATCH (m:Movie", initialCode)
                .map(indentation -> firstLine + indentation.indent(rest))
                .orElse(quotedString);
    }

    private String quote(String indentedQuery) {
        String prefix = "\"";
        String intermediateLineSuffix = "\\n\" +";
        String finalSuffix = "\";";
        String[] lines = indentedQuery.split("\n");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i != 0) {
                result.append(prefix);
            }
            result.append(lines[i]);
            if (i != lines.length - 1) {
                result.append(intermediateLineSuffix);
                result.append("\n");
            } else {
                result.append(finalSuffix);
            }
        }
        return result.toString();
    }
}
