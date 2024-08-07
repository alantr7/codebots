package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.BotRegistry;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class MineFunction extends RuntimeNativeFunction {

    public MineFunction(Program program) {
        super(program, "mine", null);
    }

    @Override
    public boolean hasNext(BlockContext context) {
        return !context.getFlag(BlockContext.FLAG_COMPLETED);
    }

    @Override
    public void next(BlockContext context) {
        if (!Config.BOT_ALLOW_BLOCK_BREAKING) {
            environment.interrupt(new ExecutionException("Block breaking is disabled"));
            return;
        }

        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        int progress;

        if (context.getLineIndex() == 0) {
            progress = 0;
        }

        else {
            progress = (int) context.getExtra("progress");
        }

        if (progress == 6) {
            context.setFlag(BlockContext.FLAG_COMPLETED, true);
            context.advance();

            var blockData = bot.getLocation().add(0, -1, 0).getBlock().getBlockData();
            bot.getLocation().getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    bot.getLocation().getBlock().getLocation().add(.5, -.5, .5),
                    12,  0, 0, 0,
                    blockData
            );
            bot.getLocation().getWorld().playSound(bot.getLocation().add(0, -1, 0), blockData.getSoundGroup().getBreakSound(), 1, 1);
            bot.getInventory().addItem(bot.getLocation().add(0, -1, 0).getBlock().getDrops().toArray(new ItemStack[0]));
            bot.getLocation().add(0, -1, 0).getBlock().setType(Material.AIR);

            for (var player : Bukkit.getOnlinePlayers()) {
                player.sendBlockDamage(bot.getLocation().add(0, -1, 0), 0, bot.getEntity());
            }

            CodeBotsPlugin.inst().getSingleton(DataLoader.class).saveInventory(bot);
            return;
        }

        if (context.getLineIndex() % 5 == 0) {
            float damage = progress / 5f;

            for (var player : Bukkit.getOnlinePlayers()) {
                player.sendBlockDamage(bot.getLocation().add(0, -1, 0), damage, bot.getEntity());
            }

            context.setExtra("progress", progress + 1);
        }

        context.advance();
        environment.setHalted(true);
    }

}
