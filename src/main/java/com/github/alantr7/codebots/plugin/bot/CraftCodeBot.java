package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.util.UUID;

public class CraftCodeBot implements CodeBot {

    @Getter
    private final UUID id;

    @Getter
    private final UUID entityId;

    private final File directory;

    private Program program;

    private boolean isActive = false;

    @Getter @Setter
    private Location lastSavedLocation;

    @Getter
    private final Inventory inventory;

    public CraftCodeBot(UUID id, UUID entityId) {
        this.id = id;
        this.entityId = entityId;
        this.directory = new File(CodeBotsPlugin.inst().getDataFolder(), "bots/" + id.toString());
        this.inventory = Bukkit.createInventory(null, InventoryType.DROPPER, "Bots Inventory");
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

    @Override
    public void setDirection(Direction direction) {
        setDirection(direction, false);
    }

    @Override
    public void setDirection(Direction direction, boolean interpolate) {
        var translationFloats = getTranslation(direction);
        var angle = switch (direction) {
            case NORTH -> RotateFunction.ANGLE_NORTH;
            case WEST -> RotateFunction.ANGLE_WEST;
            case EAST -> RotateFunction.ANGLE_EAST;
            case SOUTH -> RotateFunction.ANGLE_SOUTH;
            default -> 0;
        };

        var entity = getEntity();

        var initialTranslation = entity.getTransformation().getTranslation();
        var initialTransformation = entity.getTransformation();

        var nextRotation = new AxisAngle4f(angle, 0, 1, 0);
        var nextTranslation = new Vector3f(
                translationFloats[0], initialTranslation.y, translationFloats[1]
        );

        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(20);
        entity.setTransformation(new Transformation(
                nextTranslation,
                nextRotation,
                initialTransformation.getScale(),
                new AxisAngle4f(initialTransformation.getRightRotation())
        ));
    }

    private static float[] getTranslation(Direction direction) {
        return switch (direction) {
            case EAST -> new float[] {0.6f, 0f};
            case SOUTH -> new float[] {0.6f, 0.6f};
            case WEST -> new float[]{0f, 0.6f};
            default -> new float[] {0f, 0f};
        };
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
