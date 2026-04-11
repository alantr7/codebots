package com.github.alantr7.codebots.world.bot;

import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GuiModule;
import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.*;
import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.compiler.Compiler;
import com.github.alantr7.codebots.cbslang.high.parser.Parser;
import com.github.alantr7.codebots.cbslang.low.runtime.Program;
import com.github.alantr7.codebots.cbslang.low.runtime.ProgramState;
import com.github.alantr7.codebots.cbslang.low.tokenizer.Tokenizer;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.fs.BotFileSystem;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.utils.StringPool;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.BotsChunk;
import com.github.alantr7.codebots.world.BotsRegion;
import com.github.alantr7.codebots.world.structure.CraftMonitor;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import com.github.alantr7.codebots.plugin.gui.BotGUI;
import com.github.alantr7.codebots.plugin.gui.BotProgramsGUI;
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

    private final BotFileSystem fileSystem = new BotFileSystem(this);

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

    public CraftCodeBot(BlockLocation location, Direction direction, UUID id) {
        super(location, direction);
        this.id = id;
        this.world = location.world.getBukkit();
        this.inventory = new CraftBotInventory(this);
        this.memory = new CraftMemory();
        this.collisionBarriers = false;
        setOccupiedChunks();
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
        return location.isLoaded();
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
        return location.toBukkitCentered();
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

        BlockLocation previousLocation = this.location;
        this.location = new BlockLocation(location);
        this.location.getChunk().isUnsaved = true;
        previousLocation.getChunk().isUnsaved = true;
    }

    public void fixTransformation() {
        rotate(getDirection(), false);
    }

    @Override
    public Direction getDirection() {
        return super.direction;
    }

    @Override
    public void setDirection(Direction direction) {
        rotate(direction, false);
    }

    @Override
    public void rotate(Direction direction, boolean interpolate) {
        Display entity;
        if (isMoving() || !isChunkLoaded() || (entity = getEntity()) == null)
            return;

        var angle = Direction.toAngle(direction);
        var initialTransformation = entity.getTransformation();
        var nextRotation = new AxisAngle4f(angle, 0, 1, 0);
        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(interpolate ? Config.BOT_ROTATION_DURATION * 2 : 0);
        entity.setTransformation(new Transformation(
                initialTransformation.getTranslation(),
                nextRotation,
                initialTransformation.getScale(),
                new AxisAngle4f(initialTransformation.getRightRotation())
        ));

        this.direction = direction;
        if (interpolate) {
            this.movement = new BotMovement(getLocation(), BotMovement.Type.ROTATION, direction, initialTransformation);
            location.world.getMovingBots().add(this);
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

        location.world.unregisterStructure(this);

        this.location = this.location.getRelative(direction);
        location.world.placeStructure(this);
        setOccupiedChunks();

        entity.teleport(this.location.toBukkitCentered());
        interactionEntity.teleport(this.location.toBukkitCentered());
        textDisplay.teleport(this.location.toBukkitCentered().add(0, Config.BOT_STATUS_ENTITY_OFFSET, 0));

        this.movement = new BotMovement(getBlockLocation(), BotMovement.Type.TRANSLATION, direction, entity.getTransformation());
        location.world.getMovingBots().add(this);

        return true;
    }

    @Override
    public boolean isMoving() {
        return movement != null;
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
    public void loadProgram(@NotNull Directory directory, @NotNull String fileName) throws Exception {
        if (directory == Directory.SHARED_PROGRAMS) {
            loadProgram(CodeBotsPlugin.inst().getProgramRegistry().getProgram(fileName));
        } else {
            BotFile file = fileSystem.getFile(fileName);
            if (file != null)
                loadProgram(new ProgramSource(directory, file.getName(), file, new String(file.getContent()).trim()));
        }
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
        location.world.removeStructure(this);
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
        location.world.registerBot(this);
        this.entity = BotFactory.createBotEntity(location.toBukkitCentered(), direction);
        entity.setTeleportDuration(Config.BOT_MOVEMENT_DURATION * 2);
        this.textDisplayEntity = BotFactory.createBotTextEntity(location.toBukkitCentered().add(0, Config.BOT_STATUS_ENTITY_OFFSET, 0));
        this.textDisplayEntity.setTeleportDuration(Config.BOT_MOVEMENT_DURATION * 2);
        this.interactionEntity = (Interaction) location.world.getBukkit().spawnEntity(location.toBukkitCentered(), EntityType.INTERACTION);
        this.interactionEntity.setPersistent(false);
        this.interactionEntity.setInteractionWidth(0.8f);
        this.interactionEntity.getPersistentDataContainer().set(new NamespacedKey(CodeBotsPlugin.inst(), "bot_id"), PersistentDataType.STRING, id.toString());
    }

    @Override
    public void onModelDestroy() {
        location.world.unregisterBot(this);
        this.entity.remove();
        this.textDisplayEntity.remove();
        this.interactionEntity.remove();
    }

    @Override
    public void tick() {
        long time = System.currentTimeMillis();

        if (movement != null) {
            if (movement.isCompleted()) {
                setMovement(null);
                setDirty(true);
                location.world.getMovingBots().remove(this);
            }
        }

        if (program != null && isActive()) {
            if (program.hasNext()) {
                program.run();
            } else {
                setActive(false);

                // If there was an exception, save it to the bot instance
                if (program.isInterrupted()) {
                    setError(new ProgramError(
                      ProgramError.ErrorLocation.EXECUTION,
                      program.getError().getMessage(),
                      new String[0] // todo: stack traces
                    ));
                }
            }

            isDirty = true;
            location.getChunk().isUnsaved = true;
        }

        if (isChunkLoaded() && getTextDisplay() != null) {
            String text = (isActive() ? "" : "§7") + "ʙᴏᴛ ";
            text += isActive() ? "§6ᴀᴄᴛɪᴠᴇ" : hasError() ? "§cᴄʀᴀꜱʜᴇᴅ" : "ᴏꜰꜰʟɪɴᴇ";

            if (time <= getLastStatusExpiry()) {
                text += "\n" + getLastStatus();
            }

            getTextDisplay().setText(text);
        }

        // Periodically save the bot
        if (isDirty() && System.currentTimeMillis() - getLastSaved() > Config.BOT_AUTO_SAVE_COOLDOWN && isChunkLoaded())
            save();
    }

    @Override
    public ItemStack getItemDrop() {
        return null;
    }

    public static CraftCodeBot fromBytes(BotsRegion region, BotsChunk chunk, ByteArrayReader reader) {
        int x = ByteArrayReader.toInt(reader.readBytes(1));
        int y = ByteArrayReader.toInt(reader.readBytes(2));
        int z = ByteArrayReader.toInt(reader.readBytes(1));
        BlockLocation location = new BlockLocation(chunk.world, (chunk.position.x << 4) | x, y, (chunk.position.y << 4) | z);

        // Direction
        Direction direction = Direction.toDirection((char) reader.readU1());

        // Bot ID
        UUID id = UUID.fromString(reader.readString());

        // File system
        long[] filePointers = new long[reader.readU1()];
        for (int i = 0; i < filePointers.length; i++) {
            filePointers[i] = reader.readLong();
        }

        // Loaded program file
        int directoryPointer = reader.readU1();

        CraftCodeBot bot = new CraftCodeBot(location, direction, id);
        location.world.fsManager.load(bot.fileSystem, filePointers);

        if (directoryPointer != 0) {
            Directory sourceDirectory = Directory.values()[directoryPointer - 1];
            String fileName = reader.readString();
            ProgramSource source = null;

            // Find the file
            if (sourceDirectory == Directory.SHARED_PROGRAMS) {
                source = CodeBotsPlugin.inst().getProgramRegistry().getProgram(fileName);
            } else {
                BotFile sourceFile = bot.fileSystem.getFile(fileName);
                if (sourceFile != null) {
                    source = CodeBots.loadProgram(sourceDirectory, sourceFile);
                }
            }

            if (source != null) {
                try {
                    bot.loadProgram(source);
                } catch (Exception e) {
                    System.out.println("Could not load program for bot " + id);
                    e.printStackTrace();
                }
            }
        }

        // Load program state
        int isActive = reader.readU1();
        if (isActive == 1) {
            bot.setActive(true);
            ProgramState state = ProgramState.deserialize(bot.program, reader);
            bot.program.setState(state);
        }

        return bot;
    }

    @Override
    public void save(ByteArrayWriter buffer, StringPool constants) {
        // Structure type identifier
        buffer.writeU2(2); // structure id (2 for redstone transmitter)

        int basePointer = buffer.getPointer();

        // Structure size on disk
        buffer.writeU2(0);

        // Location
        buffer.writeBytes(ByteArrayWriter.toBytes(location.x & 0xf, 1));;
        buffer.writeBytes(ByteArrayWriter.toBytes(location.y, 2));
        buffer.writeBytes(ByteArrayWriter.toBytes(location.z, 1));

        // Direction
        buffer.writeU1(direction.name().charAt(0));

        // Bot ID
        buffer.writeString(id.toString());

        // File system
        location.world.fsManager.save(fileSystem);
        buffer.writeU1(fileSystem.getFiles().size());
        for (BotFile file : fileSystem.getFiles()) {
            if (file.getPosition() != -1) {
                buffer.writeLong(file.getPosition());
            } else {
                System.err.println("Could not save file " + file.getName() + " as it is not written to the file system.");
            }
        }

        // Loaded program file
        buffer.writeU1(programSource != null ? (programSource.getDirectory().ordinal() + 1) : 0);
        if (programSource != null) {
            buffer.writeString(programSource.getName());
        }

        // State
        if (isActive) {
            byte[] state = program.getState().serialize();
            if (state.length < 1024) {
                buffer.writeU1(1);
                buffer.writeBytes(state);
            } else {
                buffer.writeU1(0);
            }
        } else {
            buffer.writeU1(0);
        }

        // Size on disk
        int returnPointer = buffer.getPointer();
        buffer.setPointer(basePointer);
        buffer.writeU2(returnPointer - basePointer - 2);
        buffer.setPointer(returnPointer);
    }

}
