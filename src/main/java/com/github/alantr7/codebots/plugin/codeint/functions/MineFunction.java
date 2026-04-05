package com.github.alantr7.codebots.plugin.codeint.functions;

import com.github.alantr7.codebots.api.bot.CodeBot;
import com.github.alantr7.codebots.api.bot.Direction;
import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.plugin.CodeBotsPlugin;
import com.github.alantr7.codebots.plugin.config.Config;
import com.github.alantr7.codebots.plugin.data.DataLoader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class MineFunction extends ExternalFunction {

    private static final byte MEMORY_MINE_PROGRESS = 0;

    public MineFunction(Module module) {
        super(module, "mine", DataType.INT, DataType.STRING);
    }

    @Override
    public void prepareContext(Context context) {
        context.getMemory()[MEMORY_MINE_PROGRESS] = Data.of(0);
    }

    @Override
    public Data handle(Context context) throws ExecutionException {
        if (!Config.BOT_ALLOW_BLOCK_BREAKING) {
            throw new ExecutionException("Block breaking is disabled");
        }

        var bot = (CodeBot) context.getProgram().getExtra("bot");
        var arg = context.getArgumentAs(0, DataType.STRING);
        var direction = (arg.equals("forward")
          ? bot.getDirection()
          : arg.equals("back")
          ? bot.getDirection().getRight().getRight()
          : Direction.toDirection(arg));

        if (direction == null) {
            throw new ExecutionException(arg + " is not a valid direction");
        }

        int progress = context.getMemory()[MEMORY_MINE_PROGRESS].getValueAs(DataType.INT);

        var blockLocation = bot.getLocation().add(direction.toVector());
        if (blockLocation.getBlock().getType().isAir()) {
            return Data.of(1);
        }

        if (progress / 5 == 6) {
            var blockData = bot.getLocation().add(direction.toVector()).getBlock().getBlockData();
            bot.getLocation().getWorld().spawnParticle(
              Particle.BLOCK_CRACK,
              blockLocation.clone().add(0, .5, 0),
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
            return Data.of(1);
        }

        if (progress % 5 == 0) {
            float damage = (progress / 5f) / 6f;
            for (var player : Bukkit.getOnlinePlayers()) {
                player.sendBlockDamage(blockLocation, damage, bot.getEntity());
            }
        }

        context.getMemory()[MEMORY_MINE_PROGRESS].updateValue(DataType.INT, v -> v + 1);
        context.setRecall(true);
        return null;
    }

}
