package com.github.alantr7.codebots.plugin.listener;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.data.TransmitterManager;
import com.github.alantr7.codebots.plugin.redstone.CraftRedstoneTransmitter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@Singleton
public class RedstoneEventListener implements Listener {

    @Inject
    TransmitterManager transmitterRegistry;

    @EventHandler
    void onTransmitterBreak(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        RedstoneTransmitter transmitter = transmitterRegistry.getTransmitter(event.getClickedBlock().getLocation());
        if (transmitter == null)
            return;

        transmitterRegistry.unregisterTransmitter(transmitter);
        CodeBotsPlugin.inst().getSingleton(DataLoader.class).delete((CraftRedstoneTransmitter) transmitter);

        ((CraftRedstoneTransmitter) transmitter).remove();
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void registerEvents(@Inject CodeBotsPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
