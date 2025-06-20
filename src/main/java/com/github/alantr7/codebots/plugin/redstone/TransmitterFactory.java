package com.github.alantr7.codebots.plugin.redstone;

import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

public class TransmitterFactory {

    public static RedstoneTransmitter createTransmitter(@NotNull Location location) {
        Location blockLocation = location.toBlockLocation();
        blockLocation.setYaw(0);
        blockLocation.setPitch(0);

        BlockDisplay displayFoundation = (BlockDisplay) location.getWorld().spawnEntity(
                blockLocation,
                EntityType.BLOCK_DISPLAY
        );
        displayFoundation.setBlock(Material.SMOOTH_STONE.createBlockData());

        Transformation foundationTransformation = displayFoundation.getTransformation();
        foundationTransformation.getScale().set(1, 0.2, 1);
        displayFoundation.setTransformation(foundationTransformation);



        BlockDisplay displayTorch = (BlockDisplay) location.getWorld().spawnEntity(
                blockLocation,
                EntityType.BLOCK_DISPLAY
        );
        displayTorch.setBlock(Material.REDSTONE_TORCH.createBlockData());

        location.getBlock().setType(Material.BARRIER);

        CraftRedstoneTransmitter transmitter = new CraftRedstoneTransmitter(blockLocation);
        transmitter.setFoundationId(displayFoundation.getUniqueId());
        transmitter.setTorchId(displayTorch.getUniqueId());

        return transmitter;
    }

}
