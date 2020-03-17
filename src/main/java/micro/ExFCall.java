package micro;

public class ExFCall extends Ex {

    private FCall fCallTemplate;
    private _Ex toCall;

    ExFCall(Env env, FCall fCallTemplate, _Ex returnTo) {
        super(env, new F(env, F.nop).label(fCallTemplate.getLabel()), returnTo);
        this.fCallTemplate = fCallTemplate;
    }

    @Override
    public void process(Value v) {
        registerReceived(v);
        if (toCall == null) {
            toCall = env.createExecution(fCallTemplate.called, this);
        }
        switch (v.getName()) {
            case Names.result:
                returnTo.accept(new Value(fCallTemplate.returnAs, v.get(), this));
                break;
            case Names.exception:
                returnTo.accept(new Value(fCallTemplate.returnAs, v.get(), this));
                break;
            default:
                propagate(v);
        }
    }

    @Override
    protected void propagate(Value v) {
        toCall.accept(v.withSender(this));
    }
}
