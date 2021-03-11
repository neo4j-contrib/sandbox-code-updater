package com.neo4j.sandbox.git;

import java.io.IOException;

public final class BlankCommitException extends IOException {

    public BlankCommitException(IOException exception) {
        super(exception);
    }
}
