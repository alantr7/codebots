package com.github.alantr7.codebots.integration.torus.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.config.Config;
import com.github.alantr7.codebots.integration.torus.machine.ComputerInstance;

public class ComputerModule extends Module {

    public ComputerModule() {
        super("computer");
    }

    @Override
    public void setup() {
        registerFunction("chat", new ExternalFunction(this, "chat", DataType.INT, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                var computer = (ComputerInstance) context.getProgram().getExtra("computer");
                computer.chat(context.getArguments()[0].getValueAs(DataType.STRING));

                return new Data(DataType.INT, 1);
            }
        });

        registerFunction("play_sound", new ExternalFunction(this, "play_sound", DataType.INT, DataType.STRING, DataType.FLOAT, DataType.FLOAT) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                if (!Config.BOT_ALLOW_SOUNDS_PLAYING) {
                    throw new ExecutionException("Playing sounds is disabled");
                }

                var computer = (ComputerInstance) context.getProgram().getExtra("computer");
                String sound = context.getArgumentAs(0, DataType.STRING);

                computer.location.world.getBukkit().playSound(computer.location.toBukkitCentered(), sound, context.getArgumentAs(1, DataType.FLOAT), context.getArgumentAs(2, DataType.FLOAT));
                return Data.of(1);
            }
        });
    }

}
