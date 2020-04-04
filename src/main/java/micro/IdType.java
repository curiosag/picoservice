package micro;

import java.util.concurrent.atomic.AtomicLong;

public enum IdType {

    EVENT, EX, F;

    private AtomicLong next = new AtomicLong(0);

    public void setNext(long value){
        next.set(value);
    }

    public long next(){
        return next.getAndIncrement();
    }

}
