import com.github.alantr7.codebots.language.compiler.CompilerLegacy;
import com.github.alantr7.codebots.language.compiler.bnf.BnfParser;
import com.github.alantr7.codebots.language.compiler.bnf.Grammar;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class CompilerLegacyTest {

    private static File directory;

    private static Grammar grammar;

    @BeforeClass
    public static void init() throws Exception {
        directory = new File("E:\\Users\\Alan\\Desktop\\CodeBots Tests");
        grammar = BnfParser.parse(
                Files.readAllLines(new File(directory, "language.bnf").toPath()).toArray(String[]::new)
        );
    }

    @Test
    public void basicTest() throws Exception {
        test("src_function.txt");
    }


    private void test(String path) throws Exception {
        var file = new File(directory, path);
        var lines = Files.readString(file.toPath());
        var block = CompilerLegacy.compile(grammar, lines);

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
