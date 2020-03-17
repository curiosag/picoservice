package micro;

public interface Id {

    long getId();

    void setId(long value);

    default long checkSetValue(long value){
        Check.invariant(getId() < 0, "can't reset id");
        Check.argument(value >= 0, "id < 0");
        return value;
    }
}
