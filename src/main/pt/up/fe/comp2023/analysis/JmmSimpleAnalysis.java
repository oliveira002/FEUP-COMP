package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.analysers.*;

import java.util.List;

public class JmmSimpleAnalysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        JmmNode root = jmmParserResult.getRootNode();
        SymbolTableCR symbolTable = new SymbolTableCR();
        SymbolTableVisitor stVisitor =  new SymbolTableVisitor();
        stVisitor.visit(root,symbolTable);


        ArrayAccess ola = new ArrayAccess();
        AssignmentSemantics ola2 = new AssignmentSemantics();
        OperationSemantics ola3 = new  OperationSemantics();
        ConditionSemantics ola4 = new ConditionSemantics();
        ThisSemantics ola5 = new ThisSemantics();

        ola.visit(root,symbolTable);
        ola2.visit(root,symbolTable);
        ola3.visit(root,symbolTable);
        ola4.visit(root,symbolTable);
        ola5.visit(root,symbolTable);
        List<Report> reps = ola3.getReports();
        //reps.addAll(ola2.getReports());
        //reps.addAll(ola3.getReports());
        //reps.addAll(ola4.getReports());

        int a = 2;
        return new JmmSemanticsResult(jmmParserResult, symbolTable, reps);
    }
}
