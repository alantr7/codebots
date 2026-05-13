package com.github.alantr7.codebots.codeint.modules;

import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.codeint.functions.HttpGetFunction;

public class HttpModule extends Module {

    public HttpModule() {
        super("http");
    }

    @Override
    public void setup() {
        registerFunction(new HttpGetFunction(this));
    }

}
