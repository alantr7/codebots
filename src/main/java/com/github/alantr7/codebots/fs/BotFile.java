package com.github.alantr7.codebots.fs;

import lombok.Getter;

import java.io.File;

public class BotFile {

    @Getter
    long position = -1;

    @Getter
    private final String name;

    @Getter
    private byte[] content;

    @Getter
    private long lastModified;

    @Getter
    private boolean isUnsaved = true;

    public BotFile(String name, byte[] content, long lastModified) {
        this.name = name;
        this.content = content;
        this.lastModified = lastModified;
    }

    public BotFile(File file) {
        this.name = file.getName();
    }

    public void setContent(byte[] content) {
        this.content = content;
        isUnsaved = true;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
        isUnsaved = true;
    }

    public void delete() {

    }

}
