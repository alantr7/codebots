package com.github.alantr7.codebots.plugin.codeint.modules;

import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.plugin.codeint.functions.MoveFunction;
import com.github.alantr7.codebots.plugin.codeint.functions.RotateFunction;

public class BotModule extends NativeModule {

    public BotModule(Program program) {
        super(program);
        init();
    }

    private void init() {
        getRootScope().setFunction("moveForward", new MoveFunction(program, "moveForward"));
        getRootScope().setFunction("moveRight", new MoveFunction(program, "moveRight"));
        getRootScope().setFunction("moveBack", new MoveFunction(program, "moveBack"));
        getRootScope().setFunction("moveLeft", new MoveFunction(program, "moveLeft"));

        getRootScope().setFunction("rotateRight", new RotateFunction(program, "rotateRight"));
    }

}
