package com.github.alantr7.codebots.plugin.gui;

import com.github.alantr7.bukkitplugin.gui.ClickType;
import com.github.alantr7.bukkitplugin.gui.CloseInitiator;
import com.github.alantr7.bukkitplugin.gui.GUI;
import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.bot.CraftBotInventory;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.editor.CodeEditorClient;
import com.github.alantr7.codebots.plugin.program.ItemFactory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BotGUI extends GUI {

    @Getter
    private final CodeBot bot;

    public BotGUI(Player player, CodeBot bot) {
        super(CodeBotsPlugin.inst(), player, false);
        this.bot = bot;

        init();
        var session = CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).getActiveSessionByBot(bot);
        if (session != null && !bot.isActive() && (System.currentTimeMillis() - session.getLastFetched() > 3000)) {
            session.fetch();
        }
    }

    @Override
    protected void init() {
        createInventory(bot.getInventory().getInternal());
        registerInteractionCallback(10, ClickType.LEFT, () -> {
            if (!bot.hasProgram()) {
                getPlayer().sendMessage("§cProgram is not loaded.");
                return;
            }

            var session = CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).getActiveSessionByBot(bot);
            if (session != null && session.isCurrentlyFetching()) {
                getPlayer().sendMessage("§cProgram download is in progress. Please try again.");
                return;
            }

            if (session != null && System.currentTimeMillis() - session.getLastFetched() > 3000) {
                session.fetch().whenComplete((sess, err) -> {
                    Bukkit.getScheduler().runTaskLater(CodeBotsPlugin.inst(), () -> bot.setActive(!bot.isActive()), 1L);
                });
            }
        });

        registerInteractionCallback(12, ClickType.LEFT, () -> {
            var programs = new BotProgramsGUI(getPlayer(), bot);
            var player = getPlayer();
            programs.registerEventCallback(Action.CLOSE, () -> new BotGUI(player, bot).open());
            programs.open();
        });

        registerInteractionCallback(12, ClickType.RIGHT, () -> {
            if (!hasClickEvent() || !getClickEvent().isShiftClick())
                return;

            var session = CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).getActiveSessionByBot(bot);
            if (session == null)
                return;

            CodeBotsPlugin.inst().getSingleton(CodeEditorClient.class).deleteSession(session);
            ((CraftCodeBot) bot).getInventory().updateProgramButton();

            getPlayer().sendMessage("§eCode editor session closed.");
        });

        registerInteractionCallback(14, ClickType.LEFT, () -> {
            if (bot.isActive()) {
                getPlayer().sendMessage("§cYou can not manually move the bot while it's running a program.");
                return;
            }
            new BotControllerGUI(getPlayer(), bot).open();
        });

        registerInteractionCallback(16, ClickType.LEFT, () -> {
            var botItem = ItemFactory.createBotItem("§eCodeBot", bot);
            if (getPlayer().getInventory().getItemInMainHand().getType().isAir()) {
                getPlayer().getInventory().setItemInMainHand(botItem);
            } else {
                getPlayer().getInventory().addItem(botItem);
            }

            bot.remove();
        });

        ((CraftCodeBot) bot).getInventory().updateProgramButton();
    }

    @Override
    protected void fill(Inventory inventory) {
        ((CraftBotInventory) bot.getInventory()).updateProgramButton();
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
