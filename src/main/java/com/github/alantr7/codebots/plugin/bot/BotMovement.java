package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.plugin.config.Config;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Transformation;

@Getter
public final class BotMovement {

    private final Location origin;

    private final Type type;

    private final Transformation initialTransformation;

    private final Direction direction;

    private final long startTick;

    public enum Type {
        ROTATION, TRANSLATION
    }

    public BotMovement(Location origin, Type type, Direction direction, Transformation initialTransformation) {
        this.origin = origin;
        this.type = type;
        this.direction = direction;
        this.initialTransformation = initialTransformation;
        this.startTick = Bukkit.getCurrentTick();
    }

    public Location getOrigin() {
        return origin.clone();
    }

    public Location getDestination() {
        return type == Type.TRANSLATION ? origin.clone().add(direction.toVector()) : origin.clone();
    }

    public boolean isCompleted() {
        return Bukkit.getCurrentTick() - startTick > getDuration();
    }

    public long getDuration() {
        return (type == Type.TRANSLATION ? Config.BOT_MOVEMENT_DURATION : Config.BOT_ROTATION_DURATION) * 2L;
    }

    public double getProgress() {
        return Math.min(1, (Bukkit.getCurrentTick() - startTick) / (double) getDuration());
    }

}
