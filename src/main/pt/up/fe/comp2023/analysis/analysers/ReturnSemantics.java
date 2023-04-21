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

public class ReturnSemantics extends SemanticAnalysisVisitor {

    private final List<Report> reports;

    public ReturnSemantics() {
        this.reports = new ArrayList<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("MethodDeclaration", this::visitReturn);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }

    private Integer visitReturn(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String methodName = jmmNode.get("name");
        if(!Objects.equals(methodName, "main")) {
            JmmNode returnExp = jmmNode.getJmmChild(jmmNode.getNumChildren()-1);
            Type returnedType = this.getNodeType(returnExp,symbolTable);

            if(Objects.equals(returnedType.getName(), "unknown")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Variable is not defined!"));
                return 1;
            }

            if(Objects.equals(returnExp.getKind(), "MethodCall") && !symbolTable.methodExists(returnExp.get("var"))) {
                return 1;
            }

            Type originalType = symbolTable.getReturnType(methodName);
            if(!Objects.equals(returnedType.getName(), originalType.getName()) || returnedType.isArray() != originalType.isArray()) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Return type doesn't  match with what is being returned!"));
            }
        }
        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
