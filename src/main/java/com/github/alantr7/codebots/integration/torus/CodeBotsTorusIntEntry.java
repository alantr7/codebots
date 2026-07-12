package com.github.alantr7.codebots.integration.torus;

import com.github.alantr7.bukkitplugin.annotations.core.Invoke;
import com.github.alantr7.bukkitplugin.annotations.core.RequiresPlugin;
import com.github.alantr7.bukkitplugin.annotations.core.Singleton;
import com.github.alantr7.codebots.CodeBotsPlugin;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ModuleRepository;
import com.github.alantr7.codebots.codeint.modules.*;
import com.github.alantr7.codebots.integration.torus.codeint.modules.ComputerModule;
import com.github.alantr7.codebots.integration.torus.codeint.modules.TorusModule;
import com.github.alantr7.codebots.integration.torus.machine.Computer;
import com.github.alantr7.torus.api.TorusAPI;
import com.github.alantr7.torus.api.addon.LifecycleAdapter;
import com.github.alantr7.torus.api.addon.TorusAddon;
import com.github.alantr7.torus.item.Category;
import com.github.alantr7.torus.item.ItemRegistry;
import com.github.alantr7.torus.item.TorusItem;
import com.github.alantr7.torus.structure.Structure;
import com.github.alantr7.torus.structure.StructureRegistry;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Singleton
@RequiresPlugin("Torus")
public class CodeBotsTorusIntEntry {

    public static TorusAddon addon;

    @Getter
    private static ModuleRepository computerModuleRepository;

    @Invoke(Invoke.Schedule.AFTER_PLUGIN_ENABLE)
    void init() {
        addon = TorusAPI.newAddon(CodeBotsPlugin.inst(), "codebots")
                .name("CodeBots")
                .register();

        computerModuleRepository = new ModuleRepository();
        computerModuleRepository.registerModule(new LangModule());
        computerModuleRepository.registerModule(new MathModule());
        computerModuleRepository.registerModule(new MonitorModule());
        computerModuleRepository.registerModule(new HttpModule());
        computerModuleRepository.registerModule(new ResponsesModule());
        computerModuleRepository.registerModule(new RedstoneModule());
        computerModuleRepository.registerModule(new ComputerModule());
        computerModuleRepository.registerModule(new TorusModule());

        Structure computer = new Computer();

        TorusAPI.getAddonLifecycle().subscribe(addon, new LifecycleAdapter(addon) {
            @Override
            public void registerStructures(StructureRegistry registry) {
                registry.registerAndInitialize(computer);
            }

            @Override
            public void registerItems(ItemRegistry registry) {
                registry.registerItem(new TorusItem(addon, "computer", new Category[]{Category.GENERAL}, computer, Material.DISPENSER, "Computer", List.of("")));
            }
        });
    }

}
