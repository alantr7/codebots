package com.github.alantr7.codebots.plugin.data;

import com.alant7_.dborm.Database;
import com.alant7_.dborm.repository.RepositoryImpl;
import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.monitor.CraftMonitor;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.bot.CraftMemory;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.utils.BotLoader;
import com.github.alantr7.codebots.plugin.utils.Compatibility;
import com.github.alantr7.codebots.plugin.utils.EventDispatcher;
import com.github.alantr7.codebots.plugin.utils.MathHelper;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.joml.AxisAngle4f;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Singleton
public class DataLoader {

    @Inject
    CodeBotsPlugin plugin;

    @Inject
    BotRegistry botsRegistry;

    @Inject
    MonitorManager monitorsRegistry;

    @Inject
    PlayerRegistry players;

    @Inject
    ProgramRegistry programs;

    private RepositoryImpl<String, CraftMonitor> monitorsDb;

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    public void load() {
        loadConfig();
        loadPrograms();

        var botsDirectory = new File(plugin.getDataFolder(), "bots");
        botsDirectory.mkdirs();

        for (var directory : botsDirectory.listFiles()) {
            try {
                loadBot(directory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        loadMonitors();

        plugin.getLogger().info("Loaded " + botsRegistry.getBots().size() + " bots.");

        // Load players
        for (var player : Bukkit.getOnlinePlayers()) {
            players.registerPlayer(new PlayerData(player.getUniqueId()));
        }
    }

    public void reload() {
        loadConfig();
        loadPrograms();

        botsRegistry.getBots().forEach((id, bot) -> {
            bot.setProgram(null);

            var source = bot.getProgramSource();
            if (source != null) {
                try {
                    bot.setProgramSource(loadProgram(source.getDirectory(), source.getSource()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            bot.setActive(false);
        });
    }

    private void loadPrograms() {
        var programsDirectory = new File(plugin.getDataFolder(), "programs");
        if (!programsDirectory.exists()) {
            programsDirectory.mkdirs();
            var resource = plugin.getResource("example.js");
            if (resource != null) {
                try {
                    Files.write(new File(programsDirectory, "example.js").toPath(), resource.readAllBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (var programFile : programsDirectory.listFiles()) {
            try {
                programs.registerProgram(loadProgram(Directory.SHARED_PROGRAMS, programFile));
            } catch (Exception e) {
            }
        }
    }

    private void loadMonitors() {
        // Setup monitors database
        File monitorsDbFile = new File(plugin.getDataFolder(), "monitors.db");
        if (!monitorsDbFile.exists()) try { monitorsDbFile.createNewFile(); } catch (Exception e) { e.printStackTrace();}
        monitorsDb = Database.builder().config(config -> {
            config.setJdbcUrl("jdbc:sqlite:" + monitorsDbFile.getPath());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("MonitorsPool");
            config.setMaximumPoolSize(1);
        }).entity(CraftMonitor.class).build().getRepository(CraftMonitor.class);

        monitorsDb.selectAll("select * from monitors").forEach(monitor -> monitorsRegistry.registerMonitor(monitor));
        plugin.getLogger().info("Loaded " + monitorsRegistry.monitors.size() + " monitor(s).");
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        var config = plugin.getConfig();

        Config.BOT_MOVEMENT_DURATION = config.getInt("Bots.MovementDuration", Config.BOT_MOVEMENT_DURATION);
        Config.BOT_ROTATION_DURATION = config.getInt("Bots.RotationDuration", Config.BOT_ROTATION_DURATION);
        Config.BOT_ALLOW_BLOCK_BREAKING = config.getBoolean("Bots.AllowBlockBreaking", Config.BOT_ALLOW_BLOCK_BREAKING);

        var scriptsOption = config.getString("Bots.AllowedScripts", "ALL");
        Config.BOT_ALLOWED_SCRIPTS = scriptsOption.equals("LOCAL") ? 1 : scriptsOption.equals("SHARED") ? 2 : 0;
        Config.BOT_CHAT_FORMAT = config.getString("Bots.ChatFormat", Config.BOT_CHAT_FORMAT);
        Config.BOT_MAX_MEMORY_ENTRIES = config.getInt("Bots.MaxMemoryEntries", Config.BOT_MAX_MEMORY_ENTRIES);
        Config.BOT_MAX_LOCAL_PROGRAMS = config.getInt("Bots.MaxLocalPrograms", Config.BOT_MAX_LOCAL_PROGRAMS);


        Config.SCRIPTS_MAX_FUNCTION_CALL_STACK_SIZE = config.getInt("Scripts.MaxFunctionCallStackSize", Config.SCRIPTS_MAX_FUNCTION_CALL_STACK_SIZE);
        Config.SCRIPTS_MAX_VARIABLES_COUNT = config.getInt("Scripts.MaxVariablesCount", Config.SCRIPTS_MAX_VARIABLES_COUNT);
        Config.SCRIPTS_MAX_FUNCTIONS_COUNT = config.getInt("Scripts.MaxFunctionsCount", Config.SCRIPTS_MAX_FUNCTIONS_COUNT);


        Config.EDITOR_URL = config.getString("EditorUrl", Config.EDITOR_URL);
    }

    private void loadBot(File directory) throws IOException {
        var data = (CompoundTag) NBTUtil.read(new File(directory, "bot.dat"), false).getTag();
        var botId = UUID.fromString(directory.getName());

        var entityId = UUID.fromString(data.getString("EntityId"));
        var interactionId = UUID.fromString(data.getString("InteractionId"));
        var textDisplayId = data.getString("TextDisplayId");

        var world = Bukkit.getWorld(data.getString("World"));
        var position = data.getIntArray("Location");
        var direction = Direction.toDirection((char) data.getByte("Direction"));

        var bot = new CraftCodeBot(world, botId, entityId, interactionId);
        bot.setCachedLocation(new Location(world, position[0], position[1], position[2]));
        bot.setCachedDirection(direction);

        if (textDisplayId != null && !textDisplayId.isEmpty())
            bot.setTextEntityId(UUID.fromString(textDisplayId));

        if (bot.isChunkLoaded()) {
            var entity = bot.getEntity();

            // Upgrade the bot if needed
            if (entity instanceof BlockDisplay)
                Compatibility.upgradeBotTo0_4_0(bot);

            var radians = new AxisAngle4f(entity.getTransformation().getLeftRotation()).angle;
            var entityDirection = MathHelper.getDirectionFromAngle(radians);

            if (entityDirection != null)
                bot.setCachedDirection(entityDirection);
        }

        var programTag = data.getCompoundTag("Program");
        if (programTag != null) {
            var programPath = programTag.getString("Name");
            var programDirectory = programTag.getString("Directory");

            if (programPath != null && programDirectory != null) {
                try {
                    var programDirectoryEnum = Directory.valueOfOrDefault(programDirectory.toUpperCase(), Directory.LOCAL_PROGRAMS);
                    var programDirectoryFile = programDirectoryEnum == Directory.SHARED_PROGRAMS ? new File(plugin.getDataFolder(), "programs") : bot.getProgramsDirectory();

                    var program = Program.createFromSourceFile(new File(programDirectoryFile, programPath));
                    var program1 = programDirectoryEnum == Directory.SHARED_PROGRAMS ? programs.getProgram(programPath) : new ProgramSource(Directory.LOCAL_PROGRAMS, programPath, new File(programDirectoryFile, programPath), program.getCode());

                    bot.setProgramSource(program1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        int selectedSlot = data.getInt("Slot");
        var memoryTag = data.getCompoundTag("Memory");

        if (memoryTag != null) {
            bot.setMemory(BotLoader.loadMemory(memoryTag));
        }

        var ownerId = data.getString("OwnerId");
        if (!ownerId.isEmpty()) {
            bot.setOwnerId(UUID.fromString(ownerId));
        }

        bot.setSelectedSlot(selectedSlot);

        var inventoryFile = new File(directory, "inventory.yml");
        if (inventoryFile.exists()) {
            var inventoryData = YamlConfiguration.loadConfiguration(inventoryFile);
            for (int i = 0; i < 7; i++) {
                bot.getInventory().setItem(i, inventoryData.getItemStack("Slot" + i));
            }
        }

        this.botsRegistry.registerBot(bot);

        if (bot.isDirty()) {
            save(bot);
        }

        if (bot.isChunkLoaded()) {
            EventDispatcher.callBotLoadEvent(bot);
        }
    }

    public ProgramSource loadProgram(Directory directory, File file) throws ParserException, IOException {
        var source = Files.readAllLines(file.toPath()).toArray(String[]::new);
        var inline = Compiler.compileModule(String.join("\n", source));

        var code = inline.split("\n");
        return new ProgramSource(directory, file.getName(), file, code);
    }

    public void save() {
        botsRegistry.getBots().forEach((id, bot) -> save(bot));
    }

    public void save(CodeBot bot) {
        var directory = new File(new File(plugin.getDataFolder(), "bots"), bot.getId().toString());
        directory.mkdirs();

        var data = new CompoundTag();
        data.putString("World", bot.getLocation().getWorld().getName());
        data.putIntArray("Location", new int[]{
                bot.getLocation().getBlockX(),
                bot.getLocation().getBlockY(),
                bot.getLocation().getBlockZ()
        });
        data.putByte("Direction", (byte) bot.getDirection().name().charAt(0));

        data.putString("EntityId", bot.getEntityId().toString());
        data.putString("InteractionId", bot.getInteractionId().toString());
        data.putString("TextDisplayId", ((CraftCodeBot) bot).getTextEntityId().toString());

        if (bot.getOwnerId() != null) {
            data.putString("OwnerID", bot.getOwnerId().toString());
        }

        if (bot.getProgramSource() != null) {
            var programCategory = new CompoundTag();
            programCategory.putString("Directory", bot.getProgramSource().getDirectory().name());
            programCategory.putString("Name", bot.getProgramSource().getSource().getName());

            data.put("Program", programCategory);
        }

        data.putInt("Slot", bot.getSelectedSlot());
        data.put("Memory", BotLoader.saveMemory((CraftMemory) bot.getMemory()));

        try {
            NBTUtil.write(data, new File(directory, "bot.dat"), false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        var programs = new File(directory, "programs");
        programs.mkdirs();

        ((CraftCodeBot) bot).setDirty(false);
        ((CraftCodeBot) bot).setLastSaved(System.currentTimeMillis());
    }

    public void saveInventory(CodeBot bot) {
        var file = new File(bot.getDirectory(), "inventory.yml");
        var data = new YamlConfiguration();

        var inventory = bot.getInventory();
        for (int i = 0; i < 7; i++) {
            var item = inventory.getItem(i);
            data.set("Slot" + i, item);
        }

        try {
            data.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(CraftMonitor monitor) {
        monitorsDb.save(monitor);
    }

    public void delete(CraftMonitor monitor) {
        monitorsDb.delete(monitor.getId());
    }

}
