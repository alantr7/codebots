package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bukkitplugin.annotations.generative.Command;
import com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.codeint.modules.BotModule;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
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

                var interaction = (Interaction) player.getWorld().spawnEntity(player.getLocation().getBlock().getLocation(), EntityType.INTERACTION);
                interaction.setInteractionWidth(0.8f);

                var bot = new CraftCodeBot(UUID.randomUUID(), blockDisplay.getUniqueId(), interaction.getUniqueId());
                interaction.getPersistentDataContainer().set(new NamespacedKey(plugin, "bot_id"), PersistentDataType.STRING, bot.getId().toString());

                botsRegistry.registerBot(bot);
                loader.save(bot);
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command sel = CommandBuilder.using("codebots")
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
                    player.sendMessage("Please look at a bot when using this command.");
                    return;
                }

                var botId = interaction.getHitEntity().getPersistentDataContainer().get(new NamespacedKey(plugin, "bot_id"), PersistentDataType.STRING);
                if (botId == null) {
                    player.sendMessage("Please look at a bot when using this command.");
                    return;
                }

                var bot = botsRegistry.getBots().get(UUID.fromString(botId));

                if (bot == null) {
                    player.sendMessage("Please look at a bot when using this command.");
                    return;
                }

                var playerData = PlayerData.get(player);
                playerData.setSelectedBot(bot);

                ctx.respond("Bot selected!");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command load = CommandBuilder.using("codebots")
            .parameter("load")
            .parameter("{path}", p -> p.defaultValue(ctx -> null))
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                var programFile = new File(bot.getProgramsDirectory(), (String) ctx.getArgument("path"));

                try {
                    var program = Program.createFromSourceFile(programFile);
                    program.setExtra("bot", bot);

                    var botModule = new BotModule(program);
                    program.registerNativeModule("bot", botModule);

                    bot.setProgram(program);
                    program.action(Program.Mode.FULL_EXEC);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ctx.respond("Program loaded.");
                loader.save(bot);
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command start = CommandBuilder.using("codebots")
            .parameter("start")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                ((CraftCodeBot) bot).fixTransformation();
                bot.setActive(true);
                bot.getProgram().prepareMainFunction();

                ctx.respond("Bot started!");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command pause = CommandBuilder.using("codebots")
            .parameter("pause")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                bot.setActive(false);
                ((CraftCodeBot) bot).fixTransformation();
                ctx.respond("Bot paused!");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command tp = CommandBuilder.using("codebots")
            .parameter("tp")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                bot.setLocation(((Player) ctx.getExecutor()).getLocation());
                ctx.respond("Bot teleported!");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command chunk = CommandBuilder.using("codebots")
            .parameter("chunk")
            .executes(ctx -> {
                var player = ((Player) ctx.getExecutor());
                var bots = botsRegistry.getBotsInChunk(player.getLocation());
                ctx.respond("Bots in chunk: " + bots.size());
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command inventory = CommandBuilder.using("codebots")
            .parameter("inv")
            .executes(ctx -> {
                var bot = PlayerData.get((Player) ctx.getExecutor()).getSelectedBot();
                if (bot == null) {
                    ctx.respond("§cPlease select a bot first.");
                    return;
                }

                var player = ((Player) ctx.getExecutor());
                player.openInventory(bot.getInventory().getInternal());
            });

}
