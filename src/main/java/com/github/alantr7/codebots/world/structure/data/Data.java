package com.github.alantr7.codebots.world.structure.data;

import java.util.UUID;

public class Data<T> {

    final DataContainer container;

    final Type<T> type;

    Object value;

    public Data(DataContainer container, Type<T> type) {
        this.container = container;
        this.type = type;
    }

    public Data(DataContainer container, Type<T> type, T value) {
        this.container = container;
        this.type = type;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        return (T) value;
    }

    public void update(T value) {
        this.value = value;
        container.isUnsaved = true;
        container.structure.location.getChunk().isUnsaved = true;
    }

    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }

    public static class Type<T> {

        public static final Type<Byte>      BYTE = new Type<>(0, "byte");
        public static final Type<Integer>   INT = new Type<>(1, "int");
        public static final Type<Float>     FLOAT = new Type<>(2, "float");
        public static final Type<String>    STRING = new Type<>(3, "string");
        public static final Type<byte[]>    BYTE_ARRAY = new Type<>(4, "byte_array");
        public static final Type<UUID>      UUID = new Type<>(5, "uuid");

        public final int id;

        public final String name;

        public Type(int id, String name) {
            this.id = id;
            this.name = name;
        }

    }

}
