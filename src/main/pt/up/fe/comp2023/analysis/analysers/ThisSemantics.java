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
        String className = symbolTable.getClassName();
        String classSuper = symbolTable.getSuper();
        String methodName = this.getMethodName(jmmNode);
        JmmNode var = jmmNode.getJmmParent();
        Type varType = this.getNodeType(var,symbolTable);

        if(Objects.equals(methodName, "main")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"This cannot be used inside static methods!"));
            return 1;
        }

        if(Objects.equals(varType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Var is not defined!"));
            return 1;
        }


        if(Objects.equals(jmmNode.getJmmParent().getKind(), "MethodCall")) {
            String method = jmmNode.getJmmParent().get("var");
            if(symbolTable.methodExists(method)) {
                return 1;
            }
        }


        if(!((Objects.equals(className, varType.getName())) || (Objects.equals(classSuper, varType.getName()) && parsedImports(symbolTable).contains(classSuper)))) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Cannot use this with this object type!"));
        }
        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
