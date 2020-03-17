package micro;

public class ExF extends Ex {
    public ExF(Env env, F template, Ex returnTo) {
        super(env, template, returnTo);
    }

    ExF(Env env) {
        super(env, new F(F.nop), null);
    }

    @Override
    public void process(Value v) {
        Check.invariant(! (template.hasFunctionAtom() && Names.result.equals(v.getName())), "no result as input expected for function atom");

        registerReceived(v);
//TODO: looks really fishy. always apply? Side effect maybe, but function should be always terminal?
        if (template.hasAtom() && paramsReceived.size() == template.numParams()) {

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
            returnTo.process(value(template.returnAs, value));
        } catch (Exception e) {
            returnTo.process(value(Names.exception, e));
        }
    }

}
