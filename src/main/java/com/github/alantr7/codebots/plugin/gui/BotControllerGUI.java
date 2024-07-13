package com.github.alantr7.codebots.plugin.gui;

import com.github.alantr7.bukkitplugin.gui.ClickType;
import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GUI;
import com.github.alantr7.codebots.api.CodeBots;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.api.bot.Directory;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.ProgramRegistry;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class BotControllerGUI extends GUI {

    @Getter
    private final CodeBot bot;

    private static final int CATEGORY_SHARED = 0;

    private static final int CATEGORY_LOCAL = 1;

    private int selectedCategory;

    public BotControllerGUI(Player player, CodeBot bot) {
        super(CodeBotsPlugin.inst(), player, false);
        this.bot = bot;

        init();
    }

    @Override
    protected void init() {
        createInventory("Manage Bot > Controller", 45);
        setInteractionEnabled(false);
    }

    @Override
    protected void fill(Inventory inventory) {

        // Create filler items
        var filler = ItemFactory.createItem(Material.GRAY_STAINED_GLASS_PANE, "§7");
        for (int i = 0; i < 45; i++)
            setItem(i, filler.clone());

        // Move forward
        setItem(11, createHorizontalMovementButton("§eMove Forward", "§7Move the bot in the direction", "§7it's facing"));
        registerInteractionCallback(11, ClickType.LEFT, jog(() -> bot.move(bot.getDirection(), true)));

        // Move backwards
        setItem(29, createHorizontalMovementButton("§eMove Backwards", "§7Move the bot in the opposite", "§7direction of which it's facing"));
        registerInteractionCallback(29, ClickType.LEFT, jog(() -> bot.move(bot.getDirection().getRight().getRight(), true)));

        // Rotate right
        setItem(21, createVerticalMovementAndRotationButton("§6Turn Right", "§7Rotate the bot to it's right"));
        registerInteractionCallback(21, ClickType.LEFT, jog(() -> bot.setDirection(bot.getDirection().getRight(), true)));

        // Rotate left
        setItem(19, createVerticalMovementAndRotationButton("§6Turn Left", "§7Rotate the bot to it's left"));
        registerInteractionCallback(19, ClickType.LEFT, jog(() -> bot.setDirection(bot.getDirection().getLeft(), true)));



        // Move up
        setItem(15, createVerticalMovementAndRotationButton("§6Move Up", "§7Move the bot up"));
        registerInteractionCallback(15, ClickType.LEFT, jog(() -> bot.move(Direction.UP, true)));

        // Move down
        setItem(33, createVerticalMovementAndRotationButton("§6Move Down", "§7Move the bot down"));
        registerInteractionCallback(33, ClickType.LEFT, jog(() -> bot.move(Direction.DOWN, true)));

    }

    private Runnable jog(Runnable r) {
        return () -> {
            if (bot.isActive() || bot.isMoving()) {
                getPlayer().sendMessage("§cThis bot either has an active program, or it's already moving.");
                return;
            }

            r.run();
        };
    }

    @Override
    protected void onInventoryOpen() {

    }

    @Override
    protected void onInventoryClose(CloseInitiator closeInitiator) {

    }

    @Override
    public void onItemInteract(int i, @NotNull ClickType clickType, @Nullable ItemStack itemStack) {

    }

    private ItemStack createHorizontalMovementButton(String name, String... lore) {
        return ItemFactory.createItem(Material.YELLOW_CONCRETE, meta -> {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
        });
    }

    private ItemStack createVerticalMovementAndRotationButton(String name, String... lore) {
        return ItemFactory.createItem(Material.ORANGE_CONCRETE, meta -> {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
        });
    }

}
