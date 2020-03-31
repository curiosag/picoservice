package micro;

import micro.event.PropagateValueEvent;

public class ExF extends Ex {
    public ExF(Node node, F template, _Ex returnTo) {
        super(node, template, returnTo);
    }

    @Override
    public void perfromValueReceived(Value v) {
        Check.isFunctionInputValue(v);
        Check.invariant(!(template.hasFunctionAtom() && Names.result.equals(v.getName())), "no result as input expected for function atom");

        propagate(v);

        if (template.hasAtom() && paramsReceived.size() == template.numParams()) {
            if (template.getPrimitive().isSideEffect()) {
                applySideEffect();
            } else {
                applyFunction();
            }
        }
    }

    private void propagate(Value v) {
        getPropagations(v.getName()).forEach(p ->
                raise(new PropagateValueEvent(node.getNextObjectId(),this, p.getTo(), new Value(p.getNameToPropagate(), v.get(), this))));
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

}