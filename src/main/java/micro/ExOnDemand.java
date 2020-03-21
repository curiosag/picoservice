package micro;

import java.util.function.Supplier;

public class ExOnDemand implements _Ex {
    Supplier<_Ex> createEx;
    _Ex ex;

    ExOnDemand(Supplier<_Ex> createEx) {
        this.createEx = createEx;
    }

    private _Ex getEx() {
        if (ex == null) {
            ex = createEx.get();
        }
        return ex;
    }

    @Override
    public _Ex returnTo() {
        return getEx().returnTo();
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
