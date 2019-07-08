package miso.ingredients;

import miso.Actress;

import java.util.HashMap;
import java.util.Map;

public class DNS {

    private Map<String, Actress> symbolMap = new HashMap<>();

    private static DNS instance;

    public static DNS dns(){
        if (instance == null)
        {
            instance = new DNS();
        }
        return instance;
    }

    public DNS add(Actress actress){
        symbolMap.put(actress.address.value, actress);
        return this;
    }

    public Actress resolve(Address address){
        return resolve(address.value);
    }

    public Actress resolve(String address){
        Actress result = symbolMap.get(address);
        if (result == null)
            throw new IllegalStateException("not found: " + address);
        return result;
    }
}
