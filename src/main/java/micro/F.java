package micro;

import micro.atoms.Atom;

import java.util.*;
import java.util.stream.Collectors;

import static micro.ExTop.TOP;

public class F implements _F {
    private String label;
    String returnAs = Names.result;
    private final Atom atom;

    private Map<String, List<FPropagation>> propagations = new TreeMap<>();
    private Map<_F, List<FPropagation>> functionsToPropagations;

    List<String> formalParameters = new ArrayList<>();

    public F(Atom atom, String... formalParams) {
        this.atom = atom;
        Collections.addAll(formalParameters, formalParams);
    }

    public F(String... formalParams) {
        this.atom = null;
        Collections.addAll(this.formalParameters, formalParams);
    }

    int numParams() {
        return formalParameters.size();
    }

    Atom getAtom() {
        return atom;
    }

    F returnAs(String returnAs) {
        this.returnAs = returnAs;
        return this;
    }

    @Override
    public void addPropagation(String name, _F to) {
        addPropagation(name, name, to);
    }

    public void ping(_F to) {
        addPropagation(Names.ping, Names.ping, to);
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
    public Ex createExecution(Env env, Ex returnTo) {
        return new ExF(env, this, returnTo);
    }

    public Ex createExecution(Env env){
        return createExecution(env, TOP);
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

    public static F f(Atom atom, String... params) {
        if(atom == null)
        {
            throw new IllegalArgumentException();
        }
        return new F(atom, params);
    }
}
