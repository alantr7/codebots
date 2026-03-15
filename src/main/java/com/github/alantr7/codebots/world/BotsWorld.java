package com.github.alantr7.codebots.world;

import com.github.alantr7.codebots.api.monitor.Monitor;
import com.github.alantr7.codebots.world.structure.CraftMonitor;
import com.github.alantr7.codebots.world.structure.StructureInstance;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BotsWorld {

    @Getter
    private final World bukkit;

    public final File botsDirectory;

    public final File botsRegionsDirectory;

    protected final Map<Vector2i, BotsRegion> regions = new HashMap<>();

    protected final Map<String, CraftMonitor> monitors = new HashMap<>();

    @Getter
    private int ticks;

    public BotsWorld(World bukkit) {
        this.bukkit = bukkit;
        this.botsDirectory = new File(bukkit.getWorldFolder(), "codebots");
        this.botsDirectory.mkdirs();
        this.botsRegionsDirectory = new File(this.botsDirectory, "region");
        this.botsRegionsDirectory.mkdirs();
    }

    static final Set<Material> MINECRAFT_BLOCK_CONTAINER_TYPES = Set.of(
      Material.CHEST, Material.TRAPPED_CHEST, Material.DROPPER, Material.DISPENSER, Material.HOPPER, Material.SHULKER_BOX
    );
    public static boolean isItemContainer(BlockLocation location) {
        Material material = location.getBlock().getType();
        if (MINECRAFT_BLOCK_CONTAINER_TYPES.contains(material))
            return true;

        if (material.name().endsWith("_SHULKER_BOX"))
            return true;

        return false;
    }

    @Nullable
    BotsRegion getRegion(BlockLocation location) {
        return regions.get(new Vector2i(location.regionX, location.regionZ));
    }

    BotsRegion getRegionAt(int x, int z) {
        return regions.get(new Vector2i(x, z));
    }

    @NotNull
    public Collection<BotsRegion> getRegions() {
        return regions.values();
    }

    @NotNull
    BotsRegion getRegionOrLoad(BlockLocation location) {
        return regions.computeIfAbsent(new Vector2i(location.regionX, location.regionZ), v -> {
            BotsRegion region = new BotsRegion(this, v.x, v.y);
            try {
                region.load();
            } catch (Exception e) {
                e.printStackTrace();
//                TorusLogger.error(Category.WORLD, e.getMessage()); todo: logs
            }

            return region;
        });
    }

    @Nullable
    public BotsChunk getChunk(BlockLocation location) {
        return getChunkAt(location.x >> 4, location.z >> 4);
    }

    @Nullable
    public BotsChunk getChunkAt(int chunkX, int chunkZ) {
        BotsRegion region = getRegionAt(chunkX >> 5, chunkZ >> 5);
        return region != null ? region.chunks.get(new Vector2i(chunkX, chunkZ)) : null;
    }

    @NotNull
    BotsChunk getChunkOrLoad(BlockLocation location) {
        return getRegionOrLoad(location).getOrLoadChunk(location.x >> 4, location.z >> 4, false);
    }

    protected void handleChunkLoad(Chunk chunk) {
        getChunkOrLoad(new BlockLocation(this, chunk.getX() << 4, 0, chunk.getZ() << 4));
    }

    protected void handleChunkUnload(Chunk chunk) {
        BotsRegion region = getRegion(new BlockLocation(this, chunk.getX() << 4, 0, chunk.getZ() << 4));
        if (region == null)
            return;

        BotsChunk torusChunk = region.chunks.get(new Vector2i(chunk.getX(), chunk.getZ()));
        if (torusChunk != null) {
            torusChunk.structures.values().forEach(StructureInstance::handleUnload);
            if (torusChunk.isUnsaved) {
                try {
                    region.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            region.chunks.remove(torusChunk.position);

            if (region.chunks.isEmpty()) {
                regions.remove(new Vector2i(region.x, region.z));
            }
        }
    }

    public StructureInstance getStructure(BlockLocation location) {
        BotsChunk chunk = getChunk(location);
        if (chunk == null)
            return null;

        BlockLocation machineLocation = chunk.occupations.get(location);
        if (machineLocation == null)
            return null;

        if (machineLocation.x >> 4 == location.x >> 4 && machineLocation.z >> 4 == location.z >> 4)
            return chunk.structures.get(machineLocation);

        BotsChunk machineChunk = getChunk(machineLocation);
        if (machineChunk == null)
            return null;

        return machineChunk.structures.get(machineLocation);
    }

    public StructureInstance currentlyTicked;
    void tick() {
        for (BotsRegion region : regions.values()) {
            for (BotsChunk chunk : region.chunks.values()) {
                for (StructureInstance s : chunk.tickableStructures.values()) {
                    if (s.isCorrupted || s.isUnloaded())
                        return;

                    currentlyTicked = s;
                    try {
                        s.tick();
                    } catch (Exception e) {
//                        TorusLogger.error(Category.STRUCTURES, "Encountered an error whilst ticking a structure - marked it as corrupted.");
                        e.printStackTrace();
                        s.corrupt();
                    }
                    currentlyTicked = null;
                }
            }
        }
        ticks++;
    }

    public void registerMonitor(CraftMonitor monitor) {
        monitors.put(monitor.getId(), monitor);
    }

    public void unregisterMonitor(CraftMonitor monitor) {
        monitors.remove(monitor.getId());
    }

    @Nullable
    public CraftMonitor getMonitorById(String id) {
        return monitors.get(id);
    }

    public void placeStructure(@NotNull StructureInstance instance) {
        BotsChunk chunk = getChunkOrLoad(instance.location);
        chunk._registerStructure(instance);
        chunk.isUnsaved = true;

        // Place bounds
        byte[] bounds = instance.getCollisionVectors();
        for (int i = 0; i < bounds.length; i += 3) {
            BlockLocation relative = instance.location.getRelative(bounds[i], bounds[i+1], bounds[i+2]);
            relative.getBlock().setType(Material.BARRIER);

            BotsChunk occupationChunk = getChunkOrLoad(relative);
            occupationChunk.occupations.put(relative, instance.location);
            occupationChunk.isUnsaved = true;
        }
    }

    public void removeStructure(@NotNull StructureInstance instance) {
        BotsChunk chunk0 = getChunkOrLoad(instance.location);
        chunk0._unregisterStructure(instance);

        instance.isRemoved = true;

        // Remove bounds
        byte[] bounds = instance.getCollisionVectors();
        for (int i = 0; i < bounds.length; i += 3) {
            BlockLocation relative = instance.location.getRelative(bounds[i], bounds[i+1], bounds[i+2]);
            relative.getBlock().setType(Material.AIR);

            BotsChunk chunk = getChunkOrLoad(relative);
            if (chunk.occupations.remove(relative) != null) {
                chunk.isUnsaved = true;
            }
        }

        // Remove models
        instance.handleUnload();

        // Run destroy callbacks
        instance.onRemove();
    }

    void load() {}

    public void save() {
        long start = System.currentTimeMillis();
        regions.forEach((loc, region) -> {
            try {
                region.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
//        if (MainConfig.LOGS_WORLD_SAVE) { todo
//            TorusLogger.info(Category.WORLD, "Saved " + bukkit.getName() + " in " + (System.currentTimeMillis() - start) + "ms.");
//        }
    }

}
