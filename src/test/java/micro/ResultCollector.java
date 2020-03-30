package micro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResultCollector {

    private final List<Value> values = new ArrayList<>();

    boolean isEmpty() {
        synchronized (this) {
            return values.isEmpty();
        }
    }

    public List<Value> get() {
        synchronized (this) {
            return new ArrayList<>(values);
        }
    }

    void set(Collection<Value> values){
        synchronized (this){
            this.values.addAll(values);
        }
    }


}
