import com.github.alantr7.codebots.language.compiler.Compiler;
import com.github.alantr7.codebots.language.compiler.parser.Parser;
import com.github.alantr7.codebots.language.compiler.Tokenizer;
import org.junit.Test;

public class CompilerTest {

    @Test
    public void testCompiler2() {
//        new Compiler().parse(Tokenizer.tokenize("function main(){5+rand(5,hello(2,hi(3)))}"));
//        new Compiler().parse(Tokenizer.tokenize("function main(){ rand(5,hello(2,hi(3)))}"));
        /*var tree = new Parser().parse(Tokenizer.tokenize(
                """
                        import turtle
                        
                        function main(first, second) {
                            print(5)
                            turtle.hi.a.move(turtle.getX()+5, 0, 0)
                            
                            var a = 1+5+3*10+4*2*4*0+3*4
                            
                            if (5+5>3) {
                                var a = random(3,17+3)*5*11+3*2+random(0, 10+3)+11
                                var length = a.length()
                            }
                        }
                        """

                        .split("\n")
        ));*/

        var tree = new Parser().parse(Tokenizer.tokenize("""
                import turtle
                
                function main() {
                    var a = 5+random(10)>3
                    var b = 3
                    
                    print(5)
                    turtle.move()
                }
                """
                .split("\n")));

        var compiled = new Compiler().compile(tree);
        System.out.println("\n\n");
        System.out.println("Compiled code:\n----------------------------");
        System.out.println(compiled);
    }

}
