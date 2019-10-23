package nano.ingredients.akka;

import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;
import nano.ingredients.*;
import nano.ingredients.tuples.Replay;

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

    private final Map<Replay, Map<String, Message>> messageMapRecovered = new HashMap<>();
    private List<Message> messagesRecovered = new ArrayList<>();
    private final Map<ComputationStack, Message> framesToReplay = new HashMap<>();

    private Akktor(Actress related) {
        this.related = related;
        this.eventStream = getContext().getSystem().eventStream();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MessageReplayTrigger.class, m -> replayMessages(m.messageFilter))
                .match(Message.class,
                        m -> {
                            if (!m.key.equals(getReturnKey()) && Ensemble.instance().hasRunProperty(TERMINATE)) {
                                return;
                            }
                            if (runModePersist()) {
                                if (m.hasAnyKey(Name.stackFrame, Name.result) && Ensemble.instance().hasRunProperty(SLOW)) {
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
                .match(RecoveryCompleted.class, m -> related.setMessagesRecovered(messageMapRecovered)
                ).match(SnapshotOffer.class, ss -> {
                    throw new IllegalStateException();
                })
                .build();
    }

    private void replayMessages(java.util.function.BiFunction<Actress, Message, Boolean> messageFilter) {
        messagesRecovered.stream()
                .filter(m -> messageFilter.apply(related, m))
                .forEach(r -> {
                    if (isntStackFrameAtAll(r) || canReplayStackFrame(r)) {
                        related.receive(r);
                    }
                });
    }

    private boolean isntStackFrameAtAll(Message r) {
        return !r.hasKey(Name.stackFrame);
    }

    private boolean canReplayStackFrame(Message r) {
        return framesToReplay.get(r.origin.getComputationPath().getStack()) != null;
    }

    private void hdlRecovery(Message message) {
        Message m = message.origin(message.origin.clearSenderRef());
        m.setProcessingDirective(MessageProcessingDirective.REPLAY);

        ComputationStack stack = message.origin.getComputationPath().getStack();

        messagesRecovered.add(m);

        Map<String, Message> keyMessageMap = messageMapRecovered.computeIfAbsent(new Replay(m.origin.senderId, stack), k -> new HashMap<>());
        if (keyMessageMap.get(m.key) != null) {
            throw new IllegalStateException("Already encountered key " + m.key);
        }
        keyMessageMap.put(m.key, m);

        if (related instanceof FunctionSignature && m.hasAnyKey(Name.stackFrame)) {
            if (framesToReplay.get(stack) != null) {
                throw new IllegalStateException();
            }
            framesToReplay.put(stack, m);
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
