package com.github.alantr7.codebots.api;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.world.BotsWorld;
import com.github.alantr7.codebots.world.bot.BotFactory;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.data.PlayerManager;
import com.github.alantr7.codebots.world.BlockLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface CodeBots {

    static CodeBot createBot(@NotNull UUID ownerId, @NotNull BlockLocation location) {
        return BotFactory.createBot(ownerId, location);
    }

    static @Nullable CodeBot getBot(UUID id) {
        for (BotsWorld world : CodeBotsPlugin.inst().getWorldManager().getWorlds()) {
            CodeBot bot = world.getBot(id);
            if (bot != null)
                return bot;
        }
        return null;
    }

    static boolean isBlockOccupied(@NotNull Location location, @Nullable CodeBot bot) {
        return new BlockLocation(location).world.isOccupied(location, bot);
    }

    static @Nullable PlayerData getPlayer(UUID id) {
        return CodeBotsPlugin.inst().getSingleton(PlayerManager.class).getPlayer(id);
    }

    @SuppressWarnings("all")
    static @NotNull PlayerData getPlayer(@NotNull Player player) {
        return getPlayer(player.getUniqueId());
    }

    static @NotNull ProgramSource loadProgram(@NotNull Directory directory, @NotNull BotFile file) {
        return CodeBotsPlugin.inst().getSingleton(DataLoader.class).loadProgram(directory, file);
    }

}
