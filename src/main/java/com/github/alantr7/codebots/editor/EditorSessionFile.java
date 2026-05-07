package com.github.alantr7.codebots.editor;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EditorSessionFile {

    private String code;

    private long lastModified;

    public EditorSessionFile(String code) {
        this.code = code;
    }

}
