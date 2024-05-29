package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bukkitplugin.annotations.generative.Command;
import com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.codeint.modules.BotModule;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.utils.FileHelper;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.io.File;
import java.util.UUID;

@Singleton
public class Commands {

    @Inject
    BotRegistry botsRegistry;

    @Inject
    CodeBotsPlugin plugin;

    @Inject
    DataLoader loader;

    @Command(name = "codebots", description = "Create bots")
    public static final String CREATE_CMD = "";

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command create = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_CREATE)
            .parameter("create")
            .executes(ctx -> {
                var player = ((Player) ctx.getExecutor());
                var blockDisplay = (BlockDisplay) player.getWorld().spawnEntity(player.getLocation().getBlock().getLocation(), EntityType.BLOCK_DISPLAY);
                blockDisplay.setBlock(Material.FURNACE.createBlockData());
                blockDisplay.setRotation(0, 0);
                var transformation = blockDisplay.getTransformation();
                blockDisplay.setTransformation(new Transformation(
                        transformation.getTranslation().add(0.2f, 0.2f, 0.2f),
                        transformation.getLeftRotation(),
                        new Vector3f(0.6f, 0.6f, 0.6f),
                        transformation.getRightRotation()
                ));
                blockDisplay.setInterpolationDuration(20);

                var interaction = (Interaction) player.getWorld().spawnEntity(player.getLocation().getBlock().getLocation().add(.5, 0, .5), EntityType.INTERACTION);
                interaction.setInteractionWidth(0.8f);

                var bot = new CraftCodeBot(player.getWorld(), UUID.randomUUID(), blockDisplay.getUniqueId(), interaction.getUniqueId());
                bot.setCachedLocation(MathHelper.toBlockLocation(blockDisplay.getLocation()));
                bot.setCachedDirection(Direction.WEST);
                bot.setOwnerId(player.getUniqueId());
                interaction.getPersistentDataContainer().set(new NamespacedKey(plugin, "bot_id"), PersistentDataType.STRING, bot.getId().toString());

                botsRegistry.registerBot(bot);
                loader.save(bot);

                ctx.respond("§eSuccessfully created a new bot.");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command delete = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_DELETE)
            .parameter("delete")
            .executes(ctx -> {
                var bot = CodeBots.getSelectedBot((Player) ctx.getExecutor());
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                bot.remove();
                ctx.respond("§eBot and its files successfully deleted.");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command sel = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_SELECT)
            .parameter("sel")
            .executes(ctx -> {
                var player = ((Player) ctx.getExecutor());
                var interaction = player.getLocation().getWorld().rayTraceEntities(
                        player.getEyeLocation(),
                        player.getLocation().getDirection(),
                        5,
                        e -> e.getType() == EntityType.INTERACTION
                );

                if (interaction == null) {
                    player.sendMessage("§cPlease look at a bot when using this command.");
                    return;
                }

                var botId = interaction.getHitEntity().getPersistentDataContainer().get(new NamespacedKey(plugin, "bot_id"), PersistentDataType.STRING);
                if (botId == null) {
                    player.sendMessage("§cPlease look at a bot when using this command.");
                    return;
                }

                var bot = botsRegistry.getBots().get(UUID.fromString(botId));

                if (bot == null) {
                    player.sendMessage("§cPlease look at a bot when using this command.");
                    return;
                }

                var playerData = PlayerData.get(player);
                playerData.setSelectedBot(bot);

                ctx.respond("§eBot selected.");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command start = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_START)
            .parameter("start")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                bot.setActive(true);
                ctx.respond("§eBot started!");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command stop = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_STOP)
            .parameter("stop")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                bot.setActive(false);
                ctx.respond("§eBot stopped.");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command tp = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_TELEPORT)
            .parameter("tp")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                bot.setLocation(((Player) ctx.getExecutor()).getLocation());
                ctx.respond("§eBot has been teleported to your location!");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command rotate = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_TELEPORT)
            .parameter("rotate")
            .parameter("{direction}", p -> p.ifNotProvided(ctx -> ctx.respond("§cPlease provide a direction.")))
            .requireMatches(1)
            .executes(ctx -> {
                var directionRaw = (String) ctx.getArgument("direction");
                var direction = Direction.toDirection(directionRaw.toUpperCase());

                if (direction == null) {
                    ctx.respond("§cInvalid direction specified. Valid directions are:");
                    ctx.respond("§6 - north, east, south, west");
                    return;
                }

                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                bot.setDirection(direction, true);
                ctx.respond("§eBot has been rotated.");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command inventory = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_INVENTORY)
            .parameter("inv")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                new BotGUI((Player) ctx.getExecutor(), bot).open();
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command save = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_SELECT)
            .parameter("save")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(bot);
                ctx.respond("§eBot has been saved.");
            });

}
