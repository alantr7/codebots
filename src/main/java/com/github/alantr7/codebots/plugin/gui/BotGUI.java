package com.github.alantr7.codebots.plugin.gui;

import com.github.alantr7.bukkitplugin.gui.ClickType;
import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GUI;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BotGUI extends GUI {

    private final CodeBot bot;

    public BotGUI(Player player, CodeBot bot) {
        super(CodeBotsPlugin.inst(), player, false);
        this.bot = bot;

        init();
    }

    @Override
    protected void init() {
        createInventory(bot.getInventory().getInternal());
        registerInteractionCallback(11, ClickType.LEFT, () -> {
            if (bot.isActive()) {
                bot.setActive(false);
            } else {
                bot.setActive(true);
            }
        });

        registerInteractionCallback(13, ClickType.LEFT, () -> {
            new BotProgramsGUI(getPlayer(), bot).open();
        });
    }

    @Override
    protected void fill(Inventory inventory) {
    }

    @Override
    protected void onInventoryOpen() {

    }

    @Override
    protected void onInventoryClose(CloseInitiator closeInitiator) {

    }

    @Override
    public void onItemInteract(int i, @NotNull ClickType clickType, @Nullable ItemStack itemStack) {
        if (!((i >= 37 && i <= 44) || (i >= 54 && !getClickEvent().isShiftClick()))) {
            setCancelled(true);
        }
    }

}
