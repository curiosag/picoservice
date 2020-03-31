package micro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PartiallyAppliedFunction implements KryoSerializable {

    public F baseFunction;
    public List<Value> partialValues;

    public PartiallyAppliedFunction(F baseFunction, Collection<Value> partialValues) {
        this.baseFunction = baseFunction;
        this.partialValues = new ArrayList<>(partialValues);
    }

    @Override
    public void write(Kryo kryo, Output output) {

    }

    @Override
    public void read(Kryo kryo, Input input) {

    }
}
