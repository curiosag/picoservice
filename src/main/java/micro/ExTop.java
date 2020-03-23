package micro;

public class ExTop extends ExF {

    public ExTop(Node node) {
        super(node, new F(node, F.nop), new _Ex() {
            @Override
            public _Ex returnTo() {
                return null;
            }

            @Override
            public _F getTemplate() {
                return null;
            }

            @Override
            public void receive(Value v) {

            }

            @Override
            public Address getAddress() {
                return null;
            }

            @Override
            public long getId() {
                return 0;
            }

            @Override
            public void setId(long value) {

            }
        });
    }

    @Override
    public void perfromFunctionInputValueReceived(Value v) {
        if(v.getName().equals(Names.exception))
        {
            throw new RuntimeException((Exception) v.get());
        }
    }

    @Override
    public String toString() {
        return "TOP";
    }
}
