package micro.compiler;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import micro.Check;
import micro.F;
import micro.Names;

import java.util.List;

import static com.github.javaparser.ast.expr.UnaryExpr.Operator.LOGICAL_COMPLEMENT;
import static micro.compiler.FType.TBlockStmt;

public class Compiler {

    private static final FNode root = new FNode(null, null, null);

    private final JavaParserFacade solver;

    public Compiler(JavaParserFacade solver) {
        this.solver = solver;
    }

    public List<F> compile(Node node) {
        FNode parsingRoot = createTargetNode(root, node);
        FNode n = node(parsingRoot, node);
        return TargetModelMapper.map(n);
    }

    private FNode node(FNode parent, Node n) {
        var fType = FType.decode(n.getClass());

        FNode t = fType != FType.TUnknown && decideOnBlock(n, fType) ? createTargetNode(parent, n) : parent;

        switch (fType) {
            case TMethodDeclaration -> methodDeclaration(t, (MethodDeclaration) n);
            case TUnaryExpr -> unaryExpr(t, (UnaryExpr) n);
            case TBinaryExpr -> binaryExpr(t, (BinaryExpr) n);
            case TIfStmt -> ifStmt(t, (IfStmt) n);
            case TConditionalExpr -> conditionalExpr(t, (ConditionalExpr) n);
            case TMethodCallExpr -> methodCallExpr(t, (MethodCallExpr) n);
            case TBlockStmt -> compileChildren(n, t);
            case TUnknown, TCompilationUnit -> nonTargetNode(t, n);
        }
        return t;
    }

    private boolean decideOnBlock(Node n, FType fType) {
        // block statements of method declarations don't get an own target model node
        return fType != TBlockStmt || !(n.getParentNode().get() instanceof MethodDeclaration);
    }

    /**
     * something that doesn't get an own node in the target model
     * */
    private void nonTargetNode(FNode t, Node n) {
        if (n instanceof VariableDeclarationExpr vde) {
            vde.getVariables().forEach(v -> variableDeclarator(t, v));
        } else if (n instanceof ReturnStmt v) {
            returnStmt(t, v);
        } else compileChildren(n, t);
    }

    private void returnStmt(FNode t, ReturnStmt v) {
        v.getExpression().ifPresent(e -> namedExpr(t, Names.result, e));
    }

    private void variableDeclarator(FNode t, VariableDeclarator v) {
        String name = v.getName().getIdentifier();
        v.getInitializer().ifPresentOrElse(e -> namedExpr(t, name, e), () -> t.knownNames.add(name));
    }

    /* statements:
    *
    * VariableDeclaratorExpr.variables: List<VariableDeclarator>
    *                             |
    *                           VariableDeclarator (type: ClassOrInterfaceType, name: SimpleName, initializer:Expression)
    *
    * AssignExpr (target:NameExpr, value: Expression)
    * */

    // as in those parent nodes:
    // return statement: return name;
    // assignment: int a = name;
    // method call: max(name, 1)
    // expression: name + name
    private void namedExpr(FNode t, String returnAs, Expression e) {
        t.parent.addKnownName(returnAs);

        if (e instanceof NameExpr expr) {
            t.addPropagation(returnAs, expr.getNameAsString(), t);
        } else if (e instanceof LiteralExpr lit) {
            Object val = switch (lit.getClass().getSimpleName()) {
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
            t.setLiteralValue(val);
        } else if (e instanceof AssignExpr ass) {

        } else node(t, e).setReturnAs(returnAs);
    }

    private void methodCallExpr(FNode t, MethodCallExpr n) {
        t.actualParams.addAll(n.getArguments());
        resolveCorrespondingDeclaration(t, n);
        for (int i = 0; i < t.actualParams.size(); i++) {
            namedExpr(t, t.formalParams.get(i), t.actualParams.get(i));
        }
    }

    private void resolveCorrespondingDeclaration(FNode t, MethodCallExpr call) {
        ResolvedMethodDeclaration corr = solver.solve(call).getCorrespondingDeclaration();
        for (int i = 0; i < corr.getNumberOfParams(); i++)
            t.addFormal(corr.getParam(i).getName());
    }

    private void conditionalExpr(FNode t, ConditionalExpr n) {
        compileChildren(n, t);
    }

    private void ifStmt(FNode t, IfStmt n) {
        namedExpr(t, Names.condition, n.getCondition());

        ifBranch(t, Names.onTrue, n.getThenStmt());
        n.getElseStmt().ifPresent(els ->
                ifBranch(t, Names.onFalse, els));
    }

    private void ifBranch(FNode t, String returnAs, Statement stmt) {
        if (stmt instanceof BlockStmt block) { /// ... then { ... block ... }
            node(t, block);
        } else if (stmt instanceof ReturnStmt ret) { // ... then return x;
            namedExpr(t, returnAs, ret.getExpression().orElseThrow(IllegalStateException::new));
        } else if (stmt instanceof ExpressionStmt ex && ex.getExpression() instanceof AssignExpr ass) {
            if (ass.getTarget() instanceof NameExpr name) { // ... then a = 1;
                hdlResultAsAssignment(node(t, ass.getValue()), name.getNameAsString());
            } else
                throw new IllegalStateException();
        }
    }

    /**
     * var x;
     * if(a) x = 0 else x = 1; // a single defined variable outside the if statements gets initialized inside
     * */
    private void hdlResultAsAssignment(FNode t, String name) {
        FNode current = t;
        while (!current.parent.knownNames.contains(name)) {
            current = current.parent;
            if (current == root)
                throw new IllegalStateException();
        }
        current.setReturnAs(name); // the right side of the assignment (x = 0;) produces a "result" value being passed upwards.
                                   // at the last step it must be passed on as "x", not as "result"
    }

    private void methodDeclaration(FNode t, MethodDeclaration n) {
        t.setType(n.getType().resolve());
        n.getParameters().forEach(p -> t.addFormal(p.getName().getIdentifier()));
        compileChildren(n, t);
    }

    private void binaryExpr(FNode t, BinaryExpr n) {
        t.addFormal(Names.left);
        t.addFormal(Names.right);

        t.addActual(n.getLeft());
        t.addActual(n.getRight());

        Check.invariant(t.formalParams.size() == t.actualParams.size());

        for (int i = 0; i < t.actualParams.size(); i++) {
            if (t.actualParams.get(i) instanceof NameExpr name)
                t.parent.addPropagation(t.formalParams.get(i), name.getName().getIdentifier(), t);
        }

        compileChildren(n, t);
    }

    private void unaryExpr(FNode t, UnaryExpr n) {
        Check.preCondition(n.getOperator() == LOGICAL_COMPLEMENT);
        t.addActual(n.getExpression());
        t.addFormal(Names.arg);

        compileChildren(n, t);
    }

    private void compileChildren(Node n, FNode t) {
        for (var i : n.getChildNodes())
            node(t, i);
    }

    public FNode createTargetNode(FNode parent, Node node) {
        Check.notNull(node);

        FNode result = new FNode(parent, node, resolveType(node));

        parent.addChild(result);
        return result;
    }

    private ResolvedType resolveType(Node node) {
        try {
            if (node instanceof Expression i) {
                return solver.getType(i, true);
            }
        } catch (RuntimeException e) {
        }
        return null;
    }


}
