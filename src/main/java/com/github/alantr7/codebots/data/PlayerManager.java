package com.github.alantr7.codebots.data;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.player.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class PlayerManager {

    final Map<UUID, PlayerData> players = new HashMap<>();

    public PlayerData getPlayer(UUID id) {
        return players.computeIfAbsent(id, PlayerData::new);
    }

    public void registerPlayer(PlayerData player) {
        players.put(player.getId(), player);
    }

    public void unregisterPlayer(UUID id) {
        players.remove(id);
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        registerPlayer(new PlayerData(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        unregisterPlayer(event.getPlayer().getUniqueId());
    }

}
