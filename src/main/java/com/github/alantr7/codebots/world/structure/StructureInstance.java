package com.github.alantr7.codebots.world.structure;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.world.bot.CraftCodeBot;
import com.github.alantr7.codebots.utils.MathUtils;
import com.github.alantr7.codebots.utils.StringPool;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.BotsChunk;
import com.github.alantr7.codebots.world.BotsRegion;
import com.github.alantr7.codebots.world.structure.data.DataContainer;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;

import java.util.HashSet;
import java.util.Set;

public abstract class StructureInstance {

    public BlockLocation location;

    public Direction direction;

    public boolean isCorrupted;

    public boolean isRemoved;

    public boolean collisionBarriers = true;

    private byte[] collisionVectors;

    private int[][] occupiedChunks;

    protected final DataContainer dataContainer = new DataContainer();

    public StructureInstance(BlockLocation location, Direction direction) {
        this.location = location;
        this.direction = direction;
        this.dataContainer.structure = this;
    }

    public abstract byte[] getOriginalCollisionVectors();

    public byte[] getCollisionVectors() {
        if (this.collisionVectors != null)
            return this.collisionVectors;

        return this.collisionVectors = MathUtils.rotateVectors(getOriginalCollisionVectors(), direction);
    }

    protected void setOccupiedChunks() {
        byte[] bounds = getCollisionVectors();
        Set<Vector2i> positions = new HashSet<>();

        for (int i = 0; i < bounds.length; i+=3) {
            positions.add(new Vector2i((location.x + bounds[i]) >> 4, (location.z + bounds[i + 2]) >> 4));
        }

        this.occupiedChunks = positions.stream().map(v -> new int[]{v.x, v.y}).toArray(int[][]::new);
    }

    public void makePhysical() {
        try {
            onModelSpawn();
        } catch (Exception exc) {
//            TorusLogger.error(Category.STRUCTURES, "Could not spawn the model for " + structure.id + " at " + location);
            exc.printStackTrace();
        }
    }

    public void handleUnload() {
        try {
            onModelDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isUnloaded() {
        for (int[] chunkPos : occupiedChunks) {
            if (location.world.getChunkAt(chunkPos[0], chunkPos[1]) != null)
                return false;
        }
        return true;
    }

    public void corrupt() {
        isCorrupted = true;
    }

    public void save(ByteArrayWriter writer, StringPool constants) {}

    public void onModelSpawn() {}

    public void onModelDestroy() {}

    public void onRemove() {}

    public abstract ItemStack getItemDrop();

    public void setup() {}

    public static void place(StructureInstance instance) {
        try {
            instance.setup();
        } catch (Exception exc) {
            instance.isCorrupted = true;
            exc.printStackTrace();
        }

        instance.makePhysical();
    }

    public static StructureInstance fromBytes(BotsRegion region, BotsChunk chunk, ByteArrayReader reader, int structureId) {
        return switch (structureId) {
            case 2 -> CraftCodeBot.fromBytes(region, chunk, reader);
            case 3 -> CraftMonitor.fromBytes(region, chunk, reader);
            case 4 -> CraftRedstoneTransmitter.fromBytes(region, chunk, reader);
            default -> null;
        };
    }

}
