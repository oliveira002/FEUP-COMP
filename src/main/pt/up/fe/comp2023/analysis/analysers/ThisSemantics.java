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

public class ThisSemantics extends SemanticAnalysisVisitor {

    private final List<Report> reports;

    public ThisSemantics() {
        this.reports = new ArrayList<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("This", this::visitThis);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }

    private Integer visitThis(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String className = this.getClassName(jmmNode);
        String classSuper = symbolTable.getSuper();
        JmmNode var = jmmNode.getJmmParent();
        Type varType = this.getNodeType(var,symbolTable);

        // still need to check if method is static or not
        if((!Objects.equals(varType.getName(), className) && !Objects.equals(varType.getName(), classSuper)) || varType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"This is not being used with correct Class Type!"));
        }
        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
