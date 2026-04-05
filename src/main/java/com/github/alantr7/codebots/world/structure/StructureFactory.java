package com.github.alantr7.codebots.world.structure;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.api.monitor.Monitor;
import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import com.github.alantr7.codebots.utils.MathUtils;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.github.alantr7.codebots.plugin.program.ItemFactory.key;

public class StructureFactory {

    private static final Map<String, StructureConstructor<StructureInstance>> constructors = new HashMap<>();
    static {
        constructors.put("bot", (location, direction, item) -> {
            CraftCodeBot bot = new CraftCodeBot(location, direction, UUID.randomUUID());
            if (item != null && item.hasItemMeta()) {
                var pdc = item.getItemMeta().getPersistentDataContainer();

                // load file system
                long[] filePointers = pdc.get(key("FileSystem"), PersistentDataType.LONG_ARRAY);
                if (filePointers != null) {
                    location.world.fsManager.load(bot.getFileSystem(), filePointers);
                }

                // load active program
                var pdcProgram = pdc.get(key("Program"), PersistentDataType.TAG_CONTAINER);
                if (pdcProgram != null) {
                    String sourceDirectoryName = pdcProgram.get(key("Dir"), PersistentDataType.STRING);
                    String sourceName = pdcProgram.get(key("File"), PersistentDataType.STRING);

                    if (sourceName != null && sourceDirectoryName != null) {
                        Directory sourceDirectory = Directory.valueOfOrDefault(sourceDirectoryName, null);
                        if (sourceDirectory != null) {
                            try {
                                bot.loadProgram(sourceDirectory, sourceName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            return bot;
        });
        constructors.put("redstone_transmitter", (location, direction, item) -> new CraftRedstoneTransmitter(location));
        constructors.put("monitor_2x1", (location, direction, item) -> new CraftMonitor(
          NanoIdUtils.randomNanoId(new SecureRandom(), NanoIdUtils.DEFAULT_ALPHABET, 8), location, direction, Monitor.Size.SIZE_2x1
        ));
        constructors.put("monitor_3x2", (location, direction, item) -> new CraftMonitor(
          NanoIdUtils.randomNanoId(new SecureRandom(), NanoIdUtils.DEFAULT_ALPHABET, 8), location, direction, Monitor.Size.SIZE_3x2
        ));
        constructors.put("monitor_4x3", (location, direction, item) -> new CraftMonitor(
          NanoIdUtils.randomNanoId(new SecureRandom(), NanoIdUtils.DEFAULT_ALPHABET, 8), location, direction, Monitor.Size.SIZE_4x3
        ));
    }

    private static final Map<String, byte[]> structureCollisions = new HashMap<>();
    static {
        structureCollisions.put("bot", new byte[] { 0, 0, 0 });
        structureCollisions.put("redstone_transmitter", new byte[] { 0, 0, 0 });
        structureCollisions.put("monitor_2x1", CraftMonitor.COLLISION_2x1);
        structureCollisions.put("monitor_3x2", CraftMonitor.COLLISION_3x2);
        structureCollisions.put("monitor_4x3", CraftMonitor.COLLISION_4x3);
    }

    @SuppressWarnings("unchecked")
    public static <T extends StructureInstance> T construct(String id, BlockLocation location, Direction direction, @Nullable ItemStack item) {
        StructureConstructor<StructureInstance> constructor = constructors.get(id);
        if (constructor == null)
            return null;

        StructureInstance instance = constructor.instantiate(location, direction, item);
        return (T) instance;
    }

    public static boolean isPlaceableAt(String id, BlockLocation location, Direction direction) {
        byte[] collisions = structureCollisions.get(id);
        if (collisions == null)
            return false;

        byte[] bounds = MathUtils.rotateVectors(collisions, direction);
        for (int i = 0; i < bounds.length; i += 3) {
            if (location.getRelative(bounds[i], bounds[i + 1], bounds[i + 2]).getStructure() != null)
                return false;
        }

        return true;
    }

    @FunctionalInterface
    interface StructureConstructor<T> {
        T instantiate(BlockLocation location, Direction direction, @Nullable ItemStack item);
    }

}
