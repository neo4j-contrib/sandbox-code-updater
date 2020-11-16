package com.neo4j.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class FakeNorthwindRepositoryCloner implements RepositoryCloner {

    private final Path tempDir;

    public FakeNorthwindRepositoryCloner(Path tempDir) {
        this.tempDir = tempDir;
    }

    @Override
    public void clone(String uri, Path destinationDirectory) throws IOException {
        Path northwindClone = tempDir.resolve("northwind");
        Path pythonCodeFolder = northwindClone.resolve("code").resolve("python");
        assertThat(pythonCodeFolder.toFile().mkdirs())
                .overridingErrorMessage("could not create sandbox file tree")
                .isTrue();
        Files.write(northwindClone.resolve("README.adoc"), (":name: northwind\n" +
                ":query: MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]-> +\n" +
                " (:Category {categoryName:$category}) +\n" +
                " RETURN p.productName as product +\n" +
                ":param-name: category\n" +
                ":param-value: Dairy Products\n" +
                ":result-column: product\n" +
                ":expected-result: Geitost").getBytes(UTF_8));
        Files.write(pythonCodeFolder.resolve("example.py"), ("" +
                "# pip3 install neo4j-driver\n" +
                "# python3 example.py\n" +
                "\n" +
                "from neo4j import GraphDatabase, basic_auth\n" +
                "\n" +
                "driver = GraphDatabase.driver(\n" +
                "  \"bolt://<HOST>:<BOLTPORT>\",\n" +
                "  auth=basic_auth(\"<USERNAME>\", \"<PASSWORD>\"))\n" +
                "\n" +
                "cypher_query = '''\n" +
                "MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]-> \n" +
                "(:Category {categoryName:$category}) \n" +
                "RETURN p.productName as product \n" +
                "'''\n" +
                "\n" +
                "with driver.session(database=\"neo4j\") as session:\n" +
                "  results = session.read_transaction(\n" +
                "    lambda tx: tx.run(cypher_query,\n" +
                "      category=\"Dairy Products\").data())\n" +
                "\n" +
                "  for record in results:\n" +
                "    print(record['product'])\n" +
                "\n" +
                "driver.close()").getBytes(UTF_8));
    }
}
