package micro;

import micro.primitives.Primitive;

import java.util.*;
import java.util.stream.Collectors;

import static micro.PropagationType.COND_INDISCRIMINATE;

public class F implements _F, Id {

    private long id;

    private String label;
    public String returnAs = Names.result;
    private Primitive primitive;

    private final List<FPropagation> propagations = new ArrayList<>();

    public List<String> formalParameters = new ArrayList<>();
    List<String> meansDone = new ArrayList<>();

    protected boolean isTailRecursive;

    public F(Primitive primitive) {
        this.primitive = primitive;
    }

    public F(Primitive primitive, List<String> formalParams) {
        this(primitive);
        this.formalParameters.addAll(formalParams);
    }

    public F(Primitive primitive, String... formalParams) {
        this(primitive);
        Collections.addAll(formalParameters, formalParams);
    }

    @Override
    public Address getAddress() {
        return Address.localhost;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long value) {
        this.id = value;
    }

    int numParams() {
        return formalParameters.size();
    }

    public boolean isParam(String name) {
        return formalParameters.contains(name);
    }

    void setPrimitive(Primitive primitive) {
        this.primitive = primitive;
    }

    Primitive getPrimitive() {
        return primitive;
    }

    boolean hasPrimitive() {
        return getPrimitive() != Primitive.nop;
    }

    boolean hasFunctionPrimitive() {
        return hasPrimitive() && !getPrimitive().isSideEffect();
    }

    public F returnAs(String returnAs) {
        this.returnAs = returnAs;
        return this;
    }

    public void addPropagation(String name, _F to) {
        addPropagation(COND_INDISCRIMINATE, name, name, to);
    }

    void addPropagation(String nameExpected, String namePropagated, _F to) {
        addPropagation(COND_INDISCRIMINATE, nameExpected, namePropagated, to);
    }

    @Override
    public void addPropagation(PropagationType type, String nameExpected, String namePropagated, _F to) {
        propagations.add(new FPropagation(type, nameExpected, namePropagated, to));
    }

    @Override
    public Ex createExecution(long id, _Ex returnTo, Env env) {
        return isTailRecursive ? new ExFTailRecursive( env,id, this, returnTo) : new ExF(env, id, this, returnTo);
    }

    public String getLabel() {
        return label;
    }

    public F label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public List<FPropagation> getPropagations() {
        return propagations;
    }

    public List<_F> getTargets() {
        return propagations.stream()
                .map(i -> i.target)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return label != null ? label : "no name";
    }

    public static F f(Env env, Primitive primitive, String... params) {
        if (primitive == null) {
            throw new IllegalArgumentException();
        }
        F result = new F(primitive, params);
        env.register(result);
        return result;
    }

    public static F f(Env env, String... params) {
        return f(env, Primitive.nop, params);
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

    public F tailRecursive() {
        Check.invariant(primitive == Primitive.nop, "Primitive can't be recursive");
        Check.invariant(formalParameters.size() > 0, "Recursion needs at least one parameter");

        this.isTailRecursive = true;
        return this;
    }

    @Override
    public boolean isTailRecursive() {
        return isTailRecursive;
    }

    @Override
    public void doneOn(String... params) {
        meansDone.addAll(Arrays.asList(params));
    }
}
