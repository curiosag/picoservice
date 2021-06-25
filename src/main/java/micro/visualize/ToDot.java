package micro.visualize;

import micro.*;

import java.io.BufferedWriter;
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

public class ToDot {

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

    public ToDot(String className, _F f, Path dest) {
        this.start = f;
        writer = createWriter(className + '.' + f.getLabel(), dest);
    }

    private String edgeLabel(String nameReceived, String nameToPropagate, PropagationType propagationType) {
        String renamed = nameReceived.equals(nameToPropagate) ? "" : ("[" + nameToPropagate + "]");
        return String.format("[label=\"%s\" color=\"%s\" fontcolor=\"%s\"]", nameReceived + renamed, getColor(propagationType), getColor(propagationType));
    }

    private String getColor(PropagationType propagationType) {
        return colors.get(propagationType);
    }

    public void render() {
        writeLn("digraph G {\nlabel=" + legend + "graph [ranksep=0 rankdir=LR fontsize=\"14\"];\nnode [shape=circle];\nedge [fontsize=\"12\"];\n");
        renderNodes(start);
        covered.clear();
        writeLn("");
        renderEdges(start);
        writeLn("}");
        close();
    }

    private void renderEdges(_F f) {
        if (!covered.contains(f)) {
            covered.add(f);
            f.getPropagations().forEach(p -> {
                edge(f, p.target, edgeLabel(p.nameReceived, p.nameToPropagate, p.propagationType));
                if (p.nameToPropagate.equals(Names.result)) {
                    retcovered.add(f);
                }
                if (!retcovered.contains(p.target) && p.target instanceof F retFrom && !isSelfPropagation(f, p)) {
                    retcovered.add(retFrom);
                    edge(retFrom, f, edgeLabel(Names.result, retFrom.returnAs, p.propagationType));
                }
                renderEdges(p.target);
            });
        }
    }

    private boolean isSelfPropagation(_F f, FPropagation p) {
        return p.target.equals(f);
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
        if (f instanceof F ff && ff.formalParameters.size() > 0) {
            params = "\n(" + String.join(",", ff.formalParameters) + ")";
        }

        String color = f.isNative() ? "gray90" : "black";
        String displayName;
        if (f.isNative() || f instanceof FCall) {
            String[] parts = f.getLabel().split("\\.");
            Check.condition(parts.length >= 2);
            displayName = parts[parts.length -2] + ".\n" + parts[parts.length -1] + params;
        } else {
            displayName = f.getLabel() + params;
        }
        return String.format("N%d [label= \"%s\" color=\"%s\" fontcolor=\"%s\" fullName=\"%s\" params=\"%s\"];", f.getId(), displayName, color, "black", f.getLabel(), params);
    }

    private BufferedWriter createWriter(String name, Path dest) {
        String dotName = String.format("/%s.dot", name);
        Path path = Paths.get(dest.toString() + dotName);
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

    private void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
