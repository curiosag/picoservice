package micro;

import static micro.atoms.Nop.nop;

public class ExF extends Ex {
    public ExF(Env env, F template, Ex returnTo) {
        super(env, template, returnTo);
    }

    ExF() {
        super(null, new F(), null);
    }

    @Override
    public ExF accept(Value v) {
        registerReceived(v);
//TODO: looks really fishy. always apply? Side effect maybe, but function should be always terminal?
        if (template.getAtom() != nop && paramsReceived.size() == template.numParams()) {
            if (template.getAtom().isSideEffect()) {
                applySideEffect();
            } else {
                applyFunction();
            }
        }

        if (Names.result.equals(v.getName())) {
            returnTo.accept(value(template.returnAs, v.get()));
        } else if (Names.exception.equals(v.getName())) {
            returnTo.accept(v.withSender(this));
        } else {
            propagate(v);
        }
        return this;
    }

    @Override
    protected void propagate(Value v) {
        getPropagations(v.getName()).forEach(p -> p.accept(value(p.template.nameToPropagate, v.get())));
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
            returnTo.accept(value(template.returnAs, value));
        } catch (Exception e) {
            returnTo.accept(value(Names.exception, e));
        }
    }

}
