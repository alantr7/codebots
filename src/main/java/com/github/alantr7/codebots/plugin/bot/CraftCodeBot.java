package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.bot.Memory;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import com.github.alantr7.codebots.plugin.codeint.modules.BotModule;
import com.github.alantr7.codebots.plugin.codeint.modules.MemoryModule;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.util.UUID;

public class CraftCodeBot implements CodeBot {

    @Getter
    private World world;

    @Getter
    private final UUID id;

    @Getter
    private final UUID entityId;

    @Getter
    private final UUID interactionId;

    @Getter @Setter
    private UUID ownerId;

    private final File directory;

    @Getter @Setter
    private Program program;

    @Getter
    private ProgramSource programSource;

    private boolean isActive = false;

    @Getter @Setter
    private Location lastSavedLocation;

    @Getter @Setter
    private Location cachedLocation;

    @Getter @Setter
    private Direction cachedDirection;

    @Getter
    private final CraftBotInventory inventory;

    @Getter @Setter
    private Memory memory;

    @Getter
    private int selectedSlot = 0;

    public CraftCodeBot(World world, UUID id, UUID entityId, UUID interactionId) {
        this.world = world;
        this.id = id;
        this.entityId = entityId;
        this.interactionId = interactionId;
        this.directory = new File(CodeBotsPlugin.inst().getDataFolder(), "bots/" + id.toString());
        this.inventory = new CraftBotInventory(this);
        this.memory = new CraftMemory();
    }

    @Override
    public BlockDisplay getEntity() {
        return (BlockDisplay) Bukkit.getEntity(entityId);
    }

    @Override
    public boolean isEntityLoaded() {
        return getEntity() != null;
    }

    @Override
    public boolean isChunkLoaded() {
        int chunkX = cachedLocation.getBlockX() >> 4;
        int chunkZ = cachedLocation.getBlockZ() >> 4;

        return world.isChunkLoaded(chunkX, chunkZ);
    }

    @Override
    public Interaction getInteraction() {
        return (Interaction) Bukkit.getEntity(interactionId);
    }

    @Override
    public Location getLocation() {
        return cachedLocation;
    }

    @Override
    public void setLocation(@NotNull Location location) {
        var blockLocation = MathHelper.toBlockLocation(location);
        getEntity().teleport(blockLocation.clone().add(.2, 0, .2));
        getInteraction().teleport(blockLocation.add(.5, 0, .5));

        this.cachedLocation = location;
        CodeBotsPlugin.inst().getSingleton(BotRegistry.class).updateBotLocation(this);
    }

    public void fixTransformation() {
        setDirection(getDirection(), false);
    }

    @Override
    public Direction getDirection() {
        return cachedDirection;
    }

    /*
    public Direction getDirectionFromEntity() {
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
    }*/

    @Override
    public void setDirection(Direction direction) {
        setDirection(direction, false);
    }

    @Override
    public void setDirection(Direction direction, boolean interpolate) {
        BlockDisplay entity;
        if (!isChunkLoaded() || (entity = getEntity()) == null)
            return;

        var translationFloats = getTranslation(direction);
        var angle = switch (direction) {
            case NORTH -> RotateFunction.ANGLE_NORTH;
            case WEST -> RotateFunction.ANGLE_WEST;
            case EAST -> RotateFunction.ANGLE_EAST;
            case SOUTH -> RotateFunction.ANGLE_SOUTH;
            default -> 0;
        };


        var initialTranslation = entity.getTransformation().getTranslation();
        var initialTransformation = entity.getTransformation();

        var nextRotation = new AxisAngle4f(angle, 0, 1, 0);
        var nextTranslation = new Vector3f(
                translationFloats[0], initialTranslation.y, translationFloats[1]
        );

        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(interpolate ? 20 : 0);
        entity.setTransformation(new Transformation(
                nextTranslation,
                nextRotation,
                initialTransformation.getScale(),
                new AxisAngle4f(initialTransformation.getRightRotation())
        ));

        cachedDirection = direction;
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
    public void loadProgram(ProgramSource program) throws ParseException {
        this.program = Program.createFromCompiledCode(program.getSource().getParentFile(), program.getSource(), program.getCode());
        this.program.setExtra("bot", this);

        this.program.registerNativeModule("bot", new BotModule(this.program));
        this.program.registerNativeModule("memory", new MemoryModule(this.program));
        this.programSource = program;

        this.program.action(Program.Mode.FULL_EXEC);
        inventory.updateProgramButton();

        CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(this);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setProgramSource(ProgramSource source) {
        this.programSource = source;
        inventory.updateProgramButton();
    }

    public void setActive(boolean active) {
        isActive = active;
        fixTransformation();

        if (program == null && this.programSource != null) {
            try {
                loadProgram(this.programSource);
                program.prepareMainFunction();
            } catch (Exception e){
                e.printStackTrace();
                isActive = false;
            }
        }

        inventory.updateControlButton();
    }

    @Override
    public File getDirectory() {
        return directory;
    }

    @Override
    public File getProgramsDirectory() {
        return new File(directory, "programs");
    }

    @Override
    public void setSelectedSlot(int slot) {
        if (slot < 0 || slot > 6)
            throw new IllegalArgumentException("Slot must be between 0 and 6 (inclusive)");

        selectedSlot = slot;
        inventory.updateSelectedSlotHighlights();
    }

}
