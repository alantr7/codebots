package com.github.alantr7.codebots.plugin.utils;

import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import org.bukkit.Location;

public class MathHelper {

    public static final float EPSILON = 0.0001f;

    public static boolean floatsEqual(float f1, float f2) {
        return Math.abs(f1 - f2) < EPSILON;
    }

    public static Direction getDirectionFromAngle(float rad) {
        if (floatsEqual(rad, RotateFunction.ANGLE_NORTH))
            return Direction.NORTH;

        if (floatsEqual(rad, RotateFunction.ANGLE_EAST))
            return Direction.EAST;

        if (floatsEqual(rad, RotateFunction.ANGLE_SOUTH))
            return Direction.SOUTH;

        if (floatsEqual(rad, RotateFunction.ANGLE_WEST))
            return Direction.WEST;

        return null;
    }

    public static Location toBlockLocation(Location location) {
        return new Location(
                location.getWorld(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public static <T> T any(T val, T def) {
        return val != null ? val : def;
    }

}
