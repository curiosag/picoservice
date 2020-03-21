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

    String getReturnValueName() {
        return callTemplate.returnAs;
    }

    @Override
    public void process(Value v) {
        Check.invariant(!(Names.result.equals(v.getName()) || Names.exception.equals(v.getName())), "result and exception expected to be processed in base class");

        if (beingCalled == null) {
            beingCalled = env.createExecution(callTemplate.called, this);
        }
        beingCalled.receive(v.withSender(this));
    }

}
