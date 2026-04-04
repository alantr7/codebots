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
import com.github.alantr7.codebots.item.BotsItem;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.editor.EditorSession;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import org.bukkit.Bukkit;
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

    @Command(name = "codebots", description = "Create bots.")
    public static final String CREATE_CMD = "";

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
                editorClient.createSession(bot.getFileSystem().getFiles()).whenComplete((sess, err) -> {
                    editorClient.registerActiveSessionByBot(sess, bot);
                    sess.sendLink(ctx.getExecutor());
                    sess.subscribe(EditorSession.createBotSubscriber(bot));
                });
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command createMonitor = CommandBuilder.using("codebots")
            .permission(Permissions.COMMAND_GIVE)
            .parameter("give")
            .parameter("{player}", p -> p.tabComplete(args -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList()))
            .parameter("{item}", p -> p
                    .tabComplete("bot", "redstone_transmitter", "monitor_2x1", "monitor_3x2", "monitor_4x3")
                    .ifNotProvided(ctx -> ctx.respond("§cItem is not provided.")))
            .executes(ctx -> {
                BotsItem item = BotsItem.getById((String) ctx.getArgument("item"));
                if (item == null) {
                    ctx.respond("§cSpecified item does not exist.");
                    return;
                }

                ((Player) ctx.getExecutor()).getInventory().addItem(item.toItemStack());
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
