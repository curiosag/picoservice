package miso.ingredients;

import miso.Actress;
import miso.Message;
import miso.Name;

import java.util.ArrayList;
import java.util.List;

public class GenericFunc extends Func {

    private final Func body;

    private GenericFunc(Func body) {
        this.body = body;
        body.resultTo(this);
        body.resultKey(Name.impl);
    }

    public static GenericFunc func(Func body) {
        return new GenericFunc(body);
    }

    @Override
    public void recieve(Message message) {
        System.out.println(this.getClass().getSimpleName() + " receiving \n" + message.toString());

        Object result = message.get(Name.impl);
        if (result == null) {
            List<Actress> allTargets = body.getAllTargets(this, new ArrayList<>());
            allTargets.forEach(t -> t.recieve(message));
        }
        else {
            setCurrent(Message.of(resultKey, result));
            send(getNext());
        }
    }

    @Override
    protected Message getNext() {
       return getCurrent().orElseThrow(IllegalStateException::new);
    }

    public GenericFunc params(String... params) {
        return this;
    }


}
