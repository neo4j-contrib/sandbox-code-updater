package com.neo4j.sandbox.updater;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataReaderTest {

    private final MetadataReader metadataReader = new MetadataReader();

    @Test
    void reads_metadata() throws Exception {
        Metadata metadata = metadataReader.readMetadata(new StringReader(
                ":param-name: category\n" +
                        ":param-value: Dairy Products\n" +
                        ":result-column: product\n" +
                        ":expected-result: Geitost\n" +
                        ":query: MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]-> +\n" +
                        " (:Category {categoryName:$category}) +\n" +
                        " RETURN p.productName as product +"));

        assertThat(metadata.getQuery()).isEqualTo("MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]-> \n" +
                "(:Category {categoryName:$category}) \n" +
                "RETURN p.productName as product");
        assertThat(metadata.getExpectedResult()).isEqualTo("Geitost");
        assertThat(metadata.getParameterName()).isEqualTo("category");
        assertThat(metadata.getParameterValue()).isEqualTo("Dairy Products");
        assertThat(metadata.getResultColumn()).isEqualTo("product");
    }

    @Test
    void preserves_query_left_spaces() throws Exception {
        Metadata metadata = metadataReader.readMetadata(new StringReader(
                ":query: MATCH (m:Merchant{name:$name})<-[:TO]-(:Transaction)<-[:PERFORMED]-(c:Client) +\n" +
                        "RETURN c.name as client +\n"));

        assertThat(metadata.getQuery()).isEqualTo("MATCH (m:Merchant{name:$name})<-[:TO]-(:Transaction)<-[:PERFORMED]-(c:Client) \n" +
                "RETURN c.name as client");
    }
}
