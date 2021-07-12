package com.neo4j.sandbox;

import com.neo4j.sandbox.updater.BrowserGuideConverter;
import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;

import static com.neo4j.sandbox.updater.TestPaths.classpathFile;
import static org.assertj.core.api.Assertions.assertThat;

public class GenerateBrowserGuideTest {

    @Test
    public void converts_browser_guide() throws URISyntaxException {
        BrowserGuideConverter browserGuideConverter = new BrowserGuideConverter(Asciidoctor.Factory.create());
        File asciiDocFile = classpathFile("/fake-twitter-repo/documentation/twitter.adoc").toFile();
        String content = browserGuideConverter.convert(asciiDocFile);
        assertThat(content).contains("<article class=\"guide\" ng-controller=\"AdLibDataController\">");
        assertThat(content).contains("(u:Me:User)-[p:POSTS]-&gt;(t:Tweet)-[:MENTIONS]-&gt;(m:User)");
    }
}
