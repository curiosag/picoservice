package miso.ingredients.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import miso.ingredients.Actress;
import miso.ingredients.Message;

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
