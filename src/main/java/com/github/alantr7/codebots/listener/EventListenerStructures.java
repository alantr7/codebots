package com.github.alantr7.codebots.listener;

import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.item.BotsItem;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.Permissions;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import com.github.alantr7.codebots.world.structure.StructureFactory;
import com.github.alantr7.codebots.world.structure.StructureInstance;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

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

        StructureInstance structure = StructureFactory.construct(item.id, new BlockLocation(location), direction, stack);
        if (structure != null) {
            StructureInstance.place(structure);
            CodeBotsPlugin.inst().getWorldManager().getWorld(event.getClickedBlock().getWorld()).placeStructure(structure);

            stack.setAmount(stack.getAmount() - 1);
        }
    }

    @EventHandler
    void onBotInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.INTERACTION)
            return;

        var interaction = (Interaction) event.getRightClicked();
        var botId = interaction.getPersistentDataContainer().get(new NamespacedKey(CodeBotsPlugin.inst(), "bot_id"), PersistentDataType.STRING);

        if (botId == null) {
            return;
        }

        if (!(new BlockLocation(interaction.getLocation()).getStructure() instanceof CraftCodeBot bot)) {
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onPlaceBlockInsideOfBot(BlockPlaceEvent event) {
        var registry = CodeBotsPlugin.inst().getSingleton(BotRegistry.class);
        var bot = registry.getBotAt(event.getBlockPlaced().getLocation());
        if (bot != null) {
            event.setCancelled(true);
            return;
        }

        bot = registry.getBotMovingTo(event.getBlockPlaced().getLocation());
        if (bot != null) {
            event.setCancelled(true);
            return;
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
