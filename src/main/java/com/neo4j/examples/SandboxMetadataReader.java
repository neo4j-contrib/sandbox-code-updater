package com.neo4j.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SandboxMetadataReader {

    public SandboxMetadata readMetadata(Reader path) throws IOException {
        SandboxMetadata.Builder result = SandboxMetadata.builder();
        List<String> lines = readContents(path);
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext(); ) {
            String line = iterator.next();
            if (line.startsWith(":query:")) {
                boolean firstLine = true;
                StringBuilder queryBuilder = new StringBuilder();
                while (line.trim().endsWith("+")) {
                    String queryLine;
                    if (firstLine) {
                        queryLine = line.substring(":query: ".length());
                        firstLine = false;
                    } else {
                        queryLine = line;
                    }
                    queryBuilder.append(queryLine, 0, queryLine.length() - 1);
                    queryBuilder.append("\n");
                    if (!iterator.hasNext()) {
                        break;
                    }
                    line = iterator.next();
                }
                result.setQuery(queryBuilder.toString().trim());
            }
            if (line.startsWith(":param-name:")) {
                result.setParameterName(line.substring(":param-name:".length()).trim());
            } else if (line.startsWith(":param-value:")) {
                result.setParameterValue(line.substring(":param-value:".length()).trim());
            } else if (line.startsWith(":result-column:")) {
                result.setResultColumn(line.substring(":result-column:".length()).trim());
            } else if (line.startsWith(":expected-result:")) {
                result.setExpectedResult(line.substring(":expected-result:".length()).trim());
            }
        }
        return result.build();
    }

    private List<String> readContents(Reader path) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(path)) {
            return bufferedReader.lines().collect(Collectors.toList());
        }
    }
}
