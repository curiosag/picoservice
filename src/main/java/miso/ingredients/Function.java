package miso.ingredients;

import miso.message.Message;
import miso.message.Name;

import java.util.Optional;

public class Function<T> extends Func<T> {

    private Function(Func<T> body) {
        body.addTarget(Name.result,this);
    }

    public static <T> Function function(Func<T> body) {
        return new Function<>(body);
    }

    @Override
    public void recieve(Message message) {
        System.out.println(this.getClass().getSimpleName() + " function call \n" + message.toString());

        if (message.hasKey(Name.result)) {
            //TODO: vielleicht hier params verteilen?
        } else {
            super.recieve(message);
        }
    }

    @Override
    protected void process(Message message) {
        if (!message.hasKey(Name.result)) {
            throw new IllegalStateException();
        }

        targets.forEach((k, target) ->
                send(target, message));

    }

}
