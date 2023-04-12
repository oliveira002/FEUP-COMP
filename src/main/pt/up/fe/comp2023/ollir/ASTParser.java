package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.SymbolTableCR;

public class ASTParser implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {

        ASTParserVisitor visitor = new ASTParserVisitor((SymbolTableCR) jmmSemanticsResult.getSymbolTable());
        JmmNode root = jmmSemanticsResult.getRootNode();

        //Need to use StringBuilder instead of String because strings in java are immutable
        StringBuilder ollirCode = new StringBuilder();

        visitor.visit(root,ollirCode);
        System.out.println("Ollir: "+ollirCode);

        //OllirResult has 3 constructors: in the second there's no access to the symbol table.
        //The third constructor, this one, does the same as the first but programmatically.
        return new OllirResult(jmmSemanticsResult, ollirCode.toString(), jmmSemanticsResult.getReports());
    }
}
