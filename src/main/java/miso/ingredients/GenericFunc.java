package miso.ingredients;

import miso.Actress;
import miso.Message;
import miso.Name;

import java.util.ArrayList;
import java.util.List;

public class GenericFunc extends Func {

    private final Actress implementation;

    protected GenericFunc(Func implementation) {
        this.implementation = implementation;
        implementation.resultTo(this);
        implementation.resultKey(Name.impl);
    }

    public static final GenericFunc func(Func implementation) {
        return new GenericFunc(implementation);
    }

    @Override
    public void recieve(Message message) {
        System.out.println(this.getClass().getSimpleName() + " receiving \n" + message.toString());

        Object result = message.get(Name.impl);
        if (result == null) {
            List<Actress> allTargets = implementation.getAllTargets(this, new ArrayList<>());
            allTargets.forEach(t -> t.recieve(message));
        }
        else {
            setCurrent(Message.of(resultKey, result));
            send(getNext());
        }
    }

    @Override
    protected Message getNext() {
       return getCurrent().orElseThrow(() -> new IllegalStateException());
    }

    public GenericFunc params(String... params) {
        return this;
    }


}
