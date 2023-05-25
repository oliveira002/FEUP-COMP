package pt.up.fe.comp2023.ollir.optimization;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.ArrayList;

public class RegisterAllocation {
    private final int numRegisters;

    private final OllirResult ollirResult;

    private final ClassUnit ollirClass;

    public RegisterAllocation(OllirResult ollirResult, int n) {
        this.numRegisters = n;
        this.ollirResult = ollirResult;
        this.ollirClass = ollirResult.getOllirClass();
    }

    public void regAlloc() {
        ArrayList<Method> methods = ollirClass.getMethods();
        ollirClass.buildCFGs();
        for(Method method: methods) {
            LivenessAnalysis liveAnalysis = new LivenessAnalysis(method);
            liveAnalysis.analyse();
            InterferenceGraph graph = new InterferenceGraph(liveAnalysis, numRegisters);
            graph.buildGraph();
            graph.colorGraph();
        }
    }
}
