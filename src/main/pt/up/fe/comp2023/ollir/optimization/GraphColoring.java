package pt.up.fe.comp2023.ollir.optimization;

import org.antlr.v4.misc.Graph;

import java.util.ArrayList;

public class GraphColoring {
    private InterferenceGraph graph;
    private int k;
    private ArrayList<InterferenceNode> stack;

    public GraphColoring(InterferenceGraph graph, int k, ArrayList<InterferenceNode> stack) {
        this.graph = graph;
        this.k = k;
        this.stack = stack;
    }

    public int initStack() {
        ArrayList<InterferenceNode> nodes = this.graph.getNodes();
        return 1;
    }

}
