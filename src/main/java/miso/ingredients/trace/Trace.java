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

    private boolean active;
    private BufferedWriter writer;
    private List<TraceMessage> messages = new ArrayList<>();

    @Override
    protected Actress resolveTracer() {
        return this;
    }

    @Override
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
        super(new Address(Adresses.trace, 0));
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
        if (m.traced().key.equals(Name.removeState) || m.traced().key.equals(Name.removePartialAppValues)) {
            return;
        }

        String labelSender = node(m.traced().origin.sender);
        String labelReceiver = node(m.receiver());

        CallStack stackSender = new CallStack(m.traced().origin.functionCallTreeNode().getCallStack());
        CallStack stackReceiver = new CallStack(m.traced().origin.functionCallTreeNode().getCallStack());

        /*  call level matching: idea is to set each occurance of a function call, no matter if it sends or
         *  receives messages to the same call level (call stack), so that the graph becomes connected
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
        if (fromFunctionCallToAnywhereExceptSignatureAndSelfCalls(m)) {
            stackSender = stackSender.push(m.traced().origin.sender.address.id);
        }

        if (fromAnywhereToFunctionCallExceptSignatureAndSelfCalls(m))
        {
            stackReceiver = stackReceiver.push(m.receiver().address.id);
        }

        long exId = m.origin.functionCallTreeNode().getExecutionId();

        writeLn(String.format("%s -> %s [label=%s];",
                renderNode(labelSender, exId + "//" + stackSender),
                renderNode(labelReceiver, exId + "//" + stackReceiver),
                payload(m.traced())));
    }

    private boolean fromAnywhereToFunctionCallExceptSignatureAndSelfCalls(TraceMessage m) {
        return (m.receiver() instanceof FunctionCall) && !(m.traced().origin.sender.equals(m.receiver())) && !(m.traced().origin.sender instanceof FunctionSignature);
    }

    private boolean fromFunctionCallToAnywhereExceptSignatureAndSelfCalls(TraceMessage m) {
        return (m.traced().origin.sender instanceof FunctionCall) && !(m.receiver().equals(m.traced().origin.sender)) && !(m.receiver() instanceof FunctionSignature);
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
