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
        String methodName = jmmNode.getJmmParent().get("name");

        Type left_hand =  this.getVariableType(left.get("var"),methodName,symbolTable);

        // check if left side is an array
        if(!left_hand.isArray() || (!Objects.equals(left_hand.getName(), "int"))) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Array Access must be done over an integer array!"));
        }

        // check if right side is a literal
        if(this.isLiteral(right)) {
            if(!Objects.equals(right.getKind(), "Integer")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Array Access must use an integer"));
            }
        }

        // if it's not a literal need to check its original type
        else {
            Type right_hand = this.getVariableType(right.get("var"),methodName,symbolTable);
            // check if right side is an integer and not an array
            if(!Objects.equals(right_hand.getName(), "int") || right_hand.isArray())  {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Array Access must use an integer!"));
            }
        }

        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
