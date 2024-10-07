package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.utils.FileHelper;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.io.File;
import java.util.UUID;

public class BotFactory {

    public static CodeBot createBot(@NotNull UUID ownerId, @NotNull Location location) {
        return createBot(UUID.randomUUID(), ownerId, location, Material.FURNACE);
    }

    public static CodeBot createBot(@NotNull UUID botId, @NotNull UUID ownerId, @NotNull Location location, @NotNull Material type) {
        var blockDisplay = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        blockDisplay.setBlock(type.createBlockData());
        blockDisplay.setRotation(0, 0);
        var transformation = blockDisplay.getTransformation();
        blockDisplay.setTransformation(new Transformation(
                transformation.getTranslation().add(0.2f, 0.2f, 0.2f),
                transformation.getLeftRotation(),
                new Vector3f(0.6f, 0.6f, 0.6f),
                transformation.getRightRotation()
        ));
        blockDisplay.setInterpolationDuration(20);

        var interaction = (Interaction) location.getWorld().spawnEntity(location.getBlock().getLocation().add(.5, 0, .5), EntityType.INTERACTION);
        interaction.setInteractionWidth(0.8f);

        var bot = new CraftCodeBot(location.getWorld(), botId, blockDisplay.getUniqueId(), interaction.getUniqueId());
        bot.setCachedLocation(MathHelper.toBlockLocation(blockDisplay.getLocation()));
        bot.setCachedDirection(Direction.WEST);
        bot.setOwnerId(ownerId);
        interaction.getPersistentDataContainer().set(new NamespacedKey(CodeBotsPlugin.inst(), "bot_id"), PersistentDataType.STRING, bot.getId().toString());

        bot.setLocation(location);
        bot.fixTransformation();
        CodeBotsPlugin.inst().getSingleton(BotRegistry.class).registerBot(bot);
        CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(bot);

        // Create a default program file, and load it into the bot
        var defaultProgramFile = new File(bot.getProgramsDirectory(), "default.js");
        FileHelper.saveResource("default_program.js", defaultProgramFile);

        try {
            bot.loadProgram(CodeBots.loadProgram(Directory.LOCAL_PROGRAMS, defaultProgramFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bot;
    }

}
