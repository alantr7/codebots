import com.github.alantr7.codebots.language.compiler.bnf.BnfParser;
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
        test("branches.bnf", "function print5(text, times) {var a = 5 function one(arg) {} function two(arg) {}}");
    }

    @Test
    public void testMath() throws Exception {
        test("math.bnf", "5+5+5-5+5-5-2");
    }

    private void test(String path, String input) throws Exception {
        var file = new File(directory, path);
        var lines = Files.readAllLines(file.toPath()).toArray(String[]::new);

        var bnf = BnfParser.parse(lines);
        var testResult = bnf.test(bnf.getRule("code_block"), input);

        System.out.println("\nBNF Parse Result: " + testResult);
        System.out.println("Success: " + !testResult.getChildren().isEmpty());
    }

}
