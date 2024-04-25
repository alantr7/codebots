package com.github.alantr7.codebots.plugin;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.bukkitplugin.annotations.generative.Command;
import com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler;
import com.github.alantr7.bukkitplugin.commands.factory.CommandBuilder;
import com.github.alantr7.codebots.language.parser.AssemblyParser;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.BlockScope;
import com.github.alantr7.codebots.language.runtime.BlockStackEntry;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.codeint.modules.BotModule;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

@Singleton
public class Commands {

    @Inject
    BotRegistry botsRegistry;

    @Inject
    CodeBotsPlugin plugin;

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

                var bot = new CraftCodeBot(UUID.randomUUID());
                bot.setEntity(blockDisplay);

                botsRegistry.registerBot(bot);
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command load = CommandBuilder.using("codebots")
            .parameter("load")
            .parameter("{path}", p -> p.defaultValue(ctx -> null))
            .executes(ctx -> {
                var bot = botsRegistry.getBots().entrySet().iterator().next();
                var dir = plugin.getDataFolder();

                dir.mkdir();
                var programFile = new File(dir, (String) ctx.getArgument("path"));

                try {
                    var program = new Program(dir);
                    program.setExtra("bot", bot.getValue());
                    bot.getValue().setProgram(program);

                    var lines = Files.readAllLines(programFile.toPath()).toArray(String[]::new);

                    var mainBlock = AssemblyParser.parseCodeBlock(program, lines);
                    var module = new FileModule(program, programFile, mainBlock);
                    var botModule = new BotModule(program);

                    program.registerNativeModule("bot", botModule);

                    program.setMainModule(module);

                    program.getEnvironment().getBlockStack().add(new BlockStackEntry(mainBlock, new BlockContext()));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                ctx.respond("Program loaded.");
            });

    @CommandHandler
    public com.github.alantr7.bukkitplugin.commands.registry.Command start = CommandBuilder.using("codebots")
            .parameter("start")
            .executes(ctx -> {
                var bot = botsRegistry.getBots().entrySet().iterator().next();
                bot.getValue().setActive(true);

                ctx.respond("Bot started!");
            });

}
