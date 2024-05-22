package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.runtime.DataType;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;

import static com.github.alantr7.codebots.language.runtime.errors.Assertions.expectArguments;

public class MemoryModule extends NativeModule {

    public MemoryModule(Program program) {
        super(program);
        init();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        registerFunction("save", args -> {
            expectArguments(args, String.class, Object.class);
            var bot = (CodeBot) program.getExtra("bot");
            bot.getMemory().save((String) args[0], (DataType<Object>) DataType.of(args[1]), args[1]);

            return null;
        });

        registerFunction("load", args -> {
            expectArguments(args, String.class);
            var bot = (CodeBot) program.getExtra("bot");
            var value = bot.getMemory().load((String) args[0], DataType.ANY);

            return value != null || args.length == 1 ? value : args[1];
        });
    }

}
