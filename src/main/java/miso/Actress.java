package miso;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Actress {

    public final List<Actress> targets = new ArrayList<>();
    public List<String> paramsRequired = new ArrayList<>();

    public Actress paramsRequired(List<String> params) {
        paramsRequired.clear();
        paramsRequired.addAll(params);
        return this;
    }

    public Actress paramsRequired(String ... params) {
        return paramsRequired(Arrays.asList(params));
    }

    protected Supplier NULL = () -> Message.of(this, Name.error, "message is null");

    Message current = null;

    public Actress() {
    }

    public Actress resultTo(Actress r) {
        targets.clear();
        targets.add(r);
        return this;
    }

    ;

    public Actress resultTo(Actress... r) {
        targets.clear();
        targets.addAll(Arrays.asList(r));
        return this;
    }


    public Optional<Message> getCurrent() {
        return Optional.ofNullable(current);
    }

    public Actress setCurrent(Message current) {
        this.current = current;
        return this;
    }

    public void recieve(Message message) {
        System.out.println(this.getClass().getSimpleName() + " receiving \n" + message.toString());
        setCurrent(message);
        send(getNext());
    }

    protected abstract Message getNext();

    protected void send(Message message) {
        targets.forEach(r -> r.recieve(message));
    }


    public List<Actress> getAllTargets(Actress caller, List<Actress> traced) {
        if (traced.contains(this) || this == caller)
            return traced;
        else {
            List<Actress> result = targets.stream()
                    .flatMap(t -> t.getAllTargets(caller, traced).stream())
                    .filter(a -> a != caller)
                    .collect(Collectors.toList());
            result.add(this);
            result.addAll(traced);
            return result;
        }
    }
}
