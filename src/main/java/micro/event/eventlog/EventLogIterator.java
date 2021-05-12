package micro.event.eventlog;

import micro.Hydratable;

import java.io.Closeable;
import java.util.Iterator;

public interface EventLogIterator extends Iterator<Hydratable>, Closeable {


}
