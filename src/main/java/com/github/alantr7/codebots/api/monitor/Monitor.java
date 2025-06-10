package com.github.alantr7.codebots.api.monitor;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Monitor {

    String getId();

    Location getLocation();

    Direction getDirection();

    int getWidth();

    int getHeight();

    Size getSize();

    void moveCursor(int column, int line);

    void write(String text);

    void writeln(String text);

    void setText(@NotNull String text);

    void clear();

    void setBackgroundColor(@NotNull PresetColor color);

    void setTextColor(@NotNull TextColor color);

    @NotNull TextDisplay.TextAlignment getTextAlignment();

    void setTextAlignment(@NotNull TextDisplay.TextAlignment textAlignment);

    @Nullable CodeBot getConnectedBot();

    void setConnectedBot(CodeBot bot);

    @Getter
    enum Size {
        SIZE_2x1(2, 1),
        SIZE_3x2(3, 2),
        SIZE_4x3(4, 3);

        final int width, height;
        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}
