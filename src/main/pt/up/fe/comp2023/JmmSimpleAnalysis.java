package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public class JmmSimpleAnalysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        JmmNode root = jmmParserResult.getRootNode();
        SymbolTableCR symbolTable = new SymbolTableCR();
        SymbolTableVisitor stVisitor =  new SymbolTableVisitor();
        stVisitor.visit(root,symbolTable);
        return new JmmSemanticsResult(jmmParserResult, symbolTable, stVisitor.reports);
    }
}
