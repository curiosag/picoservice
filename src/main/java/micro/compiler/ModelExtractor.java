package micro.compiler;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import micro.Check;
import micro.F;
import micro.Names;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.expr.UnaryExpr.Operator.LOGICAL_COMPLEMENT;
import static micro.compiler.FType.TBlockStmt;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalGetWithoutIsPresent"})
public class ModelExtractor {

    private boolean classUndermined;

    private static final FNode root = new FNode(null, null, null);

    private final JavaParserFacade solver;

    private final Map<String, Object> consts = new HashMap<>();
    private final List<FNode> nodes = new ArrayList<>();

    public ModelExtractor(JavaParserFacade solver) {
        this.solver = solver;
    }

    public List<F> get(Node compilationUnit) {
        FNode parsingRoot = createTargetNode(root, compilationUnit, Names.result);
        FNode n = node(parsingRoot, compilationUnit, null);
        nodes.stream()
                .filter(i -> i.propagationSources.size() == 0)
                .forEach(i -> i.parent.requestPing(i));

        compilationUnit.findAll(FieldDeclaration.class).forEach(this::fieldDeclaration);
        classUndermined = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).map(
                c -> c.getAnnotations().stream().anyMatch(i -> i.getNameAsString().equals(Undermine.class.getSimpleName()))
        ).orElse(false);

