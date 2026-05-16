package com.github.alantr7.codebots.codeint.http;

import java.util.UUID;

public class JsonResponse extends Response<Object> {

    public JsonResponse(int handle, UUID processId, long expiry, int statusCode, String body, Object value, String exception) {
        super(handle, processId, expiry, statusCode, body, value, exception);
    }

}
