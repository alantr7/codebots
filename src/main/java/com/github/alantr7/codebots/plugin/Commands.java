package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bukkitplugin.annotations.generative.Command;
import com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import com.github.alantr7.codebots.item.BotsItem;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class Commands {

    @Inject
    CodeBotsPlugin plugin;

    @Inject
    CodeEditorClient editorClient;

    @Inject
    DataLoader loader;

    @Command(name = "codebots", description = "Create bots.")
    public static final String CREATE_CMD = "";

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command give = CommandBuilder.using("codebots")
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
