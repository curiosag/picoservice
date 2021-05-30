package micro;

public class Command extends Value{

    public final static String terminateTailRecursionElements = "terminate";

    public Command(String name, Object value, _Ex sender) {
        super(name, value, sender);
    }

    public boolean is(String cmdName){
        return getName().equals(cmdName);
    }
}
