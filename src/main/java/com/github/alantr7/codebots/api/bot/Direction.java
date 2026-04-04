package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;
import lombok.Getter;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

@Getter
public enum Direction {

    NORTH(0, 0, -1, 0),

    EAST(1, 0, 0, 90),

    SOUTH(0, 0, 1, 180),

    WEST(-1, 0, 0, 270),

    UP(0, 1, 0, 0),

    DOWN(0, -1, 0, 0);

    public final int modX;

    public final int modY;

    public final int modZ;

    public final int rotH;

    Direction(int modX, int modY, int modZ, int rotH) {
        this.modX = modX;
        this.modY = modY;
        this.modZ = modZ;
        this.rotH = rotH;
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

    public static float toAngle(Direction direction) {
        return switch (direction) {
            case NORTH -> RotateFunction.ANGLE_NORTH;
            case WEST -> RotateFunction.ANGLE_WEST;
            case EAST -> RotateFunction.ANGLE_EAST;
            case SOUTH -> RotateFunction.ANGLE_SOUTH;
            default -> 0;
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
