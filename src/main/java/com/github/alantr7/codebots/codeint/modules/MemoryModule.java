package com.github.alantr7.codebots.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Memory;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;

public class MemoryModule extends Module {

    public MemoryModule() {
        super("memory");
    }

    @Override
    public void setup() {
        registerFunction("save_int", new ExternalFunction(this, "save_int", DataType.INT, DataType.STRING, DataType.INT) {
            @Override
            public Data handle(Context context) {
                return handleSave(context, DataType.INT);
            }
        });
        registerFunction("save_float", new ExternalFunction(this, "save_float", DataType.INT, DataType.STRING, DataType.FLOAT) {
            @Override
            public Data handle(Context context) {
                return handleSave(context, DataType.FLOAT);
            }
        });
        registerFunction("save_string", new ExternalFunction(this, "save_string", DataType.INT, DataType.STRING, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return handleSave(context, DataType.STRING);
            }
        });

        registerFunction("load_int", new ExternalFunction(this, "load_int", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return handleLoad(context, DataType.INT);
            }
        });
        registerFunction("load_float", new ExternalFunction(this, "load_float", DataType.FLOAT, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return handleLoad(context, DataType.FLOAT);
            }
        });
        registerFunction("load_string", new ExternalFunction(this, "load_string", DataType.STRING, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                return handleLoad(context, DataType.STRING);
            }
        });
    }

    private static <T> Data handleLoad(Context context, DataType<T> type) {
        Memory memory = ((CodeBot) context.getProgram().getExtra("bot")).getMemory();
        String key = context.getArgumentAs(0, DataType.STRING);
        return switch (type.getSerializationId()) {
            case 1 -> Data.of(memory.load(key, DataType.INT, 0));
            case 2 -> Data.of(memory.load(key, DataType.LONG, 0L));
            case 3 -> Data.of(memory.load(key, DataType.FLOAT, 0f));
            case 4 -> Data.of(memory.load(key, DataType.STRING, ""));
            default -> null;
        };
    }

    private static <T> Data handleSave(Context context, DataType<T> type) {
        Memory memory = ((CodeBot) context.getProgram().getExtra("bot")).getMemory();
        String key = context.getArgumentAs(0, DataType.STRING);
        try {
            switch (type.getSerializationId()) {
                case 1 -> memory.save(key, DataType.INT, context.getArgumentAs(1, DataType.INT));
                case 2 -> memory.save(key, DataType.LONG, context.getArgumentAs(1, DataType.LONG));
                case 3 -> memory.save(key, DataType.FLOAT, context.getArgumentAs(1, DataType.FLOAT));
                case 4 -> memory.save(key, DataType.STRING, context.getArgumentAs(1, DataType.STRING));
                default -> {
                    return Data.of(0);
                }
            };
            return Data.of(1);
        } catch (Exception e) {
            context.getProgram().interrupt(e);
            return Data.of(0);
        }
    }

}
