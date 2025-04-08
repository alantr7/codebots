package com.github.alantr7.codebots.api.bot;

import lombok.Getter;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

@Getter
public enum Direction {

    NORTH(0, 0, -1),

    EAST(1, 0, 0),

    SOUTH(0, 0, 1),

    WEST(-1, 0, 0),

    UP(0, 1, 0),

    DOWN(0, -1, 0);

    final int modX;

    final int modY;

    final int modZ;

    Direction(int modX, int modY, int modZ) {
        this.modX = modX;
        this.modY = modY;
        this.modZ = modZ;
    }

    public Direction getLeft() {
        return values()[clamp(0, 4, ordinal() - 1)];
    }

    public Direction getRight() {
        return values()[clamp(0, 4, ordinal() + 1)];
    }

    public Vector toVector() {
        return new Vector(modX, modY, modZ);
    }

    public static Direction fromVector(Vector v) {
        return switch ((int) v.getX()) {
            case 1 -> EAST;
            case -1 -> WEST;
            default -> switch ((int) v.getZ()) {
                case 1 -> SOUTH;
                case -1 -> NORTH;
                default -> switch ((int) v.getY()) {
                    case 1 -> UP;
                    case -1 -> DOWN;
                    default -> NORTH;
                };
            };
        };
    }

    public static Direction fromBlockFace(BlockFace face) {
        return switch (face) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> null;
        };
    }

    public static Direction toDirection(String name) {
        return switch (name.toUpperCase()) {
            case "NORTH" -> NORTH;
            case "EAST" -> EAST;
            case "SOUTH" -> SOUTH;
            case "WEST" -> WEST;
            case "UP" -> UP;
            case "DOWN" -> DOWN;
            default -> null;
        };
    }

    public static Direction toDirection(Character ch) {
        if (ch == null) return NORTH;
        return switch (Character.toUpperCase(ch)) {
            case 'N' -> NORTH;
            case 'E' -> EAST;
            case 'S' -> SOUTH;
            case 'W' -> WEST;
            default -> NORTH;
        };
    }

    private static int clamp(int a, int b, int val) {
        while (val >= b)
            val -= b;

        while (val < a)
            val += b;

        return val;
    }

}
