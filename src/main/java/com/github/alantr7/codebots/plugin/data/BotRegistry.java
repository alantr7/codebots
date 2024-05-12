package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import org.bukkit.Location;
import org.joml.Vector2i;

import java.util.*;

@Singleton
public class BotRegistry {

    private final Map<UUID, CraftCodeBot> bots = new LinkedHashMap<>();

    private final Map<Vector2i, Map<UUID, CraftCodeBot>> botsPerChunk = new LinkedHashMap<>();

    @Inject
    public static BotRegistry instance;

    public void registerBot(CraftCodeBot display) {
        bots.put(display.getId(), display);
    }

    public void unregisterBot(UUID id) {
        bots.remove(id);
    }

    public void updateBotLocation(CraftCodeBot bot) {
        var entity = bot.getEntity();
        var location = entity.getLocation();
        var lastLocation = bot.getLastSavedLocation();

        if (lastLocation != null) {
            if (lastLocation.getWorld() == location.getWorld() && lastLocation.hashCode() == location.hashCode())
                return;

            var map = botsPerChunk.get(new Vector2i(lastLocation.getBlockX() >> 4, lastLocation.getBlockZ() >> 4));
            if (map != null) {
                map.remove(bot.getId());
            }
        }

        var chunk = new Vector2i(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        var map = botsPerChunk.computeIfAbsent(chunk, k -> new HashMap<>());
        map.put(bot.getId(), bot);
        bot.setLastSavedLocation(location);
    }

    public Collection<CraftCodeBot> getBotsInChunk(Location location) {
        return getBotsInChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public Collection<CraftCodeBot> getBotsInChunk(int x, int z) {
        return botsPerChunk.getOrDefault(new Vector2i(x, z), Collections.emptyMap()).values();
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
