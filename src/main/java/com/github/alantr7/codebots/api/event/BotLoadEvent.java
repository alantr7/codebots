package com.github.alantr7.codebots.api.event;

import com.github.alantr7.codebots.api.bot.CodeBot;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BotLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final CodeBot bot;

    @Getter
    private final Chunk chunk;

    public BotLoadEvent(CodeBot bot, Chunk chunk) {
        this.bot = bot;
        this.chunk = chunk;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
