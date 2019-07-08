package miso.ingredients;

import miso.Actress;
import miso.Name;

public abstract class Func extends Actress {

    protected String resultKey = Name.result;

    public Func resultKey(String name){
        this.resultKey = name;
        return this;
    }

}
