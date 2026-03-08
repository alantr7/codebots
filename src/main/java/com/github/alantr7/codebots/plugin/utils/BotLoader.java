package com.github.alantr7.codebots.plugin.utils;

import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.plugin.bot.CraftCodeBot;
import com.github.alantr7.codebots.plugin.bot.CraftMemory;
import net.querz.nbt.tag.*;

import java.util.*;

public class BotLoader {

    public static CraftMemory loadMemory(CompoundTag tag) {
        var memory = new CraftMemory();
        tag.forEach((key, value) -> {
            try {
                switch (value.getID()) {
                    // Int
                    case 3 -> memory.save(key, DataType.INT, ((IntTag) value).asInt());

                    // Float
                    case 5 -> memory.save(key, DataType.FLOAT, ((FloatTag) value).asFloat());

                    // String
                    case 8 -> memory.save(key, DataType.STRING, ((StringTag) value).getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
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

            switch (type.getTypeName()) {
                case "INT" -> tag.putInt(key, (Integer) value);
                case "FLOAT" -> tag.putFloat(key, (Float) value);
                case "STRING" -> tag.putString(key, (String) value);
            }
        }

        return tag;
    }

    @FunctionalInterface
    private interface DataSerializer {

        void serialize(CompoundTag tag, String key, Object value);

    }

}
