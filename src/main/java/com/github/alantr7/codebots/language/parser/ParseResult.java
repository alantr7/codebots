package com.github.alantr7.codebots.language.parser;

import com.github.alantr7.codebots.language.runtime.RuntimeCodeBlock;
import lombok.Getter;

public class ParseResult {

    @Getter
    RuntimeCodeBlock block;

    @Getter
    int blockLength;

    public ParseResult(RuntimeCodeBlock block, int blockLength) {
        this.block = block;
        this.blockLength = blockLength;
    }

}
