package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.Assertions;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.TransmitterManager;
import org.bukkit.Location;

public class RedstoneModule extends NativeModule {

    public RedstoneModule(Program program) {
        super(program);

        registerFunction("getSignal", args -> {
            Assertions.expectArguments(args, Integer.class, Integer.class, Integer.class);
            int x = (int) args[0];
            int y = (int) args[1];
            int z = (int) args[2];

            CodeBot bot = (CodeBot) program.getExtra("bot");

            Location location = new Location(bot.getLocation().getWorld(), x, y, z);
            RedstoneTransmitter transmitter = CodeBotsPlugin.inst().getSingleton(TransmitterManager.class)
                    .getTransmitter(location);

            return transmitter == null ? 0 : transmitter.getPowerAt(bot.getLocation());
        });

        registerFunction("getInput", args -> {
            CodeBot bot = (CodeBot) program.getExtra("bot");
            return bot.getLocation().getBlock().getBlockPower();
        });
    }

}
