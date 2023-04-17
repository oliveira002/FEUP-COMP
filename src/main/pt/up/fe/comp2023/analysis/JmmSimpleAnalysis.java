package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.analysers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmmSimpleAnalysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        JmmNode root = jmmParserResult.getRootNode();
        SymbolTableCR symbolTable = new SymbolTableCR();
        SymbolTableVisitor stVisitor =  new SymbolTableVisitor();
        List<Report> reps = symbolTable.getReports();
        stVisitor.visit(root,symbolTable);

        List<SemanticAnalysisVisitor> visitors = Arrays.asList(new ArrayAccess(), new ConditionSemantics());
        for(SemanticAnalysisVisitor v: visitors) {
            v.visit(root,symbolTable);
            reps.addAll(v.getReports());
        }
        return new JmmSemanticsResult(jmmParserResult, symbolTable, reps);
    }
}
