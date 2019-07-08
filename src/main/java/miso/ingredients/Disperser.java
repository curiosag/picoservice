package miso.ingredients;

import miso.Actress;
import miso.Message;

import java.util.ArrayList;
import java.util.List;

public class Disperser {

    List<Actress> listers = new ArrayList<>();

    public Disperser add(Actress a){
        listers.add(a);
        return this;
    }

    public void disperse(Message message){
        listers.forEach(l -> l.recieve(message));
    };


}
