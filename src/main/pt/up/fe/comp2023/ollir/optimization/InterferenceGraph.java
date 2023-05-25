package pt.up.fe.comp2023.ollir.optimization;

import org.specs.comp.ollir.Method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public class InterferenceGraph {

    private final LivenessAnalysis liveness;
    private ArrayList<InterferenceNode> nodes;
    private final HashSet<String> variables;

    private final Method method;

    private final HashSet<String> pairs;

    private int minRegisters;

    public InterferenceGraph(LivenessAnalysis liveness, int registers) {
        this.liveness = liveness;
        this.variables = liveness.getVariables();
        this.pairs = liveness.getPairs();
        this.nodes = new ArrayList<>();
        this.minRegisters = registers;
        this.method = liveness.getMethod();
    }

    public void buildGraph() {
        initNodes();
        initEdges();
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

    public void colorGraph() {
        int numColors = minRegisters; // Set the number of colors (registers) to the minimum required

        Stack<InterferenceNode> stack = new Stack<>();

        // Find the initial nodes with less than numColors edges
        for (InterferenceNode node : nodes) {
            if (node.getEdges().size() < numColors) {
                stack.push(node);
            }
        }

        // Check if the algorithm can be applied
        if (stack.isEmpty()) {
            System.out.println("Cannot apply the algorithm. No nodes with less than " + numColors + " edges.");
            return;
        }

        int maxColor = -1; // Tracks the maximum register assigned

        // Remove nodes from the graph and assign colors
        while (!stack.isEmpty()) {
            InterferenceNode node = stack.pop();

            // Find the lowest available color (register) for the node
            boolean[] usedColors = new boolean[numColors];
            for (InterferenceNode neighbor : node.getEdges()) {
                if (neighbor.getRegister() != -1) {
                    usedColors[neighbor.getRegister()] = true;
                }
            }

            // Assign the lowest unused color
            int color;
            for (color = 0; color < numColors; color++) {
                if (!usedColors[color]) {
                    break;
                }
            }

            node.setRegister(color);
            maxColor = Math.max(maxColor, color); // Update the maximum register assigned

            // Remove the node from the graph
            nodes.remove(node);
            for (InterferenceNode neighbor : node.getEdges()) {
                neighbor.getEdges().remove(node);
                if (neighbor.getEdges().size() < numColors && !stack.contains(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }

        int numUsedColors = maxColor + 1; // Calculate the number of used colors
        System.out.println("Number of colors used: " + numUsedColors);
    }
}
