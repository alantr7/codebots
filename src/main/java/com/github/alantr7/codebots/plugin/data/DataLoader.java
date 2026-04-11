package com.github.alantr7.codebots.plugin.data;

import com.github.alantr7.bukkitplugin.annotations.core.Inject;
import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.bot.ProgramSource;
import com.github.alantr7.codebots.api.player.PlayerData;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.fs.BotFile;
import com.github.alantr7.codebots.world.structure.CraftMonitor;
import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import com.github.alantr7.codebots.world.bot.CraftMemory;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.world.structure.CraftRedstoneTransmitter;
import com.github.alantr7.codebots.plugin.utils.BotLoader;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Singleton
public class DataLoader {

    @Inject
    CodeBotsPlugin plugin;

    @Inject
    PlayerManager players;

    @Inject
    ProgramRegistry programs;

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    public void load() {
        loadConfig();
        loadPrograms();

        // Load players
        for (var player : Bukkit.getOnlinePlayers()) {
            players.registerPlayer(new PlayerData(player.getUniqueId()));
        }
    }

    public void reload() {
        loadConfig();
        loadPrograms();
    }

    private void loadPrograms() {
        var programsDirectory = new File(plugin.getDataFolder(), "programs");
        if (!programsDirectory.exists()) {
            programsDirectory.mkdirs();
            var resource = plugin.getResource("example.cbs");
            if (resource != null) {
                try {
                    Files.write(new File(programsDirectory, "example.cbs").toPath(), resource.readAllBytes());
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

    public ProgramSource loadProgram(Directory directory, File file) throws IOException {
        String source = String.join("\n", Files.readAllLines(file.toPath()).toArray(String[]::new));
        return new ProgramSource(directory, file.getName(), new BotFile(file), source);
    }

    public ProgramSource loadProgram(Directory directory, BotFile file) {
        return new ProgramSource(directory, file.getName(), file, new String(file.getContent()));
    }

}
