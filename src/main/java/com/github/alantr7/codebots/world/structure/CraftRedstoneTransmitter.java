package com.github.alantr7.codebots.world.structure;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import com.github.alantr7.codebots.utils.StringPool;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.BotsChunk;
import com.github.alantr7.codebots.world.BotsRegion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CraftRedstoneTransmitter extends StructureInstance implements RedstoneTransmitter {

    @Getter
    String id;

    @Getter @Setter
    private UUID foundationId;

    @Getter @Setter
    private UUID torchId;

    private BlockDisplay foundationDisplay;

    private BlockDisplay torchDisplay;

    public CraftRedstoneTransmitter(BlockLocation location) {
        super(location, Direction.NORTH);
    }

    @Override
    public Location getLocation() {
        return location.toBukkit();
    }

    @Override
    public int getOutput() {
        return location.getBlock().getBlockPower();
    }

    @Override
    public double getPowerAt(@NotNull Location location) {
        if (this.location.world.getBukkit() != location.getWorld())
            return 0;

        int output = getOutput();
        if (output == 0)
            return 0;

        double distance = location.distance(this.location.toBukkitCentered());
        if (distance > MAXIMUM_RANGE)
            return 0;

        return (MAXIMUM_RANGE - distance) / (double) MAXIMUM_RANGE * output;
    }

    @Override
    public void onModelSpawn() {
        Location blockLocation = location.toBukkit();
        blockLocation.setYaw(0);
        blockLocation.setPitch(0);

        foundationDisplay = (BlockDisplay) blockLocation.getWorld().spawnEntity(
          blockLocation,
          EntityType.BLOCK_DISPLAY
        );
        foundationDisplay.setPersistent(false);
        foundationDisplay.setBlock(Material.SMOOTH_STONE.createBlockData());
        foundationId = foundationDisplay.getUniqueId();

        Transformation foundationTransformation = foundationDisplay.getTransformation();
        foundationTransformation.getScale().set(1, 0.2, 1);
        foundationDisplay.setTransformation(foundationTransformation);

        torchDisplay = (BlockDisplay) blockLocation.getWorld().spawnEntity(
          blockLocation,
          EntityType.BLOCK_DISPLAY
        );
        torchDisplay.setPersistent(false);
        torchDisplay.setBlock(Material.REDSTONE_TORCH.createBlockData());
        torchId = torchDisplay.getUniqueId();

        location.getBlock().setType(Material.BARRIER);
    }

    @Override
    public void onModelDestroy() {
        foundationDisplay.remove();
        torchDisplay.remove();
    }

    public void remove() {
        location.world.removeStructure(this);
    }

    @Override
    public void tick() {
    }

    public static CraftRedstoneTransmitter fromBytes(BotsRegion region, BotsChunk chunk, ByteArrayReader reader) {
        int x = ByteArrayReader.toInt(reader.readBytes(1));
        int y = ByteArrayReader.toInt(reader.readBytes(2));
        int z = ByteArrayReader.toInt(reader.readBytes(1));
        BlockLocation location = new BlockLocation(chunk.world, (chunk.position.x << 4) | x, y, (chunk.position.y << 4) | z);

        // Direction
        reader.readU1();

        return new CraftRedstoneTransmitter(location);
    }

    @Override
    public void save(ByteArrayWriter buffer, StringPool constants) {
        // Structure type identifier
        buffer.writeU2(4); // structure id (4 for redstone transmitter)

        int basePointer = buffer.getPointer();

        // Structure size on disk
        buffer.writeU2(0);

        // Location
        buffer.writeBytes(ByteArrayWriter.toBytes(location.x & 0xf, 1));;
        buffer.writeBytes(ByteArrayWriter.toBytes(location.y, 2));
        buffer.writeBytes(ByteArrayWriter.toBytes(location.z, 1));

        // Direction
        buffer.writeU1(0);

        int returnPointer = buffer.getPointer();
        buffer.setPointer(basePointer);
        buffer.writeU2(returnPointer - basePointer - 2);
        buffer.setPointer(returnPointer);
    }

}
