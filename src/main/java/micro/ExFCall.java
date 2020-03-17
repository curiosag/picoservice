package micro;

public class ExFCall extends Ex {

    private FCall fCallTemplate;
    private Ex calledEx;

    ExFCall(Env env, FCall fCallTemplate, Ex returnTo) {
        super(env, new F(F.nop).label(fCallTemplate.getLabel()), returnTo);
        this.fCallTemplate = fCallTemplate;
    }

    @Override
    public void process(Value v) {
        registerReceived(v);
        if (calledEx == null) {
            calledEx = fCallTemplate.called.createExecution(env, this);
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
        calledEx.accept(v.withSender(this));
    }
}
