package micro;

public class ExF extends Ex {
    ExF(Node node, F template, _Ex returnTo) {
        super(node, template, returnTo);
    }

    @Override
    public void processInputValue(Value v) {
        Check.isFunctionInputValue(v);

        initiatePropagations(v, template.getPropagations(v.getName()));

        if (template.hasAtom() && paramsReceived.size() == template.numParams()) {
            if (template.getPrimitive().isSideEffect()) {
                applySideEffect();
            } else {
                applyFunction();
            }
        }
    }

    private void applySideEffect() {
        try {
            template.getPrimitive().execute(paramsReceived);
        } catch (Exception e) {
            node.log(e.getMessage()); // todo remove
        }
    }

    private void applyFunction() {
        try {
            Object value = template.getPrimitive().execute(paramsReceived);
            returnTo.receive(new Value(template.returnAs, value, this));
        } catch (Exception e) {
            returnTo.receive(new Value(Names.exception, e, this));
        }
    }

    @Override
    public String toString() {
        return "{\"ExF\":{" +
                "\"id\":" + getId() +
                ", \"template\":" + template.getId() +
                ", \"returnTo\":" + returnTo.getId() +
                ", \"paramsReceived\":" + paramsReceived +
                "}}";
    }
}
