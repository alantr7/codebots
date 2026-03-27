package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.world.bot.BotFactory;
import com.github.alantr7.codebots.plugin.data.*;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.utils.EventDispatcher;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import static com.github.alantr7.codebots.plugin.program.ItemFactory.key;

@Singleton
public class Events implements Listener {

    @Inject
    BotRegistry botsRegistry;

    @Inject
    PlayerRegistry players;

    @Inject
    CodeBotsPlugin plugin;

    @EventHandler
    void onChunkLoad(ChunkLoadEvent event) {
        botsRegistry.getBotsInChunk(event.getChunk().getX(), event.getChunk().getZ()).forEach(bot -> {
            bot.fixTransformation();
            botsRegistry.updateBotLocation(bot);

            EventDispatcher.callBotLoadEvent(bot);
        });
    }

    @EventHandler
    void onChunkUnload(ChunkUnloadEvent event) {
        botsRegistry.getBotsInChunk(event.getChunk().getX(), event.getChunk().getZ()).forEach(bot -> {
            bot.setActive(false);
            bot.setProgram(null);
            botsRegistry.getMovingBots().remove(bot.getId());

            plugin.getLogger().info("Bot " + bot.getId() + " has been deactivated due to chunk unload.");

            // Save bot on chunk unload
            if (bot.isDirty()) {
                bot.save();
            }
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
    void onPlayerJoin(PlayerJoinEvent event) {
        players.registerPlayer(new PlayerData(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        players.unregisterPlayer(event.getPlayer().getUniqueId());
    }

}
