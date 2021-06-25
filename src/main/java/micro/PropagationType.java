package micro;

public enum PropagationType {
    INDISCRIMINATE, COND_CONDITION, COND_TRUE_BRANCH, COND_FALSE_BRANCH;

    public boolean in(PropagationType t0, PropagationType t1){
        return this == t0 || this == t1;
    }

}
