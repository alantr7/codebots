package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
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
//        registerFunction("save", new ExternalFunction() {
//            @Override
//            public Data handle(Context context) {
//                var bot = (CodeBot) context.getProgram().getExtra("bot");
//                bot.getMemory().save(context.getArguments()[0].getValueAs(DataType.STRING), (DataType<Object>) DataType.of(args[1]), args[1]);
//
//                return null;
//            }
//        });
//        registerFunction("save", args -> {
//            expectArguments(args, String.class, Object.class);
//
//        });
//
//        registerFunction("load", args -> {
//            expectArguments(args, String.class);
//            var bot = (CodeBot) program.getExtra("bot");
//            var value = bot.getMemory().load((String) args[0], DataType.ANY);
//
//            return value != null || args.length == 1 ? value : args[1];
//        });
    }
}
