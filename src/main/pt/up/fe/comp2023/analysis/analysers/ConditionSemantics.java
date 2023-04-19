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

public class ConditionSemantics extends SemanticAnalysisVisitor {

    private final List<Report> reports;

    public ConditionSemantics() {
        this.reports = new ArrayList<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("ConditionStmt", this::visitConditionStmt);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }

    private Integer visitConditionStmt(JmmNode jmmNode, SymbolTableCR symbolTable) {
        JmmNode condition = jmmNode.getJmmChild(0);
        Type conditionType = this.getNodeType(condition,symbolTable);

        if(Objects.equals(conditionType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable is not defined!"));
            return 1;
        }

        if(Objects.equals(condition.getKind(), "MethodCall") && !symbolTable.methodExists(condition.get("var"))) {
            return 1;
        }

        if(!Objects.equals(conditionType, new Type("boolean", false))) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Condition must be of type boolean!"));
        }
        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
