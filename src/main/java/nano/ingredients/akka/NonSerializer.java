package nano.ingredients.akka;


import akka.serialization.Serializer;
import scala.Option;

public class NonSerializer implements Serializer {

    @Override
    public int identifier() {
        return 0;
    }

    @Override
    public byte[] toBinary(Object o) {
        return new byte[0];
    }

    @Override
    public boolean includeManifest() {
        return false;
    }

    @Override
    public Object fromBinary(byte[] bytes, Option<Class<?>> manifest) {
        return 0;
    }
}
