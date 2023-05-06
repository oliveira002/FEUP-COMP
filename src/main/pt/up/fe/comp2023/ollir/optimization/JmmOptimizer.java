package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.ollir.optimization.ConstantFolding;

public class JmmOptimizer implements JmmOptimization {

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        JmmNode root = semanticsResult.getRootNode();
        ConstantFolding constantFold = new ConstantFolding();
        constantFold.visit(root,null);
        ConstantPropagation constantProp = new ConstantPropagation();
        constantProp.visit(root,null);
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return JmmOptimization.super.optimize(ollirResult);
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        return null;
    }
}
