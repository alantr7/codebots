package com.github.alantr7.codebots.plugin.monitor;

import com.alant7_.dborm.annotation.Data;
import com.alant7_.dborm.annotation.Entity;
import com.alant7_.dborm.annotation.Id;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.monitor.ColorPalette;
import com.github.alantr7.codebots.api.monitor.Monitor;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Entity("monitors")
public class CraftMonitor implements Monitor {

    @Getter
    @Id
    @Data
    private String id;

    @Data("text_display_id")
    private UUID textDisplayId;

    @Data("block_display_id")
    private UUID blockDisplayId;

    @Data("screen_display_id")
    private UUID screenDisplayId;

    private BlockDisplay blockDisplay;

    private BlockDisplay screenDisplay;

    private TextDisplay textDisplay;

    @Data
    private Location location;

    @Data
    @Getter
    @Setter
    private Direction direction;

    @Data
    @Getter
    private TextDisplay.TextAlignment textAlignment = TextDisplay.TextAlignment.LEFT;

    @Data
    private byte[] display = {(byte) 0};

    private int currentLineIndex;

    private TextComponent[] lines;

    private String[] linesContents;

    private TextColor textColor = TextColor.color(255, 255, 255);

    @Data
    private int width;

    @Data
    private int height;

    private Size size;

    @Data
    private UUID botId;

    public static final int[] MAX_CHARS_PER_LINES = {
            7,  // Width = 1
            15, // Width = 2
            23, // Width = 3
            32, // Width = 4
    };

    public static final int[] MAX_LINE_WIDTHS = {
            55,  // Width = 1
            105, // Width = 2
            155, // Width = 3
            200, // Width = 4
    };

    public static final int[] MAX_LINES = {
            4,   // Height = 1
            9,  // Height = 2
            15,  // Height = 3
            18,  // Height = 4
    };

    public static final String[] FILL_CHARACTERS = {
            " ".repeat(14),
            " ".repeat(26),
            " ".repeat(38),
            " ".repeat(50),
    };

    public static final float[] TEXT_DISPLAY_HORIZONTAL_OFFSETS = {
            0f,
            0f,
            -0.05f,
            -0.08f,
    };

    public static final float[] TEXT_DISPLAY_VERTICAL_OFFSETS = {
            0.05f,
            0.1f,
            -0.05f,
            0
    };

    public CraftMonitor(String id, Location location, Direction direction, BlockDisplay blockDisplay, BlockDisplay screenDisplay, TextDisplay textDisplay, Size size) {
        this.id = id;
        this.location = location;
        this.direction = direction;
        this.blockDisplayId = blockDisplay.getUniqueId();
        this.blockDisplay = blockDisplay;
        this.screenDisplay = screenDisplay;
        this.screenDisplayId = screenDisplay.getUniqueId();
        this.textDisplayId = textDisplay.getUniqueId();
        this.textDisplay = textDisplay;
        this.size = size;
        this.width = size.getWidth();
        this.height = size.getHeight();
        write("");
    }

    CraftMonitor() {
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Size getSize() {
        if (size != null) return size;

        try {
            return Size.valueOf("SIZE_" + width + "x" + height);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Size.SIZE_1x1;
    }

    @Override
    public void moveCursor(int column, int line) {

    }

    @Override
    public void write(String textA) {
        if (textDisplay() == null)
            return;

        // Create lines array
        if (this.lines == null) {
            this.lines = new TextComponent[MAX_LINES[this.height - 1]];
            this.linesContents = new String[lines.length];
            for (int i = 0; i < this.lines.length; i++) {
                lines[i] = Component.empty();
                linesContents[i] = "";
            }
        }


        // Append text from input text
        String[] tokens = StringUtils.tokenizeForMonitorText(textA);
        for (String text : tokens) {
            if (currentLineIndex >= MAX_LINES[height - 1])
                break;

            if (text.equals("\\n")) {
                currentLineIndex++;
                continue;
            }

            while (!text.isEmpty() && currentLineIndex < MAX_LINES[height - 1]) {
                TextComponent line = this.lines[currentLineIndex];
                String content = this.linesContents[currentLineIndex];

                if (content.length() + text.length() <= MAX_CHARS_PER_LINES[width - 1]) {
                    this.lines[currentLineIndex] = line.append(Component.text(text).color(textColor));
                    this.linesContents[currentLineIndex] += text;
                    break;
                }

                int fittingCharsCount = MAX_CHARS_PER_LINES[width - 1] - (content.length() + text.length());
                String fittingPart = text.substring(0, fittingCharsCount);

                this.lines[currentLineIndex] = line.append(Component.text(fittingPart).color(textColor));
                this.linesContents[currentLineIndex] += fittingPart;
                text = text.substring(fittingCharsCount);

                currentLineIndex++;
            }
        }


        // Resulting component
        Component component = Component.empty();
        for (TextComponent line : lines) {
            component = component.append(line).appendNewline();
        }

        // Fills the bottom line to allow proper text alignment
        component = component.append(Component.text(FILL_CHARACTERS[width - 1]));
        textDisplay.text(component);
    }

    @Override
    public void writeln(String text) {
        write(text + "\\n");
    }

    @Override
    public void clear() {
        currentLineIndex = 0;
        lines = null;
        textColor = ColorPalette.WHITE;

        write("");
    }

    @Override
    public void setTextColor(@NotNull TextColor color) {
        this.textColor = color;
    }

    @Override
    public void setTextAlignment(TextDisplay.@NotNull TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
        if (textDisplay() != null)
            textDisplay.setAlignment(textAlignment);
    }

    @Override
    public @Nullable CodeBot getConnectedBot() {
        return botId == null ? null : CodeBots.getBot(botId);
    }

    @Override
    public void setConnectedBot(CodeBot bot) {
        CodeBot previousBot = getConnectedBot();
        if (previousBot != null) {
            ((CraftCodeBot) previousBot).setMonitor(null);
        }

        if (bot != null) ((CraftCodeBot) bot).setMonitor(this);
        botId = bot == null ? null : bot.getId();
    }

    public BlockDisplay screenDisplay() {
        return this.screenDisplay = (BlockDisplay) Bukkit.getEntity(screenDisplayId);
    }

    public BlockDisplay blockDisplay() {
        return this.blockDisplay = (BlockDisplay) Bukkit.getEntity(blockDisplayId);
    }

    public TextDisplay textDisplay() {
        return this.textDisplay = (TextDisplay) Bukkit.getEntity(textDisplayId);
    }

    public void remove() {
        if (blockDisplay() != null) blockDisplay.remove();
        if (screenDisplay() != null) screenDisplay.remove();
        if (textDisplay() != null) textDisplay.remove();
    }

    public int getMaxLineWidth() {
        return MAX_LINE_WIDTHS[width - 1];
    }

}
