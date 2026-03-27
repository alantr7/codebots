package com.github.alantr7.codebots.world;

import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.utils.Timing;
import com.github.alantr7.codebots.world.structure.StructureInstance;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class BotsWorldManager {

    private final Map<UUID, BotsWorld> worlds = new HashMap<>();

    @InvokePeriodically(interval = 0, limit = 1, delay = 2L)
    void initialize() {
        for (World world : Bukkit.getWorlds()) {
//            if (MainConfig.WORLD_BLACKLIST.contains(world.getName())) todo
//                continue;

            BotsWorld botsWorld = new BotsWorld(world);
            botsWorld.load();

            for (Chunk chunk : world.getLoadedChunks()) {
                botsWorld.getChunkOrLoad(new BlockLocation(botsWorld, chunk.getX() << 4, 0, chunk.getZ() << 4));
            }

            worlds.put(world.getUID(), botsWorld);
        }
    }

    public boolean isWorldSupported(World world) {
        return worlds.containsKey(world.getUID());
    }

    public BotsWorld getWorld(World world) {
        return worlds.get(world.getUID());
    }

    public Collection<BotsWorld> getWorlds() {
        return worlds.values();
    }

    @EventHandler
    void handleChunkLoadEvent(ChunkLoadEvent event) {
        BotsWorld world = worlds.get(event.getWorld().getUID());
        if (world == null)
            return;

        world.handleChunkLoad(event.getChunk());
    }

    @EventHandler
    void handleChunkUnloadEvent(ChunkUnloadEvent event) {
        BotsWorld world = worlds.get(event.getWorld().getUID());
        if (world == null)
            return;

        world.handleChunkUnload(event.getChunk());
    }

    @Getter
    private final Timing tickDurationTimings = new Timing(5);

    @InvokePeriodically(interval = 2L, delay = 2L)
    private void tickLoadedStructures() {
        long start = System.currentTimeMillis();
        worlds.values().forEach(BotsWorld::tick);
        tickDurationTimings.add((int) (System.currentTimeMillis() - start));
    }

    @InvokePeriodically(interval = 20 * 60, delay = 20 * 60)
    @Invoke(Invoke.Schedule.AFTER_PLUGIN_DISABLE)
    private void autoSaveStructures() {
        worlds.values().forEach(BotsWorld::save);
    }

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_DISABLE)
    private void removeModelsOnDisable() {
        worlds.values().forEach(world -> world.regions.values().forEach(region -> region.chunks.values().forEach(chunk -> {
            chunk.structures.values().forEach(StructureInstance::handleUnload);
        })));
    }

}
