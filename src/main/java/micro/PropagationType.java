package micro;

public enum PropagationType {
    INDISCRIMINATE, SELF, CONDITION, TRUE_BRANCH, FALSE_BRANCH;

    public boolean in(PropagationType t0, PropagationType t1){
        return this == t0 || this == t1;
    }
}
