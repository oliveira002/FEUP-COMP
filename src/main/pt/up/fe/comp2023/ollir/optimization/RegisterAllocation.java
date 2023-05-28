package pt.up.fe.comp2023.ollir.optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Map;

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
        for (Method method : methods) {
            Map<String, Descriptor> varTable = method.getVarTable();
            LivenessAnalysis liveAnalysis = new LivenessAnalysis(method);
            liveAnalysis.analyse();
            InterferenceGraph graph = new InterferenceGraph(liveAnalysis, numRegisters, method);
            graph.buildGraph();
            if(!graph.colorGraph(numRegisters)) {
                ollirResult.getReports().add(Report.newError(Stage.OPTIMIZATION,0,0,"Not enough registers provided in input",null));
                return;
            };

            int regOffset = method.isStaticMethod() ? 0 : 1;
            regOffset += method.getParams().size();

            for(InterferenceNode node: graph.getNodes()) {
                if(!(varTable.get(node.getVar()).getScope().equals(VarScope.PARAMETER) || varTable.get(node.getVar()).getVarType().getTypeOfElement().equals(ElementType.THIS)))
                    varTable.get(node.getVar()).setVirtualReg(node.getRegister() + regOffset - 1);
            }
        }

        int a = 2;
    }
}
