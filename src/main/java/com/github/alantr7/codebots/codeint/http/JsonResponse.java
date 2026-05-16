package com.github.alantr7.codebots.codeint.http;

import java.net.http.HttpResponse;

public class JsonResponse extends Response<Object> {

    public JsonResponse(int handle, HttpResponse<String> response, Object value, Throwable exception) {
        super(handle, response, value, exception);
    }

}
