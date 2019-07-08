package miso.ingredients;

import miso.Actress;
import miso.message.*;

import java.util.HashMap;
import java.util.Map;

import static miso.message.Message.message;

public class Cells extends Actress {

    private Map<Address, Object> values = new HashMap<>();

    public Cells() {
        super(Address.of(Adresses.cells));
    }

    @Override
    public void recieve(Message message) {
        if (message instanceof CellMessage) {
            super.recieve(message);
        }
    }

    @Override
    protected Message getNext() {
        return getCurrent()
                .map(this::resolve)
                .orElseThrow(IllegalStateException::new);

    }

    private Message resolve(Message message) {
        if (!(message instanceof CellMessage)) {
            throw new IllegalStateException();
        }
        Address address = (Address) message.get(Name.adress);

        if (message.get(Name.ReadWrite) == ReadWrite.Read) {
            return message()
                    .put(Name.symbol, message.get(Name.symbol))
                    .put(Name.value, values.get(address))
                    .put(Name.recipient, message.get(Name.recipient));

        }

        values.put(address, message.get(Name.value));
        return Message.NULL;
    }
}
