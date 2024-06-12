package com.neo4j.sandbox.updater;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

@Service
public class BrowserGuideConverter {

    private final File browserGuideTemplatesDir;

    private final Asciidoctor parser;

    public BrowserGuideConverter(Asciidoctor parser) throws URISyntaxException {
        this.parser = parser;
        this.browserGuideTemplatesDir = new File(Objects.requireNonNull(BrowserGuideConverter.class.getResource("/templates/browser-guide")).toURI());
    }

    public String convert(File asciiDocFile) {
        Options asciidoctorOptions = Options.builder()
            .templateDirs(this.browserGuideTemplatesDir)
            .headerFooter(true)
            .toFile(false)
            .build();
        return parser.convertFile(asciiDocFile, asciidoctorOptions);
    }
}
