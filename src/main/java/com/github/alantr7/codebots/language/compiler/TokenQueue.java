package com.github.alantr7.codebots.language.compiler;

import java.util.Arrays;

public class TokenQueue {

    private final String[][] queue;

    private int row = 0;

    private int col = 0;

    public TokenQueue(String[][] queue) {
        this.queue = queue;
        for (int i = 0; i < queue.length; i++) {
            var line = queue[i];
            System.out.println("Token line #" + (1 + i) + ": " + Arrays.toString(line));
        }
    }

    public String peek() {
        if (isEmpty())
            return null;

        return queue[row][col];
    }

    public String next() {
        var token = queue[row][col];
        advance();

        return token;
    }

    public void rollback() {
        col--;

        if (col < 0) {
            row--;
            col = queue[row].length - 1;
        }
    }

    public void advance() {
        col++;

        if (col >= queue[row].length) {
            row++;
            col = 0;
        }
    }

    public int getLine() {
        return row + 1;
    }

    public boolean isEmpty() {
        return row >= queue.length;
    }

}
