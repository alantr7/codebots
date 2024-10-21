package com.github.alantr7.codebots.plugin.utils;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.event.BotLoadEvent;
import org.bukkit.Bukkit;

public class EventDispatcher {

    public static void callBotLoadEvent(CraftCodeBot bot) {
        bot.onChunkLoad();
        var event = new BotLoadEvent(bot, bot.getLocation().getChunk());
        Bukkit.getPluginManager().callEvent(event);
    }

}
