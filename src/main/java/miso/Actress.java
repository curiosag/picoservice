package miso;

import miso.ingredients.Address;
import miso.ingredients.Disperser;
import miso.message.Message;
import miso.message.Name;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static miso.ingredients.DNS.dns;

public abstract class Actress {

    public final List<Actress> targets = new ArrayList<>();
    public List<String> paramsRequired = new ArrayList<>();
    public final Address address;

    public Actress paramsRequired(List<String> params) {
        paramsRequired.clear();
        paramsRequired.addAll(params);
        return this;
    }

    public Actress paramsRequired(String... params) {
        return paramsRequired(Arrays.asList(params));
    }

    protected Supplier NULL = () -> Message.of(Name.error, "message is null");

    Message current = null;


    public Actress() {
        address = new Address(UUID.randomUUID().toString());
        dns().add(this);
    }

    public Actress(Address address) {
        this.address = address;
        dns().add(this);
    }

    public <T extends Actress> T signOnTo(Disperser d) {
        d.add(this);
        return (T) this;
    }

    public Actress resultTo(Actress r) {
        targets.clear();
        targets.add(r);
        return this;
    }

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
        Optional<Object> recipient = message.maybe(Name.recipient);
        if (recipient.isPresent()) {
            Address address = (Address) recipient.get();
            dns().resolve(address).recieve(message);
        } else {
            targets.forEach(r -> r.recieve(message));
        }
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
