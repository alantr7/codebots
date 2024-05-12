package com.github.alantr7.codebots.plugin.utils;

import org.bukkit.Location;

public class MathHelper {

    public static final float EPSILON = 0.0001f;

    public static boolean floatsEqual(float f1, float f2) {
        return Math.abs(f1 - f2) < EPSILON;
    }

    public static Location toBlockLocation(Location location) {
        return new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

}
