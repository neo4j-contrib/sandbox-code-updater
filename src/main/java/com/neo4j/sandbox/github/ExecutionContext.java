package com.neo4j.sandbox.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "context")
public class ExecutionContext {

    private String commit;

    private String dispatch;

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public void setDispatch(String dispatch) {
        this.dispatch = dispatch;
    }

    public String getCommit() {
        return commit;
    }

    public String getDispatch() {
        return dispatch;
    }

    @Override
    public String toString() {
        return "ExecutionContext{" +
                "commit='" + commit + '\'' +
                ", dispatch='" + dispatch + '\'' +
                '}';
    }
}
