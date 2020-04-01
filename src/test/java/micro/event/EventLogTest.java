package micro.event;

import org.junit.Assert;
import org.junit.Test;

public class EventLogTest {

    @Test
    public void testReadWrite() {

        Event e0 = new ExecutionCreatedEvent(0, 0, 0);
        Event e1 = new ExecutionCreatedEvent(1, 1, 1);
        e0.setId(0);
        e1.setId(1);


        try (EventLogWriter w = new EventLogWriter("/home/ssmertnig/temp/kryotest.bin", true)) {
            w.put(e0);
            w.put(e1);
        }

        EventLogReader r = new EventLogReader("/home/ssmertnig/temp/kryotest.bin");
        try (EventLogIterator iter = r.iterator()) {
            Assert.assertTrue(iter.hasNext());
            Assert.assertEquals(iter.next(), e0);
            Assert.assertTrue(iter.hasNext());
            Assert.assertEquals(iter.next(), e1);
            Assert.assertFalse(iter.hasNext());
        }

    }
}