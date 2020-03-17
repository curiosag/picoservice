package micro;

import micro.atoms.Atom;
import micro.atoms.Nop;

import java.util.*;
import java.util.stream.Collectors;


public class F implements _F, Id {
    public static final Atom nop = null;

    private long id = -1;
    private String label;
    String returnAs = Names.result;
    private final Atom atom;

    private Map<String, List<FPropagation>> propagations = new TreeMap<>();
    private Map<_F, List<FPropagation>> functionsToPropagations;

    List<String> formalParameters = new ArrayList<>();

    public F(Env env, Atom atom, String... formalParams) {
        env.enlist(this);
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

    boolean hasAtom(){
        return getAtom() != Nop.nop;
    }

    boolean hasFunctionAtom(){
        return hasAtom() && ! getAtom().isSideEffect();
    }

    F returnAs(String returnAs) {
        this.returnAs = returnAs;
        return this;
    }

    @Override
    public void addPropagation(String name, _F to) {
        addPropagation(name, name, to);
    }

    @Override
    public void addPropagation(String nameExpected, String namePropagated, _F to) {
        addPropagation(new FPropagation(nameExpected, namePropagated, to));
    }

    protected void addPropagation(FPropagation p){
        assertNoAccessHappened();
        getPropagations()
                .computeIfAbsent(p.nameReceived, k -> new ArrayList<>())
                .add(p);
    }

    private void assertNoAccessHappened() {
        if (functionsToPropagations != null) {
            throw new IllegalStateException();
        }
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

    private Map<String, List<FPropagation>> getPropagations() {
        return propagations;
    }

    Map<_F, List<FPropagation>> getTargetFunctionsToPropagations() {
        if (functionsToPropagations == null) {
            functionsToPropagations = propagations.values().stream()
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.groupingBy(i -> i.target));
        }
        return functionsToPropagations;
    }

    @Override
    public String toString() {
        return  label != null ? label : "no name";
    }

    public static F f(Env env, Atom atom, String... params) {
        if(atom == null)
        {
            throw new IllegalArgumentException();
        }
        return new F(env, atom, params);
    }
}
