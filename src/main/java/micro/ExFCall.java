package micro;

public class ExFCall extends Ex {
    private static int instances = 0;

    private final int level;
    FCall fCallTemplate;
    private Ex calledEx;

    ExFCall(Env env, FCall fCallTemplate, Ex returnTo) {
        super(env, new F().label(fCallTemplate.getLabel()), returnTo);
        this.fCallTemplate = fCallTemplate;
        this.level = instances;
    }

    @Override
    public void process(Value v) {
        registerReceived(v);
        if (calledEx == null) {
            calledEx = fCallTemplate.called.createExecution(env, this);
            CallLevel.level++;
        }
        switch (v.getName()) {
            case Names.result:
                System.out.println("returned " + v.get() + " from (" + level + ") " + template.getLabel() + " to " + returnTo.template.getLabel());
                returnTo.accept(new Value(fCallTemplate.returnAs, v.get(), this));
                break;
            case Names.exception:
                returnTo.accept(new Value(fCallTemplate.returnAs, v.get(), this));
                break;
            default:
                calledEx.accept(v.withSender(this));
        }
    }

    @Override
    protected void propagate(Value v) {

    }
}
