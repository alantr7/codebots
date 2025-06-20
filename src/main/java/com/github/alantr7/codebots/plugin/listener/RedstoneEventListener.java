package com.github.alantr7.codebots.plugin.listener;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.data.TransmitterManager;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import com.github.alantr7.codebots.plugin.redstone.CraftRedstoneTransmitter;
import com.github.alantr7.codebots.plugin.redstone.TransmitterFactory;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.github.alantr7.codebots.plugin.program.ItemFactory.key;

@Singleton
public class RedstoneEventListener implements Listener {

    @Inject
    TransmitterManager transmitterRegistry;

    @EventHandler
    void onTransmitterPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        var item = event.getItem();
        if (item == null)
            return;

        if (!item.getType().isBlock())
            return;

        var pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(key("Transmitter")))
            return;

        event.setCancelled(true);

        var block = event.getClickedBlock();
        var location = block.getType().isSolid() ? block.getRelative(event.getBlockFace()).getLocation() : block.getLocation();

        // Check if obstructed
        if (location.getBlock().getType().isSolid()) {
            event.getPlayer().sendMessage("§cCould not place a transmitter here.");
            return;
        }

        CraftRedstoneTransmitter transmitter = (CraftRedstoneTransmitter) TransmitterFactory.createTransmitter(location);
        transmitterRegistry.registerTransmitter(transmitter);
        CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(transmitter);

        item.setAmount(item.getAmount() - 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onTransmitterBreakCreative(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;

        RedstoneTransmitter transmitter = transmitterRegistry.getTransmitter(event.getBlock().getLocation());
        if (transmitter != null) {
            transmitterRegistry.unregisterTransmitter(transmitter);
            CodeBotsPlugin.inst().getSingleton(DataLoader.class).delete((CraftRedstoneTransmitter) transmitter);

            ((CraftRedstoneTransmitter) transmitter).remove();
        }
    }

    @EventHandler
    void onTransmitterBreak(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        RedstoneTransmitter transmitter = transmitterRegistry.getTransmitter(event.getClickedBlock().getLocation());
        if (transmitter == null)
            return;

        transmitterRegistry.unregisterTransmitter(transmitter);
        CodeBotsPlugin.inst().getSingleton(DataLoader.class).delete((CraftRedstoneTransmitter) transmitter);

        event.getPlayer().getInventory().addItem(ItemFactory.createTransmitterItem());
        ((CraftRedstoneTransmitter) transmitter).remove();
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void registerEvents(@Inject CodeBotsPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
