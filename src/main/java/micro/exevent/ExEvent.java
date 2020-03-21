package micro.exevent;

import com.esotericsoftware.kryo.KryoSerializable;
import micro.Ex;

public interface ExEvent extends KryoSerializable {
    Ex getEx();

    default void perform(){
        getEx().handle(this);
    }
}
