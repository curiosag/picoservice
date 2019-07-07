package miso;

public class Target {

    public final Actress target;
    private Name paramName;

    public Target(Actress target) {
        this.target = target;
    }

    public Target inParam(Name name)
    {
        this.paramName = name;
        return this;
    }

    public static Target target(Actress t){
        return new Target(t);
    }

}
