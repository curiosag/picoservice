package micro.compiler;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import micro.Check;
import micro.Names;
import micro.PropagationType;

import java.util.*;

import static micro.Names.ping;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class FNode {

    public String assignmentTarget;

    public boolean isNative;
    private ResolvedMethodDeclaration resolvedMethodDeclaration;

    public HashSet<FNode> propagationSources = new HashSet<>();

    public ResolvedMethodDeclaration getResolvedMethodDeclaration() {
        Check.preCondition(resolvedMethodDeclaration != null);
        return resolvedMethodDeclaration;
    }

    public void setSolvedMethodDeclaration(ResolvedMethodDeclaration solved) {
        this.resolvedMethodDeclaration = solved;
    }

    private static class AbsentValue {
    }

    private final AbsentValue absentValue = new AbsentValue();

    public record Propagation(String formalParam, String actualParam, FNode recipient, PropagationType type) {
    }

    public String methodName;

    public final FNode parent;
    public final Node node;
    public String returnAs = Names.result;

    public final List<FNode> children = new ArrayList<>();
    private final Map<String, Object> consts;

    public List<String> formalParams = new ArrayList<>();

    public List<Propagation> propagations = new ArrayList<>();
    public List<String> values = new ArrayList<>();

    public List<String> knownNames = new ArrayList<>();
    private List<String> initializedVariables = new ArrayList<>();

    private Object literalValue = absentValue; // literal values

    public FNode(FNode parent, Node node, Map<String, Object> consts) {
        this.parent = parent;
        this.node = node;
        this.consts = consts;
    }

    public void noteInitialization(String name) {
        if (initializedVariables.contains(name))
            throw new IllegalStateException("repeated assignment to variable " + name);
        initializedVariables.add(name);
    }

    public boolean hasLiteralValue() {
        return !absentValue.equals(literalValue);
    }

    public Object getLiteralValue() {
        Check.preCondition(hasLiteralValue());
        return literalValue;
    }

    public void setStaticValue(Object literalValue) {
        this.literalValue = literalValue;
    }

    public void addChild(FNode n) {
        children.add(n);
    }

    public FType getType() {
        return node == null ? FType.TUnknown : FType.decode(node.getClass());
    }

    public void addPropagation(String formalParam, String actualParam, FNode recipient) {
        addPropagation(actualParam, formalParam, recipient, PropagationType.INDISCRIMINATE);
    }

    public void addPropagation(String actualParam, String formalParam, FNode recipient, PropagationType type) {
        propagations.add(new Propagation(formalParam, actualParam, recipient, type));
    }

    public void addPropagation(String name, FNode t) {
        addPropagation(name, name, t);
    }

    public void addFormal(String name) {
        Check.preCondition((!formalParams.contains(name)));
        if (!name.equals(ping))
            formalParams.add(name);
        addKnownName(name);
    }

    public void addKnownName(String name) {
        Check.preCondition(!knownNames.contains(name));
        if (!name.equals(ping)) {
            knownNames.add(name);
        }
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

    public void requestPing(FNode to) {
        addPropagation(Names.ping, Names.ping, to, getPropagationType(to.node));
    }

    /**
     * "name" needs to get propagated here
     */
    public void propagationNeededFor(Node origin, String actualName, String formalName) {
        maybeCreateConstNode(actualName);
        if (!actualName.equals(Names.result) && formalName.equals(Names.result)) { // for "return x;" with actual name "x"
            PropagationType propType = getPropagationType(origin);
            String formalNameToUse = getFormalNameToUse(formalName, propType);
            addPropagation(actualName, formalNameToUse, this, propType);
            parent.createPropagationChain(origin, actualName, actualName, this);
        } else if (fromVariableDeclarator(origin)) {
            addPropagation(formalName, actualName, this);
        } else {
            parent.createPropagationChain(origin, actualName, formalName, this);
        }
    }

    private boolean fromVariableDeclarator(Node n) {
        if (n == this.node)
            return false;
        if (n instanceof VariableDeclarator)
            return true;
        return fromVariableDeclarator(n.getParentNode().orElseThrow(IllegalStateException::new));
    }

    private String getFormalNameToUse(String formalName, PropagationType type) {
        return switch (type) {
            case INDISCRIMINATE -> formalName;
            case COND_CONDITION -> Names.condition;
            case COND_TRUE_BRANCH -> Names.onTrue;
            case COND_FALSE_BRANCH -> Names.onFalse;
        };
    }

    private void createPropagationChain(Node origin, String actualName, String formalName, FNode to) {
        to.addPropagationSource(this);
        addPropagation(actualName, formalName, to, getPropagationType(origin));
        if (!knownNames.contains(actualName)) {
            addKnownName(actualName);
            if (this.parent != null && !(this.node instanceof MethodDeclaration) && !actualName.equals(ping)) {
                parent.createPropagationChain(origin, actualName, actualName, this);
            }
        }
    }

    private void addPropagationSource(FNode fNode) {
        propagationSources.add(fNode);
    }

    public PropagationType getPropagationType(Node to) {
        if (node instanceof IfStmt ifStmt) {
            Node branchIndicator = followUpwards(to);
            if (ifStmt.getCondition().equals(branchIndicator))
                return PropagationType.COND_CONDITION;
            if (ifStmt.getThenStmt().equals(branchIndicator))
                return PropagationType.COND_TRUE_BRANCH;
            if (ifStmt.getElseStmt().isPresent() && ifStmt.getElseStmt().get().equals(branchIndicator))
                return PropagationType.COND_FALSE_BRANCH;

            throw new IllegalStateException("can't identify propagation type in IfStmt");
        }

        if (node instanceof ConditionalExpr cond) {
            Node branchIndicator = followUpwards(to);
            if (cond.getCondition().equals(branchIndicator))
                return PropagationType.COND_CONDITION;
            if (cond.getThenExpr().equals(branchIndicator))
                return PropagationType.COND_TRUE_BRANCH;
            if (cond.getElseExpr().equals(branchIndicator))
                return PropagationType.COND_FALSE_BRANCH;

            throw new IllegalStateException("can't identify propagation type in ConditionalExpr");
        }

        return PropagationType.INDISCRIMINATE;
    }

    private Node followUpwards(Node node) {
        Check.preCondition(node.getParentNode().isPresent());

        if (node.getParentNode().get().equals(this.node))
            return node;
        else
            return followUpwards(node.getParentNode().get());
    }

    private void maybeCreateConstNode(String name) {
        if (consts == null)
            return;

        Object val = consts.get(name);
        if (val != null) {
            FNode constNode = new FNode(this, null, null);
            constNode.setReturnAs(name);
            constNode.setStaticValue(val);
            addPropagation(ping, constNode);
            addKnownName(name);
        }
    }

    public Optional<String> isAssignmentTarget() {
        return Optional.ofNullable(assignmentTarget);
    }

    public void setAssignmentTarget(String value) {
        assignmentTarget = value;
    }

    @Override
    public String toString() {
        return node.getClass().getSimpleName();
    }
}