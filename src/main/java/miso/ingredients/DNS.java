package miso.ingredients;

import java.util.HashMap;
import java.util.Map;

public class DNS {

    private Map<String, Actress> symbolMap = new HashMap<>();

    private static DNS instance;

    public static DNS dns() {
        if (instance == null) {
            instance = new DNS();
        }
        return instance;
    }

    public void add(Actress actress) {
        symbolMap.put(actress.address.value, actress);
    }

    public Actress resolve(Address address) {
        return resolve(address.value);
    }

    public Actress resolve(String address) {
        Actress result = symbolMap.get(address);
        if (result == null)
            throw new IllegalStateException("not found: " + address);
        return result;
    }
}
