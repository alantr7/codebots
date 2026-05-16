package com.github.alantr7.codebots.codeint.http;

import java.net.http.HttpResponse;

public class StringResponse extends Response<String> {

    public StringResponse(int handle, HttpResponse<String> response, String value, Throwable exception) {
        super(handle, response, value, exception);
    }

}
