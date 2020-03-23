package micro;

import micro.event.PropagateValueEvent;

public class ExF extends Ex {
    public ExF(Node node, F template, _Ex returnTo) {
        super(node, template, returnTo);
    }

    public ExF(Node node) {
        super(node, new F(node, F.nop), null);
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
            node.log(e.getMessage()); // todo remove
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
