package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.language.runtime.Program;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

public interface CodeBot {

    UUID getId();

    UUID getEntityId();

    BlockDisplay getEntity();

    Interaction getInteraction();

    Location getLocation();

    /**
     * Teleports the bot to the specified location
     * @param location new location
     */
    void setLocation(@NotNull Location location);

    Direction getDirection();

    /**
     * Set the direction of the bot
     * @param direction new direction
     * @param interpolate if rotation should be animated
     */
    void setDirection(Direction direction, boolean interpolate);

    /**
     * Set the direction of the bot without animating the rotation
     * @param direction new direction
     */
    void setDirection(Direction direction);

    Program getProgram();

    void setProgram(Program program);

    boolean isActive();

    void setActive(boolean flag);

    File getDirectory();

    File getProgramsDirectory();

    Inventory getInventory();

}
