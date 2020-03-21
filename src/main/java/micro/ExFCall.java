package micro;

public class ExFCall extends Ex {

    private FCall callTemplate;
    private _Ex beingCalled;

    ExFCall(Env env, FCall callTemplate, _Ex returnTo) {
        super(env, new F(env, F.nop).label(callTemplate.getLabel()), returnTo);
        this.callTemplate = callTemplate;
    }

    public ExFCall(Env env) {
        super(env);
    }

    @Override
    public void process(Value v) {
        if (beingCalled == null) {
            beingCalled = env.createExecution(callTemplate.called, this);
        }
        switch (v.getName()) {
            case Names.result:
                returnTo.receive(new Value(callTemplate.returnAs, v.get(), this));
                break;
            case Names.exception:
                returnTo.receive(new Value(callTemplate.returnAs, v.get(), this));
                break;
            default:
                beingCalled.receive(v.withSender(this));
        }
    }

}
