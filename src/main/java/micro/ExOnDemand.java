package micro;

public class ExOnDemand implements _Ex {
    Node node;
    Ex returnTo;
    _F targetFunc;
    _Ex ex;

    public ExOnDemand(Node node, _F targetFunc, Ex returnTo) {
        this.node = node;
        this.returnTo = returnTo;
        this.targetFunc = targetFunc;
    }

    private _Ex getEx() {
        if (ex == null) {
            ex = node.getExecution(targetFunc, returnTo);
        }
        return ex;
    }

    @Override
    public _Ex returnTo() {
        return getEx().returnTo();
    }

    @Override
    public _F getTemplate() {
        return targetFunc;
    }

    @Override
    public void receive(Value v) {
        getEx().receive(v);
    }

    @Override
    public Address getAddress() {
        return getEx().getAddress();
    }

    @Override
    public long getId() {
        return getEx().getId();
    }

    @Override
    public void setId(long value) {
        getEx().setId(value);
    }

}