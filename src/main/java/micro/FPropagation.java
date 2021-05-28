package micro;

public class FPropagation implements Id { //TODO should be a record, need no id
    final long id;
    public final PropagationType propagationType;
    public final String nameReceived;
    public final String nameToPropagate;
    public final _F target;

    /**
     * If this.equals(target) a reflexive propagation will be established, meaning an execution propagates values to itself
     * */
    FPropagation(long id, PropagationType propagationType, String nameReceived, String nameToPropagate, _F target) {
        this.id = id;
        this.propagationType = propagationType;
        this.nameReceived = nameReceived;
        this.nameToPropagate = nameToPropagate;
        this.target = target;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long value) {
        Check.fail("can't set id value explicitly");
    }
}