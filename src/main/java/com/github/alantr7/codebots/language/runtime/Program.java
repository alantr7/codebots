package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.compiler.parser.error.ParserException;
import com.github.alantr7.codebots.language.parser.AssemblyParser;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.language.runtime.modules.MemoryModule;
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

    @Getter
    private Module mainModule;

    @Getter
    private final File directory;

    @Getter
    private final BlockScope rootScope = new BlockScope();

    private final Map<String, Module> loadedModules = new LinkedHashMap<>();

    private final Map<String, Object> extra = new HashMap<>();

    public Program(File directory) {
        this.directory = directory;
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
        while (mainModule.hasNext() && !environment.isInterrupted()) {
            mainModule.next();
        }

        if (environment.isInterrupted()) {
            var block = environment.getBlockStack().getLast().block().getBlock();
            var context = environment.getBlockStack().getLast().context();

            System.err.print("Error while executing the program.");
            if (environment.getException() != null) {
                System.err.println(": " + environment.getException().getMessage());
            } else {
                System.err.println();
            }

            System.err.println("Stack trace:");
            var stackTrace = environment.getStackTrace();
            for (var entry : stackTrace) {
                System.err.println("  at " + entry.toString());
            }
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

    public void registerDefaultFunctionsFromModule(Module module) {
        for (var function : module.getRootScope().getFunctions()) {
            this.rootScope.setFunction(function.getLabel(), function);
        }
    }

    public void setMainModule(Module module) {
        this.mainModule = module;
        this.getEnvironment().REGISTRY_CURRENT_SCOPE.setValue(mainModule);
        this.getEnvironment().getBlockStack().clear();
        this.getEnvironment().getCallStack().clear();

        // Add module to the block stack, and prepare it for execution
        this.getEnvironment().getBlockStack().add(new BlockStackEntry(module.getBlock(), new BlockContext(module.getRootScope())));
    }

    public void loadAndSetMainModule(String assembly) throws ParseException {
        var block = AssemblyParser.parseCodeBlock(this, assembly.split("\n"));
        var module = new MemoryModule(this, block);

        setMainModule(module);
        executeEverything();
    }

    public void prepareMainFunction() {
        getEnvironment().getBlockStack().add(new BlockStackEntry(mainModule.getRootScope().getFunction("main"), new BlockContext(BlockScope.nestIn(mainModule.getRootScope()))));
        getEnvironment().getCallStack().add(new FunctionCall(mainModule.getRootScope(), "main", 0));
    }

    public static Program createFromFileModule(File file) throws Exception {
        var directory = file.getParentFile();
        var program = new Program(directory);

        var contents = Files.readAllLines(file.toPath()).toArray(String[]::new);
        var moduleBlock = AssemblyParser.parseCodeBlock(program, contents);
        var module = new FileModule(program, file, moduleBlock);
        program.setMainModule(module);

        program.getEnvironment().getBlockStack().add(new BlockStackEntry(moduleBlock, new BlockContext(BlockScope.nestIn(program.getRootScope()))));
        return program;
    }

}
