package micro;

public class ExTop implements  _Ex, Id {

    public final static long TOP_ID = 0;

    private final _F template;
    private final Address address;

    public ExTop(Address address) {
        this.address = address;
        this.template = createTemplate();
    }

    private _F createTemplate() {
        return new _F(){
            @Override
            public long getId() {
                return TOP_ID;
            }

            @Override
            public void setId(long value) {
                throw new IllegalStateException();
            }

            @Override
            public void addPropagation(PropagationType type, String nameExpected, String namePropagated, _F to) {
                throw new IllegalStateException();
            }

            @Override
            public _Ex createExecution(_Ex returnTo) {
                throw new IllegalStateException();
            }

        };
    }

    @Override
    public String toString() {
        return "TOP";
    }

    @Override
    public _Ex returnTo() {
        return this;
    }

    @Override
    public _F getTemplate() {
        return template;
    }

    @Override
    public void receive(Value v) {

    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public long getId() {
        return TOP_ID;
    }

    @Override
    public void setId(long value) {

    }
}
