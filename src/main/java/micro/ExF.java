package micro;

import micro.exevent.PropagateValueEvent;

public class ExF extends Ex {
    public ExF(Env env, F template, _Ex returnTo) {
        super(env, template, returnTo);
    }

    public ExF(Env env) {
        super(env, new F(env, F.nop), null);
    }

    @Override
    public void perfromFunctionInputValueReceived(Value v) {
        Check.isFunctionInputValue(v);
        Check.invariant(!(template.hasFunctionAtom() && Names.result.equals(v.getName())), "no result as input expected for function atom");

        propagate(v);

        //TODO: looks really fishy. always apply? Side effect maybe, but function should be always terminal?
        if (template.hasAtom() && paramsReceived.size() == template.numParams()) {

            if (template.getAtom().isSideEffect()) {
                applySideEffect();
            } else {
                applyFunction();
            }
        }
    }

    private void propagate(Value v) {
        getPropagations(v.getName()).forEach(p -> raise(new PropagateValueEvent(this, p.getTo(), new Value(p.getNameToPropagate(), v.get(), this))));
    }

    private void applySideEffect() {
        try {
            template.getAtom().execute(paramsReceived);
        } catch (Exception e) {
            env.log(e.getMessage());
        }
    }

    private void applyFunction() {
        try {
            Object value = template.getAtom().execute(paramsReceived);
            returnTo.receive(new Value(template.returnAs, value, this));
        } catch (Exception e) {
            returnTo.receive(new Value(Names.exception, e, this));
        }
    }

}
