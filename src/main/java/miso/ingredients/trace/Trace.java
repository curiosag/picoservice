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

    private final BufferedWriter writer = createWriter();
    private List<TraceMessage> messages = new ArrayList<>();

    public Trace() {
        super(new Address(Adresses.trace));

        writeLn("digraph G {\n graph [ranksep=0];\nnode [shape=record];\n");
    }

    private void writeLn(String s) {
        write(s);
        write("\n");
        try {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(String s) {
        try {
            writer.write(s);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String node(Actress f) {
        return f.address.label.length() > 0 ? f.address.label : f.address.toString();
    }

    private String node(Origin f) {
        return node(f.sender);
    }

    private String payload(Message m) {
        String value = m.value instanceof Actress ? ((Actress) m.value).address.value : (m.value == null ? "NULL" : m.value.toString());

        return '"' + m.key + ":" + value
                .replace("[", "(")
                .replace("]", ")")
                + '"';
    }

    private void write(TraceMessage m) {
        Long exId = m.origin.executionId;

        Integer levelSender = m.sender().callLevel;
        String labelSender = node(m.sender().sender);
        String scopeSender = node(m.sender().scope);
        Integer levelReceiver = levelSender;
        String labelReceiver = node(m.receiver());
        String scopeReceiver = scopeSender;

        System.out.println(String.format("%s(%s)%s --> %s(%s)%s", labelSender, scopeSender, levelSender, labelReceiver, scopeReceiver, levelReceiver));

        if ((m.origin.sender instanceof FunctionCall) && (m.receiver() instanceof FunctionSignature)) {
            // levelSender ok
            // scopeSender ok;
            levelSender--;
            scopeReceiver = labelSender;
        }

        if ((m.origin.sender instanceof FunctionSignature) && (m.receiver() instanceof FunctionCall)) {
            levelSender ++;
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
        return '"' + String.format("%s(%d/%d)\n(%s)", label, executionId, callLevel, scope) + '"';
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
        writeLn("}");
        writer.flush();
        writer.close();
    }

}
