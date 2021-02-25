package com.neo4j.sandbox.updater;

import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MetadataReaderTest {

    private final MetadataReader metadataReader = new MetadataReader(Asciidoctor.Factory.create());

    @Test
    void reads_metadata() throws Exception {
        Metadata metadata = metadataReader.read(new StringReader(
                "[source,cypher,role=query-example,param-name=category,param-value=Dairy Products,result-column=product,expected-result=Geitost]\n" +
                        "----\n" +
                        "MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\n" +
                        " (:Category {categoryName:$category})\n" +
                        " RETURN p.productName as product\n" +
                        "----"));

        assertThat(metadata.getQuery()).isEqualTo("MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\n" +
                " (:Category {categoryName:$category})\n" +
                " RETURN p.productName as product");
        assertThat(metadata.getExpectedResult()).isEqualTo("Geitost");
        assertThat(metadata.getParameterName()).isEqualTo("category");
        assertThat(metadata.getParameterValue()).isEqualTo("Dairy Products");
        assertThat(metadata.getResultColumn()).isEqualTo("product");
    }

    @Test
    void fails_if_no_source_listing_is_found() {
        assertThatThrownBy(() -> metadataReader.read(new StringReader("")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected exactly 1 Cypher source listing with \"query-example\" role: 0 found.");
    }

    @Test
    void fails_if_more_than_1_source_listing_is_found() {
        assertThatThrownBy(() -> metadataReader.read(new StringReader("[source,cypher,role=query-example,param-name=ignored,param-value=ignored,result-column=count,expected-result=0]\n" +
                "----\n" +
                "MATCH (n) RETURN COUNT(n) AS count\n" +
                "----\n" +
                "[source,cypher,role=query-example,param-name=ignored,param-value=ignored,result-column=count,expected-result=0]\n" +
                "----\n" +
                "MATCH (n) RETURN COUNT(n) AS count\n" +
                "----")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected exactly 1 Cypher source listing with \"query-example\" role: 2 found.");
    }
}
