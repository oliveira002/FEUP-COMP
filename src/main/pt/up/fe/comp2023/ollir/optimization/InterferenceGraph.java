package pt.up.fe.comp2023.ollir.optimization;

import java.util.ArrayList;

public class InterferenceGraph {

    private final LivenessAnalysis liveness;
    private ArrayList<InterferenceNode> nodes;

    public InterferenceGraph(LivenessAnalysis liveness) {
        this.liveness = liveness;
        this.nodes = new ArrayList<>();
    }
}
