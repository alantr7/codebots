package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.plugin.bot.BotFactory;
import com.github.alantr7.codebots.plugin.data.*;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.utils.EventDispatcher;
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

import java.io.File;
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
    void onBotPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        var item = event.getItem();
        if (item == null)
            return;

        if (!item.getType().isBlock())
            return;

        var pdc = item.getItemMeta().getPersistentDataContainer();
        var botId = pdc.get(key("BotId"), PersistentDataType.STRING);

        // If this is null, then the item is not a bot
        if (botId == null)
            return;

        event.setCancelled(true);

        var id = UUID.fromString(botId);
        if (CodeBots.getBot(id) != null) {
            event.getPlayer().sendMessage("§cThis bot is already placed.");
            return;
        }

        var block = event.getClickedBlock();
        var location = block.getType().isSolid() ? block.getRelative(event.getBlockFace()).getLocation() : block.getLocation();

        var bot = BotFactory.createBot(id, event.getPlayer().getUniqueId(), location);
        bot.setDirection(Direction.fromVector(event.getPlayer().getFacing().getOppositeFace().getDirection()), false);

        item.setAmount(item.getAmount() - 1);

        var pdcProgram = pdc.get(key("Program"), PersistentDataType.TAG_CONTAINER);
        if (pdcProgram != null) {
            var path = pdcProgram.get(key("File"), PersistentDataType.STRING);
            var dir = pdcProgram.get(key("Dir"), PersistentDataType.STRING);

            var directory = Directory.valueOf(dir);
            try {
                var program = directory == Directory.SHARED_PROGRAMS ?
                        CodeBotsPlugin.inst().getSingleton(ProgramRegistry.class).getProgram(path) :
                        CodeBots.loadProgram(Directory.LOCAL_PROGRAMS, new File(bot.getProgramsDirectory(), path));

                bot.loadProgram(program);
            } catch (Exception e) {
                CodeBotsPlugin.inst().getLogger().warning("Could not load program for a bot.");
                e.printStackTrace();
            }
        }

        var pdcInventory = pdc.get(key("Inventory"), PersistentDataType.TAG_CONTAINER);
        if (pdcInventory != null) {
            for (int i = 0; i < bot.getInventory().getItems().length; i++) {
                var yaml = new YamlConfiguration();
                try {
                    var stackAsString = pdcInventory.get(key(String.valueOf(i)), PersistentDataType.STRING);
                    if (stackAsString == null)
                        continue;

                    yaml.loadFromString(stackAsString);

                    var stack = ItemStack.deserialize(yaml.getValues(true));
                    bot.getInventory().setItem(i, stack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

        var bot = botsRegistry.getBots().get(UUID.fromString(botId));
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

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void registerEvents(@Inject BukkitPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

}
