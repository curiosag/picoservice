package micro.compiler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.vavr.collection.Stream;
import micro.compiler.sources.ToDot;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CompilerTest {

    @Test
    public void testCompile() throws FileNotFoundException {
        String fileName = "/home/ssmertnig/dev/repo/microservice/src/main/java/micro/compiler/sources/Functions.java";

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        JavaParserTypeSolver srcSolver = new JavaParserTypeSolver(new File("/home/ssmertnig/dev/repo/microservice/src/main/java"));

   //     SourceRoot sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(LogicPositivizer.class).resolve("src/main/resources"));


        combinedTypeSolver.add(srcSolver);

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        StaticJavaParser.getConfiguration().setCharacterEncoding(StandardCharsets.UTF_8);

        // Parse some code
        FileInputStream in = new FileInputStream(fileName);
        CompilationUnit cu = StaticJavaParser.parse(in);

       // List<F> methods = new Compiler(JavaParserFacade.get(combinedTypeSolver)).compile(cu);


        writeString(new ToDot(true).get(cu.getType(0)), System.getProperty("user.dir") + "/ast.dot");
    }

    public static void writeString(String value, String targetPath) {
        try {
            FileWriter w = new FileWriter(targetPath, false);
            w.write(value);
            w.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void name() {
        io.vavr.collection.List<Integer> l = io.vavr.collection.List.of(1, 2, 3);
        Stream<Integer> s = Stream.from(1);
        var a = l.append(1);
    }
}