package pt.up.fe.comp2023.analysis.analysers;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.SemanticAnalysisVisitor;
import pt.up.fe.comp2023.analysis.SymbolTableCR;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        addVisit("CompareOp",this::visitCompareOp);
        addVisit("Not",this::visitNot);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }

    private Integer visitBinaryOp(JmmNode jmmNode, SymbolTableCR symbolTable) {
        JmmNode firstOperand = jmmNode.getJmmChild(0);
        JmmNode secOperand = jmmNode.getJmmChild(1);
        String op = jmmNode.get("op");

        Type fst = this.getNodeType(firstOperand,symbolTable);
        Type snd = this.getNodeType(secOperand,symbolTable);

        if(Objects.equals(fst.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Var is not defined!"));
            return 1;
        }

        if(Objects.equals(snd.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Var is not defined!"));
            return 1;
        }

        if(!Objects.equals(fst.getName(), "int") || fst.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"First Operand must be of type integer"));
        }
        if(!Objects.equals(snd.getName(), "int") || snd.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"First Operand must be of type integer"));
        }

        return 1;
    }

    private Integer visitLogicalOp(JmmNode jmmNode, SymbolTableCR symbolTable) {
        JmmNode firstOperand = jmmNode.getJmmChild(0);
        JmmNode secOperand = jmmNode.getJmmChild(1);
        String op = jmmNode.get("op");

        Type fst = this.getNodeType(firstOperand,symbolTable);
        Type snd = this.getNodeType(secOperand,symbolTable);

        if(Objects.equals(fst.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Var is not defined!"));
            return 1;
        }

        if(Objects.equals(snd.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Var is not defined!"));
            return 1;
        }

        if(!Objects.equals(fst.getName(), "boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"First Operand must be of type boolean"));
        }
        if(!Objects.equals(snd.getName(), "boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"First Operand must be of type boolean"));
        }

        return 1;
    }

    private Integer visitCompareOp(JmmNode jmmNode, SymbolTableCR symbolTable) {
        JmmNode firstOperand = jmmNode.getJmmChild(0);
        JmmNode secOperand = jmmNode.getJmmChild(1);
        String op = jmmNode.get("op");

        Type fst = this.getNodeType(firstOperand,symbolTable);
        Type snd = this.getNodeType(secOperand,symbolTable);

        if(Objects.equals(fst.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Var is not defined!"));
            return 1;
        }

        if(Objects.equals(snd.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Var is not defined!"));
            return 1;
        }

        if(!Objects.equals(fst.getName(),"int")  || !Objects.equals(snd.getName(),"int") || fst.isArray() || snd.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Types don't match in the comparison (<)"));
        }

        return 1;
    }

    private Integer visitNot(JmmNode jmmNode, SymbolTableCR symbolTable) {
        JmmNode var = jmmNode.getJmmChild(0);
        Type varType = this.getNodeType(var,symbolTable);

        if(Objects.equals(varType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Index is not defined!"));
            return 1;
        }

        if(!Objects.equals(varType.getName(), "boolean") || varType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Variable is not an integer array!"));
        }

        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
