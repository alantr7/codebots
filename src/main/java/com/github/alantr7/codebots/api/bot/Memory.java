package com.github.alantr7.codebots.api.bot;

import com.github.alantr7.codebots.language.runtime.DataType;

public interface Memory {

    <T> void save(String key, DataType<T> type, T value);


    /**
     * Read the method below. The only difference is that this method returns null if the value is not present
     * @see Memory#load(String, DataType, Object)
     */
    <T> T load(String key, DataType<T> type);

    /**
     *
     * @param key key of the stored value
     * @param type type of the stored value. It can be of any except {@link DataType#ANY} and {@link DataType#NULL}
     * @param def value that will be returned if the specified key is not present in the memory
     * @return stored value if it exists, or the specified default value if it does not
     */
    <T> T load(String key, DataType<T> type, T def);

    boolean contains(String key);

}
