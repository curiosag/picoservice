package miso.message;

import miso.ingredients.Address;

public class CellWrite extends CellMessage {

    public static CellWrite cellWrite(Address address) {
        CellWrite result = new CellWrite();
        result.put(Name.ReadWrite, ReadWrite.Write);
        result.put(Name.adress, address);
        return result;
    }

    public CellWrite value(Object value) {
        put(Name.value, value);
        return this;
    }

}
