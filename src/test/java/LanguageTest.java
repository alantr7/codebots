import com.github.alantr7.codebots.language.parser.AssemblyParser;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.FileModule;
import com.github.alantr7.codebots.language.runtime.modules.standard.ConsolePrintModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class LanguageTest {

    private static File directory;

    @BeforeClass
    public static void init() {
        directory = new File("E:\\Users\\Alan\\Desktop\\CodeBots Tests");
    }

    @Test
    public void testTypes() throws Exception {
        var program = test("code_types_test.txt");
    }

    private Program test(String path) throws Exception {
        var program = Program.createFromFileModule(new File(directory, path));
        program.registerNativeModule("console", new ConsolePrintModule(program));
        program.executeEverything();

        return program;
    }

}
