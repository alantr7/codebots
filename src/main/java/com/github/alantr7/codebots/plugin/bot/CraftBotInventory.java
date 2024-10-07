package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.bot.BotInventory;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.error.ProgramError;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CraftBotInventory implements BotInventory {

    private final CodeBot bot;

    private final Inventory inventory;

    private final ItemStack CONTROL_START = ItemFactory.createItem(Material.LIME_CONCRETE, "§aStart Program");

    private final ItemStack CONTROL_STOP = ItemFactory.createItem(Material.RED_CONCRETE, "§cStop Program");

    private final ItemStack PROGRAM_INFO = new ItemStack(Material.PAPER);

    private final ItemStack MANUAL_MOVEMENT = ItemFactory.createItem(Material.COMPASS, meta -> {
        meta.setDisplayName("§fManual Movement");
        meta.setLore(Collections.singletonList("§7Click to manually move the bot"));
    });

    private final ItemStack PICKUP_BOT = ItemFactory.createItem(Material.DISPENSER, meta -> {
        meta.setDisplayName("§fPick Up");
        meta.setLore(Collections.singletonList("§7Click to pick up the bot"));
    });

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
        inventory.setItem(14, MANUAL_MOVEMENT);
        inventory.setItem(16, PICKUP_BOT);
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
        var item = bot.isActive() ? CONTROL_STOP : CONTROL_START.clone();
        if (!bot.isActive() && bot.hasError() && bot.getError().getLocation() == ProgramError.ErrorLocation.EXECUTION) {
            var meta = item.getItemMeta();
            var lore = new LinkedList<String>();
            var error = bot.getError();

            lore.add("§cProgram has crashed!");
            lore.add("§cError: §4" + error.getMessage());
            lore.add("§cStack trace:");
            lore.addAll(Arrays.stream(error.getStackTrace()).map(s -> " §4" + s).toList());

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        inventory.setItem(10, item);
    }

    public void updateProgramButton() {
        var meta = PROGRAM_INFO.getItemMeta();
        var lore = new LinkedList<String>();

        if (bot.getProgramSource() != null) {
            meta.setDisplayName("§eProgram is ready");
            lore.addAll(List.of(
                    "§7Click to change program",
                    "",
                    "§fFile: §e" + bot.getProgramSource().getName(),
                    "§fLocation: §e" + bot.getProgramSource().getDirectory().name()
            ));
            meta.setDisplayName("§fProgram: §e" + bot.getProgramSource().getName());
        } else {
            meta.setDisplayName("§cProgram not loaded");
            lore.addAll(List.of(
                    "§7Click to browse programs"
            ));
        }

        var editorSession = CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).getActiveSessionByBot(bot);
        if (editorSession != null) {
            lore.add("");
            lore.add("§eEditor session is active!");
            lore.add("§eShift-right-click to close the editor");
        }

        if (bot.hasError() && bot.getError().getLocation() == ProgramError.ErrorLocation.PARSER) {
            lore.addAll(List.of(
                    "",
                    "§cCould not parse program!"
            ));
            lore.addAll(List.of(bot.getError().getStackTrace()));
        }

        meta.setLore(lore);
        PROGRAM_INFO.setItemMeta(meta);

        inventory.setItem(12, PROGRAM_INFO);
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
