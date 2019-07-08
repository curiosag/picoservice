package miso.message;

import miso.ingredients.Address;

public class CellRead extends CellMessage {

    public static CellRead cellRead(Address value) {
        CellRead result = new CellRead();
        result.put(Name.ReadWrite, ReadWrite.Read);
        result.put(Name.adress, value);
        return result;
    }

    public CellRead recipient(Address address, String symbol){
        put(Name.recipient, address);
        put(Name.symbol, symbol);
        return this;
    }

}
