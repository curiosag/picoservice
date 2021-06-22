package micro.compiler;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import micro.Check;
import micro.F;
import micro.If.If;
import micro.PropagationType;
import micro.primitives.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.expr.BinaryExpr.Operator.*;
import static micro.primitives.CallByReflection.callByReflection;

public class TargetModelMapper {
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

    public static List<F> map(FNode node) {
        Check.preCondition(node.node instanceof CompilationUnit);
        return node.children.stream()
                .map(TargetModelMapper::toTargetModel)
                .collect(Collectors.toList());
    }

    private static F toTargetModel(FNode n) {
        F f = switch (FType.decode(n.node.getClass())) {
            case TMethodDeclaration -> new F(Primitive.nop, n.formalParams).label(((MethodDeclaration)n.node).getNameAsString());
            case TBlockStmt -> new F(Primitive.nop, n.formalParams).label("block");
            case TUnaryExpr -> new F(Not.not(), n.formalParams).label("!");
            case TBinaryExpr -> new F(getPrimitive(((BinaryExpr) n.node)), n.formalParams).label(((BinaryExpr) n.node).getOperator().asString());
            case TIfStmt -> getIf(n).label("if");
            case TMethodCallExpr -> new F(getCallByReflection(n, (MethodCallExpr) n.node)).label(((MethodCallExpr) n.node).getNameAsString());
            case TConditionalExpr, TCompilationUnit, TUnknown -> throw new IllegalStateException();
        };

        f.setId(++fid);
        f.returnAs(n.returnAs);

        Map<FNode, F> mapped = new HashMap<>();
        n.children.forEach(c -> mapped.put(c, toTargetModel(c)));
        n.propagations.forEach(p -> {
            F target = mapped.get(p.recipient());
            if (target != null)
                //throw new IllegalStateException();
            f.addPropagation(PropagationType.COND_INDISCRIMINATE, p.actualParam(), p.formalParam(), target);
        });
        return f;
    }

    private static Primitive getCallByReflection(FNode n, MethodCallExpr node) {
        return callByReflection("", node.getNameAsString(), n.formalParams);
    }

    private static F getIf(FNode n) {
        F result = new If();
        IfStmt iff = (IfStmt) n.node;
        Expression cond = iff.getCondition();
        Statement thenStmt = iff.getThenStmt();
        Statement elseStmt = iff.getElseStmt().orElse(null);
//        F fCond =
//        n.propagations.forEach(p -> {
//            Node r = p.recipient().node;
//            final PropagationType t;
//            if (r.equals(cond)) {
//                t = PropagationType.COND_CONDITION;
//            } else if (r.equals(thenStmt)) {
//                t = PropagationType.COND_TRUE_BRANCH;
//            } else if (r.equals(elseStmt)) {
//                t = PropagationType.COND_FALSE_BRANCH;
//            } else throw new IllegalStateException("Propagation type not availabe in IfStmt");
//            result.addPropagation(t, p.formalParam(), p.actualParam(), null); //TODO
//        });
        return result;
    }



    private static Primitive getPrimitive(BinaryExpr b) {
        return Optional.ofNullable(binOp.get(b.getOperator()))
                .orElseThrow(() -> new IllegalStateException("Operator not covered: " + b.getOperator().asString()));
    }

}
