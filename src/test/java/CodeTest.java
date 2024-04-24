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
    public void testIfElseIfElse() throws Exception {
        testCode("""                
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
                    main()
                  }
                }
                """
        );
    }

    @Test
    public void testRecursiveness() throws Exception {
        testCode("""
                function getTriesUntilMatch(input, counter) {
                  var rand = random(50)
                  counter = counter + 1
                  
                  if (rand < input) {
                    print("IT IS LESS THAN THAT!")
                    print(counter)
                  } else {
                    if (counter < 100) {
                      getTriesUntilMatch(input, counter)
                    } else {
                      print(counter)
                      print("Counter is greater than 100")
                    }
                  }
                }
                
                function main() {
                  var number = 17
                  getTriesUntilMatch(number, 0)
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
