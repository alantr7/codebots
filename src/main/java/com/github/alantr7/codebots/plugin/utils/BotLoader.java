package com.github.alantr7.codebots.plugin.utils;

import com.github.alantr7.codebots.language.runtime.DataType;
import com.github.alantr7.codebots.language.runtime.Dictionary;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.bot.CraftMemory;
import net.querz.nbt.tag.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BotLoader {

    public static CraftMemory loadMemory(CompoundTag tag) {
        var memory = new CraftMemory();
        tag.forEach((key, value) -> {
            switch (value.getID()) {
                // Boolean
                case 1 -> memory.save(key, DataType.BOOLEAN, ((NumberTag<?>) value).asByte() == 1);

                // Int
                case 3 -> memory.save(key, DataType.INT, ((IntTag) value).asInt());

                // Float
                case 5 -> memory.save(key, DataType.FLOAT, ((FloatTag) value).asFloat());

                // String
                case 8 -> memory.save(key, DataType.STRING, ((StringTag) value).getValue());

                // Compound
                // TODO: Optimize compound loading
                case 10 -> {
                    var map = loadMemory((CompoundTag) value).map();
                    var dict = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue()));

                    var dictionary = new Dictionary();
                    dictionary.putAll(dict);

                    memory.save(key, DataType.DICTIONARY, dictionary);
                }
            }
        });

        return memory;
    }

    public static CompoundTag saveMemory(CraftMemory memory) {
        return saveMemory(memory.entries());
    }

    private static CompoundTag saveMemory(Set<Map.Entry<String, Map.Entry<DataType<Object>, Object>>> entries) {
        var tag = new CompoundTag();
        for (var entry : entries) {
            var key = entry.getKey();
            var type = (DataType<?>) entry.getValue().getKey();
            var value = entry.getValue().getValue();

            switch (type.name()) {
                case "BOOLEAN" -> tag.putBoolean(key, (Boolean) value);
                case "INT" -> tag.putInt(key, (Integer) value);
                case "FLOAT" -> tag.putFloat(key, (Float) value);
                case "STRING" -> tag.putString(key, (String) value);
                case "DICTIONARY" -> {
                    // TODO: Optimize dictionary saving
                    Map<String, Map.Entry<DataType<Object>, Object>> values = new HashMap<>();
                    ((Dictionary) value).forEach((key1, value1) -> values.put(key1, new AbstractMap.SimpleEntry<>((DataType<Object>) DataType.of(value1), value1)));

                    tag.put(key, saveMemory(values.entrySet()));
                }
            }
        }

        return tag;
    }

    @FunctionalInterface
    private interface DataSerializer {

        void serialize(CompoundTag tag, String key, Object value);

    }

}
