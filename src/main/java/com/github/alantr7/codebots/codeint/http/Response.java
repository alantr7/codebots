package com.github.alantr7.codebots.codeint.http;

import java.net.http.HttpResponse;

public abstract class Response<T> {

    public final int handle;

    public final HttpResponse<String> response;

    public final Exception exception;

    public final T value;

    public Response(int handle, HttpResponse<String> response, T value, Exception exception) {
        this.handle = handle;
        this.response = response;
        this.value = value;
        this.exception = exception;
    }

}
