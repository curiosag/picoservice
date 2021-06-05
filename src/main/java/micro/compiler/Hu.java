package micro.compiler;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.Java;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;

import java.io.IOException;

public class Hu {

    public static void main(String[] args) throws IOException, CompileException {
        Java.AbstractCompilationUnit cu = new Parser(new Scanner("fileName", "r")).parseAbstractCompilationUnit();


// Manipulate the AST in memory.
// ...

    }

}
