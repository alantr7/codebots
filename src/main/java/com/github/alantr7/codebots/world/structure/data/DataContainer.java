package com.github.alantr7.codebots.world.structure.data;

import com.github.alantr7.bytils.buffer.ByteArrayReader;
import com.github.alantr7.bytils.buffer.ByteArrayWriter;
import com.github.alantr7.codebots.utils.StringPool;
import com.github.alantr7.codebots.world.structure.StructureInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class DataContainer {

    public StructureInstance structure;

    @Getter
    @Setter
    boolean isUnsaved;

    @Getter
    private final Map<String, Data<Object>> entries = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Data<T> persist(String name, Data.Type<T> type) {
        return persist(name, type, null);
    }

    @SuppressWarnings("unchecked")
    public <T> Data<T> persist(String name, Data.Type<T> type, T defaultValue) {
        Data data = entries.computeIfAbsent(name, v -> {
            Data d = new Data(this, type);
            d.value = defaultValue;

            return d;
        });

        if (data.type != type)
            throw new RuntimeException("Conflicting data types!");

        return data;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String name, Data.Type<T> type, T defaultValue) {
        Data data = entries.get(name);
        if (data == null || data.type != type)
            return defaultValue;

        return (T) data.value;
    }

    public byte[] toBytes(StringPool keys) {
        ByteArrayWriter buffer = new ByteArrayWriter();
        buffer.writeU1(entries.size());

        entries.forEach((key, entry) -> {
            buffer.writeU1(keys.pool(key));
            buffer.writeU1(entry.type.id);
            switch (entry.type.id) {
                // BYTE
                case 0 -> buffer.writeBytes(new byte[] { (byte) entry.value });
                // INT
                case 1 -> buffer.writeBytes(ByteArrayWriter.toBytes((int) entry.value, 4));
                // FLOAT
                case 2 -> buffer.writeBytes(ByteArrayWriter.toBytes(Float.floatToIntBits((float) entry.value), 4));
                // STRING
                case 3 -> buffer.writeString((String) entry.value);
                // BYTE[]
                case 4 -> {
                    buffer.writeU2(((byte[]) entry.value).length);
                    buffer.writeBytes((byte[]) entry.value);
                }
                // UUID
                case 5 -> {
                    if (entry.value != null) {
                        buffer.writeLong(((UUID) entry.value).getMostSignificantBits());
                        buffer.writeLong(((UUID) entry.value).getLeastSignificantBits());
                    } else {
                        buffer.writeLong(0);
                        buffer.writeLong(0);
                    }
                }
            }
        });

        return buffer.getBuffer();
    }

    public static DataContainer fromBytes(ByteArrayReader buffer, StringPool keys) {
        int entriesCount = buffer.readU1();
        DataContainer container = new DataContainer();

        for (int i = 0; i < entriesCount; i++) {
            String key = keys.at(buffer.readU1());
            int typeId = buffer.readU1();
            Data.Type type;
            Object value;

            switch (typeId) {
                // BYTE
                case 0 -> {
                    type = Data.Type.BYTE;
                    value = buffer.readBytes(1)[0];
                }
                // INT
                case 1 -> {
                    type = Data.Type.INT;
                    value = ByteArrayReader.toInt(buffer.readBytes(4));
                }
                // FLOAT
                case 2 -> {
                    type = Data.Type.FLOAT;
                    value = Float.intBitsToFloat(ByteArrayReader.toInt(buffer.readBytes(4)));
                }
                // STRING
                case 3 -> {
                    type = Data.Type.STRING;
                    value = buffer.readString();
                }
                // BYTE[]
                case 4 -> {
                    type = Data.Type.BYTE_ARRAY;
                    value = buffer.readBytes(ByteArrayReader.toInt(buffer.readBytes(2)));
                }
                // UUID
                case 5 -> {
                    type = Data.Type.UUID;
                    long mostSignificant = buffer.readLong();
                    long leastSignificant = buffer.readLong();
                    if (mostSignificant != 0 || leastSignificant != 0) {
                        value = new UUID(mostSignificant, leastSignificant);
                    } else {
                        value = null;
                    }
                }
                default -> {
                    type = null;
                    value = null;
                }
            }

            if (type != null) {
                Data data = new Data(container, type);
                data.value = value;
                container.entries.put(key, data);
            }
        }

        return container;
    }

    public static void overwrite(DataContainer original, DataContainer data, Collection<String> whitelist) {
        for (String key : whitelist) {
            Data<Object> newValue = data.entries.get(key);
            Data<Object> oldValue = original.entries.get(key);

            if (newValue == null || oldValue == null)
                continue;

            oldValue.value = newValue.value;
        }
    }

}
