package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.config.Config;
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

        var call = environment.getCallStack().getLast();
        if (call.getArguments().length != 1) {
            environment.interrupt(new ExecutionException("Expected 1 argument, but received " + call.getArguments().length));
            return;
        }

        if (!(call.getArguments()[0] instanceof String)) {
            environment.interrupt(new ExecutionException("Expected a string argument"));
            return;
        }

        var bot = (CodeBot) environment.getProgram().getExtra("bot");
        var arg = call.getArguments()[0];
        var direction = (arg.equals("forward")
                ? bot.getDirection()
                : arg.equals("back")
                ? bot.getDirection().getRight().getRight()
                : Direction.toDirection((String) call.getArguments()[0]));

        if (direction == null) {
            environment.interrupt(new ExecutionException(arg + " is not a valid direction"));
            return;
        }

        int progress;

        if (context.getLineIndex() == 0) {
            progress = 0;
        }

        else {
            progress = (int) context.getExtra("progress");
        }

        var blockLocation = bot.getLocation().add(direction.toVector());
        if (blockLocation.getBlock().getType().isAir()) {
            context.setFlag(BlockContext.FLAG_COMPLETED, true);
            context.advance();

            return;
        }

        if (progress == 6) {
            context.setFlag(BlockContext.FLAG_COMPLETED, true);
            context.advance();

            var blockData = bot.getLocation().add(direction.toVector()).getBlock().getBlockData();
            bot.getLocation().getWorld().spawnParticle(
                    Particle.BLOCK_CRACK,
                    blockLocation.add(.5, .5, .5),
                    12,  0, 0, 0,
                    blockData
            );
            bot.getLocation().getWorld().playSound(blockLocation, blockData.getSoundGroup().getBreakSound(), 1, 1);
            bot.getInventory().addItem(blockLocation.getBlock().getDrops().toArray(new ItemStack[0]));
            blockLocation.getBlock().setType(Material.AIR);

            for (var player : Bukkit.getOnlinePlayers()) {
                player.sendBlockDamage(blockLocation, 0, bot.getEntity());
            }

            CodeBotsPlugin.inst().getSingleton(DataLoader.class).saveInventory(bot);
            return;
        }

        if (context.getLineIndex() % 5 == 0) {
            float damage = progress / 5f;

            for (var player : Bukkit.getOnlinePlayers()) {
                player.sendBlockDamage(blockLocation, damage, bot.getEntity());
            }

            context.setExtra("progress", progress + 1);
        }

        context.advance();
        environment.setHalted(true);
    }

}
