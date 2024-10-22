package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.utils.FileHelper;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import com.github.alantr7.codebots.plugin.utils.SkullFactory;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.io.File;
import java.util.UUID;

public class BotFactory {

    private static final ItemStack ROBOT_HEAD = SkullFactory.createSkull("http://textures.minecraft.net/texture/60d4fed78f246fbed9a2d9fe3887b9b3e08e42ffcfb842819cd171b3f5d319");

    public static CodeBot createBot(@NotNull UUID ownerId, @NotNull Location location) {
        return createBot(UUID.randomUUID(), ownerId, location);
    }

    public static CodeBot createBot(@NotNull UUID botId, @NotNull UUID ownerId, @NotNull Location location) {
        var blockDisplay = createBotEntity(location);
        var interaction = (Interaction) location.getWorld().spawnEntity(location.getBlock().getLocation().add(.5, 0, .5), EntityType.INTERACTION);
        interaction.setInteractionWidth(0.8f);
        var textDisplay = createBotTextEntity(location.getBlock().getLocation().add(.5, Config.BOT_STATUS_ENTITY_OFFSET, .5));

        var bot = new CraftCodeBot(location.getWorld(), botId, blockDisplay.getUniqueId(), interaction.getUniqueId());
        bot.setCachedLocation(MathHelper.toBlockLocation(blockDisplay.getLocation()));
        bot.setCachedDirection(Direction.WEST);
        bot.setOwnerId(ownerId);
        bot.setTextEntityId(textDisplay.getUniqueId());
        interaction.getPersistentDataContainer().set(new NamespacedKey(CodeBotsPlugin.inst(), "bot_id"), PersistentDataType.STRING, bot.getId().toString());

        bot.setLocation(location);
        bot.fixTransformation();
        CodeBotsPlugin.inst().getSingleton(BotRegistry.class).registerBot(bot);
        CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(bot);

        // Create a default program file, and load it into the bot
        var defaultProgramFile = new File(bot.getProgramsDirectory(), "program_0.js");
        FileHelper.saveResource("default_program.js", defaultProgramFile);

        try {
            bot.loadProgram(CodeBots.loadProgram(Directory.LOCAL_PROGRAMS, defaultProgramFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bot;
    }

    public static ItemDisplay createBotEntity(@NotNull Location location) {
        var entity = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        var skin = ROBOT_HEAD.clone();

        entity.setItemStack(skin);
        entity.setRotation(0, 0);
        var transformation = entity.getTransformation();
        entity.setTransformation(new Transformation(
                transformation.getTranslation().add(0.3f, 0.75f, 0.3f),
                transformation.getLeftRotation(),
                new Vector3f(1, 1f, 1f),
                transformation.getRightRotation()
        ));
        entity.setInterpolationDuration(20);

        return entity;
    }

    public static TextDisplay createBotTextEntity(@NotNull Location location) {
        var entity = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        entity.setBillboard(Display.Billboard.VERTICAL);

        return entity;
    }

}
