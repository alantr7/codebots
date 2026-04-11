package com.github.alantr7.codebots.world.bot;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.utils.FileHelper;
import com.github.alantr7.codebots.plugin.utils.SkullFactory;
import com.github.alantr7.codebots.world.BlockLocation;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class BotFactory {

    private static final ItemStack ROBOT_HEAD = SkullFactory.createSkull("http://textures.minecraft.net/texture/60d4fed78f246fbed9a2d9fe3887b9b3e08e42ffcfb842819cd171b3f5d319");

    public static CodeBot createBot(@NotNull UUID ownerId, @NotNull BlockLocation location) {
        return createBot(UUID.randomUUID(), ownerId, location);
    }

    public static CodeBot createBot(@NotNull UUID botId, @NotNull UUID ownerId, @NotNull BlockLocation location) {
        var bot = new CraftCodeBot(location, Direction.WEST, botId);
        bot.setOwnerId(ownerId);

        bot.onModelSpawn();

        // Create a default program file, and load it into the bot
        BotFile defaultProgramFile = bot.getFileSystem().createFile("program_0.cbs");
        defaultProgramFile.setContent(FileHelper.loadResource("default_program.cbs"));

        try {
            bot.loadProgram(new ProgramSource(
              Directory.LOCAL_PROGRAMS,
              "program_0.cbs",
              defaultProgramFile,
              new String(defaultProgramFile.getContent())
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bot;
    }

    public static ItemDisplay createBotEntity(@NotNull Location location, @NotNull Direction direction) {
        var entity = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        entity.setPersistent(false);

        var skin = ROBOT_HEAD.clone();
        entity.setItemStack(skin);
        entity.setRotation(0, 0);
        var transformation = entity.getTransformation();
        entity.setTransformation(new Transformation(
                new Vector3f(0, 0.75f, 0),
                new AxisAngle4f(Direction.toAngle(direction), 0, 1, 0),
                new Vector3f(1, 1f, 1f),
                new AxisAngle4f(transformation.getRightRotation())
        ));
        entity.setInterpolationDuration(20);

        return entity;
    }

    public static TextDisplay createBotTextEntity(@NotNull Location location) {
        var entity = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        entity.setBillboard(Display.Billboard.VERTICAL);
        entity.setPersistent(false);

        return entity;
    }

}
