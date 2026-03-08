package com.github.alantr7.codebots.plugin.redstone;

import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CraftRedstoneTransmitter  implements RedstoneTransmitter {

    @Getter
    String id;

    @Getter
    private Location location;

    @Getter @Setter
    private UUID foundationId;

    @Getter @Setter
    private UUID torchId;

    private BlockDisplay foundationDisplay;

    private BlockDisplay torchDisplay;

    CraftRedstoneTransmitter() {
    }

    public CraftRedstoneTransmitter(Location location) {
        this.location = location;
    }

    @Override
    public int getOutput() {
        return location.getBlock().getBlockPower();
    }

    @Override
    public double getPowerAt(@NotNull Location location) {
        if (this.location.getWorld() != location.getWorld())
            return 0;

        int output = getOutput();
        if (output == 0)
            return 0;

        double distance = location.distance(this.location);
        if (distance > MAXIMUM_RANGE)
            return 0;

        return (MAXIMUM_RANGE - distance) / (double) MAXIMUM_RANGE * output;
    }

    public BlockDisplay foundationDisplay() {
        return this.foundationDisplay = (BlockDisplay) Bukkit.getEntity(foundationId);
    }

    public BlockDisplay torchDisplay() {
        return this.torchDisplay = (BlockDisplay) Bukkit.getEntity(torchId);
    }

    public void remove() {
        if (foundationDisplay() != null)
            foundationDisplay.remove();

        if (torchDisplay() != null)
            torchDisplay.remove();

        location.getBlock().setType(Material.AIR);
    }

}
