package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.Program;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;

import java.util.UUID;

public class CraftCodeBot implements CodeBot {

    @Getter
    private final UUID id;

    @Getter @Setter
    private BlockDisplay entity;

    @Getter @Setter
    private Program program;

    private Direction direction;

    private boolean isActive = false;

    public CraftCodeBot(UUID id) {
        this.id = id;
    }

    @Override
    public Location getLocation() {
        return entity.getLocation();
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}
