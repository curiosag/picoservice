package miso.ingredients;

import miso.Actress;
import miso.Message;

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
        agg.recieve(message);
    }

    public static FAgg fagg(Func target) {
        return new FAgg(target);
    }

    @Override
    protected Message getNext() {
        throw new IllegalStateException();
    }
}
