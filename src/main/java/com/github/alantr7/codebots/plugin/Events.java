package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.PlayerRegistry;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

@Singleton
public class Events implements Listener {

    @Inject
    BotRegistry registry;

    @Inject
    PlayerRegistry players;

    @Inject
    CodeBotsPlugin plugin;

    @EventHandler
    void onChunkLoad(ChunkLoadEvent event) {
    }

    @EventHandler
    void onChunkUnload(ChunkUnloadEvent event) {
        registry.getBotsInChunk(event.getChunk().getX(), event.getChunk().getZ()).forEach(bot -> {
            bot.setActive(false);
            bot.getProgram().reset();

            plugin.getLogger().info("Bot " + bot.getId() + " has been deactivated due to chunk unload.");
        });
    }

    @EventHandler
    void onBotInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.INTERACTION)
            return;

        var interaction = (Interaction) event.getRightClicked();
        var botId = interaction.getPersistentDataContainer().get(new NamespacedKey(plugin, "bot_id"), PersistentDataType.STRING);

        if (botId == null) {
            return;
        }

        var bot = registry.getBots().get(UUID.fromString(botId));
        if (bot == null) {
            return;
        }

        if (!event.getPlayer().getUniqueId().equals(bot.getOwnerId())) {
            if (!event.getPlayer().hasPermission(Permissions.ACTION_OPEN_ANY_INVENTORY)) {
                event.getPlayer().sendMessage("§cYou do not have access to this bot.");
                return;
            }
        }
        new BotGUI(event.getPlayer(), bot).open();
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        players.registerPlayer(new PlayerData(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        players.unregisterPlayer(event.getPlayer().getUniqueId());
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void registerEvents(@Inject BukkitPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
