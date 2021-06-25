package micro.compiler;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import micro.Check;
import micro.F;
import micro.FCall;
import micro.If.If;
import micro.primitives.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.javaparser.ast.expr.BinaryExpr.Operator.*;
import static micro.primitives.CallByReflection.callByReflection;
import static micro.primitives.Primitive.nop;

public class PicoServiceBackend {
    private static int fid = 0;

    private static Map<BinaryExpr.Operator, Primitive> binOp = new HashMap<>() {{
        put(BINARY_OR, Or.or());
        put(BINARY_AND, And.and());
        put(EQUALS, Eq.eq());
        put(NOT_EQUALS, NEq.neq());
        put(LESS, Lt.lt());
        put(GREATER, Gt.gt());
        put(LESS_EQUALS, Lteq.lteq());
        put(GREATER_EQUALS, Gteq.gteq());
        put(PLUS, Plus.plus());
        put(MINUS, Minus.minus());
        put(MULTIPLY, Mul.mul());
        put(DIVIDE, Div.div());
        put(REMAINDER, Mod.mod());
    }};

    public static F map(FNode node) {
        Check.preCondition(node.node instanceof MethodDeclaration);
        return get(node);
    }

    private static F get(FNode n) {
        F f = switch (FType.decode(n.node.getClass())) {
            case TMethodDeclaration -> new F(nop, n.formalParams).label(((MethodDeclaration) n.node).getNameAsString());
            case TBlockStmt -> new F(nop, n.formalParams).label("block");
            case TUnaryExpr -> new F(Not.not(), n.formalParams).label("!");
            case TBinaryExpr -> new F(getPrimitive(((BinaryExpr) n.node)), n.formalParams).label(((BinaryExpr) n.node).getOperator().asString());
            case TIfStmt, TConditionalExpr -> getIf(n).label("if");
            case TMethodCallExpr -> methodCall(n);
            case TLiteralExpr -> new F(new Constant(n.getLiteralValue())).label(n.getLiteralValue().toString());
            case TUnknown -> throw new IllegalStateException();
        };

        f.setId(++fid);
        f.returnAs(n.returnAs);

        Map<FNode, F> mapped = new HashMap<>();
        n.children.forEach(c -> mapped.put(c, get(c)));
        n.propagations.forEach(p -> {
            F target = mapped.get(p.recipient());
            if (target == null) {
                if (p.recipient().equals(n)) { // a propagation to itself
                    target = f;
                } else {
                    throw new IllegalStateException();
                }
            }
            f.addPropagation(p.type(), p.actualParam(), p.formalParam(), target);
        });
        return f;
    }

    private static F methodCall(FNode n) {
        MethodCallExpr call = (MethodCallExpr) n.node;

        if (n.isNative) {
            ResolvedMethodDeclaration called = n.getResolvedMethodDeclaration();
            String name = ((called.getPackageName().equals("") ? "" : called.getPackageName() + '.'))
                    + called.getClassName() + '.' + called.getName();
            return new F(getCallByReflection(n, (MethodCallExpr) n.node, n.formalParams), n.formalParams)
                    .label(name);
        } else {
            String name = call.getScope().map(i -> i.toString() + '.').orElse("") +
                    call.getNameAsString();
            return new FCall(new F(nop, n.formalParams)).label(name);
        }
    }

    private static Primitive getCallByReflection(FNode n, MethodCallExpr node, List<String> formalParams) {

        return callByReflection("", node.getNameAsString(), formalParams);
    }

    private static F getIf(FNode n) {
        return new If();
    }


    private static Primitive getPrimitive(BinaryExpr b) {
        return Optional.ofNullable(binOp.get(b.getOperator()))
                .orElseThrow(() -> new IllegalStateException("Operator not covered: " + b.getOperator().asString()));
    }

}
