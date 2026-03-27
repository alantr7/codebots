package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.world.bot.BotMovement;
import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.config.Config;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.*;

@Singleton
public class BotRegistry {

    @Getter
    private final Map<UUID, CraftCodeBot> bots = new LinkedHashMap<>();

    @Getter
    private final Map<UUID, CraftCodeBot> movingBots = new HashMap<>();

    private final Map<Vector2i, Map<UUID, CraftCodeBot>> botsPerChunk = new LinkedHashMap<>();

    public CodeBot getBotAt(@NotNull Location location) {
        var bots = botsPerChunk.get(new Vector2i(location.getBlockX() >> 4, location.getBlockZ() >> 4));
        if (bots == null)
            return null;

        var blockLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (var bot : bots.values())
            if (bot.getBlockLocation().equals(blockLocation))
                return bot;

        return null;
    }

    public CodeBot getBotMovingTo(@NotNull Location location) {
        var blockLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (var bot : movingBots.values())
            if (bot.getMovement().getDestination().equals(blockLocation))
                return bot;

        return null;
    }

    public boolean isOccupied(@NotNull Location location, @Nullable CodeBot bot) {
        CodeBot occupying;
        return !location.getBlock().getType().isAir() || getBotAt(location) != null ||
                ((occupying = getBotMovingTo(location)) != null && occupying != bot);
    }

    public void updateBotLocation(CraftCodeBot bot) {
        var entity = bot.getEntity();
        if (entity == null)
            return;

        var lastLocation = bot.getLastSavedLocation();

        var location = entity.getLocation();
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

    @InvokePeriodically(interval = 2)
    public void tickBots() {
        var it = movingBots.entrySet().iterator();
        while (it.hasNext()) {
            var bot = it.next().getValue();
            if (bot.getMovement().isCompleted()) {
                bot.setMovement(null);
                bot.setDirty(true);
                it.remove();
            }
        }
    }

}
