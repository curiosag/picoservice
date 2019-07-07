package miso.ingredients;

import miso.Name;

public abstract class Func extends Agg {

    protected String resultKey = Name.result;

    public Func resultKey(String name){
        this.resultKey = name;
        return this;
    }

}
