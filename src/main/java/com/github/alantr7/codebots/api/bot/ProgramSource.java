package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.plugin.program.ItemFactory;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.File;

@Getter
public class ProgramSource {

    private final String name;

    private final File source;

    private final String[] code;

    public ProgramSource(String name, File source, String[] code) {
        this.name = name;
        this.source = source;
        this.code = code;
    }

    public ItemStack toItem() {
        return ItemFactory.createProgramItem(name, code);
    }

}
