package micro.compiler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.CodeGenerationUtils;
import io.vavr.collection.Stream;
import micro.F;
import micro.visualize.ToDot;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModelExtractorTest {

    @Test
    public void testCompile() throws FileNotFoundException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        Path modulePath = CodeGenerationUtils.mavenModuleRoot(ModelExtractorTest.class);
        Path sourceRoot = modulePath.resolve("src/main/java/");
        Path uDotPath = Path.of(System.getProperty("user.dir"));// modulePath.resolve("src/main/resources/udot");
        try {
            if (!Files.exists(uDotPath))
                Files.createDirectory(uDotPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TypeSolver srcSolver = new JavaParserTypeSolver(sourceRoot);
        combinedTypeSolver.add(srcSolver);

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        StaticJavaParser.getConfiguration().setCharacterEncoding(StandardCharsets.UTF_8);

        List<Path> files = findSources(sourceRoot.resolve("micro/compiler/sources"));

        files.forEach(f -> {
            try {
                CompilationUnit cu = StaticJavaParser.parse(f);
                String clazz = cu.findAll(ClassOrInterfaceDeclaration.class).get(0).getNameAsString();

                visualizeAst(cu, clazz);

                List<F> methods = new ModelExtractor(JavaParserFacade.get(combinedTypeSolver)).get(cu);
                methods.forEach(m -> new ToDot(getPackage(cu) + '.' + clazz, m, uDotPath).render());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void visualizeAst(CompilationUnit cu, String clazz) {
        String pkg = getPackage(cu);
        writeString(new micro.compiler.sources.ToDot(true).get(cu.getType(0)), System.getProperty("user.dir") + "/AST." + pkg + '.' + clazz + ".dot");
    }

    private String getPackage(CompilationUnit cu) {
        return cu.getPackageDeclaration().orElseThrow(IllegalStateException::new).getNameAsString();
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

    private List<Path> findSources(Path srcDirectory) {
        List<Path> result = new ArrayList<>();
        findSources(srcDirectory, result);
        return result;
    }

    private void findSources(Path srcDirectory, List<Path> acc) {
        try {
            if (Files.exists(srcDirectory)) {
                Files.newDirectoryStream(srcDirectory)
                        .forEach(path -> {
                            if (path.getFileName().toString().toLowerCase().endsWith(".java") && isCandidate(path)) {
                                acc.add(path);
                            } else if (path.toFile().isDirectory()) {
                                findSources(path, acc);
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isCandidate(Path path) {
        try (java.util.stream.Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            return stream.anyMatch(s -> s.contains('@' + Undermine.class.getSimpleName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}