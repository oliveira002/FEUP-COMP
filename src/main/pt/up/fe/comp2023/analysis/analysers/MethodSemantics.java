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

public class MethodSemantics extends SemanticAnalysisVisitor {

    private final List<Report> reports;

    public MethodSemantics() {
        this.reports = new ArrayList<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("MethodCall", this::visitMethodCall);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }

    private Integer visitMethodCall(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String methodName = jmmNode.get("var");

        if(!symbolTable.methodExists(methodName)) {
            this.methodNotExists(jmmNode,symbolTable);
            return 1;
        }
        this.methodExists(jmmNode,symbolTable);
        return 1;
    }

    public Integer methodNotExists(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String methodName = jmmNode.get("var");
        String className = this.getClassName(jmmNode);
        JmmNode obj = jmmNode.getJmmChild(0);
        Type objType = this.getNodeType(obj,symbolTable);

        // invalid type
        if(Objects.equals(objType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Invalid object!"));
            return 1;
        }

        // parse import list
        List <String> importList = symbolTable.getImports();
        List <String> parsedImports = new ArrayList<>();

        for (String s : importList) {
            int lastDotIndex = s.lastIndexOf(".");
            parsedImports.add(s.substring(lastDotIndex + 1));
        }


        if(!parsedImports.contains(symbolTable.getSuper())) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Extended class is not imported!"));
            return 1;
        }

        return 1;
    }

    public Integer methodExists(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String methodName = jmmNode.get("var");
        String className = this.getClassName(jmmNode);
        JmmNode obj = jmmNode.getJmmChild(0);
        Type objType = this.getNodeType(obj,symbolTable);

        // invalid type
        if(Objects.equals(objType.getName(), "unknown")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Invalid object!"));
            return 1;
        }

        Type methodRType = symbolTable.getReturnType(methodName);
        if(!Objects.equals(objType.getName(), methodRType.getName()) || objType.isArray() != methodRType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Return type doesn't match with the object's type!"));
        }

        int num_children = jmmNode.getNumChildren();
        List<JmmNode> args = jmmNode.getChildren().subList(1,num_children);

        List<Type> currArgs = new ArrayList<>();
        for(JmmNode arg: args) {
            currArgs.add(this.getNodeType(arg,symbolTable));
        }

        List<Type> originalArgs = this.getArgTypes(methodName,symbolTable);

        // check if size of args match
        if(currArgs.size() != originalArgs.size()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Number of arguments doesn't match!"));
            return 1;
        }

        // check args one by one to see if their type match
        for(int i = 0; i < originalArgs.size(); i++) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,0,"Argument Number %d doesn't match!".formatted(i+1)));
        }

        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}
