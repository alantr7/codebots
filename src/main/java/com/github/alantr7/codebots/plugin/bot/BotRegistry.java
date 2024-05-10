package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.language.runtime.Program;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class BotRegistry {

    private final Map<UUID, CraftCodeBot> bots = new LinkedHashMap<>();

    @Inject
    public static BotRegistry instance;

    public void registerBot(CraftCodeBot display) {
        bots.put(display.getEntity().getUniqueId(), display);
    }

    public void unregisterBot(UUID id) {
        bots.remove(id);
    }

    public Map<UUID, CraftCodeBot> getBots() {
        return bots;
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_DISABLE)
    public void despawnEntities() {
        bots.forEach((id, bot) -> bot.getEntity().remove());
        bots.clear();
    }

    @InvokePeriodically(interval = 2)
    public void tickBots() {
        bots.forEach((id, bot) -> {
            var program = bot.getProgram();
            if (program != null && bot.isActive()) {
                if (program.getMainModule().hasNext() && !program.getEnvironment().isInterrupted()) {
                    program.action(Program.Mode.AUTO_HALT);
                } else {
                    bot.setActive(false);
                }
            }
        });
    }

}
