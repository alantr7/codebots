package com.github.alantr7.codebots.codeint.http;

import java.util.UUID;

public class StringResponse extends Response<String> {

    public StringResponse(int handle, UUID processId, long expiry, int statusCode, String body, String exception) {
        super(handle, processId, expiry, statusCode, body, body, exception);
    }

}
