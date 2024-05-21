package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.bot.BotInventory;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraftBotInventory implements BotInventory {

    private final CodeBot bot;

    private final Inventory inventory;

    private final ItemStack CONTROL_START = ItemFactory.createItem(Material.LIME_CONCRETE, "§aStart Program");

    private final ItemStack CONTROL_STOP = ItemFactory.createItem(Material.RED_CONCRETE, "§cStop Program");

    private final ItemStack PROGRAM_INFO = new ItemStack(Material.PAPER);

    private final ItemStack INVENTORY_SLOT_NOT_SELECTED = ItemFactory.createItem(Material.BLACK_STAINED_GLASS_PANE, "§7");

    private final ItemStack INVENTORY_SLOT_SELECTED = ItemFactory.createItem(Material.LIME_STAINED_GLASS_PANE, "§7Item below is selected.");

    public CraftBotInventory(CodeBot bot) {
        this.bot = bot;
        this.inventory = Bukkit.createInventory(null, 54, "Manage Bot");

        // Create filler items
        var filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName("§7");
        filler.setItemMeta(fillerMeta);

        // Filler rows (top, middle, bottom)
        for (int i = 1; i < 8; i++) {
            inventory.setItem(i, filler);
            inventory.setItem(i + 18, filler);
            inventory.setItem(i + 45, filler);
        }

        // Filler columns (left and right)
        for (int i = 0; i < 6; i++) {
            inventory.setItem(i * 9, filler);
            inventory.setItem(i * 9 + 8, filler);
        }

        var startMeta = CONTROL_START.getItemMeta();
        startMeta.setDisplayName("");

        updateControlButton();
        updateProgramButton();
        updateSelectedSlotHighlights();
    }

    @Override
    public @NotNull Inventory getInternal() {
        return inventory;
    }

    @Override
    public ItemStack[] getItems() {
        var array = new ItemStack[7];
        for (int i = 0; i < 7; i++) {
            array[i] = inventory.getItem(i + 37);
        }

        return array;
    }

    @Override
    public @Nullable ItemStack getItem(int slot) {
        return inventory.getItem(slot + 37);
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot < 0 || slot >= 7) throw new IllegalArgumentException("Slot must be between 0 and 6");
        inventory.setItem(slot + 37, item);
    }

    @Override
    public void addItem(ItemStack stack) {
        for (int i = 0; i < 7; i++) {
            var current = inventory.getItem(i + 37);
            if (current == null) {
                inventory.setItem(i + 37, stack);
                return;
            } else if (current.isSimilar(stack)) {
                int max = current.getMaxStackSize();
                int amount = current.getAmount() + stack.getAmount();
                if (amount <= max) {
                    current.setAmount(amount);
                    return;
                } else {
                    current.setAmount(max);
                    stack.setAmount(amount - max);
                }
            }
        }
    }

    @Override
    public void addItem(ItemStack[] items) {
        for (var item : items)
            addItem(item);
    }

    @Override
    public boolean isFull() {
        for (int i = 0; i < 7; i++) {
            if (inventory.getItem(i + 37) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canAccept(ItemStack stack) {
        return false;
    }

    public void updateControlButton() {
        if (!bot.isActive()) {
            inventory.setItem(11, CONTROL_START);
        } else {
            inventory.setItem(11, CONTROL_STOP);
        }
    }

    public void updateProgramButton() {
        var meta = PROGRAM_INFO.getItemMeta();
        if (bot.getProgram() != null && bot.getProgram().getMainModule() != null) {
            meta.setDisplayName("§eProgram is ready");
            meta.setLore(List.of(
                    "§7Click to change program",
                    "",
                    "§fFile: §e" + bot.getProgramSource().getName(),
                    "§fLocation: §e" + bot.getProgramSource().getDirectory().name(),
                    ""
            ));
            meta.setDisplayName("§fProgram: §e" + ((FileModule) bot.getProgram().getMainModule()).getFile().getName());
        } else {
            meta.setDisplayName("§cProgram not loaded");
            meta.setLore(List.of(
                    "§7Click to browse programs"
            ));
        }
        PROGRAM_INFO.setItemMeta(meta);

        inventory.setItem(13, PROGRAM_INFO);
    }

    public void updateSelectedSlotHighlights() {
        for (int i = 0; i < 7; i++) {
            if (bot.getSelectedSlot() == i) {
                inventory.setItem(28 + i, INVENTORY_SLOT_SELECTED);
            } else {
                inventory.setItem(28 + i, INVENTORY_SLOT_NOT_SELECTED);
            }
        }
    }

}
