package micro;

public class ExOnDemand implements _Ex {
    Node node;
    Ex returnTo;
    _F template;
    _Ex ex;

    public ExOnDemand(Node node, _F template, Ex returnTo) {
        this.node = node;
        this.returnTo = returnTo;
        this.template = template;
    }

    private _Ex getEx() {
        if (ex == null) {
            ex = node.getExecution(template, returnTo);
        }
        return ex;
    }

    @Override
    public _Ex returnTo() {
        return getEx().returnTo();
    }

    @Override
    public _F getTemplate() {
        return template;
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
