package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.joml.AxisAngle4f;

import java.io.File;
import java.util.UUID;

public class CraftCodeBot implements CodeBot {

    @Getter
    private final UUID id;

    @Getter
    private final UUID entityId;

    private final File directory;

    private Program program;

    private Direction direction;

    private boolean isActive = false;

    public CraftCodeBot(UUID id, UUID entityId) {
        this.id = id;
        this.entityId = entityId;
        this.direction = getEntity() != null
                ? Direction.fromVector(getEntity().getLocation().getDirection())
                : Direction.NORTH;
        this.directory = new File(CodeBotsPlugin.inst().getDataFolder(), "bots/" + id.toString());
    }

    @Override
    public BlockDisplay getEntity() {
        return (BlockDisplay) Bukkit.getEntity(entityId);
    }

    @Override
    public Location getLocation() {
        return getEntity().getLocation();
    }

    @Override
    public Direction getDirection() {
        var entity = getEntity();
        if (entity == null)
            return null;

        var rotation = new AxisAngle4f(entity.getTransformation().getLeftRotation());
        if (rotation.angle == 0)
            return Direction.NORTH;

        if (MathHelper.floatsEqual(rotation.angle, RotateFunction.ANGLE_EAST))
            return Direction.EAST;

        if (MathHelper.floatsEqual(rotation.angle, RotateFunction.ANGLE_SOUTH))
            return Direction.SOUTH;

        if (MathHelper.floatsEqual(rotation.angle, RotateFunction.ANGLE_WEST))
            return Direction.WEST;

        return Direction.NORTH;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Program getProgram() {
        return program;
    }

    @Override
    public void setProgram(Program program) {
        this.program = program;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (active) {
            Bukkit.broadcastMessage("§eStarted program execution.");
        } else {
            Bukkit.broadcastMessage("§eProgram has completed.");
        }
        isActive = active;
    }

    @Override
    public File getDirectory() {
        return directory;
    }

    @Override
    public File getProgramsDirectory() {
        return new File(directory, "programs");
    }

}
