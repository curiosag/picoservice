package micro.trace;

import micro.Concurrent;
import micro._Ex;
import micro.event.ExEvent;
import micro.event.ValueEvent;
import micro.event.ValueReceivedEvent;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Tracer implements Closeable, Runnable {

    private boolean active;
    private BufferedWriter writer;
    private Queue<ValueEvent> events = new ConcurrentLinkedQueue<>();
    private AtomicBoolean terminate = new AtomicBoolean(false);

    public void setTrace(boolean active) {
        if (writer != null) {
            close();
        }

        this.active = active;
        if (active) {
            writer = createWriter();
            writeLn("digraph G {\n graph [ranksep=0.5]; rankdir=LR; \nnode [shape=record];\n");
            new Thread(this).start();
        }
    }

    public Tracer(boolean active) {
        if (active) {
            setTrace(active);
        }
    }

    private void writeLn(String s) {
        write(s);
        write("\n");
        try {
            if (active)
                writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(String s) {
        try {
            if (active)
                writer.write(s);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String payload(ValueEvent m) {
        String value;
        if (m.value.get() == null) {
            value = "NULL";
        } else {
            value = m.value.get().toString();
        }

        return '"' + value
                .replace("[", "(")
                .replace("]", ")")
                + '"';
    }

    private String getLabel(_Ex ex){
        return '"' + ex.getLabel() + '\n' + ex.getId() + '"';
    }

    private void write(ValueEvent m) {
        String labelSender = getLabel(m.value.getSender());
        String labelReceiver = getLabel(m.ex);

        writeLn(String.format("%s -> %s [label=%s];",
                labelSender, labelReceiver, payload(m)));
    }

    public void trace(ExEvent m) {
        if (m instanceof ValueReceivedEvent r)
            events.add(r);
    }

    private BufferedWriter createWriter() {
        String traceName = "/trace.dot";//String.format("/trace_%s.dot", LocalDateTime.now());
        String tracePath = System.getProperty("user.dir");
        Path path = Paths.get(tracePath + traceName);
        System.out.println("trace at " + path);
        try {
            return Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void close() {
        try {
            Concurrent.await(() -> events.isEmpty());
            terminate.set(true);
            Thread.yield();
            if (active) {
                writeLn("}");
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!terminate.get()) {
            ValueEvent m = events.poll();
            if (m != null) {
                write(m);
            } else {
                Concurrent.await(200);
            }
        }
    }
}
