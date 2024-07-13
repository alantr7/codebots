package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.bot.BotMovement;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.*;

@Singleton
public class BotRegistry {

    @Getter
    private final Map<UUID, CraftCodeBot> bots = new LinkedHashMap<>();

    @Getter
    private final Map<UUID, CraftCodeBot> movingBots = new HashMap<>();

    private final Map<Vector2i, Map<UUID, CraftCodeBot>> botsPerChunk = new LinkedHashMap<>();

    @Inject
    public static BotRegistry instance;

    public void registerBot(CraftCodeBot bot) {
        bots.put(bot.getId(), bot);
        botsPerChunk.computeIfAbsent(new Vector2i(bot.getLocation().getBlockX() >> 4, bot.getLocation().getBlockZ() >> 4), k -> new HashMap<>()).put(bot.getId(), bot);
    }

    public void unregisterBot(UUID id) {
        bots.remove(id);
        movingBots.remove(id);
    }

    public CodeBot getBotAt(@NotNull Location location) {
        var bots = botsPerChunk.get(new Vector2i(location.getBlockX() >> 4, location.getBlockZ() >> 4));
        if (bots == null)
            return null;

        // TODO: Use BlockVector instead of getting the block and then getting its location
        for (var bot : bots.values())
            if (bot.getLocation().getBlock().getLocation().equals(location.getBlock().getLocation()))
                return bot;

        return null;
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
        bot.setCachedLocation(location);
        bot.setLastSavedLocation(location);
    }

    public Collection<CraftCodeBot> getBotsInChunk(Location location) {
        return getBotsInChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public Collection<CraftCodeBot> getBotsInChunk(int x, int z) {
        return botsPerChunk.getOrDefault(new Vector2i(x, z), Collections.emptyMap()).values();
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

        var it = movingBots.entrySet().iterator();
        while (it.hasNext()) {
            var bot = it.next().getValue();
            if (bot.getMovement().isCompleted()) {
                if (bot.getMovement().getType() == BotMovement.Type.TRANSLATION) {
                    try {
                        bot.completeTranslation();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                bot.setMovement(null);
                it.remove();
            }
        }
    }

}
