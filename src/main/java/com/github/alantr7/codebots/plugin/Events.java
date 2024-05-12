package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

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

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void registerEvents(@Inject BukkitPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
