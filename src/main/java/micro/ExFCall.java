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

    String getNameForReturnValue() {
        return callTemplate.returnAs;
    }

    @Override
    public void perfromFunctionInputValueReceived(Value v) {
        Check.isFunctionInputValue(v);
        getBeingCalled().receive(v.withSender(this));
    }

    private _Ex getBeingCalled() {
        if (beingCalled == null) {
            beingCalled = env.createExecution(callTemplate.called, this);
        }
        return beingCalled;
    }

}
