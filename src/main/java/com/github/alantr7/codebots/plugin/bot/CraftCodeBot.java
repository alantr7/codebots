package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GuiModule;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Memory;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import com.github.alantr7.codebots.plugin.codeint.modules.BotModule;
import com.github.alantr7.codebots.plugin.codeint.modules.MemoryModule;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.gui.BotProgramsGUI;
import com.github.alantr7.codebots.plugin.utils.FileHelper;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CraftCodeBot implements CodeBot {

    @Getter
    private World world;

    @Getter
    private final UUID id;

    @Getter @Setter
    private UUID entityId;

    @Getter
    private final UUID interactionId;

    @Getter @Setter
    private UUID nameEntityId;

    @Getter @Setter
    private UUID textEntityId;

    @Getter
    @Setter
    private UUID ownerId;

    private final File directory;

    @Getter
    @Setter
    private Program program;

    @Getter
    private ProgramSource programSource;

    @Getter(onMethod_ = @Nullable)
    private ProgramError error;

    private boolean isActive = false;

    @Getter
    @Setter
    private Location lastSavedLocation;

    @Getter
    @Setter
    private Location cachedLocation;

    @Getter
    @Setter
    private Direction cachedDirection;

    @Getter
    @Setter
    private BotMovement movement;

    @Getter
    private final CraftBotInventory inventory;

    @Getter
    @Setter
    private Memory memory;

    @Getter
    private int selectedSlot = 0;

    @Getter
    private String lastStatus;

    @Getter
    private long lastStatusExpiry;

    @Getter @Setter
    private boolean isDirty = false;

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
    public Display getEntity() {
        return (Display) Bukkit.getEntity(entityId);
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

    public TextDisplay getNameDisplay() {
        return (TextDisplay) Bukkit.getEntity(nameEntityId);
    }

    public TextDisplay getTextDisplay() {
        return (TextDisplay) Bukkit.getEntity(textEntityId);
    }

    @Override
    public Interaction getInteraction() {
        return (Interaction) Bukkit.getEntity(interactionId);
    }

    @Override
    public Location getLocation() {
        return cachedLocation != null ? cachedLocation.clone() : null;
    }

    @Override
    public Location getBlockLocation() {
        var location = getLocation();
        return location != null ? new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ()) : null;
    }

    @Override
    public void setLocation(@NotNull Location location) {
        var blockLocation = MathHelper.toBlockLocation(location);
        getEntity().teleport(blockLocation.clone().add(.2, 0, .2));
        getInteraction().teleport(blockLocation.clone().add(.5, 0, .5));
        getTextDisplay().teleport(blockLocation.clone().add(.5, Config.BOT_STATUS_ENTITY_OFFSET, .5));

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

    @Override
    public void setDirection(Direction direction) {
        setDirection(direction, false);
    }

    @Override
    public void setDirection(Direction direction, boolean interpolate) {
        Display entity;
        if (isMoving() || !isChunkLoaded() || (entity = getEntity()) == null)
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
        entity.setInterpolationDuration(interpolate ? Config.BOT_ROTATION_DURATION * 2 : 0);
        entity.setTransformation(new Transformation(
                nextTranslation,
                nextRotation,
                initialTransformation.getScale(),
                new AxisAngle4f(initialTransformation.getRightRotation())
        ));

        cachedDirection = direction;
        if (interpolate) {
            this.movement = new BotMovement(getLocation(), BotMovement.Type.ROTATION, direction, initialTransformation);
            CodeBotsPlugin.inst().getSingleton(BotRegistry.class).getMovingBots().put(id, this);
        }
    }

    @Override
    public boolean move(Direction direction) {
        return move(direction, false);
    }

    @Override
    public boolean move(Direction direction, boolean interpolate) {
        if (isMoving())
            return false;

        var entity = getEntity();
        var textDisplay = getTextDisplay();

        if (CodeBots.isBlockOccupied(getBlockLocation().add(direction.toVector()), this)) {
            /*
            var destination = getBlockLocation().add(direction.toVector());
            var registry = CodeBotsPlugin.inst().getSingleton(BotRegistry.class);
            CodeBot occupying;
            if (!destination.getBlock().getType().isAir()) {
                Bukkit.broadcastMessage("Destination obstructed by a block: " + destination);
            }
            else if ((occupying = registry.getBotAt(destination)) != null) {
                Bukkit.broadcastMessage("Destination obstructed by a bot: " + occupying.getBlockLocation());
            }
            else if ((occupying = registry.getBotMovingTo(destination)) != null) {
                Bukkit.broadcastMessage("Destination obstructed by a moving bot: " + occupying.getBlockLocation());
            }
            Bukkit.broadcastMessage("Bot is at " + getBlockLocation());
            */
            return false;
        }

        var initialTransformation = entity.getTransformation();
        var initialTranslation = initialTransformation.getTranslation();

        var nextTranslation = direction.toVector().toVector3f().add(initialTranslation);

        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(Config.BOT_MOVEMENT_DURATION * 2);
        entity.setTransformation(new Transformation(
                nextTranslation,
                initialTransformation.getLeftRotation(),
                initialTransformation.getScale(),
                initialTransformation.getRightRotation()
        ));

        // Interpolate text display above the bot
        /*
        textDisplay.setInterpolationDelay(0);
        textDisplay.setInterpolationDuration(Config.BOT_MOVEMENT_DURATION * 2);
        textDisplay.setTransformation(new Transformation(
                direction.toVector().toVector3f(),
                new AxisAngle4f(0, 0, 0, 0),
                new Vector3f(1, 1, 1),
                new AxisAngle4f(0, 0, 0, 0)
        ));*/

        this.movement = new BotMovement(getBlockLocation(), BotMovement.Type.TRANSLATION, direction, initialTransformation);
        CodeBotsPlugin.inst().getSingleton(BotRegistry.class).updateBotLocation(this);
        CodeBotsPlugin.inst().getSingleton(BotRegistry.class).getMovingBots().put(id, this);

        return true;
    }

    @Override
    public boolean isMoving() {
        return movement != null;
    }

    public void completeTranslation() {
        var entity = getEntity();
        var initialTranslation = movement.getInitialTransformation().getTranslation();
        var direction = movement.getDirection().toVector();
        entity.setInterpolationDuration(0);
        entity.setTransformation(new Transformation(
                initialTranslation,
                entity.getTransformation().getLeftRotation(),
                entity.getTransformation().getScale(),
                entity.getTransformation().getRightRotation()
        ));
        setLocation(entity.getLocation().add(direction));
    }

    @Override
    public void chat(String raw) {
        var message = ChatColor.translateAlternateColorCodes('&', Config.BOT_CHAT_FORMAT
                .replace("{message}", raw));

        var receivers = getLocation().getWorld().getNearbyEntities(getLocation(), 15, 15, 15, e -> e.getType() == EntityType.PLAYER);
        receivers.forEach(e -> e.sendMessage(message));

        if (Config.BOT_SHOW_CHAT_AS_STATUS) {
            setStatus(raw);
        }
    }

    @Override
    public void setStatus(String status) {
        setStatus(status, System.currentTimeMillis() + 5000);
    }

    @Override
    public void setStatus(String status, long expiry) {
        if (status.length() > Config.BOT_MAX_STATUS_LENGTH) {
            status = status.substring(0, Config.BOT_MAX_STATUS_LENGTH - 3) + "...";
        }
        lastStatus = "§7" + status;
        lastStatusExpiry = expiry;
    }

    public void onChunkLoad() {
        // Check if all entities exist
        if (getTextDisplay() == null) {
            this.textEntityId = BotFactory.createBotTextEntity(getLocation().clone().add(0.5, Config.BOT_STATUS_ENTITY_OFFSET, 0.5)).getUniqueId();
            isDirty = true;
        }

        if (isDirty)
            CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(this);
    }

    private static float[] getTranslation(Direction direction) {
        return new float[]{0.3f, 0.3f};
    }

    @Override
    public boolean hasProgram() {
        return this.programSource != null;
    }

    // This method handles program loading logic. It is separated from the method below to
    // allow reloading without checking whether the editor is active
    private void _loadProgram(ProgramSource program) throws ParseException {
        try {
            this.program = Program.createFromCompiledCode(program.getSource().getParentFile(), program.getSource(), program.getCode());
            this.program.setExtra("bot", this);

            this.program.registerNativeModule("bot", new BotModule(this.program));
            this.program.registerNativeModule("memory", new MemoryModule(this.program));
            this.programSource = program;

            this.program.action(Program.Mode.FULL_EXEC);
            inventory.updateProgramButton();

            CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(this);
        } catch (ParseException e) {
            setError(new ProgramError(ProgramError.ErrorLocation.PARSER, e.getMessage()));
            throw e;
        }
    }

    @Override
    public void loadProgram(ProgramSource program) throws ParseException {
        _loadProgram(program);
    }

    @Override
    public void reloadProgram() throws ParserException, ParseException, IOException {
        if (this.programSource == null)
            return;

        try {
            var programSource = CodeBots.loadProgram(this.programSource.getDirectory(), this.programSource.getSource());
            _loadProgram(programSource);
        } catch (ParserException | ParseException e) {
            setError(new ProgramError(ProgramError.ErrorLocation.PARSER, e.getMessage(), new String[] { e.getMessage() }));
            throw e;
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setProgramSource(ProgramSource source) {
        this.programSource = source;
        inventory.updateProgramButton();
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

    public void setError(ProgramError error) {
        this.error = error;
        inventory.updateProgramButton();
        inventory.updateControlButton();
    }

    public void setActive(boolean active) {
        isActive = active;
        if (active && error != null && error.getLocation() == ProgramError.ErrorLocation.EXECUTION) {
            setError(null);
        }

        fixTransformation();

        if (active) {
            if (program == null && this.programSource != null) {
                try {
                    loadProgram(this.programSource);
                    program.prepareMainFunction();
                } catch (Exception e) {
                    e.printStackTrace();
                    isActive = false;
                }
            } else if (this.programSource != null) {
                program.reset();
                program.prepareMainFunction();

                setError(null);
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

    @Override
    public void remove() {
        // Load the chunk so that entities can be removed
        boolean isChunkLoaded;
        if (isChunkLoaded()) {
            isChunkLoaded = true;
        } else {
            cachedLocation.getChunk().load();
            isChunkLoaded = false;
        }

        var blockDisplay = getEntity();
        if (blockDisplay != null) {
            blockDisplay.remove();
        }

        var interaction = getInteraction();
        if (interaction != null) {
            interaction.remove();
        }

        FileHelper.deleteDirectory(getDirectory());

        if (!isChunkLoaded) {
            cachedLocation.getChunk().unload();
        }

        // Remove this bot from the registry
        CodeBotsPlugin.inst().getSingleton(BotRegistry.class).unregisterBot(id);

        // Close all GUIs related to this bot
        inventory.getInternal().clear();
        for (var player : Bukkit.getOnlinePlayers()) {
            var gui = GuiModule.getManager().getOpenInventory(player);
            if (gui == null)
                continue;

            CodeBot bot = gui.is(BotGUI.class) ? ((BotGUI) gui).getBot() :
                    gui.is(BotProgramsGUI.class) ? ((BotProgramsGUI) gui).getBot() : null;

            if (bot == this) {
                gui.close(CloseInitiator.EXTERNAL);
            }
        }
    }

}
