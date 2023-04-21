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
        addVisit("NewObj",this::visitNewObj);
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

    private Integer visitNewObj(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String className = jmmNode.get("var");

        if(!Objects.equals(className, symbolTable.getClassName())) {
            if(!this.parsedImports(symbolTable).contains(className)) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Objects' class is not being imported!"));
            }
        }

        return 1;
    }

    public Integer methodNotExists(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String methodName = jmmNode.get("var");
        String className = this.getClassName(jmmNode);
        String superClass = symbolTable.getSuper();

        JmmNode obj = jmmNode.getJmmChild(0);
        Type objType = this.getNodeType(obj,symbolTable);

        if(obj.hasAttribute("var")) {
            if(parsedImports(symbolTable).contains(obj.get("var"))) {
                return 1;
            }
        }

        // invalid type
        if(Objects.equals(objType.getName(), "unknown") || Objects.equals(objType.getName(), "int") || Objects.equals(objType.getName(), "boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Invalid object!"));
            return 1;
        }


        List<String> parsedImports = this.parsedImports(symbolTable);

        if(Objects.equals(objType.getName(), "this")) {
            if(!parsedImports.contains(superClass)) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Class extended is not in imports!"));
                return 1;
            }
            return 1;
        }

        if(Objects.equals(objType.getName(), className)) {
            if(!(!Objects.equals(superClass, "") && parsedImports.contains(superClass))) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Class extended is not in imports or doesn't extend anything!"));
                return 1;
            }
        }
        else {
            if(!parsedImports.contains(objType.getName())) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Class is not imported!"));
                return 1;
            }
        }

        return 1;
    }

    public Integer methodExists(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String methodName = jmmNode.get("var");
        String className = symbolTable.getClassName();

        JmmNode obj = jmmNode.getJmmChild(0);
        Type objType = this.getNodeType(obj,symbolTable);

        // case where a method is called from another class, but exists with the same name on the current class
        if(obj.hasAttribute("var")) {
            if(parsedImports(symbolTable).contains(obj.get("var")) || (parsedImports(symbolTable).contains(obj.get("var")) && symbolTable.getSuper().equals(obj.get("var")))) {
                return 1;
            }
        }
        // invalid type
        if(Objects.equals(objType.getName(), "unknown") || (!Objects.equals(objType.getName(), className) && !Objects.equals(objType.getName(), "this")) ) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Invalid object!"));
            return 1;
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
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Number of arguments doesn't match!"));
            return 1;
        }

        for(int i = 0; i < originalArgs.size(); i++) {
            if(Objects.equals(originalArgs.get(i).getName(),"unknown")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Argument Number %d doesn't match!".formatted(i+1)));
                return 1;
            }
            if(Objects.equals(currArgs.get(i).getName(),"unknown")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Argument Number %d doesn't match!".formatted(i+1)));
                return 1;
            }
        }

        // check args one by one to see if their type match
        for(int i = 0; i < originalArgs.size(); i++) {
            if(currArgs.get(i).getName().equals("this")) {
                currArgs.set(i, new Type(className, false));
            }
            if(!Objects.equals(originalArgs.get(i).getName(),currArgs.get(i).getName()) || originalArgs.get(i).isArray() != currArgs.get(i).isArray()) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")),Integer.parseInt(jmmNode.get("colStart")),"Argument Number %d doesn't match!".formatted(i+1)));
                return 1;
            }
        }

        return 1;
    }

    public List<Report> getReports() {
        return reports;
    }
}

