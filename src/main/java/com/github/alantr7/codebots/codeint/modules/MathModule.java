package com.github.alantr7.codebots.codeint.modules;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;

public class MathModule extends Module {

    public MathModule() {
        super("math");
    }

    @Override
    public void setup() {
        registerFunction("abs", new ExternalFunction(this, "abs", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of(Math.abs(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("floor", new ExternalFunction(this, "floor", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.floor(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("ceil", new ExternalFunction(this, "ceil", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.ceil(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("round", new ExternalFunction(this, "round", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.round(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("random", new ExternalFunction(this, "random", DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.random());
            }
        });
        registerFunction("sin", new ExternalFunction(this, "sin", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.sin(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("cos", new ExternalFunction(this, "cos", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.cos(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("tan", new ExternalFunction(this, "tan", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.tan(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("cot", new ExternalFunction(this, "cot", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of(1f / (float) Math.tan(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("atan", new ExternalFunction(this, "atan", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.atan(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
        registerFunction("atan2", new ExternalFunction(this, "atan2", DataType.FLOAT, DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.atan2(context.getArgumentAs(0, DataType.FLOAT), context.getArgumentAs(1, DataType.FLOAT)));
            }
        });
        registerFunction("sqrt", new ExternalFunction(this, "sqrt", DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return Data.of((float) Math.sqrt(context.getArgumentAs(0, DataType.FLOAT)));
            }
        });
    }

}
