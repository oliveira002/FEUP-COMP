package pt.up.fe.comp2023.ollir.optimization;

import org.specs.comp.ollir.Method;

import java.util.*;

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
        int a = 2;
    }

    public void initNodes() {
        for(String var: variables) {
            InterferenceNode node = new InterferenceNode(var);
            this.nodes.add(node);
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

    public void colorGraph(Integer... maxRegisters) {
        int numColors;
        if (maxRegisters.length > 0) {
            int max = maxRegisters[0];
            numColors = Math.min(max, minRegisters); // Set the number of colors (registers) to the minimum required or the specified maximum
        } else {
            numColors = minRegisters; // Use the minimum required registers
        }

        // Create an array to store the assigned colors for each node
        int[] nodeColors = new int[nodes.size()];

        // Iterate over the nodes and color them using the greedy algorithm
        for (int i = 0; i < nodes.size(); i++) {
            InterferenceNode node = nodes.get(i);

            // Create a boolean array to track the used colors by the neighboring nodes
            boolean[] usedColors = new boolean[numColors];

            // Check the colors used by the neighboring nodes
            for (InterferenceNode neighbor : node.getEdges()) {
                int neighborColor = nodeColors[nodes.indexOf(neighbor)];
                if (neighborColor != 0) {
                    usedColors[neighborColor - 1] = true;
                }
            }

            // Find the lowest unused color for the current node
            int color;
            for (color = 0; color < numColors; color++) {
                if (!usedColors[color]) {
                    break;
                }
            }

            // Assign the color to the current node
            nodeColors[i] = color + 1;
        }

        int numUsedColors = Arrays.stream(nodeColors).max().orElse(0); // Calculate the number of used colors
        System.out.println("Number of colors used: " + numUsedColors);

        // Print nodes for each color
        Map<Integer, List<InterferenceNode>> colorMap = new HashMap<>();
        for (int i = 0; i < nodeColors.length; i++) {
            int color = nodeColors[i];
            colorMap.computeIfAbsent(color, k -> new ArrayList<>()).add(nodes.get(i));
        }

        for (int color = 1; color <= numUsedColors; color++) {
            List<InterferenceNode> nodesForColor = colorMap.getOrDefault(color, Collections.emptyList());
            for(int i = 0; i < nodesForColor.size(); i++) {
                System.out.println("Nodes with color " + color + ": " + nodesForColor.get(i).getVar());
            }
        }
    }

    private void resetGraph() {
        for (InterferenceNode node : nodes) {
            node.setRegister(-1); // Reset the register for each node
        }
    }
}
