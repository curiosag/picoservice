package nano.ingredients.akka;

import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;
import nano.ingredients.*;
import nano.ingredients.tuples.StrackframeAndResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nano.ingredients.RunProperty.*;

public class Akktor extends AbstractPersistentActor {

    public static Props props(Actress related) {
        return Props.create(Akktor.class, () -> new Akktor((Actress) related));
    }

    private Actress related;

    private akka.event.EventStream eventStream;

    private final Map<ComputationStack, StrackframeAndResult> resultsRecovered = new HashMap<>();

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
                            if (runModePersist() && m.hasAnyKey(Name.stackFrame, Name.result)) {
                                if (Ensemble.instance().hasRunProperty(SLOW)) {
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        //
                                    }
                                }
                                persist(m, (Message i) -> eventStream.publish(i));
                            }
                            related.receive(m);
                        }

                ).build();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(Message.class, this::hdlRecovery)
                .match(RecoveryCompleted.class, m -> {
                            clearCallsRecoveredWithoutResults(resultsRecovered);
                            related.setResultsRecovered(resultsRecovered);
                        }
                ).match(SnapshotOffer.class, ss -> {
                    throw new IllegalStateException();
                })
                .build();
    }

    private void clearCallsRecoveredWithoutResults(Map<ComputationStack, StrackframeAndResult> resultsRecovered) {
        List<ComputationStack> toDelete = resultsRecovered.entrySet().stream()
                .filter(e -> e.getValue().getResult() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        toDelete.forEach(resultsRecovered::remove);
    }

    private void hdlRecovery(Message message) {
        if (related instanceof FunctionSignature) {
            Message m = message.origin(message.origin.clearSenderRef());
            ComputationStack stack = message.origin.getComputationPath().getStack();

            StrackframeAndResult current = resultsRecovered.get(stack);
            if (current == null) {
                if (m.key.equals(Name.result)) {
                    throw new IllegalStateException();
                }
                resultsRecovered.computeIfAbsent(stack, k -> new StrackframeAndResult(m, null));
            } else {
                if (m.key.equals(Name.stackFrame)) {
                    throw new IllegalStateException();
                }
                resultsRecovered.put(stack, new StrackframeAndResult(current.getStackFrame(), m));
            }
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
