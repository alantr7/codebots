package com.github.alantr7.codebots.api;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.BotFactory;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.data.PlayerRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface CodeBots {

    static CodeBot createBot(@NotNull UUID ownerId, @NotNull Location location) {
        return BotFactory.createBot(ownerId, location);
    }

    static @Nullable CodeBot getBot(UUID id) {
        return CodeBotsPlugin.inst().getSingleton(BotRegistry.class).getBots().get(id);
    }

    static boolean isBlockOccupied(@NotNull Location location) {
        return CodeBotsPlugin.inst().getSingleton(BotRegistry.class).isOccupied(location);
    }

    static @Nullable PlayerData getPlayer(UUID id) {
        return CodeBotsPlugin.inst().getSingleton(PlayerRegistry.class).getPlayer(id);
    }

    @SuppressWarnings("all")
    static @NotNull PlayerData getPlayer(@NotNull Player player) {
        return getPlayer(player.getUniqueId());
    }

    static @Nullable CodeBot getSelectedBot(@NotNull Player player) {
        return getPlayer(player).getSelectedBot();
    }

    static @NotNull ProgramSource loadProgram(@NotNull Directory directory, @NotNull File file) throws ParserException, IOException {
        return CodeBotsPlugin.inst().getSingleton(DataLoader.class).loadProgram(directory, file);
    }

}
