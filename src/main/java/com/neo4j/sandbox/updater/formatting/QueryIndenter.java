package com.neo4j.sandbox.updater.formatting;

public interface QueryIndenter {

    String indent(String initialCode, String newQuery);
}
