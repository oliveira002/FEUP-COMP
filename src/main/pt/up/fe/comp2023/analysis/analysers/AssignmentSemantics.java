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

public class AssignmentSemantics extends SemanticAnalysisVisitor {

    private final List<Report> reports;

    public AssignmentSemantics() {
        this.reports = new ArrayList<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("VarAssign", this::visitVarAssign);
        addVisit("ArrayAssign", this::visitArrayAssign);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }

    private Integer visitVarAssign(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String varName = jmmNode.get("var");
        String methodName = this.getMethodName(jmmNode);
        Type varType = this.getVariableType(varName,methodName,symbolTable);

        // check if variable exists
        if(Objects.equals(varType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned doesn't not exist!"));
        }

        // check if value is the same type of the variable
        JmmNode value = jmmNode.getJmmChild(0);
        Type valueType = this.getNodeType(value,symbolTable);

        List<String> imports = this.parsedImports(symbolTable);

        // if both types are imported
        boolean both_imported = imports.contains(valueType.getName()) && imports.contains(varType.getName());

        if(both_imported || (Objects.equals(valueType.getName(), symbolTable.getClassName()) && Objects.equals(varType.getName(), symbolTable.getSuper()))){
            return 1;
        }

        if(!Objects.equals(valueType.getName(), varType.getName()) || !Objects.equals(valueType.isArray(), varType.isArray())) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"The assignment doesn't not have the type of the variable!"));
        }

        return 1;
    }

    private Integer visitArrayAssign(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String varName = jmmNode.get("var");
        String methodName = jmmNode.getJmmParent().get("name");
        Type varType = this.getVariableType(varName,methodName,symbolTable);

        // check if variable exists
        if(Objects.equals(varType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned doesn't not exist!"));
        }

        // check if index is an int
        JmmNode index = jmmNode.getJmmChild(0);
        boolean valid_index = this.checkValidNode(index,symbolTable,"Integer", false);

        if(!valid_index) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Accessing an array by index must be done with Integers only!"));
        }

        // check if value is the same type of the variable
        JmmNode value = jmmNode.getJmmChild(1);
        boolean valid_value = this.checkValidNode(value,symbolTable,varType.getName(), false);

        if(!valid_value) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"The assignment doesn't not have the type of the variable!"));
        }

        return 1;
    }


    public List<Report> getReports() {
        return reports;
    }
}
