package com.github.alantr7.codebots.language.runtime.modules;

import com.github.alantr7.codebots.language.runtime.BlockScope;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import lombok.Getter;

import java.io.File;

public class FileModule extends Module {

    @Getter
    private final File file;

    public FileModule(Program program, File file, RuntimeCodeBlock block) {
        super(program, new BlockScope(), block);
        this.getRootScope().setModule(this);
        this.file = file;
    }

}
