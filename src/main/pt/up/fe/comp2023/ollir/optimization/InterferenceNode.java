package pt.up.fe.comp2023.ollir.optimization;

import java.util.ArrayList;

public class InterferenceNode {

    int register;
    private String var;
    private ArrayList<InterferenceNode> edges;

    public InterferenceNode(String var) {
        this.var = var;
        this.edges = new ArrayList<>();
    }

    public ArrayList<InterferenceNode> getEdges() {
        return edges;
    }

    public int getRegister() {
        return register;
    }

    public void addEdge(InterferenceNode to) {
        this.edges.add(to);
    }

    public String getVar() {
        return var;
    }

    public void setEdges(ArrayList<InterferenceNode> edges) {
        this.edges = edges;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
