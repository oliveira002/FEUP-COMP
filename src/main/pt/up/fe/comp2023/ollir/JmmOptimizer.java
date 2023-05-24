package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.analysis.SymbolTableCR;
import pt.up.fe.comp2023.ollir.optimization.ConstantFolding;
import pt.up.fe.comp2023.ollir.optimization.ConstantPropagation;

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
            ConstantPropagation constantProp = new ConstantPropagation();
            //constantProp.visit(root,null);
            ConstantFolding constantFold = new ConstantFolding();
            //constantFold.visit(root,null);
        }
        return jmmSemanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return JmmOptimization.super.optimize(ollirResult);
    }

}
