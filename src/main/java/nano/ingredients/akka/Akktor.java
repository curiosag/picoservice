package nano.ingredients.akka;

import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;
import nano.ingredients.Actress;
import nano.ingredients.ComputationBough;
import nano.ingredients.Ensemble;
import nano.ingredients.Message;
import nano.ingredients.tuples.SerializableTuple;

import java.util.HashMap;
import java.util.Map;

import static nano.ingredients.RunProperty.PERSIST;

public class Akktor extends AbstractPersistentActor {

    private Actress related;
    private akka.event.EventStream eventStream;

    private Map<SerializableTuple<Long, Long>, ComputationBough> maxStack = new HashMap<>();

    private Akktor(Actress related) {
        this.related = related;
        this.eventStream = getContext().getSystem().eventStream();
    }

    public static Props props(Actress related) {
        return Props.create(Akktor.class, () -> new Akktor(related));
    }


    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(Message.class, m -> {
                    if (Ensemble.instance().hasRunProperty(PERSIST)) {
                        System.out.println(String.format("%s rreco %s (%d<-%d) %s:%s", m.origin.getComputationBough().toString(), m.id, related.address.id,  m.origin.senderId, m.key, m.getValue().toString()));
                        related.receiveRecover(m);
                    }
                })
                .match(SnapshotOffer.class, ss -> related = (Actress) ss.snapshot())
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.class,
                        m -> {
                            if (Ensemble.instance().hasRunProperty(PERSIST)) {
                                System.out.println(String.format("%s ppers %s (%d<-%d) %s:%s", m.origin.getComputationBough().toString(), m.id, related.address.id, m.origin.senderId, m.key, m.getValue().toString()));
                                persistInternal(m);
                            }
                            related.receive(m);
                        }
                ).build();
    }

    private <T> void  persistInternal(Message m) {
        persist(m, (Message i) -> eventStream.publish(i));
    }

    @Override
    public String persistenceId() {
        return related.address.id.toString();
    }


}
