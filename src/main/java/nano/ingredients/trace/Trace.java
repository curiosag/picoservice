package nano.ingredients.trace;

import nano.ingredients.*;
import nano.misc.Adresses;

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

    private boolean active;
    private BufferedWriter writer;
    private List<TraceMessage> messages = new ArrayList<>();

    @Override
    protected Actress resolveTracer() {
        return this;
    }

    public void setTrace(boolean active) {
        if (writer != null) {
            close();
        }

        this.active = active;
        if (active) {
            writer = createWriter();
            writeLn("digraph G {\n graph [ranksep=0];\nnode [shape=record];\n");
        }
    }

    public Trace(boolean active) {
        super(new Address(Adresses.trace, 0L));
        setTrace(active);
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
        if (m.getValue() == null) {
            value = "NULL";
        } else if (m.getValue() instanceof Actress) {
            value = node((Actress) m.getValue());
        } else {
            value = (m.getValue()).toString();
        }

        return '"' + String.format("(%d/%d)%s:", m.origin.executionId, m.origin.callStack.size(), m.key) + value
                .replace("[", "(")
                .replace("]", ")")
                + '"';
    }

    private void write(TraceMessage m) {
        if (m.traced().key.equals(Name.removeState) || m.traced().key.equals(Name.removePartialAppValues)) {
            return;
        }

        String labelSender = node(m.traced().origin.getSender());
        String labelReceiver = node(m.receiver());


        /*  call level matching: idea is to set each occurence of a function call in trace messages
         *  to the same call level (call stack), no matter if it sends or receives messages.
         *  This way the graph becomes connected.
         *
         *       anything (but function signature and function call sending consts to itself)
         *
         *       /\ +1       |          1)  anything (receive) <-- call (send)
         *       |           |              augment stack of sender
         *       |           |          2)  call (receive) <-- anything (send)
         *       |           V +1           augment stack of receiver
         *
         *       function call
         *
         * */

        CallStack stack = m.traced().origin.callTreePath().getCallStack();

        CallStack stackSender = fromFunctionCallToAnywhereExceptSignatureAndSelfCalls(m)
                ? stack.push(m.traced().origin.getSender().address.id)
                : stack;

        CallStack stackReceiver = fromAnywhereToFunctionCallExceptSignatureAndSelfCalls(m)
                ? stack.push(m.receiver().address.id)
                : stack;

        long exId = m.origin.callTreePath().getExecutionId();

        writeLn(String.format("%s -> %s [label=%s];",
                renderNode(labelSender, exId + "//" + stackSender),
                renderNode(labelReceiver, exId + "//" + stackReceiver),
                payload(m.traced())));
    }

    private boolean fromAnywhereToFunctionCallExceptSignatureAndSelfCalls(TraceMessage m) {
        return (m.receiver() instanceof FunctionCall) && !(m.traced().origin.getSender().equals(m.receiver())) && !(m.traced().origin.getSender() instanceof FunctionSignature);
    }

    private boolean fromFunctionCallToAnywhereExceptSignatureAndSelfCalls(TraceMessage m) {
        return (m.traced().origin.getSender() instanceof FunctionCall) && !(m.receiver().equals(m.traced().origin.getSender())) && !(m.receiver() instanceof FunctionSignature);
    }

    private String renderNode(String label, String stack) {
        return '"' + String.format("%s(%s)", label, stack) + '"';
    }

    @Override
    public void process(Message message) {
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
    public void close() {
        try {
            if (active) {
                writeLn("}");
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void flush() {
        try {
            if (writer != null) {
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
