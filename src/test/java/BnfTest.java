import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.compiler.bnf.BnfCompiler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class BnfTest {

    private static File directory;

    @BeforeClass
    public static void init() {
        directory = new File("E:\\Users\\Alan\\Desktop\\CodeBots Tests");
    }

    @Test
    public void basicTest() throws Exception {
        test("simple.bnf", "var a = 5 var b = 5");
    }

    @Test
    public void testFunction() throws Exception {
        test("function.bnf", "function a(b) {} function a(b) {}");
    }

    @Test
    public void testBranches() throws Exception {
        test("branches.bnf", "function a(b, b) {var a = 5} function b(a) {var a = 5} function c(a) {}");
    }

    @Test
    public void testMath() throws Exception {
        test("math.bnf", "5+5+5-5+5-5-2");
    }

    private void test(String path, String input) throws Exception {
        var file = new File(directory, path);
        var lines = Files.readAllLines(file.toPath()).toArray(String[]::new);

        var bnf = BnfCompiler.compile(lines);
        var testResult = bnf.test(bnf.getRule("code_block"), input);

        System.out.println("\nBNF Parse Result: " + testResult);

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
