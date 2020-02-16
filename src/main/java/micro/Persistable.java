package micro;

public interface Persistable {
    void store(Persistence p);
    void load(Persistence p);
}
