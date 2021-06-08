package micro;

public enum PropagationType {
    COND_INDISCRIMINATE("black"), COND_CONDITION("blue"), COND_TRUE_BRANCH("green"), COND_FALSE_BRANCH("red");

    PropagationType(String color) {
        this.color = color;
    }

    public boolean in(PropagationType t0, PropagationType t1){
        return this == t0 || this == t1;
    }

    public final String color;
}
