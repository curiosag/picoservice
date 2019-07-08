package miso.ingredients;

import miso.Actress;
import miso.message.Adresses;
import miso.message.Message;

import static miso.ingredients.DNS.dns;
import static miso.message.CellRead.cellRead;

public class FAgg extends Func {

    private Agg agg = new Agg();
    private final Func target;


    private FAgg(Func target) {
        this.target = target;

        agg.resultTo(target);
        agg.paramsRequired(target.paramsRequired);
    }

    @Override
    public FAgg resultTo(Actress r) {
        target.resultTo(r);
        return this;
    }

    @Override
    public FAgg resultTo(Actress... r) {
        target.resultTo(r);
        return this;
    }

    @Override
    public FAgg resultKey(String name) {
        target.resultKey(name);
        return this;
    }

    @Override
    public FAgg paramsRequired(String... params) {
        agg.paramsRequired(params);
        target.paramsRequired(params);
        return this;
    }

    @Override
    public void recieve(Message message) {
        agg.recieve(resolveSymbols(message));
    }

    private Message resolveSymbols(Message message) {
        Message result = new Message();
        message.params.entrySet().forEach(
                e -> {
                    if (e.getValue() instanceof Address) {
                        Actress cells = dns().resolve(Adresses.cells);
                        Address cellAddress = (Address) e.getValue();
                        cells.recieve(cellRead(cellAddress).recipient(agg.address, e.getKey()));
                    } else {
                        result.put(e.getKey(), e.getValue());
                    }
                }
        );

        return result;
    }


    public static FAgg fagg(Func target) {
        return new FAgg(target);
    }

    @Override
    protected Message getNext() {
        throw new IllegalStateException();
    }
}
