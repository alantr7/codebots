package com.github.alantr7.codebots.world.structure;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.monitor.Monitor;
import com.github.alantr7.codebots.api.redstone.RedstoneTransmitter;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.data.MonitorManager;
import com.github.alantr7.codebots.world.BlockLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

public class StructureFactory {

    public static RedstoneTransmitter createTransmitter(@NotNull Location location) {
        return new CraftRedstoneTransmitter(new BlockLocation(location));
    }

    public static Monitor createMonitor(String id, Location blockLocation, Direction facingDirection, Monitor.Size size) {
        Location location = blockLocation.toBlockLocation();
        CraftMonitor monitor = new CraftMonitor(
          id != null ? id : NanoIdUtils.randomNanoId(new SecureRandom(), NanoIdUtils.DEFAULT_ALPHABET, 8),
          new BlockLocation(location),
          facingDirection,
          size
        );

        CodeBotsPlugin.inst().getSingleton(MonitorManager.class).registerMonitor(monitor);
        return monitor;
    }

}
