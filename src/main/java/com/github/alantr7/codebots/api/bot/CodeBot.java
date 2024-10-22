package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface CodeBot {

    UUID getId();

    UUID getEntityId();

    Display getEntity();

    boolean isEntityLoaded();

    boolean isChunkLoaded();

    UUID getInteractionId();

    Interaction getInteraction();

    UUID getOwnerId();

    /**
     * Move bot ownership to a new player
     * @param id new owner's UUID
     */
    void setOwnerId(@NotNull UUID id);

    Location getLocation();

    Location getBlockLocation();

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

    boolean move(Direction direction, boolean interpolate);

    boolean move(Direction direction);

    /**
     * Returns whether the bot is currently moving or rotating
     */
    boolean isMoving();

    /**
     * Sends a message to the chat as sent by bot
     * @param message Message to be sent
     */
    void chat(String message);

    /**
     * Sets a status message (a message above the bot)
     * @param status Status to be set
     */
    void setStatus(String status);

    /**
     * Sets a status message with an expiration timestamp
     * @param status Status to be set
     * @param expiry Expiry date (milliseconds since 01.01.1970.)
     */
    void setStatus(String status, long expiry);

    /**
     * Data stored by programs. This is not bot's working memory.
     * @return bot's memory
     */
    Memory getMemory();

    boolean hasProgram();

    Program getProgram();

    ProgramSource getProgramSource();

    void loadProgram(ProgramSource program) throws ParseException;

    void reloadProgram() throws ParserException, ParseException, IOException;

    @Nullable
    ProgramError getError();

    boolean hasError();

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

    /**
     * Deletes the bot and all of its files and data
     */
    void remove();

}
