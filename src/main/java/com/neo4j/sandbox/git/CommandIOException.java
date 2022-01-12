package com.neo4j.sandbox.git;

import java.io.IOException;

class CommandIOException extends IOException {

    private final String stdout;
    private final String stderr;

    public CommandIOException(String faultyCommand, int status, String stdout, String stderr) {
        super(String.format("Expected Git operation %s to succeed but got exit status %d. See error below%n%s",
                faultyCommand, status, combinedOutputs(stdout, stderr)));

        this.stdout = stdout;
        this.stderr = stderr;
    }

    public boolean commandOutputContains(String msg) {
        return this.stdout.contains(msg) || this.stderr.contains(msg);
    }

    private static String combinedOutputs(String stdout, String stderr) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("=====%n"));
        builder.append(String.format("Error stream%n"));
        builder.append(String.format("=====%n"));
        builder.append(stderr);
        builder.append("\n");
        builder.append(String.format("=====%n"));
        builder.append(String.format("Output stream%n"));
        builder.append(String.format("=====%n"));
        builder.append(stdout);
        builder.append("\n");
        return builder.toString();
    }
}
