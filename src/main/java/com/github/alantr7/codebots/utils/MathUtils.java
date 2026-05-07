package com.github.alantr7.codebots.utils;

import com.github.alantr7.codebots.api.bot.Direction;
import org.bukkit.Location;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class MathUtils {

    public static void applyRotation(Vector3f vector, float angle) {
        float[] result = rotateVector(new float[] { vector.x, vector.z }, angle);
        vector.x = result[0];
        vector.z = result[1];
    }

    public static float[] rotateVector(float[] pos, float angle) {
        return switch ((int) angle) {
            case 0, 360     -> new float[] { pos[0], pos[1] };
            case 90, -270   -> new float[] { -pos[1], pos[0] };
            case 180, -180  -> new float[] { -pos[0], -pos[1] };
            case 270, -90   -> new float[] { pos[1], -pos[0] };
            default -> {
                float distance = (float) Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1]);
                float currentAngle = (float) Math.toDegrees(Math.atan2(pos[0], pos[1]));

                yield new float[] {
                  (float) (Math.sin(Math.toRadians(currentAngle -angle)) * distance),
                  (float) (Math.cos(Math.toRadians(currentAngle -angle)) * distance)
                };
            }
        };
    }

    public static boolean hasFlag(int mask, int flag) {
        return (mask & flag) != 0;
    }

    public static int setFlag(int mask, int flag, boolean toggle) {
        return toggle ? (mask | flag) : (mask & ~flag);
    }

    public static byte[] rotateVectors(byte[] parentBounds, Direction direction) {
        byte[] bounds = new byte[parentBounds.length];

        for (int i = 0; i < bounds.length; i += 3) {
            float[] result = rotateVector(new float[] { parentBounds[i], parentBounds[i + 2] }, direction.rotH);
            bounds[i]     = (byte) Math.round(result[0]);
            bounds[i + 2] = (byte) Math.round(result[1]);
            bounds[i + 1] = parentBounds[i + 1];
        }

        return bounds;
    }

    public static byte[] rotateVectors(Vector3i parentBounds, Direction direction) {
        return rotateVectors(new byte[] { (byte) parentBounds.x, (byte) parentBounds.y, (byte) parentBounds.z }, direction);
    }

    public static float[] rotateVectors(float[] parentBounds, float rotH, float rotV) {
        float[] bounds = new float[parentBounds.length];

        for (int i = 0; i < bounds.length; i += 3) {
            float distance = (float) Math.sqrt(parentBounds[i] * parentBounds[i] + parentBounds[i + 2] * parentBounds[i + 2]);
            float angle = (float) Math.toRadians(rotH) + (float) Math.atan2(parentBounds[i + 2], parentBounds[i]);

            bounds[i] = (float) Math.cos(angle) * distance;
            bounds[i + 2] = (float) Math.sin(angle) * distance;
            bounds[i + 1] = parentBounds[i + 1];
        }

        return bounds;
    }

    public static float[] rotateVectors(Vector3f parentBounds, float rotH, float rotV) {
        return rotateVectors(new float[] { parentBounds.x, parentBounds.y, parentBounds.z }, rotH, rotV);
    }

    public static String formatNumber(int number) {
        if (number > 1_000_000) {
            return String.format("%.1fM", number / 1_000_000f);
        }
        if (number > 1_000) {
            return String.format("%.1fk", number / 1_000f);
        }
        return String.format("%d", number);
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
