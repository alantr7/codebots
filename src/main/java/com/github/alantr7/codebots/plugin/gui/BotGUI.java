package com.github.alantr7.codebots.plugin.gui;

import com.github.alantr7.bukkitplugin.gui.ClickType;
import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GUI;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BotGUI extends GUI {

    @Getter
    private final CodeBot bot;

    public BotGUI(Player player, CodeBot bot) {
        super(CodeBotsPlugin.inst(), player, false);
        this.bot = bot;

        init();
    }

    @Override
    protected void init() {
        createInventory(bot.getInventory().getInternal());
        registerInteractionCallback(10, ClickType.LEFT, () -> {
            bot.setActive(!bot.isActive());
        });

        registerInteractionCallback(12, ClickType.LEFT, () -> {
            var programs = new BotProgramsGUI(getPlayer(), bot);
            var player = getPlayer();
            programs.registerEventCallback(Action.CLOSE, () -> new BotGUI(player, bot).open());
            programs.open();
        });

        registerInteractionCallback(14, ClickType.LEFT, () -> {
            if (bot.isActive()) {
                getPlayer().sendMessage("§cYou can not manually move the bot while it's running a program.");
                return;
            }
            new BotControllerGUI(getPlayer(), bot).open();
        });

        registerInteractionCallback(16, ClickType.LEFT, () -> {
            var botItem = ItemFactory.createBotItem("§7Bot", bot);
            if (getPlayer().getInventory().getItemInMainHand().getType().isAir()) {
                getPlayer().getInventory().setItemInMainHand(botItem);
            } else {
                getPlayer().getInventory().addItem(botItem);
            }

            bot.remove();
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
