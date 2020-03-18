package micro;

import micro.atoms.Atom;
import micro.atoms.Nop;

import java.util.*;

import static micro.PropagationType.INDISCRIMINATE;


public class F implements _F, Id {
    public static final Atom nop = null;

    private long id = -1;
    private String label;
    String returnAs = Names.result;
    private final Atom atom;

    private Map<String, List<FPropagation>> paramNamesToPropagations = new TreeMap<>();
    private Map<_F, List<FPropagation>> targetsToPropagations = new HashMap<>();

    List<String> formalParameters = new ArrayList<>();

    public F(Env env, Atom atom, String... formalParams) {
        env.addF(this);
        this.atom = atom;
        Collections.addAll(formalParameters, formalParams);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long value) {
        id = checkSetValue(value);
    }

    int numParams() {
        return formalParameters.size();
    }

    Atom getAtom() {
        return atom;
    }

    boolean hasAtom() {
        return getAtom() != Nop.nop;
    }

    boolean hasFunctionAtom() {
        return hasAtom() && !getAtom().isSideEffect();
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

        paramNamesToPropagations.computeIfAbsent(p.nameReceived, k -> new ArrayList<>()).add(p);
        targetsToPropagations.computeIfAbsent(to, k -> new ArrayList<>()).add(p);
    }

    @Override
    public _Ex createExecution(Env env, _Ex returnTo) {
        return new ExF(env, this, returnTo);
    }

    public String getLabel() {
        return label;
    }

    public F label(String label) {
        this.label = label;
        return this;
    }

    private Map<String, List<FPropagation>> getParamNamesToPropagations() {
        return paramNamesToPropagations;
    }

    Map<_F, List<FPropagation>> getTargetFunctionsToPropagations() {
        return targetsToPropagations;
    }

    @Override
    public String toString() {
        return label != null ? label : "no name";
    }

    public static F f(Env env, Atom atom, String... params) {
        if (atom == null) {
            throw new IllegalArgumentException();
        }
        return new F(env, atom, params);
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
