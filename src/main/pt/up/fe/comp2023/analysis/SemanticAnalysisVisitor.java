package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class SemanticAnalysisVisitor extends PreorderJmmVisitor <SymbolTableCR,Integer> {

    private final List<Report> reports;

    public SemanticAnalysisVisitor() {
        this.reports = new ArrayList<>();
    }

    public Type getLocalVariableType(String id, String methodName, SymbolTableCR symbolTable) {
        List<Symbol> methodVars = symbolTable.getLocalVariables(methodName);
        for(Symbol s : methodVars) {
            if(Objects.equals(s.getName(), id)) {
                return s.getType();
            }
        }
        return new Type("unknown",false);
    }

    public boolean isLiteral(JmmNode node) {
        return Objects.equals(node.getKind(), "Integer") || Objects.equals(node.getKind(), "Boolean") || Objects.equals(node.getKind(), "String");
    }

    public void addReport(Report rep) {
        this.reports.add(rep);
    }
}
