package com.github.alantr7.codebots.plugin.listener;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.monitor.Monitor;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.MonitorManager;
import com.github.alantr7.codebots.plugin.monitor.CraftMonitor;
import com.github.alantr7.codebots.plugin.bot.BotFactory;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.monitor.MonitorFactory;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import static com.github.alantr7.codebots.plugin.program.ItemFactory.key;

@Singleton
public class MonitorEventListener implements Listener {

    @Inject
    MonitorManager manager;

    @EventHandler
    void onMonitorPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        var item = event.getItem();
        if (item == null)
            return;

        if (!item.getType().isBlock())
            return;

        var pdc = item.getItemMeta().getPersistentDataContainer();
        var monitorSizeOrdinal = pdc.get(key("MonitorSize"), PersistentDataType.SHORT);

        // If this is null, then the item is not a bot
        if (monitorSizeOrdinal == null)
            return;

        event.setCancelled(true);
        Monitor.Size size = Monitor.Size.values()[monitorSizeOrdinal];

        var block = event.getClickedBlock();
        var location = block.getType().isSolid() ? block.getRelative(event.getBlockFace()).getLocation() : block.getLocation();

        // Check if obstructed
        Location neighbouringBlock = location.clone();
        Direction lookupDirection = Direction.fromBlockFace(event.getPlayer().getFacing().getOppositeFace()).getLeft();
        for (int i = 0; i < size.getWidth(); i++) {
            if (neighbouringBlock.getBlock().getType().isSolid()) {
                event.getPlayer().sendMessage("§cCould not place monitor here.");
                return;
            }
            neighbouringBlock.add(lookupDirection.toVector());
        }

        CraftMonitor monitor = (CraftMonitor) MonitorFactory.createMonitor(pdc.get(key("MonitorId"), PersistentDataType.STRING), location, Direction.fromBlockFace(event.getPlayer().getFacing().getOppositeFace()), size);
        CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(monitor);

        item.setAmount(item.getAmount() - 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onMonitorBreak(BlockBreakEvent event) {
        Monitor monitor = manager.getMonitor(event.getBlock().getLocation());
        if (monitor != null) {
            manager.unregisterMonitor(monitor);
            CodeBotsPlugin.inst().getSingleton(DataLoader.class).delete((CraftMonitor) monitor);

            event.getPlayer().getInventory().addItem(ItemFactory.createMonitorItem(monitor.getId(), monitor.getSize()));
            ((CraftMonitor) monitor).remove();
        }
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void registerEvents(@Inject CodeBotsPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
