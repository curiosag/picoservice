package miso.ingredients.trace;

import miso.ingredients.*;
import miso.misc.Adresses;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Trace extends Actress implements Closeable {

    private final boolean active;
    private final BufferedWriter writer;
    private List<TraceMessage> messages = new ArrayList<>();

    public Trace(boolean active) {
        super(new Address(Adresses.trace, Actresses.nextId()));
        this.active = active;
        writer = active ? createWriter() : null;
        writeLn("digraph G {\n graph [ranksep=0];\nnode [shape=record];\n");
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

    private String node(Actress f) {
        return f.address.label.length() > 0 ? f.address.label : f.address.toString();
    }

    private String payload(Message m) {
        String value;
        if (m.value == null) {
            value = "NULL";
        } else if (m.value instanceof Actress) {
            value = node((Actress) m.value);
        } else {
            value = (m.value).toString();
        }

        return '"' + String.format("<%d>(%d/%d)%s:", m.origin.seqNr, m.origin.executionId, m.origin.callStack.size(), m.key) + value
                .replace("[", "(")
                .replace("]", ")")
                + '"';
    }

    private void write(TraceMessage m) {
        Long exId = m.origin.executionId;

        Integer levelSender = m.sender().callStack.size();
        String labelSender = node(m.sender().sender);
        String scopeSender = node(m.sender().triggeredBy);
        String stack = m.origin.callStack.toString();
        Integer levelReceiver = levelSender;
        String labelReceiver = node(m.receiver());
        String scopeReceiver = scopeSender;

        if ((m.origin.sender instanceof FunctionCall) && (m.receiver() instanceof FunctionSignature)) {
            // levelSender ok
            // scopeSender ok;
            levelSender--;
            scopeReceiver = labelSender;
        }

        if ((m.origin.sender instanceof FunctionSignature) && (m.receiver() instanceof FunctionCall)) {
            levelSender++;
            scopeSender = labelReceiver;
            // levelReceiver ok
            // scopeReceiver ok
        }

        writeLn(String.format("%s -> %s [label=%s];",
                renderNode(labelSender, exId, levelSender, scopeSender),
                renderNode(labelReceiver, exId, levelReceiver, scopeReceiver),
                payload(m.traced())));
    }

    private String renderNode(String label, Long executionId, Integer callLevel, String scope) {
        return '"' + String.format("%s(%d/%d)", label, executionId, callLevel) + '"';
    }

    @Override
    protected void process(Message message) {
        if (!(message instanceof TraceMessage)) {
            throw new IllegalStateException();
        }
        write((TraceMessage) message);
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
    public void close() throws IOException {
        if (active) {
            writeLn("}");
            writer.flush();
            writer.close();
        }
    }

}
