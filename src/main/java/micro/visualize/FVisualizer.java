package micro.visualize;

import micro.F;
import micro.FPropagation;
import micro.PropagationType;
import micro._F;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static micro.PropagationType.*;

public class FVisualizer implements Closeable {

    private static final Map<PropagationType, String> colors = new HashMap<>() {{
        put(INDISCRIMINATE, "black");
        put(COND_CONDITION, "blue");
        put(COND_TRUE_BRANCH, "green");
        put(COND_FALSE_BRANCH, "red");
    }};

    private BufferedWriter writer;

    List<_F> covered = new ArrayList<>();
    List<_F> retcovered = new ArrayList<>();

    private final _F start;

    private static final String legend = """
            
            <<table cellspacing='40' border='0' cellborder='0'>
              <tr><td><font color="blue">condition</font></td>
              <td><font color="green">true-branch</font></td>
              <td><font color="red">false-branch</font></td></tr>
            </table>>; labelloc = "b";
            
            """;

    public FVisualizer(_F f) {
        this.start = f;
        writer = createWriter(f.getLabel());
        writeLn("digraph G {\nlabel=" + legend + "graph [ranksep=0 rankdir=LR fontsize=\"14\"];\nnode [shape=circle];\nedge [fontsize=\"12\"];\n");
    }

    private String edgeLabel(FPropagation p) {
        String renamed = p.nameToPropagate.equals(p.nameReceived) ? "" : ("[" + p.nameToPropagate + "]");
        return String.format("[label=\"%s\" color=\"%s\" fontcolor=\"%s\"]",  p.nameReceived + renamed,  colors.get(p.propagationType), colors.get(p.propagationType));
    }

    public void render() {
        renderNodes(start);
        covered.clear();
        writeLn("");
        renderEdges(start);
    }

    private void renderEdges(_F f) {
        if (!covered.contains(f)) {
            covered.add(f);
            f.getPropagations().forEach(p -> {
                edge(f, p.target, edgeLabel(p));

                if (!retcovered.contains(p.target) && p.target instanceof F retFrom) {
                    retcovered.add(retFrom);
                    edge(retFrom, f, "[label=\"" + "result[" + retFrom.returnAs + "]\"]");
                }
                renderEdges(p.target);
            });
        }
    }

    private void edge(_F from, _F to, String edgeLabel) {
        writeLn(String.format("N%d -> N%d %s;", from.getId(), to.getId(), edgeLabel));
    }

    private void renderNodes(_F f) {
        if (!covered.contains(f)) {
            covered.add(f);
            writeLn(renderNode(f));
            f.getPropagations().forEach(p -> renderNodes(p.target));
        }
    }

    private String renderNode(_F f) {
        String params = "";
        if (f instanceof F ff) {
            params = String.join(",", ff.formalParameters);
        }
        return String.format("N%d [label= \"%s\"];", f.getId(), f.getLabel() + "(" + params + ")");
    }

    private BufferedWriter createWriter(String name) {
        String traceName = String.format("/%s.dot", name);
        String tracePath = System.getProperty("user.dir");
        Path path = Paths.get(tracePath + traceName);
        System.out.println("visualization at " + path);
        try {
            return Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
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

    @Override
    public void close() {
        writeLn("}");
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
