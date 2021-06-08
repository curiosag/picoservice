package micro.visualize;

import micro.F;
import micro.FPropagation;
import micro._F;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FVisualizer implements Closeable {

    private BufferedWriter writer;

    List<_F> covered = new ArrayList<>();
    List<_F> retcovered = new ArrayList<>();

    private final _F start;

    private static final String legend = """
                      <table cellspacing='40' border='0' cellborder='0'>
                        <tr><td><font color="blue">condition</font></td>
                        <td><font color="green">true-branch</font></td>
                        <td><font color="red">false-branch</font></td></tr>
                      </table>
            """;

    public FVisualizer(_F f) {
        this.start = f;
        writer = createWriter(f.getLabel());
        writeLn("digraph G {\n label=<"+ legend + ">; labelloc = \"b\"; graph [ranksep=0 rankdir=LR fontsize=\"14\"]; node [shape=circle] edge [fontsize=\"12\"];\n");
    }

    private String edgeLabel(FPropagation p) {
        String renamed = p.nameToPropagate.equals(p.nameReceived) ? "" : ("[" + p.nameToPropagate + "]");
        return "[label=\"" + p.nameReceived + renamed + "\" color=\"" + p.propagationType.color + "\" fontcolor=\"" + p.propagationType.color + "\"]";
    }

    public void render() {
        render(start);
    }

    private void render(_F f) {
        if (!covered.contains(f)) {
            covered.add(f);
            f.getPropagations().forEach(p -> {
                printNode(renderNode(f), renderNode(p.target), edgeLabel(p));

                if (!retcovered.contains(p.target) && p.target instanceof F retFrom) {
                    retcovered.add(retFrom);
                    printNode(renderNode(retFrom), renderNode(f), "[label=\"" + "result[" + retFrom.returnAs + "]\"]");
                }
                render(p.target);
            });
        }
    }

    private void printNode(String from, String to, String edgeLabel) {
        writeLn(String.format("%s -> %s %s;", from, to, edgeLabel));
    }

    private String renderNode(_F f) {
        String params = "";
        if (f instanceof F ff) {
            params = String.join(",", ff.formalParameters);
        }
        return '"' + f.getLabel() + "(" + params + ")" + '"';
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
