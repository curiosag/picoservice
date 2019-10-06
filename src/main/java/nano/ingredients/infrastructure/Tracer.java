package nano.ingredients.infrastructure;

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
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;

public class Tracer extends Actress implements Closeable {

    private boolean active;
    private BufferedWriter writer;

    private final ComputationPaths paths = new ComputationPaths();
    private final ComputationPaths replayedPaths = new ComputationPaths();

    @Override
    protected Tracer resolveTracer() {
        return this;
    }

    public ComputationPaths getComputationPaths() {
        return paths;
    }

    public ComputationPaths getReplayedComputationPaths() {
        return replayedPaths;
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

    public Tracer(boolean active) {
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

        return '"' + String.format("(%d/%d)%s:", m.origin.getExecutionId(), m.origin.getComputationPath().size(), m.key) + value
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

        ComputationPath bough = m.traced().origin.getComputationPath();

        List<Long> stackSender = bough.getStack().getItems();
        if (fromFunctionCallToAnywhereExceptSignatureAndSelfCalls(m)) {
            stackSender.add(m.traced().origin.getSender().address.id);
        }

        List<Long> stackReceiver = bough.getStack().getItems();
        if (fromAnywhereToFunctionCallExceptSignatureAndSelfCalls(m)) {
            stackReceiver.add(m.receiver().address.id);
        }

        long exId = m.origin.functionCallTreeLocation().getExecutionId();

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
        if (message.key.equals(Name.computationBranch)) {
            paths.add((ComputationPath) message.getValue());
            return;
        }

        if (!(message instanceof TraceMessage)) {
            throw new IllegalStateException();
        }
        write((TraceMessage) message);
    }

    @Override
    public void receiveRecover(Message m) {
        maybePath(m).ifPresent(p -> {
            if (paths.add(p)) {
                replayedPaths.add(p);
            }
        });
    }

    @Override
    public boolean shouldPersist(Message m) {
        return maybePath(m)
                .map(b -> !paths.contains(b))
                .orElse(FALSE);
    }

    private Optional<ComputationPath> maybePath(Message m) {
        if (m.getValue() instanceof ComputationPath) {
            return Optional.of((ComputationPath) m.getValue());
        }
        return Optional.empty();
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
