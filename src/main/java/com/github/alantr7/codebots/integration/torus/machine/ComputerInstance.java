package com.github.alantr7.codebots.integration.torus.machine;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.api.event.ProgramEndEvent;
import com.github.alantr7.codebots.api.event.ProgramStartEvent;
import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.compiler.Compiler;
import com.github.alantr7.codebots.cbslang.low.runtime.Program;
import com.github.alantr7.codebots.cbslang.low.tokenizer.Tokenizer;
import com.github.alantr7.codebots.config.Config;
import com.github.alantr7.codebots.editor.EditorSession;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.integration.torus.CodeBotsTorusIntEntry;
import com.github.alantr7.codebots.integration.torus.fs.ComputerFileSystem;
import com.github.alantr7.codebots.integration.torus.gui.ComputerProgramsGUI;
import com.github.alantr7.codebots.world.structure.Tickable;
import com.github.alantr7.torus.exception.SetupException;
import com.github.alantr7.torus.structure.DataTransmitter;
import com.github.alantr7.torus.structure.LoadContext;
import com.github.alantr7.torus.structure.Structure;
import com.github.alantr7.torus.structure.StructureInstance;
import com.github.alantr7.torus.structure.builder.StructureBodyDef;
import com.github.alantr7.torus.structure.data.Data;
import com.github.alantr7.torus.structure.socket.DataSocket;
import com.github.alantr7.torus.world.BlockLocation;
import com.github.alantr7.torus.world.Direction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class ComputerInstance extends StructureInstance implements Tickable, DataTransmitter {

    @Getter
    protected ComputerFileSystem fileSystem;

    @Getter
    protected ProgramSource programSource;

    @Getter
    protected Program program;

    @Getter
    protected boolean isActive;

    @Getter @Setter
    protected ProgramError error;

    protected Data<UUID> editorIdentifier = dataContainer.persist("editor_id", Data.Type.UUID, UUID.randomUUID());;

    protected Data<byte[]> fsBinary = dataContainer.persist("fs", Data.Type.BYTE_ARRAY, new byte[0]);

    @Getter
    protected DataSocket dataSocket;

    private Map<UUID, DataRequest> requests = new HashMap<>();

    ComputerInstance(LoadContext context) {
        super(context);
    }

    public ComputerInstance(Structure structure, BlockLocation location, StructureBodyDef bodyDef, Direction direction) {
        super(structure, location, bodyDef, direction);
    }

    @Override
    protected void setup() throws SetupException {
        fileSystem = new ComputerFileSystem(this);
        dataSocket = requireSocket("data", DataSocket.class);
        loadFiles();
    }

    @Override
    public void tick(boolean isVirtual) {
        List<UUID> deleteQueue = new ArrayList<>();
        requests.forEach((id, request) -> {
            if (request.isDeleted) {
                deleteQueue.add(id);
                return;
            }

            if (request.isCompleted)
                return;

            DataTransmitter dt = (DataTransmitter) dataSocket.getNode(request.target);
            if (dt == null) {
                request.isCompleted = true;
                request.response = 0;
                return;
            }

            int resp = dt.onDataRequest(this, request.data);
            request.isCompleted = true;
            request.response = resp;
        });

        deleteQueue.forEach(requests::remove);
    }

    @Override
    public void tick() {
        if (program != null && isActive()) {
            if (program.hasNext()) {
                program.run();
            } else {
                Bukkit.getPluginManager().callEvent(new ProgramEndEvent(program));
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
    }

    @Override
    public boolean onPlayerInteract(PlayerInteractEvent event, BlockLocation location) {
        new ComputerProgramsGUI(event.getPlayer(), this).open();
        return true;
    }

    public EditorSession getEditorSession() {
        return CodeBotsPlugin.inst().getEditorClient().getActiveSessionByBot(editorIdentifier.get());
    }

    public UUID getEditorIdentifier() {
        return editorIdentifier.get();
    }

    public void chat(String raw) {
        var message = ChatColor.translateAlternateColorCodes('&', Config.BOT_CHAT_FORMAT
                .replace("{message}", raw));

        var receivers = location.world.getBukkit().getNearbyEntities(location.toBukkitCentered(), 15, 15, 15, e -> e.getType() == EntityType.PLAYER);
        receivers.forEach(e -> e.sendMessage(message));
    }

    private void _loadProgram(UUID processId, ProgramSource program, boolean isResume) throws ParserException {
        try {
            String output = Compiler.toHumanReadable(CodeBotsTorusIntEntry.getComputerModuleRepository(), program.getCode());
            this.program = new Program(processId, Tokenizer.tokenize(output), CodeBotsTorusIntEntry.getComputerModuleRepository());
            this.program.setMode(Program.RUN_UNTIL_HALT);
            this.program.setExtra("computer", this);
            this.programSource = program;

            isDirty = true;
            location.getChunk().isUnsaved = true;

            if (!isResume) {
                if (Bukkit.isPrimaryThread()) {
                    Bukkit.getPluginManager().callEvent(new ProgramStartEvent(this.program));
                } else {
                    Bukkit.getScheduler().runTask(CodeBotsPlugin.inst(), () ->
                            Bukkit.getPluginManager().callEvent(new ProgramStartEvent(this.program))
                    );
                }
            }
        } catch (ParserException e) {
            setError(new ProgramError(ProgramError.ErrorLocation.PARSER, e.getMessage()));
            throw e;
        }
    }

    public void loadProgram(ProgramSource program) throws ParserException {
        _loadProgram(UUID.randomUUID(), program, false);
    }

    public boolean hasProgram() {
        return programSource != null;
    }

    public void reloadProgram() throws ParserException {
        if (this.programSource == null)
            return;

        try {
            var programSource = CodeBots.loadProgram(this.programSource.getDirectory(), this.programSource.getSource());
            _loadProgram(UUID.randomUUID(), programSource, false);
        } catch (ParserException e) {
            setError(new ProgramError(ProgramError.ErrorLocation.PARSER, e.getMessage(), new String[] { e.getMessage() }));
            throw e;
        }
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
    }

    public void loadFiles() {
        ByteArrayReader reader = new ByteArrayReader(fsBinary.get());
        if (!reader.hasNext())
            return;

        int filesCount = reader.readU1();
        List<BotFile> files = new LinkedList<>();
        for (int i = 0; i < filesCount; i++) {
            files.add(new BotFile(
                    CodeBotsPlugin.inst().getWorldManager().getWorld(location.world.getBukkit()).fsManager,
                    reader.readShortString(),
                    reader.readBytes(reader.readU2()),
                    reader.readLong()
            ));
        }
        fileSystem.setFiles(files);
    }

    public void saveFiles() {
        ByteArrayWriter writer = new ByteArrayWriter();
        writer.writeU1(fileSystem.getFiles().size());
        for (BotFile file : fileSystem.getFiles()) {
            writer.writeShortString(file.getName());
            writer.writeU2(file.getContent().length);
            writer.writeBytes(file.getContent());
            writer.writeLong(file.getLastModified());
        }
        fsBinary.update(writer.getBytes());
    }

    @Override
    public String getMAC() {
        return dataContainer.persist("mac", Data.Type.STRING, DataTransmitter.generateMAC()).get();
    }

    @Override
    public int onDataRequest(DataTransmitter dataTransmitter, int i) {
        return 0;
    }

    public UUID createRequest(String target, int data) {
        var request = new DataRequest(UUID.randomUUID(), target, data);
        requests.put(request.requestId, request);

        return request.requestId;
    }

    public DataRequest getRequest(UUID id) {
        return requests.get(id);
    }

    @Override
    public void onModelSpawn() {
        com.github.alantr7.codebots.world.BlockLocation cbLoc = new com.github.alantr7.codebots.world.BlockLocation(this.location.toBukkit());
        CodeBotsPlugin.inst().getWorldManager().getWorld(location.world.getBukkit())
                .getChunkOrLoad(cbLoc)
                .tickableStructures.put(cbLoc, this);

        EditorSession editorSession = CodeBotsPlugin.inst().getEditorClient().getActiveSessionByBot(editorIdentifier.get());
        if (editorSession != null) {
            editorSession.subscribe(EditorSession.createComputerSubscriber(this));
        }
    }

    @Override
    public void onModelDestroy() {
        com.github.alantr7.codebots.world.BlockLocation cbLoc = new com.github.alantr7.codebots.world.BlockLocation(this.location.toBukkit());
        CodeBotsPlugin.inst().getWorldManager().getWorld(location.world.getBukkit())
                .getChunkOrLoad(cbLoc)
                .tickableStructures.remove(cbLoc);

        EditorSession editorSession = CodeBotsPlugin.inst().getEditorClient().getActiveSessionByBot(editorIdentifier.get());
        if (editorSession != null) {
            editorSession.unsubscribeAll();
        }
    }

    public static class DataRequest {

        public final UUID requestId;

        public final String target;

        public final int data;

        @Getter
        int response;

        @Getter
        boolean isCompleted;

        boolean isDeleted;

        public DataRequest(UUID requestId, String target, int data) {
            this.requestId = requestId;
            this.target = target;;
            this.data = data;
        }

        public void delete() {
            isDeleted = true;
        }

    }

}
