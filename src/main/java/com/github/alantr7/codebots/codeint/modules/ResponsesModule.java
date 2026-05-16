package com.github.alantr7.codebots.codeint.modules;

import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.codeint.http.HttpManager;
import com.github.alantr7.codebots.codeint.http.JsonResponse;
import com.github.alantr7.codebots.codeint.http.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ResponsesModule extends Module {

    private final HttpManager httpManager;

    public ResponsesModule() {
        super("responses");
        this.httpManager = CodeBotsPlugin.inst().getSingleton(HttpManager.class);
    }

    @Override
    public void setup() {
        registerFunction(new ExternalFunction(this, "get_raw", DataType.STRING, DataType.INT) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                Response<?> response = httpManager.getResponse(context.getArgumentAs(0, DataType.INT));
                if (response == null) {
                    throw new ExecutionException("Unknown response with handle: " + context.getArgumentAs(0, DataType.INT));
                }
                return Data.of(response.body);
            }
        });
        registerFunction(new ExternalFunction(this, "get_status_code", DataType.INT, DataType.INT) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                Response<?> response = httpManager.getResponse(context.getArgumentAs(0, DataType.INT));
                if (response == null) {
                    throw new ExecutionException("Unknown response with handle: " + context.getArgumentAs(0, DataType.INT));
                }
                return Data.of(response.statusCode);
            }
        });

        registerFunction(new ExternalFunction(this, "get_int", DataType.INT, DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                Response<?> response = httpManager.getResponse(context.getArgumentAs(0, DataType.INT));
                if (response == null) {
                    throw new ExecutionException("Unknown response with handle: " + context.getArgumentAs(0, DataType.INT));
                }
                if (!(response instanceof JsonResponse jsonResponse)) {
                    throw new ExecutionException("Can not use JSON functions on non-json response");
                }
                if (!(getJsonElement(jsonResponse.value, context.getArgumentAs(1, DataType.STRING)) instanceof Long num)) {
                    return Data.of(0);
                }
                return Data.of(num.intValue());
            }
        });
        registerFunction(new ExternalFunction(this, "get_float", DataType.FLOAT, DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                Response<?> response = httpManager.getResponse(context.getArgumentAs(0, DataType.INT));
                if (response == null) {
                    throw new ExecutionException("Unknown response with handle: " + context.getArgumentAs(0, DataType.INT));
                }
                if (!(response instanceof JsonResponse jsonResponse)) {
                    throw new ExecutionException("Can not use JSON functions on non-json response");
                }
                if (!(getJsonElement(jsonResponse.value, context.getArgumentAs(1, DataType.STRING)) instanceof Double num)) {
                    return Data.of(0f);
                }
                return Data.of(num.floatValue());
            }
        });
        registerFunction(new ExternalFunction(this, "get_string", DataType.STRING, DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                Response<?> response = httpManager.getResponse(context.getArgumentAs(0, DataType.INT));
                if (response == null) {
                    throw new ExecutionException("Unknown response with handle: " + context.getArgumentAs(0, DataType.INT));
                }
                if (!(response instanceof JsonResponse jsonResponse)) {
                    throw new ExecutionException("Can not use JSON functions on non-json response");
                }
                if (!(getJsonElement(jsonResponse.value, context.getArgumentAs(1, DataType.STRING)) instanceof String string)) {
                    return Data.of("");
                }
                return Data.of(string);
            }
        });
    }

    private static Object getJsonElement(Object json, String path0) {
        Object current = json;
        String path = path0.trim();
        while (!path.isEmpty()) {
            String key;
            int index;

            int nextDot = path.indexOf('.');
            int nextBracket = path.indexOf('[');

            if (nextDot == -1 && nextBracket == -1) {
                key = path;
                index = -1;
                path = "";
            } else {
                if ((nextDot < nextBracket && nextDot != -1) || nextBracket == -1) {
                    key = path.substring(0, nextDot);
                    path = path.substring(nextDot + 1);
                    index = -1;
                }

                else if (nextBracket < nextDot || nextDot == -1) {
                    int closeIndex = path.indexOf(']', nextBracket);
                    String indexString = path.substring(nextBracket + 1, closeIndex);
                    key = path.substring(0, nextBracket);
                    path = path.substring(closeIndex + 1);
                    if (nextDot == closeIndex + 1) {
                        path = path.substring(1);
                    }
                    index = Integer.parseInt(indexString);
                }

                else return null; // todo: throw error
            }

            if (index != -1) {
                if (!key.isEmpty()) {
                    if (!(current instanceof JSONObject object)) {
                        return null; // todo: throw error
                    }

                    current = object.get(key);
                }
                if (!(current instanceof JSONArray array)) {
                    return null; // todo: throw error
                }
                current = array.get(index);
                continue;
            }

            if (!(current instanceof JSONObject object)) {
                return null; // todo: throw error
            }
            current = object.get(key);
        }

        return current;
    }

}
