package com.github.alantr7.codebots.plugin.bot;

import com.github.alantr7.codebots.api.bot.Memory;
import com.github.alantr7.codebots.language.runtime.DataType;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ExecutionException;
import com.github.alantr7.codebots.plugin.config.Config;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CraftMemory implements Memory {

    private final Map<String, Map.Entry<DataType<Object>, Object>> values = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> void save(String key, DataType<T> type, T value) throws ExecutionException {
        if (type == DataType.ANY || type == DataType.NULL || type == DataType.MODULE)
            throw new RuntimeException("Attempted to save a non-allowed data type: " + type.name());

        if (values.size() == Config.BOT_MAX_MEMORY_ENTRIES)
            throw new ExecutionException("Cannot save any more entries to the memory due to the memory size limit!");

        values.put(key, new AbstractMap.SimpleEntry<>((DataType<Object>) type, value));
    }

    @Override
    public <T> T load(String key, DataType<T> type) {
        return load(key, type, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T load(String key, DataType<T> type, T def) {
        var value = values.get(key);
        return value != null && type.isCompatibleWith(value.getKey()) ? (T) value.getValue() : def;
    }

    @Override
    public boolean contains(String key) {
        return values.containsKey(key);
    }

    public Map<String, Map.Entry<DataType<Object>, Object>> map() {
        return values;
    }

    public Set<Map.Entry<String, Map.Entry<DataType<Object>, Object>>> entries() {
        return values.entrySet();
    }

}
