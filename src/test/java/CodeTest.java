import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.runtime.Program;
import com.github.alantr7.codebots.language.runtime.modules.standard.ConsolePrintModule;
import com.github.alantr7.codebots.language.runtime.modules.standard.LangModule;
import com.github.alantr7.codebots.language.runtime.modules.standard.MathModule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

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
                  print("Number: " + number)
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

    @Test
    public void testDoWhileLoop() throws Exception {
        testCode("""
                function getTriesUntilMatch(input) {
                  var counter = 0
                  var number
                  
                  do {
                    number = random(20)
                    counter = counter + 1
                  } while (number != input)
                  
                  print(number + " = " + input)
                  
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

    @Test
    public void testForLoop() throws Exception {
        testCode("""
                function getTriesUntilMatch(input) {
                  var counter = 0
                  var number
                  
                  for (number = random(20); number != input; number = random(20)) {
                    counter = counter + 1
                    print(number + " != " + input)
                  }
                  
                  print(number + " = " + input)
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

    @Test
    public void testExpressionGroups() throws Exception {
        testCode("""
                function pow(num, pow) {
                  var result = 1
                  
                  while (pow > 0) {
                    result = result * num
                    pow = pow - 1
                  }
                    
                  return result
                }
                                
                function main() {
                  var result = (10 + (20 - pow(2, pow(2, 2)))) * 2
                  print(result)
                  
                  print("2^3 = " + pow(2, 3))
                  print("(2 + 3) * 2 = " + (2 + 3) * 2 + "!")
                }
                """);
    }

    @Test
    public void testArrays() throws Exception {
        testCode("""
                function main() {
                  var array = array(2)
                  array[0] = "Hello"
                  array[1] = "Alan!"
                  
                  print(array[0] + " " + array[1])
                  print("Array length: " + length(array))
                }
                """);
    }

    @Test
    public void testArrays2() throws Exception {
        testCode("""
                function c() {
                  return 10
                }
                                
                function b() {
                  return c()
                }
                                
                function a() {
                  var arr = array(1)
                  arr[0] = 0
                  
                  return arr
                }
                                
                function main() {
                  var arr = array(2)
                  arr[0] = a()
                  arr[1] = b()
                  
                  print("First: " + arr[0])
                  print("Second: " + arr[1])
                }
                """);
    }

    @Test
    public void testDicts() throws Exception {
        testCode("""
                function location(x, y, z) {
                  var array = array(3)
                  array[0] = x
                  array[1] = y
                  array[2] = z
                  
                  return array
                }
                                
                function main() {
                  var locations = dict()
                  
                  locations["home"] = location(12, 20, 12)
                  locations["farm"] = location(123, 20, 456)
                  
                  print("Home's X is: " + locations["home"][0])
                  dict_unset(locations, "farm")
                  
                  print("Farm is now at: " + locations["farm"])
                  print("There are " + length(locations) + " locations saved.")
                }
                """);
    }

    @Test
    public void testPrintStringCharacters() throws Exception {
        testCode("""                                
                function main() {
                  var message = "Hello world!"
                  var array = array(length(message))
                  
                  for (var i = 0; i < length(message); i = i + 1) {
                    array[i] = message[i]
                  }
                  
                  print(array + "")
                  print(to_string(array))
                }
                """);
    }

    @Test
    public void testToStringAndToInt() throws Exception {
        testCode("""
                function main() {
                  var num = to_int("5")
                  var c = to_int(to_string(3)) * num
                  
                  print("Result: " + c)
                  to_int("a")
                }
                """);
    }

    @Test
    public void testRecords() throws Exception {
        testCode("""
                record Person(name, age)
                var alan = new Person("Alan", 21)
                
                function main() {
                  print("hello " + alan)
                }
                """);
    }

    @Test
    public void testConstants() throws Exception {
        testCode("""
                function main() {
                  const hey = "Hello!"
                  print(hey)
                }
                """);
    }

    @Test
    public void testThing() throws Exception {
        testCode("""
                import math
                                
                function main() {
                  moveUntilAbove("red_wool", "west")
                  moveUntilAbove("lime_wool", "east")
                }
                                
                function moveUntilAbove(target, direction) {
                  var rand = math.random(20)
                  while (rand != 10) {
                    rand = math.random(20)
                  }
                }
                """);
    }

    private void testCode(String code) throws Exception {
        var inline = Compiler.compileModule(code);

        // Save to a file
        var testName = StackWalker.getInstance().walk(frames -> frames.collect(Collectors.toList())).get(1).getMethodName();
        System.out.println("Test Name: " + testName);

        var file = new File(System.getProperty("user.dir"), testName + ".txt");
        Files.write(file.toPath(), inline.getBytes());

        var program = new Program(new File("."));
        program.registerNativeModule("math", new MathModule(program));
        program.registerNativeModule("lang", new LangModule(program));
        program.getRootScope().setFunction("print", new ConsolePrintModule(program).getRootScope().getFunction("print"));
        program.registerDefaultFunctionsFromModule(program.getOrLoadModule("math"));
        program.registerDefaultFunctionsFromModule(program.getOrLoadModule("lang"));

        program.loadAndSetMainModule(inline);
        System.out.println(inline);

        program.prepareMainFunction();

        while (program.getMainModule().hasNext() && !program.getEnvironment().isInterrupted()) {
            program.action(Program.Mode.AUTO_HALT);
        }
    }

}
