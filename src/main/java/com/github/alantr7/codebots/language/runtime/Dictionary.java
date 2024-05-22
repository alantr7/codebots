package com.github.alantr7.codebots.language.runtime;

import lombok.Getter;

import java.util.LinkedHashMap;

public class Dictionary extends LinkedHashMap<String, Object> {

    @Getter
    private boolean isLocked = false;

    public void lock() {
        isLocked = true;
    }

}
