package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.player.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class PlayerRegistry {

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

}
