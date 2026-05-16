package com.github.alantr7.codebots.codeint.http;

import java.util.UUID;

public abstract class Response<T> {

    public final int handle;

    public final UUID processId;

    public final long expiry;

    public final int statusCode;

    public final String body;

    public final String exception;

    public final T value;

    public static final long STANDARD_LIFETIME = 7L * 86_400L * 1000L;

    public Response(int handle, UUID processId, long expiry, int statusCode, String body, T value, String exception) {
        this.handle = handle;
        this.processId = processId;
        this.expiry = expiry;
        this.statusCode = statusCode;
        this.body = body;
        this.value = value;
        this.exception = exception;
    }

}
