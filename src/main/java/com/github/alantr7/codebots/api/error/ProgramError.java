package com.github.alantr7.codebots.api.error;

import lombok.Getter;

@Getter
public class ProgramError {

    private final ErrorLocation location;

    private final String message;

    private final String[] stackTrace;

    public enum ErrorLocation {
        PARSER, EXECUTION
    }

    public ProgramError(ErrorLocation location, String message) {
        this(location, message, new String[0]);
    }

    public ProgramError(ErrorLocation location, String message, String[] stackTrace) {
        this.location = location;
        this.message = message;
        this.stackTrace = stackTrace;
    }

}
