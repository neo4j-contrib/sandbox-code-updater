package com.neo4j.sandbox.updater;

import com.neo4j.sandbox.git.FakeNorthwindGit;
import com.neo4j.sandbox.github.GithubSettings;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.neo4j.sandbox.updater.TestPaths.templateRepositoryPath;
import static org.assertj.core.api.Assertions.assertThat;

class UpdaterIT {

    private Path sandboxCloneLocation;

    private Updater updater;

    @BeforeEach
    void prepare(@TempDir Path tempDir) {
        sandboxCloneLocation = tempDir.resolve("northwind");
        updater = new Updater(
                new FakeNorthwindGit(sandboxCloneLocation),
                new MetadataReader(Asciidoctor.Factory.create()),
                new GithubSettings()
        );
    }

    @Test
    void returns_updated_sandbox_files() throws Exception {
        List<Path> paths = updater.updateCodeExamples(templateRepositoryPath(), sandboxCloneLocation, "https://github.com/neo4j-graph-examples/northwind");

        assertThat(paths)
                .extracting(UpdaterIT::absolutePathOf)
                .containsOnly(
                        absolutePathOf(sandboxCloneLocation.resolve("code").resolve("python").resolve("example.py")),
                        absolutePathOf(sandboxCloneLocation.resolve("code").resolve("java").resolve("Example.java")),
                        absolutePathOf(sandboxCloneLocation.resolve("code").resolve("csharp").resolve("Example.cs")),
                        absolutePathOf(sandboxCloneLocation.resolve("code").resolve("javascript").resolve("example.js"))
                );
    }

    @Test
    void updates_Python_example() throws Exception {
        updater.updateCodeExamples(templateRepositoryPath(), sandboxCloneLocation, "https://github.com/neo4j-graph-examples/northwind");

        Path pythonExample = sandboxCloneLocation.resolve("code").resolve("python").resolve("example.py");
        assertThat(String.join("\n", Files.readAllLines(pythonExample))).isEqualTo(
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
                        "MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\n" +
                        " (:Category {categoryName:$category})\n" +
                        " RETURN p.productName as product\n" +
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
                        "driver.close()"
        );
    }

    @Test
    void updates_Java_example() throws Exception {
        updater.updateCodeExamples(templateRepositoryPath(), sandboxCloneLocation, "https://github.com/neo4j-graph-examples/northwind");

        Path javaExample = sandboxCloneLocation.resolve("code").resolve("java").resolve("Example.java");
        assertThat(String.join("\n", Files.readAllLines(javaExample))).isEqualTo(
                "// Add your the driver dependency to your pom.xml build.gradle etc.\n" +
                        "// Java Driver Dependency: http://search.maven.org/#artifactdetails|org.neo4j.driver|neo4j-java-driver|4.0.1|jar\n" +
                        "// Reactive Streams http://search.maven.org/#artifactdetails|org.reactivestreams|reactive-streams|1.0.3|jar\n" +
                        "// download jars into current directory\n" +
                        "// java -cp \"*\" Example.java\n" +
                        "\n" +
                        "import org.neo4j.driver.*;\n" +
                        "import static org.neo4j.driver.Values.parameters;\n" +
                        "\n" +
                        "public class Example {\n" +
                        "\n" +
                        "  public static void main(String...args) {\n" +
                        "\n" +
                        "    Driver driver = GraphDatabase.driver(\"bolt://<HOST>:<BOLTPORT>\",\n" +
                        "              AuthTokens.basic(\"<USERNAME>\",\"<PASSWORD>\"));\n" +
                        "\n" +
                        "    try (Session session = driver.session(SessionConfig.forDatabase(\"neo4j\"))) {\n" +
                        "\n" +
                        "      String cypherQuery =\n" +
                        "        \"MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\\n\" +\n" +
                        "        \" (:Category {categoryName:$category})\\n\" +\n" +
                        "        \" RETURN p.productName as product\";\n" +
                        "\n" +
                        "      var result = session.readTransaction(\n" +
                        "        tx -> tx.run(cypherQuery, \n" +
                        "                parameters(\"category\",\"Dairy Products\"))\n" +
                        "            .list());\n" +
                        "\n" +
                        "      for (Record record : result) {\n" +
                        "        System.out.println(record.get(\"product\").asString());\n" +
                        "      }\n" +
                        "    }\n" +
                        "    driver.close();\n" +
                        "  }\n" +
                        "}\n" +
                        "\n"
        );
    }