        return n.children.stream()
                .map(PicoServiceBackend::map)
                .collect(Collectors.toList());
    }

    private FNode node(FNode parent, Node n, String returnAs) {
        var fType = FType.decode(n.getClass());

        FNode t = fType != FType.TUnknown && decideOnBlock(n, fType) ? createTargetNode(parent, n, returnAs) : parent;

        switch (fType) {
            case TMethodDeclaration -> methodDeclaration(t, (MethodDeclaration) n);
            case TUnaryExpr -> unaryExpr(t, (UnaryExpr) n);
            case TBinaryExpr -> binaryExpr(t, (BinaryExpr) n);
            case TIfStmt -> ifStmt(t, (IfStmt) n);
            case TConditionalExpr -> conditionalExpr(t, (ConditionalExpr) n);
            case TMethodCallExpr -> methodCallExpr(t, (MethodCallExpr) n);
            case TBlockStmt -> children(n, t, returnAs);
            case TLiteralExpr -> literalExpr((LiteralExpr) n, t);
            case TUnknown -> nonTargetNode(t, n, returnAs);
        }

        return t;
    }

    private void literalExpr(LiteralExpr n, FNode t) {
        t.setStaticValue(interpretValue(n));
    }

    private boolean decideOnBlock(Node n, FType fType) {
        // block statements of method declarations don't get an own target model node
        // neither do single-statement/expression blocks
        if (fType == TBlockStmt && n.getChildNodes().size() == 1)
            return false;
        return fType != TBlockStmt || !(n.getParentNode().get() instanceof MethodDeclaration);
    }

    /**
     * something that doesn't get an own node in the target model
     */
    private void nonTargetNode(FNode t, Node n, String returnAs) {
        if (n instanceof VariableDeclarationExpr e) {
            e.getVariables().forEach(v -> variableDeclarator(t, v));
        } else if (n instanceof ReturnStmt s) {
            returnStmt(t, s);
        } else if (n instanceof FieldAccessExpr e) {
            t.propagationNeededFor(n, e.getScope() + "." + e.getNameAsString(), returnAs);
        } else if (n instanceof NameExpr e) {
            t.propagationNeededFor(n, e.getNameAsString(), returnAs);
        } else if (n instanceof AssignExpr e) {
            assignExpr(t, e);
        } else
            children(n, t, returnAs);
    }

    private void assignExpr(FNode t, AssignExpr e) {
        String name = e.getTarget().toString();
        if (t.knownNames.contains(name)) { // a variable, previously at this FNode defined but not initialized ("int x;") gets initialized "x=1;"
            t.noteInitialization(name);
            node(t, e.getValue(), name);
        } else {
            node(t, e.getValue(), Names.result);
            propagateAssignedResultValue(t, t.parent, name);
        }
    }

    /**
     * var x;
     * if(a) x = 0 else x = 1; // a single defined variable outside the if statements gets initialized inside
     */
    public void propagateAssignedResultValue(FNode node, FNode parent, String name) {
        if (parent == root)
            throw new IllegalStateException();

        if (parent.knownNames.contains(name)) {
            node.setReturnAs(name);
        } else {
            propagateAssignedResultValue(parent, parent.parent, name);
        }
    }

    private void returnStmt(FNode t, ReturnStmt v) {
        v.getExpression().ifPresentOrElse(e -> node(t, e, Names.result), () -> {
            throw new IllegalStateException("Return with void result not covered");
        });
    }

    private void variableDeclarator(FNode t, VariableDeclarator v) {
        String name = v.getName().getIdentifier();
        v.getInitializer().ifPresentOrElse(e -> {
            t.noteInitialization(name);
            node(t, e, name);
        }, () -> t.knownNames.add(name));
    }

    private void methodCallExpr(FNode t, MethodCallExpr n) {
        ResolvedMethodDeclaration solved = solver.solve(n).getCorrespondingDeclaration();
        t.setSolvedMethodDeclaration(solved);

        boolean undermined = classUndermined || solved instanceof JavaParserMethodDeclaration m &&
                m.getWrappedNode().getAnnotations().stream()
                        .anyMatch(i -> i.getNameAsString().equals(Undermine.class.getSimpleName()));

        t.isNative = !undermined || solved instanceof ReflectionMethodDeclaration;

        for (int i = 0; i < solved.getNumberOfParams(); i++) {
            String formalName = solved.getParam(i).getName();
            t.addFormal(formalName);
            node(t, n.getArguments().get(i), formalName);
        }
    }

    private void conditionalExpr(FNode t, ConditionalExpr n) {
        node(t, n.getCondition(), Names.condition);
        node(t, n.getThenExpr(), Names.onTrue);
        node(t, n.getElseExpr(), Names.onFalse);
    }

    private void ifStmt(FNode t, IfStmt n) {
        node(t, n.getCondition(), Names.condition);
        node(t, n.getThenStmt(), Names.onTrue);
        n.getElseStmt().ifPresent(els -> node(t, els, Names.onFalse));
    }

    private void methodDeclaration(FNode t, MethodDeclaration n) {
        ResolvedType type = n.getType().resolve();
        n.getParameters().forEach(p -> t.addFormal(p.getName().getIdentifier()));
        children(n, t, Names.result);
    }

    private void binaryExpr(FNode t, BinaryExpr n) {
        t.addFormal(Names.left);
        t.addFormal(Names.right);

        node(t, n.getLeft(), Names.left);
        node(t, n.getRight(), Names.right);
    }

    private void unaryExpr(FNode t, UnaryExpr n) {
        Check.preCondition(n.getOperator() == LOGICAL_COMPLEMENT);
        t.addFormal(Names.arg);

        node(t, n.getExpression(), Names.arg);
    }

    private void children(Node n, FNode t, String returnAs) {
        for (var i : n.getChildNodes())
            node(t, i, returnAs);
    }

    public FNode createTargetNode(FNode parent, Node node, String returnAs) {
        Check.notNull(node);

        FNode result = new FNode(parent, node, consts);
        result.setReturnAs(returnAs);
        parent.addChild(result);
        nodes.add(result);
        return result;
    }

    private Object interpretValue(LiteralExpr lit) {
        return switch (lit.getClass().getSimpleName()) {
            case "BooleanLiteralExpr" -> lit.asBooleanLiteralExpr().getValue();
            case "IntegerLiteralExpr" -> lit.asIntegerLiteralExpr().asNumber() instanceof Integer i ? i : Integer.valueOf(Integer.MIN_VALUE);
            case "LongLiteralExpr" -> lit.asLongLiteralExpr().asNumber() instanceof Long l ? l : Long.valueOf(Long.MIN_VALUE);
            case "CharLiteralExpr" -> lit.asCharLiteralExpr().asChar();
            case "DoubleLiteralExpr" -> lit.asDoubleLiteralExpr().asDouble();
            case "StringLiteralExpr" -> lit.asStringLiteralExpr().getValue();
            case "TextBlockLiteralExpr" -> lit.asTextBlockLiteralExpr().getValue();
            case "LiteralStringValueExpr" -> lit.asLiteralStringValueExpr().getValue();
            case "NullLiteralExpr" -> null;
            default -> throw new IllegalStateException("Literal type not covered: " + lit.getClass().getName());
        };
    }

    private void fieldDeclaration(FieldDeclaration fieldDeclaration) {
        fieldDeclaration.getVariables().forEach(v -> {
            Optional<Expression> initializer = v.getInitializer();
            Check.preCondition(initializer.isPresent());
            if (initializer.get() instanceof LiteralExpr lit)
                consts.put(v.getNameAsString(), interpretValue(lit));
            else if (initializer.get() instanceof FieldAccessExpr acc) {
                SymbolReference<ResolvedValueDeclaration> solv = solver.solve(acc);
                throw new IllegalStateException("FieldAccessExpr not covered");
            } else
                throw new IllegalStateException("only values supported for fields");
        });
    }
}
