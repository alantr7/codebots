package com.github.alantr7.codebots.api.redstone;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface RedstoneTransmitter {

    Location getLocation();

    int getOutput();

    double getPowerAt(@NotNull Location location);

    int MAXIMUM_RANGE = 30;

}
