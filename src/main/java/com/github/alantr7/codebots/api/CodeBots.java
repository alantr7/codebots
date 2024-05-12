package com.github.alantr7.codebots.api;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.PlayerRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface CodeBots {

    static @Nullable CodeBot getBot(UUID id) {
        return CodeBotsPlugin.inst().getSingleton(BotRegistry.class).getBots().get(id);
    }

    static @Nullable PlayerData getPlayer(UUID id) {
        return CodeBotsPlugin.inst().getSingleton(PlayerRegistry.class).getPlayer(id);
    }

    @SuppressWarnings("all")
    static @NotNull PlayerData getPlayer(@NotNull Player player) {
        return getPlayer(player.getUniqueId());
    }

}
