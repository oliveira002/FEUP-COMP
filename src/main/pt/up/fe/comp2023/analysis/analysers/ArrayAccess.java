package pt.up.fe.comp2023.analysis.analysers;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.SemanticAnalysisVisitor;
import pt.up.fe.comp2023.analysis.SymbolTableCR;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrayAccess extends SemanticAnalysisVisitor {

    private final List<Report> reports;

    public ArrayAccess() {
        this.reports = new ArrayList<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("ArrayIndex", this::visitArrayIndex);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }

    private Integer visitArrayIndex(JmmNode jmmNode, SymbolTableCR symbolTable) {
        JmmNode left = jmmNode.getJmmChild(0);
        JmmNode right = jmmNode.getJmmChild(1);

        Type leftType = getNodeType(left,symbolTable);
        Type rightType = getNodeType(right,symbolTable);


        if(!Objects.equals(leftType, new Type("int", true))) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Array Access is not being done over an array!"));
        }

        if(!Objects.equals(rightType, new Type("int", false))) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Array Access is not being done with an Integer!"));
        }
        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
