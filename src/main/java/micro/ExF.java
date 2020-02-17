package micro;

public class ExF extends Ex {

    public ExF(Env env, F template, Ex returnTo) {
        super(env, template, returnTo);
    }

    ExF() {
        super(null, new F(), null);
    }

    @Override
    public void accept(Value v) {
        registerReceived(v);

        if (template.getAtom() != null && paramsReceived.size() == template.numParams()) {
            if (template.getAtom().isSideEffect()) {
                applySideEffect();
            } else {
                applyFunction();
            }
        }

        if (Names.result.equals(v.getName())) {
            returnTo.accept(value(template.returnAs, v.get()));
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
            returnTo.accept(value(template.returnAs, template.getAtom().execute(paramsReceived)));
        } catch (Exception e) {
            returnTo.accept(value(Names.exception, e));
        }
    }

}
