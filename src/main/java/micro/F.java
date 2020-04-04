package micro;

import micro.primitives.Primitive;

import java.util.*;

import static micro.PropagationType.INDISCRIMINATE;


public class F implements _F, Id {

    private final long id;
    private String label;
    String returnAs = Names.result;
    private Primitive primitive;

    private Map<_F, List<FPropagation>> targetsToPropagations = new HashMap<>();

    List<String> formalParameters = new ArrayList<>();

    private F(Node node, Primitive primitive) {
        this.id = IdType.F.next();
        node.addF(this);
        this.primitive = primitive;
    }

    public F(Node node, Primitive primitive, List<String> formalParams) {
        this(node, primitive);
        this.formalParameters.addAll(formalParams);
    }

    public F(Node node, Primitive primitive, String ... formalParams) {
        this(node, primitive);
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
        FPropagation p = new FPropagation(type, nameExpected, namePropagated, to);
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

    @Override
    public String toString() {
        return "F:{" +
                "id:" + id +
                ", label:'" + label + '\'' +
                '}';
    }
}
