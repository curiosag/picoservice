package miso.ingredients;

import miso.Actress;
import miso.message.Name;

public abstract class Func extends Actress {

    protected String resultKey = Name.result;

    public Func resultKey(String name){
        this.resultKey = name;
        return this;
    }

    @Override
    public Func resultTo(Actress r) {
        super.resultTo(r);
        return this;
    }

    @Override
    public Func resultTo(Actress... r) {
        super.resultTo(r);
        return this;
    }

}
