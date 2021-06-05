package micro.event.serialization;

/**
 * Why so serioulized?
 * */

public interface Serioulizable {

    void write(Outgoing out);

    void read(Incoming in);

}
