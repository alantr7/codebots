package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

@Singleton
public class Events implements Listener {

    @Inject
    BotRegistry registry;

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
        event.getPlayer().sendMessage("You clicked on " + event.getRightClicked().getType());
        if (event.getRightClicked().getType() != EntityType.INTERACTION)
            return;

        var interaction = (Interaction) event.getRightClicked();
        var botId = interaction.getPersistentDataContainer().get(new NamespacedKey(plugin, "bot_id"), PersistentDataType.STRING);

        if (botId == null) {
            event.getPlayer().sendMessage("Bot id is null.");
            return;
        }

        var bot = registry.getBots().get(UUID.fromString(botId));
        if (bot == null) {
            event.getPlayer().sendMessage("Bot is null.");
            return;
        }

        event.getPlayer().sendMessage("§eYou right-clicked a bot!");
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void registerEvents(@Inject BukkitPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
