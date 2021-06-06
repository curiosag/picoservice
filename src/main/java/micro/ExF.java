package micro;

public class ExF extends Ex {

    public ExF(Env env, long id, F template, _Ex returnTo) {
        super(env, id, template, returnTo);
    }

    @Override
    public void processValueDownstream(Value v) {
        Check.preCondition(isDownstream(v.getName()));
        Check.invariant(!(template.hasFunctionPrimitive() && Names.result.equals(v.getName())), "no result as input expected for function atom");

        propagate(v);

        if (template.hasPrimitive() && paramsReceived.size() == template.numParams()) {
            if(! isRecovery) {
                if (template.getPrimitive().isSideEffect()) {
                    applySideEffect();
                } else {
                    applyFunction();
                }
            }
            resultOrExceptionFromPrimitive = true;
        }
    }

    private void applySideEffect() {
        try {
            template.getPrimitive().execute(paramsReceived);
        } catch (Exception e) {
            deliverResult(new Value(Names.exception, e, this));
        }
    }

    private void applyFunction() {
        try {
            Object value = template.getPrimitive().execute(paramsReceived);
            deliverResult(new Value(template.returnAs, value, this));
        } catch (Exception e) {
            deliverResult(new Value(Names.exception, e, this));
        }
    }

}
