package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.analysis.SymbolTableCR;
import pt.up.fe.comp2023.ollir.optimization.ConstantFolding;
import pt.up.fe.comp2023.ollir.optimization.ConstantPropagation;
import pt.up.fe.comp2023.ollir.optimization.RegisterAllocation;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {

        JmmOptimizerVisitor visitor = new JmmOptimizerVisitor((SymbolTableCR) jmmSemanticsResult.getSymbolTable());
        JmmNode root = jmmSemanticsResult.getRootNode();

        //Need to use StringBuilder instead of String because strings in java are immutable
        StringBuilder ollirCode = new StringBuilder();

        visitor.visit(root,ollirCode);

        if(jmmSemanticsResult.getConfig().getOrDefault("optimize", "false").equals("true"))
            return optimize(new OllirResult(jmmSemanticsResult, ollirCode.toString(), jmmSemanticsResult.getReports()));
        return new OllirResult(jmmSemanticsResult, ollirCode.toString(), jmmSemanticsResult.getReports());
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult jmmSemanticsResult) {
        JmmNode root = jmmSemanticsResult.getRootNode();
        if(jmmSemanticsResult.getConfig().getOrDefault("optimize", "false").equals("true")) {
            boolean changes = true;
            while(changes) {
                ConstantPropagation constantProp = new ConstantPropagation();
                ConstantFolding constantFold = new ConstantFolding();
                changes = constantFold.visit(root,1);
                changes = constantProp.visit(root,1) || changes;
            }
        }
        return jmmSemanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {

        // check for register flag
        int numRegisters = -1;
        if(ollirResult.getConfig().containsKey("registerAllocation")) {
            numRegisters = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
        }

        if(numRegisters == -1) {
            return ollirResult;
        }
        else {
            RegisterAllocation registerAllocation = new RegisterAllocation(ollirResult,numRegisters, ollirResult.getConfig());
            registerAllocation.regAlloc();
        }
        return ollirResult;
    }

}
