package miso.ingredients;

import miso.Actress;
import miso.message.Message;
import miso.message.Name;

import java.util.ArrayList;
import java.util.List;

public class Function extends Func {

    private final Func body;

    private Function(Func body) {
        this.body = body;
        body.resultTo(this);
        body.resultKey(Name.impl);
    }

    public static Function function(Func body) {
        return new Function(body);
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


}
