package com.neo4j.sandbox.updater.formatting;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaQueryIndenterTest {

    private final JavaQueryIndenter indenter = new JavaQueryIndenter(new IndentDetector());

    private final String templateQuery = "MATCH (m:Movie {title:$movie})<-[:RATED]-(u:User)-[:RATED]->(rec:Movie)\nRETURN distinct rec.title AS recommendation LIMIT 20";

    @Test
    void includes_newlines_in_query_lines() {
        String multilineQuery = "MATCH (m:Merchant{name:$name})<-[:TO]-(:Transaction)<-[:PERFORMED]-(c:Client)\n" +
                "RETURN c.name as client";

        String result = indenter.indent(templateQuery, multilineQuery);

        assertThat(result).isEqualTo("MATCH (m:Merchant{name:$name})<-[:TO]-(:Transaction)<-[:PERFORMED]-(c:Client)\\n\" +\n" +
                "\"RETURN c.name as client\";");
    }
}