package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bukkitplugin.annotations.generative.Command;
import com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.BotBuilder;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
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
            .parameter("{model}", p -> p.defaultValue(c -> null).tabComplete(c -> Collections.singletonList("furnace")))
            .requireMatches(1)
            .executes(ctx -> {
                var player = ((Player) ctx.getExecutor());
                Material model;

                try {
                    model = Material.valueOf(((String) ctx.optArgument("model").orElse("furnace")).toUpperCase());
                } catch (Exception e) {
                    ctx.respond("§cInvalid block-type specified.");
                    return;
                }

                if (!model.isBlock()) {
                    ctx.respond("§cInvalid block-type specified.");
                    return;
                }

                var item = ItemFactory.createBotItem(new BotBuilder()
                        .name("§7Bot")
                        .model(model)
                );

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
            .permission(Permissions.COMMAND_RELOAD)
            .parameter("editor")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                if (!bot.hasProgram() || bot.getProgramSource().getDirectory() != Directory.LOCAL_PROGRAMS) {
                    ctx.respond("§cBot doesn't have a program loaded or it's a shared program.");
                    return;
                }

                try {
                    var futureSession = editorClient.createSession(String.join("\n", Files.readAllLines(bot.getProgramSource().getSource().toPath())).getBytes(StandardCharsets.UTF_8));
                    futureSession.whenComplete((session, e) -> {
                        if (session == null) {
                            ctx.respond("§cError.");
                            return;
                        }

                        ctx.respond("Created a new editor session!");
                        var editorButton = Component.text("here")
                                .decorate(TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.openUrl(
                                        Config.EDITOR_URL + "/edit/" + session.id() + "?token=" + session.accessToken()
                                ));

                        ctx.getExecutor().sendMessage(
                                Component.text("Click ")
                                        .append(editorButton)
                                        .append(Component.text("§r to open the editor."))
                        );
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
