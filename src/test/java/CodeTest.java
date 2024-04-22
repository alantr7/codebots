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
                  print(random(5))
                  var a = 5 * random(10)
                }
                """
        );
    }

    private void testCode(String code) throws Exception {
        var tokens = Tokenizer.tokenize(code.split("\n"));
        var parser = new Parser();
        var compiler = new Compiler().compile(parser.parse(tokens)).split("\n");

        var program = new Program(new File("."));
        var block = AssemblyParser.parseCodeBlock(program, compiler);

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