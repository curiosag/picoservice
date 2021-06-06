package micro.visualize;

import micro.F;
import micro.FPropagation;
import micro.Names;
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
    private final _F start;

    public FVisualizer(_F f) {
        this.start = f;
        writer = createWriter(f.getLabel());
        writeLn("digraph G {\n graph [ranksep=0];\nnode [shape=record];\n");
    }

    private String edgeLabel(FPropagation p) {
        return '"' + p.nameReceived + "\n" + (p.nameToPropagate.equals(p.nameReceived) ? "" : p.nameToPropagate) + '"';
    }

    public void render() {
        render(start);
    }

    private void render(_F f) {
        if (!covered.contains(f)) {
            f.getPropagations().forEach(p -> {
                printNode(renderNode(f), renderNode(p.target), edgeLabel(p));

                if (p.target instanceof F ff) {
                    printNode(renderNode(p.target), renderNode(f), '"' + (ff.returnAs.equals(Names.result) ? "R": "R:" + ff.returnAs )+ '"');
                }
            });

            covered.add(f);
            f.getPropagations().stream()
                    .map(i -> i.target)
                    .distinct()
                    .forEach(this::render);
        }
    }

    private void printNode(String from, String to, String edge) {
        writeLn(String.format("%s -> %s [label=%s];", from, to, edge));
    }

    private String renderNode(_F f) {
        return '"' + f.getClass().getSimpleName() + "/" + f.getLabel() + '"';
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
