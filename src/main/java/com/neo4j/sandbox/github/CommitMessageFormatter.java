package com.neo4j.sandbox.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo4j.sandbox.updater.BatchUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommitMessageFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchUpdater.class);

    private final ObjectMapper objectMapper;

    private final ExecutionContext context;

    public CommitMessageFormatter(ObjectMapper objectMapper,
                                  ExecutionContext context) {

        this.objectMapper = objectMapper;
        this.context = context;
    }

    public String createMessage() {
        LOGGER.debug("Execution context: {}", context);

        String dispatch = context.getDispatch();
        if (isSet(dispatch)) {
            DispatchContext dispatchContext = deserialize(dispatch);
            return String.format("Triggered by dispatch event. Origin: https://github.com/%s/commit/%s",
                    dispatchContext.getSourceRepository(),
                    dispatchContext.getCommitReference());
        }
        String directCommitRef = context.getCommit();
        if (!directCommitRef.isBlank()) {
            return String.format(
                    "Triggered by direct commit. Origin: https://github.com/neo4j-contrib/sandbox-code-updater/commit/%s",
                    directCommitRef);
        }
        return "Manually triggered";
    }

    private DispatchContext deserialize(String dispatch) {
        try {
            return objectMapper.readValue(dispatch, DispatchContext.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Could not deserialize dispatch context when creating commit message", e);
        }
    }

    // quick fix to work around GitHub built-in toJson function that prints null like this
    // see https://docs.github.com/en/actions/reference/context-and-expression-syntax-for-github-actions#tojson
    private boolean isSet(String dispatch) {
        return dispatch != null && !dispatch.equals("") && !dispatch.equals("null");
    }
}
