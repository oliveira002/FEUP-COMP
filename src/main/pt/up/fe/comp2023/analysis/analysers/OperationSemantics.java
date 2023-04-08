package pt.up.fe.comp2023.analysis.analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.SemanticAnalysisVisitor;
import pt.up.fe.comp2023.analysis.SymbolTableCR;

import java.util.ArrayList;
import java.util.List;

public class OperationSemantics extends SemanticAnalysisVisitor {

    private final List<Report> reports;

    public OperationSemantics() {
        this.reports = new ArrayList<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("BinaryOp", this::visitBinaryOp);
        addVisit("LogicalOp", this::visitLogicalOp);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }

    private Integer visitBinaryOp(JmmNode jmmNode, SymbolTableCR symbolTable) {
        JmmNode firstOperand = jmmNode.getJmmChild(0);
        JmmNode secOperand = jmmNode.getJmmChild(1);
        String op = jmmNode.get("op");

        boolean firstValid = this.checkValidNode(firstOperand,symbolTable,"Integer",false);
        boolean secValid = this.checkValidNode(secOperand,symbolTable,"Integer",false);

        if(!firstValid) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"First Operand must be of type Integer"));
        }
        if(!secValid) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Second Operand must be of type Integer"));
        }

        return 1;
    }

    private Integer visitLogicalOp(JmmNode jmmNode, SymbolTableCR symbolTable) {
        JmmNode firstOperand = jmmNode.getJmmChild(0);
        JmmNode secOperand = jmmNode.getJmmChild(1);
        String op = jmmNode.get("op");

        boolean firstValid = this.checkValidNode(firstOperand,symbolTable,"Boolean",false);
        boolean secValid = this.checkValidNode(secOperand,symbolTable,"Boolean",false);

        if(!firstValid) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"First Operand must be of type boolean"));
        }
        if(!secValid) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"First Operand must be of type boolean"));
        }

        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
