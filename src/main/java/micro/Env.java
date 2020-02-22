package micro;

public class Env {
    public void log(String msg){
        System.out.println(msg);
    }

    public void debug(String msg) {
        System.out.println(msg);
    }

    public void enq(Value v, Ex target){
        target.process(v);
    }
}
