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
        JmmNode value = jmmNode.getJmmChild(0);
        Type valueType = this.getNodeType(value,symbolTable);

        if(Objects.equals(varType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned doesn't not exist!"));
            return 1;
        }

        if(Objects.equals(valueType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned doesn't not exist!"));
            return 1;
        }

        if(Objects.equals(methodName, "main")) {
            String temp = "";
            if(Objects.equals(value.getKind(), "Identifier")) {
                temp = value.get("var");
            }
            if(symbolTable.fieldExists(varName) || symbolTable.fieldExists(temp)) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Field in static!"));
                return 1;
            }
        }

        if(Objects.equals(value.getKind(), "MethodCall")) {
            if(Objects.equals(valueType.getName(), "inexistent")) {
                return 1;
            }
            if(!Objects.equals(valueType.getName(), varType.getName())) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned doesn't not exist!"));
                return 1;
            }
            return 1;
        }

        if(Objects.equals(valueType.getName(),"this")) {
            String classe = symbolTable.getClassName();
            String superClass = symbolTable.getSuper();
            String tipo = varType.getName();
            if(Objects.equals(tipo,classe)) {
                return 1;
            }
            else if(Objects.equals(tipo,superClass) && parsedImports(symbolTable).contains(superClass)) {
                return 1;
            }
            else {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable type for this is wrong!"));
            }
            return 1;
        }


        if(parsedImports(symbolTable).contains(varType.getName()) && Objects.equals(symbolTable.getSuper(), varType.getName()) && (Objects.equals(valueType.getName(), symbolTable.getClassName()))) {
            return 1;
        }

        if(parsedImports(symbolTable).contains(varType.getName()) && parsedImports(symbolTable).contains(valueType.getName())) {
            return 1;
        }

        if(!Objects.equals(valueType.getName(), varType.getName()) || valueType.isArray() != varType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Types in the assignment don't match!"));
        }

        return 1;
    }

    private Integer visitArrayAssign(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String varName = jmmNode.get("var");
        String methodName = jmmNode.getJmmParent().get("name");
        Type varType = this.getVariableType(varName,methodName,symbolTable);

        if(Objects.equals(varType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned doesn't not exist!"));
            return 1;
        }

        if(!Objects.equals(varType.getName(), "int") || !varType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned isn't an integer array!"));
            return 1;
        }

        // check if index is an int
        JmmNode index = jmmNode.getJmmChild(0);
        Type idxType = this.getNodeType(index,symbolTable);

        if(Objects.equals(idxType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned doesn't not exist!"));
            return 1;
        }

        if(!Objects.equals(idxType.getName(), "int") || idxType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Index isn't an integer!"));
            return 1;
        }

        // check if value is the same type of the variable
        JmmNode value = jmmNode.getJmmChild(1);
        Type valType = this.getNodeType(value,symbolTable);

        if(Objects.equals(valType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Variable assigned doesn't not exist!"));
            return 1;
        }

        if(!Objects.equals(valType.getName(), "int") || valType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Value isn't an integer!"));
            return 1;
        }

        return 1;
    }


    public List<Report> getReports() {
        return reports;
    }
}
