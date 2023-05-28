package pt.up.fe.comp2023.ollir.optimization;

import org.specs.comp.ollir.Method;

import java.util.*;

public class InterferenceGraph {

    private final LivenessAnalysis liveness;
    private ArrayList<InterferenceNode> nodes;
    private final HashSet<String> variables;

    private final Method method;

    private final HashSet<String> pairs;

    private int registers;
    private  Map<String, String> config;

    public InterferenceGraph(LivenessAnalysis liveness, int registers, Method method, Map<String, String> config) {
        this.liveness = liveness;
        this.variables = liveness.getVariables();
        this.pairs = liveness.getPairs();
        this.nodes = new ArrayList<>();
        this.registers = registers;
        this.method = method;
        this.config = config;
    }

    public void buildGraph() {
        initNodes();
        initEdges();
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

    public boolean colorGraph(Integer maxRegisters) {
        int numColors;
        int minColors = calculateMinimumRegisters();
        if(maxRegisters == 0) {
            numColors = minColors;
        }
        else {
            numColors = maxRegisters;
        }

        if(numColors < minColors) {
            System.out.println("Not enough colors to color the graph: " + " need at least " + minColors);
            return false;
        }

        int[] nodeColors = new int[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            InterferenceNode node = nodes.get(i);

            boolean[] usedColors = new boolean[numColors];

            for (InterferenceNode neighbor : node.getEdges()) {
                int neighborColor = nodeColors[nodes.indexOf(neighbor)];
                if (neighborColor != 0) {
                    usedColors[neighborColor - 1] = true;
                }
            }

            int color;
            for (color = 0; color < numColors; color++) {
                if (!usedColors[color]) {
                    break;
                }
            }

            nodeColors[i] = color + 1;
        }

        int numUsedColors = Arrays.stream(nodeColors).max().orElse(0);

        if(config.getOrDefault("debug", "false").equals("true")) {
            System.out.println("Number of colors used: " + numUsedColors);
        }
        Map<Integer, List<InterferenceNode>> colorMap = new HashMap<>();
        for (int i = 0; i < nodeColors.length; i++) {
            int color = nodeColors[i];
            colorMap.computeIfAbsent(color, k -> new ArrayList<>()).add(nodes.get(i));
        }

        for (int color = 1; color <= numUsedColors; color++) {
            List<InterferenceNode> nodesForColor = colorMap.getOrDefault(color, Collections.emptyList());
            for(int i = 0; i < nodesForColor.size(); i++) {
                if(config.getOrDefault("debug", "false").equals("true")) {
                    System.out.println("Nodes with color " + color + ": " + nodesForColor.get(i).getVar());
                }
                InterferenceNode no = this.getNode(nodesForColor.get(i).getVar());
                no.setRegister(color);
            }
        }

        return true;
    }

    private int calculateMinimumRegisters() {
        int maxDegree = 0;
        for (InterferenceNode node : nodes) {
            int degree = node.getEdges().size();
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }
        return maxDegree + 1;
    }
}
