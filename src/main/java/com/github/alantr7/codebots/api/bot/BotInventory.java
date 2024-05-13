package com.github.alantr7.codebots.api.bot;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BotInventory {

    @NotNull Inventory getInternal();

    ItemStack[] getItems();

    @Nullable ItemStack getItem(int slot);

    void setItem(int slot, ItemStack item);

    /**
     * Add an item to the inventory if there's enough space, otherwise void it
     * @param stack item to add
     */
    void addItem(ItemStack stack);

    /**
     * Add multiple items to the inventory if there's enough space, otherwise void them
     * @param items items to add
     */
    void addItem(ItemStack[] items);

    boolean isFull();

    boolean canAccept(ItemStack stack);

}
