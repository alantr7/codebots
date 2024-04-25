import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.compiler.Tokenizer;
import com.github.alantr7.codebots.language.compiler.parser.Parser;
import com.github.alantr7.codebots.language.parser.AssemblyParser;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.BlockStackEntry;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.functions.FunctionCall;
import com.github.alantr7.codebots.language.runtime.modules.MemoryModule;
import com.github.alantr7.codebots.language.runtime.modules.NativeModule;
import com.github.alantr7.codebots.language.runtime.modules.standard.MathModule;
import org.junit.Test;

import java.io.File;

public class CodeTest {

    @Test
    public void testIfElseIfElse() throws Exception {
        testCode("""
                function a(counter) {
                  
                }
                                
                function main() {
                  var number = random(100)
                  if (number < 30) {
                    print("Less than 30!")
                  }
                  else if (number < 60) {
                    print("Less than 60!")
                  }
                  else {
                    print("Less than 100!")
                  }
                  
                  a(0)
                }
                """
        );
    }

    @Test
    public void testRecursiveness() throws Exception {
        testCode("""
                function getTriesUntilMatch(input, counter) {
                  var rand = random(20)
                  counter = counter + 1
                 
                  if (rand == input) {
                    return counter
                  } else {
                    return getTriesUntilMatch(input, counter)
                  }
                }
                
                function noReturn() {
                  print("I have no return :D")
                }
                
                function main() {
                  var number = 17
                  var tries = getTriesUntilMatch(number, 0)
                  
                  print("Tries:")
                  print(tries)
                  
                  print("No return result:")
                  print(noReturn())
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
        program.getEnvironment().getBlockStack().add(new BlockStackEntry(block, new BlockContext()));
        program.getRootScope().setFunction("random", program.getOrLoadModule("math").getBlock().getFunction("random"));

        program.executeEverything();

        var main = block.getFunction("main");
        program.getEnvironment().getBlockStack().add(new BlockStackEntry(main, new BlockContext()));
        program.getEnvironment().getCallStack().add(new FunctionCall(main.getScope(), "main"));

        program.executeEverything();
    }

}
