import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.compiler.Tokenizer;
import com.github.alantr7.codebots.language.compiler.parser.Parser;
import com.github.alantr7.codebots.language.parser.AssemblyParser;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.modules.MemoryModule;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.language.runtime.modules.standard.MathModule;
import org.junit.Test;

import java.io.File;

public class CodeTest {

    @Test
    public void testVariableAssignWithExpressions() throws Exception {
        testCode("""
                function main() {
                  var a = 10 + 1 == 11 - 1 * 0
                  print(a == true)
                }
                """
        );
    }

    private void testCode(String code) throws Exception {
        var tokens = Tokenizer.tokenize(code.split("\n"));
        var parser = new Parser();
        var inline = new Compiler().compile(parser.parse(tokens));
        var compiled = inline.split("\n");

        System.out.println(inline);

        var program = new Program(new File("."));
        var block = AssemblyParser.parseCodeBlock(program, compiled);

        program.registerNativeModule("math", new MathModule(program));
        program.setMainModule(new MemoryModule(program, block));
        program.getEnvironment().getBlockStack().add(block);
        program.getRootScope().setFunction("random", program.getOrLoadModule("math").getBlock().getFunction("random"));

        program.executeEverything();

        var main = block.getFunction("main");
        program.getEnvironment().getBlockStack().add(main);
        program.getEnvironment().getCallStack().add(new FunctionCall(main.getScope(), "main"));

        program.executeEverything();
    }

}
