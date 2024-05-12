package com.github.alantr7.codebots.language.runtime;

import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.parser.AssemblyParser;
import com.github.alantr7.codebots.language.runtime.errors.exceptions.ParseException;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.functions.RuntimeNativeFunction;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.language.runtime.modules.MemoryModule;
import com.github.alantr7.codebots.language.runtime.modules.Module;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.language.runtime.modules.standard.LangModule;
import com.github.alantr7.codebots.language.runtime.modules.standard.MathModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

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

    @Getter @Setter
    private Mode mode = Mode.SINGLE_LINE;

    public enum Mode {
        SINGLE_LINE,

        AUTO_HALT,

        FULL_EXEC
    }

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

    public void action() {
        action(mode);
    }

    public void action(Mode mode) {
        while (mainModule.hasNext() && !environment.isInterrupted()) {
            mainModule.next();

            if (mode == Mode.SINGLE_LINE || (mode == Mode.AUTO_HALT && environment.isHalted())) {
                break;
            }
        }

        if (environment.isInterrupted()) {

            if (environment.getException() != null) {
                System.err.println("Error while executing the program: " + environment.getException().getMessage());
            } else {
                System.err.println("Error while executing the program");
            }

            System.err.println("Stack trace:");
            var stackTrace = environment.getStackTrace();
            for (var entry : stackTrace) {
                System.err.println("  at " + entry.toString());
            }
        }

        environment.setHalted(false);
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
        action(Mode.FULL_EXEC);
    }

    public void prepareMainFunction() {
        getEnvironment().getBlockStack().add(new BlockStackEntry(mainModule.getRootScope().getFunction("main"), new BlockContext(BlockScope.nestIn(mainModule.getRootScope()))));
        getEnvironment().getCallStack().add(new FunctionCall(mainModule.getRootScope(), "main", 0));
    }

    public void reset() {
        environment.getTokenStack().clear();
        environment.getBlockStack().clear();
        environment.getCallStack().clear();
    }

    public static Program createFromSourceFile(File file) throws Exception {
        var source = Files.readAllLines(file.toPath()).toArray(String[]::new);
        var inline = Compiler.compileModule(String.join("\n", source));

        var program = new Program(file.getParentFile());
        program.registerNativeModule("math", new MathModule(program));
        program.registerNativeModule("lang", new LangModule(program));
        program.registerDefaultFunctionsFromModule(program.getOrLoadModule("math"));
        program.registerDefaultFunctionsFromModule(program.getOrLoadModule("lang"));
        program.getRootScope().setFunction("print", new RuntimeNativeFunction(program, "print", args -> {
            var value = args[0];
            Bukkit.broadcastMessage(value.toString());

            return null;
        }));

        var moduleBlock = AssemblyParser.parseCodeBlock(program, inline.split("\n"));
        var module = new FileModule(program, file, moduleBlock);
        program.setMainModule(module);

        for (var line : inline.split("\n"))
            System.out.println(line);

        return program;
    }

    public static Program createFromCompiledFile(File file) throws Exception {
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
