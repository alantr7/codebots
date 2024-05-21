package com.github.alantr7.codebots.plugin.utils;

import java.io.File;

public class FileHelper {

    public static void deleteDirectory(File directory) {
        var files = directory.listFiles();
        if (files != null) {
            for (var file : files) {
                deleteDirectory(file);
            }
        }

        directory.delete();
    }

}
