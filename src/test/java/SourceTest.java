import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.compiler.bnf.BnfCompiler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class SourceTest {

    private static File directory;

    @BeforeClass
    public static void init() {
        directory = new File("E:\\Users\\Alan\\Desktop\\CodeBots Tests");
    }

    @Test
    public void simpleTest() throws Exception {
        test("src_simple.txt");
    }

    private void test(String path) throws Exception {
        String bnfPath = "language.bnf";
        var file = new File(directory, bnfPath);
        var lines = Files.readAllLines(file.toPath()).toArray(String[]::new);

        var bnf = BnfCompiler.compile(lines);
        var code = Files.readString(new File(directory, path).toPath());

        System.out.println("Source Code:\n" + code);

        var testResult = bnf.test(bnf.getRule("program"), code);

        System.out.println("\nBNF Parse Result: " + testResult);
    }

}
