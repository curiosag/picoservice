package miso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class Actress {

    private final List<Actress> targets = new ArrayList<>();

    protected Supplier NULL = () -> Message.of(this, Name.error, "message is null");

    Message current = null;

    public Actress() {
    }

    public Actress resultTo(Actress r){
        targets.clear();
        targets.add(r);
        return this;
    };

    public Actress resultTo(Actress ... r){
        targets.clear();
        targets.addAll(Arrays.asList(r));
        return this;
    };

    public Optional<Message> getCurrent() {
        return Optional.ofNullable(current);
    }

    public Actress setCurrent(Message current){
        this.current = current;
        return this;
    }

    public void recieve(Message message) {
        System.out.println(this.getClass().getSimpleName() + " receiving \n" + message.toString());
        setCurrent(message);
        send(getNext());
    }

    protected abstract Message getNext() ;

    protected void send(Message message) {
        targets.forEach(r -> r.recieve(message));
    }


}
