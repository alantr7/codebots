package com.github.alantr7.codebots.codeint.functions;

import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.codeint.http.HttpManager;
import com.github.alantr7.codebots.codeint.modules.HttpModule;

public class HttpGetFunction extends ExternalFunction {

    public HttpGetFunction(HttpModule module) {
        super(module, "get", DataType.INT, DataType.STRING, DataType.STRING);
    }

    @Override
    public void prepareContext(Context context) {
        context.getMemory()[0] = new Data(DataType.INT, 0); // request sent
        context.getMemory()[1] = new Data(DataType.INT, 0); // response received
    }

    @Override
    public Data handle(Context context) throws ExecutionException {
        if (context.getMemory()[0].getValueAs(DataType.INT) == 0) {
            String responseType = context.getArgumentAs(1, DataType.STRING);
            String url = context.getArgumentAs(0, DataType.STRING);

            if (responseType.equals("json")) {
                CodeBotsPlugin.inst().getSingleton(HttpManager.class).getJson(url)
                    .whenComplete((response, throwable) -> {
                        System.out.println(response.response.body());
                        context.getMemory()[1].setValue(DataType.INT, response.handle);
                    });
            }
            else if (responseType.equals("text")) {
                CodeBotsPlugin.inst().getSingleton(HttpManager.class).getString(url)
                  .whenComplete((response, throwable) -> {
                      System.out.println(response.response.body());
                      context.getMemory()[1].setValue(DataType.INT, response.handle);
                  });
            } else {
                throw new ExecutionException("Invalid response type: " + responseType + ". It must be be json or text.");
            }

            context.getMemory()[0].setValue(DataType.INT, 1);
        } else {
            if (context.getMemory()[1].getValueAs(DataType.INT) != 0) {
                context.setRecall(false);
                return Data.of(context.getMemory()[1].getValueAs(DataType.INT));
            }
        }

        context.setRecall(true);
        return null;
    }

}
