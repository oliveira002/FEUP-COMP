package pt.up.fe.comp2023.ollir.optimization;

import java.util.ArrayList;
import java.util.HashSet;

public class InterferenceGraph {

    private final LivenessAnalysis liveness;
    private ArrayList<InterferenceNode> nodes;
    private final HashSet<String> variables;

    private final HashSet<String> pairs;

    private int minRegisters;

    public InterferenceGraph(LivenessAnalysis liveness) {
        this.liveness = liveness;
        this.variables = liveness.getVariables();
        this.pairs = liveness.getPairs();
        this.nodes = new ArrayList<>();
    }

    public void buildGraph() {
        initNodes();
        initEdges();
        int a = 2;
    }

    public void initNodes() {
        for(String var: variables) {
            InterferenceNode node = new InterferenceNode(var);
            nodes.add(node);
        }
    }

    public void initEdges() {
        for(String pair: pairs) {
            String[] elements = pair.split("-");
            String from = elements[0];
            String to = elements[1];
            addEdge(from,to);
        }
    }

    public void addEdge(String from, String to) {
        InterferenceNode fromNode = getNode(from);
        InterferenceNode toNode = getNode(to);
        fromNode.addEdge(toNode);
        toNode.addEdge(fromNode);
    }

    public InterferenceNode getNode(String var) {
        for(InterferenceNode node: nodes) {
            if(node.getVar().equals(var)) {
                return node;
            }
        }
        return null;
    }

    public ArrayList<InterferenceNode> getNodes() {
        return nodes;
    }
}
