package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.TransmitterManager;
import org.bukkit.Location;

public class RedstoneModule extends Module {

    public RedstoneModule() {
        super("redstone");
    }

    @Override
    public void setup() {
        registerFunction("get_signal", new ExternalFunction(this, "get_signal", DataType.INT, DataType.INT, DataType.INT, DataType.INT) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                int x = context.getArguments()[0].getValueAs(DataType.INT);
                int y = context.getArguments()[1].getValueAs(DataType.INT);
                int z = context.getArguments()[2].getValueAs(DataType.INT);

                CodeBot bot = (CodeBot) context.getProgram().getExtra("bot");
                Location location = new Location(bot.getLocation().getWorld(), x, y, z);
                RedstoneTransmitter transmitter = CodeBotsPlugin.inst().getSingleton(TransmitterManager.class)
                  .getTransmitter(location);

                return new Data(DataType.INT, transmitter == null ? 0 : transmitter.getPowerAt(bot.getLocation()));
            }
        });

        registerFunction("get_input", new ExternalFunction(this, "get_input", DataType.INT) {
            @Override
            public Data handle(Context context) throws ExecutionException {
                CodeBot bot = (CodeBot) context.getProgram().getExtra("bot");
                return new Data(DataType.INT, bot.getLocation().getBlock().getBlockPower());
            }
        });
    }

}
