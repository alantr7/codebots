package com.github.alantr7.codebots.plugin.utils;

public class StringUtils {

    public static String[] wrapText(String text, int maxLines, int maxCharsPerLine) {
        String[] lines = new String[maxLines];
        String[] split = text.contains("\\n") ? text.split("\\\\n") : new String[] { text };
        int currentLine = 0;

        // Wrap each line if it exceeds maxCharsPerLine
        for (String line : split) {
            if (line.length() <= maxCharsPerLine) {
                lines[currentLine++] = line.trim();
                if (currentLine == maxLines)
                    return lines;

                continue;
            }

            while (line.length() > maxCharsPerLine) {
                String newLine = line.substring(0, maxCharsPerLine);
                line = line.substring(maxCharsPerLine);

                lines[currentLine++] = newLine.trim();
                if (currentLine == maxLines)
                    return lines;
            }

            if (currentLine >= maxLines)
                return lines;

            lines[currentLine++] = line.trim();
        }

        return lines;
    }

}
