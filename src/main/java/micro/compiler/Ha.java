package micro.compiler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import micro.Check;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Ha {

    public static void main(String[] args) throws FileNotFoundException {
        new Ha().ha();
    }

    private void ha() throws FileNotFoundException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        StaticJavaParser.getConfiguration().setCharacterEncoding(StandardCharsets.UTF_8);

        // Parse some code
        FileInputStream in = new FileInputStream(new File("/home/ssmertnig/dev/repo/microservice/src/main/java/micro/compiler/sources/Functions.java"));
        CompilationUnit cu = StaticJavaParser.parse(in);

        toDot(cu);

        NodeList<ImportDeclaration> imports = cu.getImports();

        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        types.stream().filter(t -> t instanceof ClassOrInterfaceDeclaration)
                .map(t -> (ClassOrInterfaceDeclaration) t)
                .forEach(this::convert);

        // Find all the calculations with two sides:
        cu.findAll(BinaryExpr.class).forEach(be -> {
            // Find out what type it has:
            ResolvedType resolvedType = be.calculateResolvedType();

            // Show that it's "double" in every case:
            System.out.println(be.toString() + " is a: " + resolvedType);
        });
    }


    private void toDot(Node n){
        writeString(new DotPrinter(true).output(n), "ast.dot");
    }

    private void convert(ClassOrInterfaceDeclaration cls) {
        cls.getNameAsString();
        cls.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(i -> (MethodDeclaration) i)
                .forEach(this::convert);
    }

    private void convert(MethodDeclaration b) {
        Type type = b.getType();
        if (type instanceof PrimitiveType pt) {
            PrimitiveType.Primitive ppt = pt.getType(); //enum
        }
        String name = b.getNameAsString();

        Check.preCondition(b.getBody().isPresent());
        b.getBody().get().getStatements().forEach(this::conv);
    }

    private void conv(Statement statement) {

        switch (MetaType.decode(statement)) {
            case TExpression -> {
               // convExpression((Expression) statement);
            }
            case TExpressionStmt -> {
                convExpressionStmt((ExpressionStmt) statement);
            }
            case TIfStmt -> { // condition, thenStmt, elseStmt ... BinaryExpr
            }
            default -> {
            }
        }
        //if (a > b) ... BinaryExpr (left, right) operator (GREATER) codeRepresentation(>)

        //return a; ReturnStmt.NameExpr ("a")

        // var result = b -a; VariableDeclarationExpr.variables (NodeList)(0)VariableDeclarator.name = "result" .initializer BinaryExpr (Minus)


    }

    private void convExpression(Expression statement) {
        switch (MetaType.decode(statement)) {
            case TVariableDeclarationExpr -> {
                VariableDeclarationExpr vs = (VariableDeclarationExpr) statement;
                vs.getVariables().forEach(this::convVariableDeclarator);
            }
            default -> {
            }
        }
    }

    private void convExpressionStmt(ExpressionStmt statement) {
        switch (MetaType.decode(statement)) {
            case TVariableDeclarationExpr -> {

            }
            default -> {
            }
        }
    }

    private void convVariableDeclarator(VariableDeclarator i) {
        var name = i.getName().getIdentifier();
        var init = i.getInitializer().orElseThrow(IllegalStateException::new);
        convExpression(init);
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
}


