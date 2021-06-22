package micro.compiler;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import micro.Check;
import micro.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FNode {

    private static class AbsentValue{}
    private final AbsentValue absentValue = new AbsentValue();

    public record Propagation(String formalParam, String actualParam, FNode recipient) {
    }

    private ResolvedType type;
    public final FNode parent;
    public final Node node;

    public String returnAs = Names.result;

    public final List<FNode> children = new ArrayList<>();

    public List<String> formalParams = new ArrayList<>();
    public List<Expression> actualParams = new ArrayList<>();
    public List<Propagation> propagations = new ArrayList<>();

    public List<String> values = new ArrayList<>();
    public List<String> knownNames = new ArrayList<>();

    private Object literalValue = absentValue; // literal values

    public FNode(FNode parent, Node node, ResolvedType resolvedType) {
        this.parent = parent;
        this.node = node;
        this.type = resolvedType;
    }

    public boolean hasLiteralValue() {
        return ! absentValue.equals(literalValue);
    }

    public Object getLiteralValue() {
        Check.preCondition(hasLiteralValue());
        return literalValue;
    }

    public void setLiteralValue(Object literalValue) {
        this.literalValue = literalValue;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public void addChild(FNode n) {
        children.add(n);
    }

    public ResolvedType getType() {
        return type;
    }

    public void setType(ResolvedType type) {
        this.type = type;
    }

    public void addPropagation(String formalParam, String actualParam, FNode recipient) {
        propagations.add(new Propagation(formalParam, actualParam, recipient));

        if (!knownNames.contains(actualParam)) {
            parent.addPropagation(actualParam, actualParam, this);
            knownNames.add(actualParam);
        }
    }

    public void addFormal(String name) {
        Check.preCondition((!formalParams.contains(name)));
        formalParams.add(name);
        addKnownName(name);
    }

    public void addActual(Expression param) {
        Check.preCondition((!actualParams.contains(param)));
        actualParams.add(param);
        if (param instanceof NameExpr name)
            addKnownName(name.getName().getIdentifier());
    }

    public void addKnownName(String name)
    {
        Check.preCondition(!knownNames.contains(name));
        knownNames.add(name);
    }

    public void setReturnAs(String returnAs) {
        this.returnAs = returnAs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FNode fNode = (FNode) o;
        return Objects.equals(node, fNode.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }
}