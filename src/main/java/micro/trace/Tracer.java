package micro.trace;

import micro.Check;
import micro.Ex;
import micro.actor.Message;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Tracer implements Closeable, Runnable {

    private boolean active;
    private BufferedWriter writer;
    private Queue<Message> messages = new ConcurrentLinkedQueue<>();
    private AtomicBoolean terminate = new AtomicBoolean(false);

    private void setTrace(boolean active) {
        if (writer != null) {
            close();
        }

        this.active = active;
        if (active) {
            writer = createWriter();
            writeLn("digraph G {\n graph [ranksep=0];\nnode [shape=record];\n");
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

    private String payload(Message m) {
        String value;
        if (m.value.get() == null) {
            value = "NULL";
        } else {
            value = m.value.get().toString();
        }

        Check.argument(m.value.getSender() instanceof Ex, "uh...");

        return '"' + String.format("%s:", ((Ex) m.value.getSender()).template.getLabel()) + value
                .replace("[", "(")
                .replace("]", ")")
                + '"';
    }

    private void write(Message m) {
        Check.argument(m.value.getSender() instanceof Ex, "uh...");

        String labelSender = ((Ex)m.value.getSender()).getLabel();
        String labelReceiver = m.target.getLabel();

        writeLn(String.format("%s -> %s [label=%s];",
                labelSender, labelReceiver, payload(m)));
    }

    public void trace(Message m) {
        messages.add(m);
    }

    private BufferedWriter createWriter() {
        String traceName = String.format("/trace_%s.dot", LocalDateTime.now().toString());
        String tracePath = System.getProperty("user.dir");
        Path path = Paths.get(tracePath + traceName);
        System.out.println("trace at " + path.toString());
        try {
            return Files.newBufferedWriter(path, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void close() {
        try {
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
            Message m = messages.poll();
            if (m != null) {
                write(m);
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException();
                }
            }
        }
    }
}
