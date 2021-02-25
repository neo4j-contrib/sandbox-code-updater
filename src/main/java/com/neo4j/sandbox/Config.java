package com.neo4j.sandbox;

import com.fasterxml.jackson.databind.ObjectMapper;
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
}
