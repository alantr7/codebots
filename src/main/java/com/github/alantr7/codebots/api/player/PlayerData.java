package com.github.alantr7.codebots.api.player;

import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerData {

    @Getter
    private final UUID id;

    private UUID selectedBot;

    public PlayerData(UUID id) {
        this.id = id;
    }

    public boolean isOnline() {
        return bukkit() != null;
    }

    public Player bukkit() {
        return Bukkit.getPlayer(id);
    }

    public @Nullable CodeBot getSelectedBot() {
        return selectedBot != null ? CodeBots.getBot(selectedBot) : null;
    }

    public void setSelectedBot(@Nullable CodeBot bot) {
        this.selectedBot = bot != null ? bot.getId() : null;
    }

    public static PlayerData get(Player player) {
        return CodeBots.getPlayer(player);
    }

}
