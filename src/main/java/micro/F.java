package micro;

import micro.atomicFunctions.Atom;

import java.util.*;
import java.util.stream.Collectors;

public class F {
    private String label;
    String returnAs = Names.result;
    private final Atom atom;

    private Map<String, List<FPropagation>> propagations = new TreeMap<>();
    private Map<F, List<FPropagation>> functionsToPropagations;

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

    void addPropagation(String name, F to) {
        addPropagation(name, name, to);
    }

    void addPropagation(String nameExpected, String namePropagated, F to) {
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

    public Ex newExecution(Env env, Ex returnTo) {
        return new ExF(env, this, returnTo);
    }

    public String getLabel() {
        return label;
    }

    public F setLabel(String label) {
        this.label = label;
        return this;
    }

    private Map<String, List<FPropagation>> getPropagations() {
        return propagations;
    }

    Map<F, List<FPropagation>> getTargetFunctionsToPropagations() {
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

}
