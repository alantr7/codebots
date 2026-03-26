package com.github.alantr7.codebots.plugin.bot;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

public class BotFile {

    @Getter
    private final String name;

    @Getter @Setter
    private byte[] content;

    public BotFile(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public BotFile(File file) {
        this.name = file.getName();
    }

    public void delete() {

    }

}
