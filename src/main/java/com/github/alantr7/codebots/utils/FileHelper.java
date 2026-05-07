package com.github.alantr7.codebots.utils;

import com.github.alantr7.codebots.CodeBotsPlugin;
import com.google.common.io.Files;

import java.io.File;
import java.io.InputStream;

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

    public static byte[] loadResource(String resourcePath) {
        try (InputStream is = CodeBotsPlugin.inst().getResource(resourcePath)) {
            if (is == null)
                return new byte[0];

            return is.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static void saveResource(String resourcePath, String destination) {
        saveResource(resourcePath, new File(destination));
    }

    public static void saveResource(String resourcePath, File destination) {
        try (var resource = CodeBotsPlugin.inst().getResource(resourcePath)) {
            if (resource == null)
                return;

            var bytes = resource.readAllBytes();
            Files.write(bytes, destination);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
