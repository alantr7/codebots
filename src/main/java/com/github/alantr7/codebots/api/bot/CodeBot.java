package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

public interface CodeBot {

    UUID getId();

    UUID getEntityId();

    BlockDisplay getEntity();

    UUID getInteractionId();

    Interaction getInteraction();

    UUID getOwnerId();

    /**
     * Move bot ownership to a new player
     * @param id new owner's UUID
     */
    void setOwnerId(@NotNull UUID id);

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

    /**
     * Data stored by programs. This is not bot's working memory.
     * @return bot's memory
     */
    Memory getMemory();

    Program getProgram();

    ProgramSource getProgramSource();

    void loadProgram(ProgramSource program) throws ParseException;

    boolean isActive();

    void setActive(boolean flag);

    File getDirectory();

    File getProgramsDirectory();

    BotInventory getInventory();

    /**
     * Get the selected slot in the bot's inventory
     * @return selected slot
     */
    int getSelectedSlot();

    /**
     * Set the selected slot in the bot's inventory
     * @param slot new selected slot. Must be between 0 and 6 (inclusive)
     */
    void setSelectedSlot(int slot);

}
