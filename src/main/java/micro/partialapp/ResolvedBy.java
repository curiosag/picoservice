package micro.partialapp;

public class ResolvedBy {

    private final FPartialApp partial;
    private String param;

    public ResolvedBy(FPartialApp partial, String param){
        this.partial = partial;
    }

    public FPartialApp resolvedBy(String ref)
    {
        partial.addPartial(param, ref);
        return partial;
    }
}
