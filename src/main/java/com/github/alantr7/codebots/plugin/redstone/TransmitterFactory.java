package com.github.alantr7.codebots.plugin.redstone;

import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class TransmitterFactory {

    public static RedstoneTransmitter createTransmitter(@NotNull Location location) {
        location.getBlock().setType(Material.BARRIER);
        return new CraftRedstoneTransmitter(location);
    }

}
