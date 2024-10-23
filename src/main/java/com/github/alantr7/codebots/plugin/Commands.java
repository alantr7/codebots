package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bukkitplugin.annotations.generative.Command;
import com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler;
import com.github.alantr7.bukkitplugin.commands.executor.ExecutorType;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.BotBuilder;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.editor.EditorSession;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

@Singleton
public class Commands {

    @Inject
    CodeBotsPlugin plugin;

    @Inject
    BotRegistry botsRegistry;

    @Inject
    CodeEditorClient editorClient;

    @Inject
    DataLoader loader;

    @Command(name = "codebots", description = "Create bots")
    public static final String CREATE_CMD = "";

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command create = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_CREATE)
            .parameter("create")
            .requireMatches(1)
            .executes(ctx -> {
                var player = ((Player) ctx.getExecutor());
                var item = ItemFactory.createBotItem(new BotBuilder().name("§7Bot"));

                ctx.respond("§eSuccessfully created a new bot.");
                player.getInventory().addItem(item);
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
    public com.github.alantr7.bukkitplugin.commands.registry.Command setSkin = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_SET_SKIN)
            .forExecutors(ExecutorType.PLAYER)
            .parameter("setskin")
            .executes(ctx -> {
                var bot = CodeBots.getSelectedBot((Player) ctx.getExecutor());
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                var hand = ((Player) ctx.getExecutor()).getInventory().getItemInMainHand();
                if (hand.getType() != Material.PLAYER_HEAD) {
                    ctx.respond("§cYou must hold a head.");
                    return;
                }

                var entity = bot.getEntity();
                if (!(entity instanceof ItemDisplay id)) {
                    ctx.respond("§cEntity is not valid. Please report this issue.");
                    return;
                }

                id.setItemStack(hand.clone());
                ctx.respond("§eBot skin updated!");
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
    public com.github.alantr7.bukkitplugin.commands.registry.Command editor = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_EDITOR)
            .parameter("editor")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                var session = editorClient.getActiveSessionByBot(bot);
                if (session != null) {
                    ctx.respond("§cBot already has an active session.");
                    return;
                }

                ctx.getExecutor().sendMessage("§oCreating an editor session. Please wait...");
                editorClient.createSession(bot.getProgramsDirectory().listFiles()).whenComplete((sess, err) -> {
                    editorClient.registerActiveSessionByBot(sess, bot);
                    sess.sendLink(ctx.getExecutor());
                    sess.subscribe(EditorSession.createBotSubscriber(bot));
                });
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command closeEditor = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_EDITOR)
            .parameter("close-editor")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                var session = editorClient.getActiveSessionByBot(bot);
                if (session == null || !bot.hasProgram() || bot.getProgramSource().getDirectory() != Directory.LOCAL_PROGRAMS) {
                    ctx.respond("§cBot doesn't have an active editing session.");
                    return;
                }

                editorClient.deleteSession(session);
                ctx.respond("§eEditor session deleted!");
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

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command reload = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_RELOAD)
            .parameter("reload")
            .executes(ctx -> {
                plugin.getSingleton(DataLoader.class).reload();
                ctx.respond("§ePrograms and bots have been reloaded.");
            });

}