    @Test
    void updates_Csharp_example() throws Exception {
        updater.updateCodeExamples(templateRepositoryPath(), sandboxCloneLocation, "https://github.com/neo4j-graph-examples/northwind");

        Path javaExample = sandboxCloneLocation.resolve("code").resolve("csharp").resolve("Example.cs");
        assertThat(String.join("\n", Files.readAllLines(javaExample))).isEqualTo(
                "// install dotnet core on your system\n" +
                        "// dotnet new console -o .\n" +
                        "// dotnet add package Neo4j.Driver\n" +
                        "// paste in this code into Program.cs\n" +
                        "// dotnet run\n" +
                        "\n" +
                        "using System;\n" +
                        "using System.Collections.Generic;\n" +
                        "using System.Text;\n" +
                        "using System.Threading.Tasks;\n" +
                        "using Neo4j.Driver;\n" +
                        "  \n" +
                        "namespace dotnet {\n" +
                        "  class Example {\n" +
                        "  static async Task Main() {\n" +
                        "    var driver = GraphDatabase.Driver(\"bolt://<HOST>:<BOLTPORT>\",\n" +
                        "                    AuthTokens.Basic(\"<USERNAME>\", \"<PASSWORD>\"));\n" +
                        "\n" +
                        "    var cypherQuery =\n" +
                        "      @\"\n" +
                        "      MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\n" +
                        "       (:Category {categoryName:$category})\n" +
                        "       RETURN p.productName as product\n" +
                        "      \";\n" +
                        "\n" +
                        "    var session = driver.AsyncSession(o => o.WithDatabase(\"neo4j\"));\n" +
                        "    var result = await session.ReadTransactionAsync(async tx => {\n" +
                        "      var r = await tx.RunAsync(cypherQuery, \n" +
                        "              new { category=\"Dairy Products\"});\n" +
                        "      return await r.ToListAsync();\n" +
                        "    });\n" +
                        "\n" +
                        "    await session?.CloseAsync();\n" +
                        "    foreach (var row in result)\n" +
                        "      Console.WriteLine(row[\"product\"].As<string>());\n" +
                        "\t  \n" +
                        "    }\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    void updates_js_example() throws Exception {
        updater.updateCodeExamples(templateRepositoryPath(), sandboxCloneLocation, "https://github.com/neo4j-graph-examples/northwind");

        Path javaExample = sandboxCloneLocation.resolve("code").resolve("javascript").resolve("example.js");
        assertThat(String.join("\n", Files.readAllLines(javaExample))).isEqualTo(
                "// npm install --save neo4j-driver\n" +
                        "// node example.js\n" +
                        "const neo4j = require('neo4j-driver');\n" +
                        "const driver = neo4j.driver('bolt://<HOST>:<BOLTPORT>',\n" +
                        "                  neo4j.auth.basic('<USERNAME>', '<PASSWORD>'), \n" +
                        "                  {/* encrypted: 'ENCRYPTION_OFF' */});\n" +
                        "\n" +
                        "const query =\n" +
                        "  `\n" +
                        "  MATCH (p:Product)-[:PART_OF]->(:Category)-[:PARENT*0..]->\n" +
                        "   (:Category {categoryName:$category})\n" +
                        "   RETURN p.productName as product\n" +
                        "  `;\n" +
                        "\n" +
                        "const params = {\"category\": \"Dairy Products\"};\n" +
                        "\n" +
                        "const session = driver.session({database:\"neo4j\"});\n" +
                        "\n" +
                        "session.run(query, params)\n" +
                        "  .then((result) => {\n" +
                        "    result.records.forEach((record) => {\n" +
                        "        console.log(record.get('product'));\n" +
                        "    });\n" +
                        "    session.close();\n" +
                        "    driver.close();\n" +
                        "  })\n" +
                        "  .catch((error) => {\n" +
                        "    console.error(error);\n" +
                        "  });"
        );
    }


    private static String absolutePathOf(Path path) {
        return path.toFile().getAbsolutePath();
    }
}