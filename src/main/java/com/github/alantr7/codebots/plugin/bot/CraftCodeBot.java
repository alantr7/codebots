package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GuiModule;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.*;
import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.compiler.Compiler;
import com.github.alantr7.codebots.cbslang.high.parser.Parser;
import com.github.alantr7.codebots.cbslang.low.runtime.Program;
import com.github.alantr7.codebots.cbslang.low.tokenizer.Tokenizer;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.utils.StringPool;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.structure.CraftMonitor;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.gui.BotProgramsGUI;
import com.github.alantr7.codebots.plugin.utils.FileHelper;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import com.github.alantr7.codebots.world.structure.StructureInstance;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CraftCodeBot extends StructureInstance implements CodeBot {

    @Getter
    private World world;

    @Getter
    private final UUID id;

    @Getter
    @Setter
    private UUID ownerId;

    private final BotFileSystem fileSystem = new BotFileSystem();

    @Getter
    @Setter
    private Program program;

    @Getter
    private ProgramSource programSource;

    @Getter(onMethod_ = @Nullable)
    private ProgramError error;

    @Getter
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

    private Display entity;

    private TextDisplay textDisplayEntity;

    private Interaction interactionEntity;

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

    @Getter @Setter
    private long lastSaved = 0;

    @Setter
    @Getter
    private CraftMonitor monitor;

    public CraftCodeBot(BlockLocation location, UUID id) {
        super(location, Direction.NORTH);
        this.id = id;
        this.world = location.world.getBukkit();
        this.inventory = new CraftBotInventory(this);
        this.memory = new CraftMemory();
    }

    @Override
    public Display getEntity() {
        return entity;
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

    public TextDisplay getTextDisplay() {
        return textDisplayEntity;
    }

    @Override
    public Interaction getInteraction() {
        return interactionEntity;
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
            return false;
        }

        var initialTransformation = entity.getTransformation();
        var destination = getBlockLocation().add(direction.toVector());

        // Interpolate text display above the bot
        entity.setTeleportDuration(Config.BOT_MOVEMENT_DURATION * 2);
        textDisplay.setTeleportDuration(Config.BOT_MOVEMENT_DURATION * 2);

        entity.teleport(destination.clone().add(0.2, 0, 0.2));
        textDisplay.teleport(destination.clone().add(0.5, Config.BOT_STATUS_ENTITY_OFFSET, 0.5));

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
        setLocation(getEntity().getLocation());
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

    private static float[] getTranslation(Direction direction) {
        return new float[]{0.3f, 0.3f};
    }

    @Override
    public boolean hasProgram() {
        return this.programSource != null;
    }

    // This method handles program loading logic. It is separated from the method below to
    // allow reloading without checking whether the editor is active
    private void _loadProgram(ProgramSource program) throws ParserException {
        try {
            Compiler compiler = new Compiler(Parser.parse(CodeBotsPlugin.inst().getModuleRepository(), program.getCode()));
            compiler.experimentalCompile();

            this.program = new Program(Tokenizer.tokenize(compiler.getOutput()), CodeBotsPlugin.inst().getModuleRepository());
            this.program.setMode(Program.RUN_UNTIL_HALT);
            this.program.setExtra("bot", this);
            this.programSource = program;

            inventory.updateProgramButton();
            CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(this);
        } catch (ParserException e) {
            setError(new ProgramError(ProgramError.ErrorLocation.PARSER, e.getMessage()));
            throw e;
        }
    }

    @Override
    public void loadProgram(ProgramSource program) throws ParserException {
        _loadProgram(program);
    }

    @Override
    public void reloadProgram() throws ParserException, IOException {
        if (this.programSource == null)
            return;

        try {
            var programSource = CodeBots.loadProgram(this.programSource.getDirectory(), this.programSource.getSource());
            _loadProgram(programSource);
        } catch (ParserException e) {
            setError(new ProgramError(ProgramError.ErrorLocation.PARSER, e.getMessage(), new String[] { e.getMessage() }));
            throw e;
        }
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
            if (this.programSource != null) {
                try {
                    loadProgram(this.programSource);
                } catch (Exception e) {
                    e.printStackTrace();
                    isActive = false;
                }
            }
        }

        inventory.updateControlButton();
    }

    @Override
    public BotFileSystem getFileSystem() {
        return fileSystem;
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

        var textDisplay = getTextDisplay();
        if (textDisplay != null) {
            textDisplay.remove();
        }

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

    public void save() {
        CodeBotsPlugin.inst().getSingleton(DataLoader.class).save(this);
    }

    @Override
    public byte[] getOriginalCollisionVectors() {
        return new byte[] { 0, 0, 0 };
    }

    @Override
    public void onModelSpawn() {
        this.entity = BotFactory.createBotEntity(getLocation());
        this.textDisplayEntity = BotFactory.createBotTextEntity(getLocation().clone().add(0.5, Config.BOT_STATUS_ENTITY_OFFSET, 0.5));
        this.interactionEntity = (Interaction) location.world.getBukkit().spawnEntity(location.getBlock().getLocation().add(.5, 0, .5), EntityType.INTERACTION);
        this.interactionEntity.setPersistent(false);
        this.interactionEntity.setInteractionWidth(0.8f);
        this.interactionEntity.getPersistentDataContainer().set(new NamespacedKey(CodeBotsPlugin.inst(), "bot_id"), PersistentDataType.STRING, id.toString());
    }

    @Override
    public void onModelDestroy() {
        this.entity.remove();
        this.textDisplayEntity.remove();
        this.interactionEntity.remove();
    }

    @Override
    public void tick() {

    }

    @Override
    public void save(ByteArrayWriter writer, StringPool constants) {

    }

    @Override
    public ItemStack getItemDrop() {
        return null;
    }

}
