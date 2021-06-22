package micro.compiler.sources;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;

import java.util.List;

import static com.github.javaparser.utils.Utils.SYSTEM_EOL;
import static com.github.javaparser.utils.Utils.assertNotNull;
import static java.util.stream.Collectors.toList;

public class ToDot {
    private static final String eol = SYSTEM_EOL;

    private StringBuilder output;
    private int nodeCount;
    private final boolean outputNodeType;

    public ToDot(boolean outputNodeType) {
        this.outputNodeType = outputNodeType;
    }

    public String get(Node node){
        nodeCount = 0;
        output = new StringBuilder();
        output.append("digraph {");
        output(node, null, "root", output);
        output.append(eol).append("}");
        return output.toString();
    }

    public void output(Node node, String parentNodeName, String name, StringBuilder builder) {
        assertNotNull(node);
        NodeMetaModel metaModel = node.getMetaModel();
        List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        List<PropertyMetaModel> attributes = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute)
                .filter(PropertyMetaModel::isSingular).collect(toList());
        List<PropertyMetaModel> subNodes = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode)
                .filter(PropertyMetaModel::isSingular).collect(toList());
        List<PropertyMetaModel> subLists = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList)
                .collect(toList());

        String ndName = nextNodeName();
        if (outputNodeType)
            builder.append(eol + ndName + " [label=\"" + escape(name) + " (" + metaModel.getTypeName()
                    + ")\"];");
        else
            builder.append(eol + ndName + " [label=\"" + escape(name) + "\"];");

        if (parentNodeName != null)
            builder.append(eol + parentNodeName + " -> " + ndName + ";");

        for (PropertyMetaModel a : attributes) {
            String attrName = nextNodeName();
            builder.append(eol + attrName + " [label=\"" + escape(a.getName()) + "='"
                    + escape(a.getValue(node).toString()) + "'\"];");
            builder.append(eol + ndName + " -> " + attrName + ";");

        }

        for (PropertyMetaModel sn : subNodes) {
            Node nd = (Node) sn.getValue(node);
            if (nd != null)
                output(nd, ndName, sn.getName(), builder);
        }

        for (PropertyMetaModel sl : subLists) {
            NodeList<? extends Node> nl = (NodeList<? extends Node>) sl.getValue(node);
            if (nl != null && nl.isNonEmpty()) {
                String ndLstName = nextNodeName();
                builder.append(eol + ndLstName + " [label=\"" + escape(sl.getName()) + "\"];");
                builder.append(eol + ndName + " -> " + ndLstName + ";");
                String slName = sl.getName().substring(0, sl.getName().length() - 1);
                for (Node nd : nl)
                    output(nd, ndLstName, slName, builder);
            }
        }
    }

    private String nextNodeName() {
        return "n" + (nodeCount++);
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }

}
