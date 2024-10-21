package com.github.alantr7.codebots.api.bot;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Material;

@Accessors(fluent = true, chain = true)
@Getter @Setter
public class BotBuilder {

    private String name;

    private Material model;

    private ProgramSource program;

}
