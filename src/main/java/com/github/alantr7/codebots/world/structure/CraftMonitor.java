package com.github.alantr7.codebots.world.structure;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.monitor.ColorPalette;
import com.github.alantr7.codebots.api.monitor.Monitor;
import com.github.alantr7.codebots.api.monitor.PresetColor;
import com.github.alantr7.codebots.item.BotsItem;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.utils.StringUtils;
import com.github.alantr7.codebots.utils.StringPool;
import com.github.alantr7.codebots.world.BlockLocation;
import com.github.alantr7.codebots.world.BotsChunk;
import com.github.alantr7.codebots.world.BotsRegion;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CraftMonitor extends StructureInstance implements Monitor {

    @Getter @Setter
    private String id;

    private BlockDisplay blockDisplay;

    private BlockDisplay screenDisplay;

    private TextDisplay textDisplay;

    @Getter
    @Setter
    private Direction direction;

    @Getter
    private TextDisplay.TextAlignment textAlignment = TextDisplay.TextAlignment.LEFT;

    private byte[] display = {(byte) 0};

    private int currentLineIndex;

    private TextComponent[] lines;

    private String[] linesContents;

    private TextColor textColor = TextColor.color(255, 255, 255);

    private int width;

    private int height;

    private Size size;

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

    public static final byte[] COLLISION_2x1 = {
      0, 0, 0, -1, 0, 0
    };

    public static final byte[] COLLISION_3x2 = {
      0, 1, 0, -1, 1, 0, -2, 1, 0,
      0, 0, 0, -1, 0, 0, -2, 0, 0,
    };

    public static final byte[] COLLISION_4x3 = {
      0, 2, 0, -1, 2, 0, -2, 2, 0, -3, 2, 0,
      0, 1, 0, -1, 1, 0, -2, 1, 0, -3, 1, 0,
      0, 0, 0, -1, 0, 0, -2, 0, 0, -3, 0, 0,
    };

    public CraftMonitor(String id, BlockLocation location, Direction direction, Size size) {
        super(location, direction);
        this.id = id;
        this.direction = direction;
        this.size = size;
        this.width = size.getWidth();
        this.height = size.getHeight();
        setOccupiedChunks();
        write("");
    }

    @Override
    public Location getLocation() {
        return location.toBukkit();
    }

    @Override
    public byte[] getOriginalCollisionVectors() {
        return switch (size) {
            case SIZE_4x3 -> COLLISION_4x3;
            case SIZE_3x2 -> COLLISION_3x2;
            case SIZE_2x1 -> COLLISION_2x1;
        };
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

        return Size.SIZE_2x1;
    }

    @Override
    public void moveCursor(int column, int line) {

    }

    @Override
    public void write(String textA) {
        if (textDisplay == null)
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

                int fittingCharsCount = MAX_CHARS_PER_LINES[width - 1] - content.length();
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
    public void setText(@NotNull String text) {
        clear();
        write(text);
    }

    @Override
    public void clear() {
        currentLineIndex = 0;
        lines = null;
        textColor = ColorPalette.WHITE.text();

        write("");
    }

    @Override
    public void setBackgroundColor(@NotNull PresetColor color) {
        if (screenDisplay == null)
            return;

        screenDisplay.setBlock(color.background().createBlockData());
    }

    @Override
    public void setTextColor(@NotNull TextColor color) {
        this.textColor = color;
    }

    @Override
    public void setTextAlignment(TextDisplay.@NotNull TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
        if (textDisplay != null)
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

        if (bot != null) {
            clear();
        } else {
            showDefaultText();
        }
    }

    public void showDefaultText() {
        clear();
        write("Monitor ID: ");
        setTextColor(ColorPalette.GREEN.text());
        write(id);
    }

    @Override
    public void onModelSpawn() {
        location.world.registerMonitor(this);
        blockDisplay = (BlockDisplay) location.world.getBukkit().spawnEntity(location.toBukkit(), EntityType.BLOCK_DISPLAY);
        screenDisplay = (BlockDisplay) location.world.getBukkit().spawnEntity(location.toBukkit(), EntityType.BLOCK_DISPLAY);
        textDisplay = (TextDisplay) location.world.getBukkit().spawnEntity(location.toBukkitCentered(), EntityType.TEXT_DISPLAY);

        Location textLocation = location.toBukkit().add(direction.toVector());
        textLocation.setDirection(direction.toVector());

        textDisplay.setLineWidth(getMaxLineWidth());
        Transformation transformation = textDisplay.getTransformation();
        transformation.getTranslation().z = -0.98f;
        transformation.getTranslation().x = CraftMonitor.TEXT_DISPLAY_HORIZONTAL_OFFSETS[getWidth() - 1];
        transformation.getTranslation().y = -0.1f + CraftMonitor.TEXT_DISPLAY_VERTICAL_OFFSETS[getHeight() - 1];
        transformation.getScale().set(0.75);
        textDisplay.setTransformation(transformation);
        textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);
        textDisplay.setBackgroundColor(Color.fromARGB(0));

        // TODO: Fix this offsetX, offsetZ mess

        // Get right direction
        float modX = 0; float modZ = 0;

        if (direction.getModZ() == -1) {
            modX = 0.5f - 0.08f + -getWidth() / 2f;
            modZ = -0.5f;
        } else if (direction.getModZ() == 1) {
            modX = -0.5f + 0.08f + getWidth() / 2f;
            modZ = 0.5f;
        }

        if (direction.getModX() == 1) {
            modX = 0.5f;
            modZ = 0.5f - 0.08f + -getWidth() / 2f;
        }
        else if (direction.getModX() == -1) {
            modX = -0.5f;
            modZ = -0.5f + 0.08f + getWidth() / 2f;
        }

        float xOffset = 0.5f + modX;
        float zOffset = 0.5f + modZ;

        textLocation.add(xOffset, 0, zOffset);
        textDisplay.teleport(textLocation);


        Transformation blockTransform = blockDisplay.getTransformation();
        blockTransform.getScale().x = getWidth();
        blockTransform.getScale().y = getHeight();
        blockDisplay.setTransformation(blockTransform);


        Transformation screenTransform = screenDisplay.getTransformation();
        screenTransform.getScale().x = getWidth() - 0.16f;
        screenTransform.getScale().z = 0.01f;
        screenTransform.getScale().y = getHeight() - 0.16f;
        screenDisplay.setTransformation(screenTransform);


        xOffset = 0;
        zOffset = 0;
        if (direction.getModZ() == -1) {
            xOffset = 0.5f;
            zOffset = 0.5f;
        }
        else if (direction.getModZ() == 1) {
            xOffset = -0.5f;
            zOffset = -0.5f;
        }

        if (direction.getModX() == -1) {
            xOffset = 0.5f;
            zOffset = -0.5f;
        }
        else if (direction.getModX() == 1) {
            xOffset = -0.5f;
            zOffset = 0.5f;
        }

        Location blockEntityLocation = location.toBukkitCentered().add(xOffset, 0, zOffset);
        blockDisplay.setBlock(Material.LIGHT_GRAY_CONCRETE.createBlockData());
        blockEntityLocation.setDirection(direction.toVector());
        blockDisplay.teleport(blockEntityLocation);

        xOffset = 0;
        zOffset = 0;
        if (direction.getModZ() == -1) {
            xOffset = 0.5f - 0.08f;
            zOffset = -0.5f;
        }
        else if (direction.getModZ() == 1) {
            xOffset = -0.5f + 0.08f;
            zOffset = 0.5f;
        }

        if (direction.getModX() == -1) {
            xOffset = -0.5f;
            zOffset = -0.5f + 0.08f;
        }
        else if (direction.getModX() == 1) {
            xOffset = 0.5f;
            zOffset = 0.5f - 0.08f;
        }
        Location screenLocation = location.toBukkitCentered().add(xOffset, 0.08f, zOffset);
        screenDisplay.setBlock(Material.BLACK_CONCRETE.createBlockData());
        screenLocation.setDirection(direction.toVector());
        screenDisplay.teleport(screenLocation);

        showDefaultText();
    }

    @Override
    public void onModelDestroy() {
        location.world.unregisterMonitor(this);
        blockDisplay.remove();
        screenDisplay.remove();
        textDisplay.remove();
    }

    public void remove() {
        location.world.removeStructure(this);
    }

    public int getMaxLineWidth() {
        return MAX_LINE_WIDTHS[width - 1];
    }

    @Override
    public void tick() {}

    @Override
    public ItemStack getItemDrop() {
        BotsItem drop = switch (size) {
            case SIZE_2x1 -> BotsItem.MONITOR_2x1;
            case SIZE_3x2 -> BotsItem.MONITOR_3x2;
            case SIZE_4x3 -> BotsItem.MONITOR_4x3;
        };
        ItemStack stack = drop.toItemStack();
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(CodeBotsPlugin.inst(), "monitor_id"), PersistentDataType.STRING, id);
        stack.setItemMeta(meta);

        return stack;
    }

    public static CraftMonitor fromBytes(BotsRegion region, BotsChunk chunk, ByteArrayReader reader) {
        int x = ByteArrayReader.toInt(reader.readBytes(1));
        int y = ByteArrayReader.toInt(reader.readBytes(2));
        int z = ByteArrayReader.toInt(reader.readBytes(1));
        BlockLocation location = new BlockLocation(chunk.world, (chunk.position.x << 4) | x, y, (chunk.position.y << 4) | z);

        // Direction
        Direction direction = Direction.toDirection((char) reader.readU1());

        String monitorId = reader.readString();

        // Size
        Size size = Size.values()[reader.readU1()];

        return new CraftMonitor(monitorId, location, direction, size);
    }

    @Override
    public void save(ByteArrayWriter buffer, StringPool constants) {
        // Structure type identifier
        buffer.writeU2(3); // structure id (4 for redstone transmitter)

        int basePointer = buffer.getPointer();

        // Structure size on disk
        buffer.writeU2(0);

        // Location
        buffer.writeBytes(ByteArrayWriter.toBytes(location.x & 0xf, 1));;
        buffer.writeBytes(ByteArrayWriter.toBytes(location.y, 2));
        buffer.writeBytes(ByteArrayWriter.toBytes(location.z, 1));

        // Direction
        buffer.writeU1(direction.name().charAt(0));
        // Monitor ID
        buffer.writeString(id);
        // Size
        buffer.writeU1(size.ordinal());

        int returnPointer = buffer.getPointer();
        buffer.setPointer(basePointer);
        buffer.writeU2(returnPointer - basePointer - 2);
        buffer.setPointer(returnPointer);
    }

}
