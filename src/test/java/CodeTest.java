import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.compiler.Tokenizer;
import com.github.alantr7.codebots.language.compiler.parser.Parser;
import com.github.alantr7.codebots.language.parser.AssemblyParser;
import com.github.alantr7.codebots.language.runtime.BlockContext;
import com.github.alantr7.codebots.language.runtime.BlockScope;
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
    public void testVariableScopes() throws Exception {
        testCode("""                
                function testIfPersists(num) {
                  var a = 20
                  if (num == 0) {
                    a = 10
                    testIfPersists(1)
                  }
                  
                  print("Attempt #" + num)
                  print(a)
                }
                                
                function main() {
                  testIfPersists(0)
                }
                """);
    }

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
                  }
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
                                
                function main() {
                  var number = 17
                  var tries = getTriesUntilMatch(number, 0)
                  
                  print("Tries: " + tries)
                }
                """
        );
    }

    @Test
    public void testStringConcat() throws Exception {
        testCode("""
                function main() {
                  print("Hello " + "Alan")
                  print("I am " + 21 + " years old")
                  print("It's year " + 20 + 24 + " now!")
                  print(12 + ":" + 30 + "pm")
                }
                """);
    }

    @Test
    public void testWhileLoop() throws Exception {
        testCode("""
                function getTriesUntilMatch(input) {
                  var counter = 1
                  var number = random(20)
                  
                  while (number != input) {
                    number = random(20)
                    counter = counter + 1
                  }
                  
                  return counter
                }
                
                function main() {
                  var toMatch = 15
                  print("Matching: " + toMatch)
                  print("----------------")
                  
                  print("Took " + getTriesUntilMatch(toMatch) + " random attempts to match the given number")
                }
                """);
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

        var module = new MemoryModule(program, block);
        program.setMainModule(module);
        module.getRootScope().setModule(module);

        program.getEnvironment().getBlockStack().add(new BlockStackEntry(block, new BlockContext(module.getRootScope())));
        program.getRootScope().setFunction("random", program.getOrLoadModule("math").getRootScope().getFunction("random"));

        program.executeEverything();

        var main = module.getRootScope().getFunction("main");
        System.out.println("Main function: " + main);

        program.getEnvironment().getBlockStack().add(new BlockStackEntry(main, new BlockContext(BlockScope.nestIn(module.getRootScope()))));
        program.getEnvironment().getCallStack().add(new FunctionCall(module.getRootScope(), "main"));

        program.executeEverything();
    }

}
