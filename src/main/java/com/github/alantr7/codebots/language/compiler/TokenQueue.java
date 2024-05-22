package com.github.alantr7.codebots.language.compiler;

import java.util.Arrays;

public class TokenQueue {

    private final String[][] queue;

    private final Integer[] lines;

    private int row = 0;

    private int col = 0;

    public TokenQueue(String[][] queue, Integer[] lines) {
        this.queue = queue;
        this.lines = lines;

        if (queue[0].length == 0)
            advance();
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

        if (!isEmpty() && queue[row].length == 0)
            advance();
    }

    public int getLine() {
        return lines[row];
    }

    public boolean isEmpty() {
        return row >= queue.length;
    }

}
