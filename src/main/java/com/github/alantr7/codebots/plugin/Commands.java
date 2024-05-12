package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bukkitplugin.annotations.generative.Command;
import com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.codeint.modules.BotModule;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
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

                var bot = new CraftCodeBot(UUID.randomUUID(), blockDisplay.getUniqueId());
                botsRegistry.registerBot(bot);
                loader.save(bot);
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command load = CommandBuilder.using("codebots")
            .parameter("load")
            .parameter("{path}", p -> p.defaultValue(ctx -> null))
            .executes(ctx -> {
                var bot = botsRegistry.getBots().entrySet().iterator().next();
                var programFile = new File(bot.getValue().getProgramsDirectory(), (String) ctx.getArgument("path"));

                try {
                    var program = Program.createFromSourceFile(programFile);
                    program.setExtra("bot", bot.getValue());

                    var botModule = new BotModule(program);
                    program.registerNativeModule("bot", botModule);

                    bot.getValue().setProgram(program);
                    program.action(Program.Mode.FULL_EXEC);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ctx.respond("Program loaded.");
                loader.save(bot.getValue());
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command start = CommandBuilder.using("codebots")
            .parameter("start")
            .executes(ctx -> {
                var bot = botsRegistry.getBots().entrySet().iterator().next();
                bot.getValue().setActive(true);
                bot.getValue().getProgram().prepareMainFunction();

                ctx.respond("Bot started!");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command tp = CommandBuilder.using("codebots")
            .parameter("tp")
            .executes(ctx -> {
                var bot = botsRegistry.getBots().entrySet().iterator().next();
                var player = ((Player) ctx.getExecutor());
                var blockDisplay = bot.getValue().getEntity();
                blockDisplay.teleport(player.getLocation().getBlock().getLocation().add(0.2, 0, 0.2));
                blockDisplay.setRotation(0, 0);

                blockDisplay.setTransformation(new Transformation(
                        new Vector3f(0f, 0f, 0f),
                        new AxisAngle4f(0f, 0f, 0f, 0f),
                        new Vector3f(0.6f, 0.6f, 0.6f),
                        new AxisAngle4f(0f, 0f, 0f, 0f)
                ));
                blockDisplay.setInterpolationDuration(20);
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
                var bot = botsRegistry.getBots().entrySet().iterator().next();
                var player = ((Player) ctx.getExecutor());
                player.openInventory(bot.getValue().getInventory());
            });

}
