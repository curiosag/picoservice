package micro;

import micro.primitives.Primitive;
import nano.ingredients.tuples.Tuple;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static micro.PropagationType.INDISCRIMINATE;


public class F implements _F, Id {

    private final Supplier<Long> nextPropagationId;
    private final long id;
    protected final Node node;
    private String label;
    public String returnAs = Names.result;
    private Primitive primitive;

    private List<Tuple<_F, List<FPropagation>>> targetsToPropagations = new ArrayList<>();

    List<String> formalParameters = new ArrayList<>();

    private F(Node node, Primitive primitive) {
        this.node = node;
        this.id = node.getNextFId();
        node.addF(this);
        this.nextPropagationId = node::getNextExId;
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

    public boolean isParam(String name){
        return formalParameters.contains(name);
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

        Optional<Tuple<_F, List<FPropagation>>> current = targetsToPropagations.stream()
                .filter(i -> i.left.equals(to))
                .findAny();

        if (current.isEmpty())
        {
            current = Optional.of(Tuple.of(to, new ArrayList<>()));
            targetsToPropagations.add(current.get());
        }
        current.get().right.add(p);
    }

    @Override
    public Ex createExecution(long id, _Ex returnTo) {
        return new ExF(this.node, id,this, returnTo);
    }

    public String getLabel() {
        return label;
    }

    public F label(String label) {
        this.label = label;
        return this;
    }

    List<Tuple<_F, List<FPropagation>>> getTargetFunctionsToPropagations() {
        return targetsToPropagations;
    }

    public List<_F> getTargets(){
        return targetsToPropagations.stream().map(i -> i.left).collect(Collectors.toList());
    }

    public int getTargetCount(){
        return targetsToPropagations.size();
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
