package nano.ingredients.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import nano.ingredients.Actress;
import nano.ingredients.Message;

public class Akktor extends AbstractActor {

    public final Actress related;

    public Akktor(Actress related) {
        this.related = related;
    }

    public static Props props(Actress related) {
        return Props.create(Akktor.class, () -> new Akktor(related));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Message.class, related::receive).build();
    }

}
