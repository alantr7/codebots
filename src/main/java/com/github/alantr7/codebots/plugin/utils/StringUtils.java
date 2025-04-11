package com.github.alantr7.codebots.plugin.utils;

public class StringUtils {

    public static String[] tokenizeForMonitorText(String text) {
        return text.contains("\\n") ? text.split("(?<=\\\\n)|(?=\\\\n)") : new String[] { text };
    }

}
