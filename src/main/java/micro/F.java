package micro;

import micro.atoms.Primitive;

import java.util.*;
import java.util.function.Supplier;

import static micro.PropagationType.INDISCRIMINATE;


public class F implements _F, Id {

    private final Supplier<Long> nextPropagationId;
    private final long id;
    private String label;
    public String returnAs = Names.result;
    private Primitive primitive;

    private Map<_F, List<FPropagation>> targetsToPropagations = new HashMap<>();

    List<String> formalParameters = new ArrayList<>();

    public F(Node node, Primitive primitive, List<String> formalParams) {
        this.id = node.getNextFId();
        node.addF(this);
        this.nextPropagationId = node::getNextObjectId;
        this.primitive = primitive;
        this.formalParameters.addAll(formalParams);
    }

    public F(Node node, Primitive primitive, String ... formalParams) {
        this(node, primitive, Collections.emptyList());
        Collections.addAll(formalParameters, formalParams);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long value) {
        Check.fail("no call expected");
    }

    int numParams() {
        return formalParameters.size();
    }

    void setPrimitive(Primitive primitive) {
        this.primitive = primitive;
    }

    Primitive getPrimitive() {
        return primitive;
    }

    boolean hasAtom() {
        return getPrimitive() != Primitive.nop;
    }

    boolean hasFunctionAtom() {
        return hasAtom() && !getPrimitive().isSideEffect();
    }

    F returnAs(String returnAs) {
        this.returnAs = returnAs;
        return this;
    }

    public void addPropagation(String name, _F to) {
        addPropagation(INDISCRIMINATE, name, name, to);
    }

    void addPropagation(String nameExpected, String namePropagated, _F to) {
        addPropagation(INDISCRIMINATE, nameExpected, namePropagated, to);
    }

    @Override
    public void addPropagation(PropagationType type, String nameExpected, String namePropagated, _F to) {
        FPropagation p = new FPropagation(nextPropagationId.get(), type, nameExpected, namePropagated, to);
        targetsToPropagations
                .computeIfAbsent(p.target, k -> new ArrayList<>())
                .add(p);
    }

    @Override
    public _Ex createExecution(Node node, _Ex returnTo) {
        return new ExF(node, this, returnTo);
    }

    public String getLabel() {
        return label;
    }

    public F label(String label) {
        this.label = label;
        return this;
    }

    Map<_F, List<FPropagation>> getTargetFunctionsToPropagations() {
        return targetsToPropagations;
    }

    @Override
    public String toString() {
        return label != null ? label : "no name";
    }

    public static F f(Node node, Primitive primitive, String... params) {
        if (primitive == null) {
            throw new IllegalArgumentException();
        }
        return new F(node, primitive, params);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        F f = (F) o;
        return id == f.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
