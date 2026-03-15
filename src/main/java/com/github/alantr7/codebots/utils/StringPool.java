package com.github.alantr7.codebots.utils;

import java.util.HashMap;
import java.util.Map;

public class StringPool {

    private final Map<String, Integer> pool = new HashMap<>();
    private final Map<Integer, String> poolByIdx = new HashMap<>();

    private int nextIndex;

    public int pool(String string) {
        if (pool.containsKey(string))
            return pool.get(string);

        pool.put(string, nextIndex);
        poolByIdx.put(nextIndex, string);
        return nextIndex++;
    }

    public String at(int index) {
        return poolByIdx.get(index);
    }

    public int getSize() {
        return pool.size();
    }

}
