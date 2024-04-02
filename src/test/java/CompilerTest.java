import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.outline.ModuleOutline;
import com.github.alantr7.codebots.language.runtime.modules.standard.ConsolePrintModule;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class CompilerTest {

    private static File directory;

    @BeforeClass
    public static void init() {
        directory = new File("E:\\Users\\Alan\\Desktop\\CodeBots Tests");
    }

    @Test
    public void basicTest() throws Exception {
        test("src_example.txt");
    }


    private void test(String path) throws Exception {
        var file = new File(directory, path);
        var lines = Files.readAllLines(file.toPath()).toArray(String[]::new);

        var block = Compiler.compile(lines);

//
//
//        var program = Program.createFromFileModule(new File(directory, path));
//        program.registerNativeModule("console", new ConsolePrintModule(program));
//        program.executeEverything();
//
//        var outline = ModuleOutline.forModule(program.getMainModule());
//        System.out.println(outline);
//
//        return program;
    }

}
