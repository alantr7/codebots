package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;

public class LangModule extends Module {

    public LangModule() {
        super("lang");
    }

    @Override
    public void setup() {
        registerFunction("is_int", new ExternalFunction(this, "is_int", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return Data.of(context.getArgumentAs(0, DataType.STRING).matches("\\d+") ? 1 : 0);
            }
        });
        registerFunction("is_float", new ExternalFunction(this, "is_int", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return Data.of(context.getArgumentAs(0, DataType.STRING).matches("-?((\\d+\\.\\d+f?)|(\\d+f))") ? 1 : 0);
            }
        });
        registerFunction("to_int", new ExternalFunction(this, "to_int", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                try {
                    return Data.of(Integer.parseInt(context.getArgumentAs(0, DataType.STRING)));
                } catch (Exception exc) {
                    throw new ExecutionException(exc.getMessage());
                }
            }
        });
        registerFunction("to_float", new ExternalFunction(this, "to_float", DataType.FLOAT, DataType.STRING) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                try {
                    return Data.of(Float.parseFloat(context.getArgumentAs(0, DataType.STRING)));
                } catch (Exception exc) {
                    throw new ExecutionException(exc.getMessage());
                }
            }
        });
        registerFunction("sleep", new ExternalFunction(this, "sleep", DataType.INT, DataType.INT) {
            @Override
            public void prepareContext(Context context) {
                context.getMemory()[0] = Data.of(0);
            }
            @Override
            public Data handle(Context context) throws ExecutionException {
                Data ticks = context.getMemory()[0];
                if (ticks.getValueAs(DataType.INT) >= context.getArguments()[0].getValueAs(DataType.INT))
                    return Data.of(1);

                ticks.updateValue(DataType.INT, t -> t + 1);
                context.setRecall(true);
                return null;
            }
        });
        registerFunction("strlen", new ExternalFunction(this, "strlen", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return Data.of(context.getArgumentAs(0, DataType.STRING).length());
            }
        });
    }

}
