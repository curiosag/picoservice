package nano.ingredients;

import java.util.function.BiFunction;

public class MessageReplayTrigger {

    public final java.util.function.BiFunction<Actress, Message, Boolean> messageFilter;

    public MessageReplayTrigger(BiFunction<Actress, Message, Boolean> messageFilter) {
        this.messageFilter = messageFilter;
    }

}
