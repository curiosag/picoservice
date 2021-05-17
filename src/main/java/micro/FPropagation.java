package micro;

class FPropagation implements Id {
    final long id;
    final PropagationType propagationType;
    final String nameReceived;
    final String nameToPropagate;
    final _F target;

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