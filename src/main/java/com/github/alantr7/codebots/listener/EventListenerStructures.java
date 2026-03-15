package com.github.alantr7.codebots.listener;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.item.BotsItem;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.structure.StructureFactory;
import com.github.alantr7.codebots.world.structure.StructureInstance;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@Singleton
public class EventListenerStructures {

    @EventHandler
    void onStructurePlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        var stack = event.getItem();
        if (stack == null)
            return;

        BotsItem item = BotsItem.getByItemStack(stack);
        if (item == null)
            return;

        event.setCancelled(true);

        var block = event.getClickedBlock();
        var location = block.getType().isSolid() ? block.getRelative(event.getBlockFace()).getLocation() : block.getLocation();

        Direction direction = Direction.fromBlockFace(event.getPlayer().getFacing().getOppositeFace());

        if (!StructureFactory.isPlaceableAt(item.id, new BlockLocation(location), direction)) {
            event.getPlayer().sendMessage(ChatColor.RED + "Not enough space to place this structure here.");
            return;
        }

        StructureInstance structure = StructureFactory.construct(item.id, new BlockLocation(location), direction);
        if (structure != null) {
            StructureInstance.place(structure);
            CodeBotsPlugin.inst().getWorldManager().getWorld(event.getClickedBlock().getWorld()).placeStructure(structure);

            stack.setAmount(stack.getAmount() - 1);
        }
    }

    @EventHandler
    void onStructureBreak(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        BlockLocation location = new BlockLocation(event.getClickedBlock().getLocation());
        StructureInstance structure = location.getStructure();

        if (structure != null) {
            location.world.removeStructure(structure);
            event.getPlayer().getInventory().addItem(structure.getItemDrop());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onStructureBreakInCreative(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
            return;

        BlockLocation location = new BlockLocation(event.getBlock().getLocation());
        StructureInstance structure = location.getStructure();

        if (structure != null) {
            location.world.removeStructure(structure);
        }
    }

}
