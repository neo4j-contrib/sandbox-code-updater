package com.neo4j.sandbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo4j.sandbox.git.GitCli;
import com.neo4j.sandbox.git.GitOperations;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Asciidoctor asciidoctor() {
        return Asciidoctor.Factory.create();
    }

    @Bean
    public GitOperations gitCli() {
        return new GitCli();
    }
}
