package com.github.alantr7.codebots.world;

import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.world.structure.StructureInstance;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BlockLocation {

    public BotsWorld world;

    public int x, y, z, regionX, regionZ;

    public BlockLocation(BotsWorld world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.regionX = x >> 9;
        this.regionZ = z >> 9;
    }

    public BlockLocation(Location location) {
        this(CodeBotsPlugin.inst().getWorldManager().getWorld(location.getWorld()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockLocation getRelative(BlockLocation location) {
        return getRelative(location.x, location.y, location.z);
    }

    public BlockLocation getRelative(Direction direction) {
        return getRelative(direction.modX, direction.modY, direction.modZ);
    }

    public BlockLocation getRelative(int x, int y, int z) {
        return new BlockLocation(world, this.x + x, this.y + y, this.z + z);
    }

    public double getDistanceTo(BlockLocation location) {
        return Math.sqrt(Math.pow(location.x - x, 2) + Math.pow(location.y - y, 2) + Math.pow(location.z - z, 2));
    }

    public @NotNull Location toBukkit() {
        return new Location(world.getBukkit(), x, y, z);
    }

    public @NotNull Location toBukkitCentered() {
        return new Location(world.getBukkit(), x + 0.5f, y, z + 0.5f);
    }

    public Block getBlock() {
        return world.getBukkit().getBlockAt(x, y, z);
    }

    public boolean isLoaded() {
        BotsChunk chunk = getChunk();
        return chunk != null;
    }

    public BotsChunk getChunk() {
        return world.getChunk(this);
    }

    public @Nullable StructureInstance getStructure() {
        return world.getStructure(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlockLocation that = (BlockLocation) o;
        return x == that.x && y == that.y && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    @Override
    public String toString() {
        return "{" +
          "x=" + x +
          ", y=" + y +
          ", z=" + z +
          '}';

    }

}
