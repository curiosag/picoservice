package nano.ingredients.akka;

import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;
import nano.ingredients.*;
import nano.ingredients.tuples.ReplayData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nano.ingredients.RunProperty.*;

public class Akktor extends AbstractPersistentActor {

    public static Props props(Actress related) {
        return Props.create(Akktor.class, () -> new Akktor((Actress) related));
    }

    private Actress related;

    private akka.event.EventStream eventStream;

    private final Map<ComputationStack, Map<String, Message>> framesRecovered = new HashMap<>();
    private final Map<ComputationStack, List<Message>> framesToReplay = new HashMap<>();
    private final List<Message> nonFramesToReplay = new ArrayList<>();

    private Akktor(Actress related) {
        this.related = related;
        this.eventStream = getContext().getSystem().eventStream();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.class,
                        m -> {
                            if (!m.key.equals(getReturnKey()) && Ensemble.instance().hasRunProperty(TERMINATE)) {
                                return;
                            }
                            if (runModePersist()) {
                                if (Ensemble.instance().hasRunProperty(SLOW) && related instanceof FunctionSignature && m.key.equals("result") && m.getValue().equals(1)) {
//                                    try {
//                                        Thread.sleep(5000);
//                                    } catch (InterruptedException e) {
//                                        //
//                                    }
                                }
                                // function calls need to be set up with proper state
                                // stack frames need to be stored to detect and prevent re-computation later on
                                if (related instanceof FunctionCall ||
                                        (related instanceof FunctionSignature && ((FunctionSignature) related).paramList.contains(m.key)) ||
                                        m.key.equals(Name.stackFrame) || m.key.equals(getReturnKey())) {
                                    persist(m, (Message i) -> eventStream.publish(i));

                                    if (Ensemble.instance().hasRunProperty(SLOW)) {
                                        try {
                                            Thread.sleep(300);
                                        } catch (InterruptedException e) {
                                            //
                                        }
                                    }
                                }
                            }
                            related.receive(m);
                        }

                ).build();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(Message.class, m -> {
                    m.setRecovered(true);
                    hdlMessageRecovery(m);
                }).match(RecoveryCompleted.class,
                        m -> {
                            // first set replay data
                            related.setReplayData(new ReplayData(framesRecovered, framesToReplay));
                            // then start replay
                            nonFramesToReplay.forEach(r -> {
                                System.out.println("recovering" + r.toString());
                                related.receive(r);
                            });
                            framesToReplay.forEach((k, v) -> v.forEach(r -> {
                                System.out.println("recovering" + r.toString());
                                related.receive(r);
                            }));
                        }
                ).match(SnapshotOffer.class, ss -> {
                    throw new IllegalStateException();
                })
                .build();
    }

    private void hdlMessageRecovery(Message m) {
        Message message = m.origin(m.origin.clearSenderRef());
        ComputationStack stack = message.origin.getComputationPath().getStack();
        if (related instanceof FunctionSignature && message.hasAnyKey(getReturnKey(), Name.stackFrame)) {
            hdlSignatureRecovery(message, stack);
        }
        if ((related instanceof FunctionSignature || related instanceof FunctionCall) &&
                !message.key.equals(Name.stackFrame) && message.origin.getSender() != related) {
            nonFramesToReplay.add(message);
        }
    }

    private void hdlSignatureRecovery(Message m, ComputationStack stack) {
        Map<String, Message> reco = framesRecovered.computeIfAbsent(stack, k -> new HashMap<>());
        if (reco.get(m.key) != null) {
            throw new IllegalStateException("Already encountered key " + m.key);
        }
        reco.put(m.key, m);

        List<Message> repl = framesToReplay.computeIfAbsent(stack, k -> new ArrayList<>());
        if (m.key.equals(getReturnKey())) {
            repl.clear();
        }

        if (!repl.contains(m)) {
            repl.add(m);
        }
    }

    private String getReturnKey() {
        return ((Function) related).returnKey;
    }


    private boolean runModePersist() {
        return Ensemble.instance().hasRunProperty(PERSIST);
    }

    @Override
    public String persistenceId() {
        return related.address.id;
    }


}
