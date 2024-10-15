package com.github.alantr7.codebots.plugin.utils;

import com.github.alantr7.codebots.plugin.bot.BotFactory;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;

public class Compatibility {

    public static void upgradeBotTo0_4_0(CraftCodeBot bot) {
        bot.getEntity().remove(); // Remove the BlockDisplay entity
        var itemDisplay = BotFactory.createBotEntity(MathHelper.toBlockLocation(bot.getLocation())); // Spawn item-display in its place
        bot.setEntityId(itemDisplay.getUniqueId()); // Update bot's entity id
        bot.fixTransformation();
        bot.setDirty(true); // Let the bot save
    }

}
