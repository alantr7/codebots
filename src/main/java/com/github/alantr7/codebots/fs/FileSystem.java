package com.github.alantr7.codebots.fs;

import java.util.Collection;

public interface FileSystem {

    BotFile createFile(String name);

    BotFile getFile(String name);

    void deleteFile(String name);

    Collection<BotFile> getFiles();

}
