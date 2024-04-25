package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.parser.AssemblyParser;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.language.runtime.modules.Module;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Program {

    @Getter
    private final RuntimeEnvironment environment = new RuntimeEnvironment(this);

    @Getter @Setter
    private Module mainModule;

    @Getter
    private final File directory;

    @Getter
    private final BlockScope rootScope = new BlockScope();

    private final Map<String, Module> loadedModules = new LinkedHashMap<>();

    private final Map<String, Object> extra = new HashMap<>();

    public Program(File directory) {
        this.directory = directory;

        environment.getNativeFunctions().put("print", new RuntimeNativeFunction(this, "print", (args) -> {
            System.out.println(args[0]);
            return null;
        }));

        rootScope.setFunction("print", environment.getNativeFunctions().get("print"));
    }

    public Module getOrLoadModule(String path) {
        if (loadedModules.containsKey(path))
            return loadedModules.get(path);


        var file = new File(environment.getProgram().getDirectory(), path);

        if (!file.exists()) {
            // THROW AN ERROR
            return null;
        }

        try {
            var code = Files.readAllLines(file.toPath()).toArray(String[]::new);
            var block = AssemblyParser.parseCodeBlock(this, code);

            var module = new FileModule(this, file, block);
            loadedModules.put(path, module);

            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void executeEverything() {
        while (mainModule.hasNext()) {
            mainModule.next();
        }
    }

    public void registerNativeModule(String name, NativeModule module) {
        this.loadedModules.put(name, module);
    }

    public void setExtra(String key, Object extra) {
        this.extra.put(key, extra);
    }

    public Object getExtra(String key) {
        return this.extra.get(key);
    }

    public static Program createFromFileModule(File file) throws Exception {
        var directory = file.getParentFile();
        var program = new Program(directory);

        var contents = Files.readAllLines(file.toPath()).toArray(String[]::new);
        var moduleBlock = AssemblyParser.parseCodeBlock(program, contents);
        var module = new FileModule(program, file, moduleBlock);
        program.setMainModule(module);

        program.getEnvironment().getBlockStack().add(new BlockStackEntry(moduleBlock, new BlockContext()));

        return program;
    }

}
