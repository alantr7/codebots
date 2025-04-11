package com.github.alantr7.codebots.api.monitor;

import net.kyori.adventure.text.format.TextColor;

import java.util.HashMap;
import java.util.Map;

public class ColorPalette {

    private static final Map<String, TextColor> colorsMap = new HashMap<>();

    public static final TextColor BLACK = TextColor.color(0, 0, 0);

    public static final TextColor WHITE = TextColor.color(255, 255, 255);

    public static final TextColor RED = TextColor.color(255, 0, 0);

    public static final TextColor GREEN = TextColor.color(0, 255, 0);

    public static final TextColor BLUE = TextColor.color(0, 0, 255);

    static {
        colorsMap.put("black", BLACK);
        colorsMap.put("white", WHITE);
        colorsMap.put("red", RED);
        colorsMap.put("green", GREEN);
        colorsMap.put("blue", BLUE);
    }

    public static TextColor getColor(String name) {
        return colorsMap.get(name);
    }

}
