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

    public Type getVariableType(String id, String methodName, SymbolTableCR symbolTable) {
        List<Symbol> methodVars = symbolTable.getLocalVariables(methodName);
        List<Symbol> params = symbolTable.getParameters(methodName);
        List<Symbol> fields = symbolTable.getFields();

        for(Symbol s : methodVars) {
            if(Objects.equals(s.getName(), id)) {
                return s.getType();
            }
        }

        for(Symbol s : params) {
            if(Objects.equals(s.getName(), id)) {
                return s.getType();
            }
        }

        for(Symbol s : fields) {
            if(Objects.equals(s.getName(), id)) {
                return s.getType();
            }
        }

        return new Type("unknown",false);
    }


    public Type getNodeType(JmmNode node, SymbolTableCR symbolTable) {
        return
        switch(node.getKind()) {
            case "Identifier","VarAssign" -> this.getIdentifierType(node,symbolTable);
            case "Integer", "String", "Boolean" -> this.getLiteralType(node);
            case "BinaryOp", "ArrayIndex" -> new Type("int",false);
            case "CompareOp", "LogicalOp" -> new Type("boolean",false);
            case "NewIntArray" -> new Type("int",true);
            case "NewObj" -> new Type(node.get("var"),false);
            case "MethodCall" -> getMethodCallType(node,symbolTable);
            default -> new Type("unknown",false);
        };
    }

    public Type getIdentifierType(JmmNode node, SymbolTableCR symbolTable) {
        String methodName = getMethodName(node);
        Type nodeType =  this.getVariableType(node.get("var"),methodName,symbolTable);
        return nodeType;
    }

    public Type getLiteralType(JmmNode node) {
        String type = node.getKind().toLowerCase();
        if(type.equals("integer")) {
            type = "int";
        }

        return new Type(type,false);
    }

    public Type getMethodCallType(JmmNode node, SymbolTableCR symbolTable) {
        JmmNode parent= node.getJmmParent();

        String methodName = getMethodName(parent);

        return symbolTable.getReturnType(methodName);
    }

    public boolean isLiteral(JmmNode node) {
        return Objects.equals(node.getKind(), "Integer") || Objects.equals(node.getKind(), "Boolean") || Objects.equals(node.getKind(), "String");
    }

    public String getMethodName(JmmNode node) {
        while(!Objects.equals(node.getKind(), "MethodDeclaration") && !Objects.equals(node.getKind(), "ClassDeclaration")) {
            node = node.getJmmParent();
        }

        if(Objects.equals(node.getKind(), "MethodDeclaration")) {
            return node.get("name");
        }
        else {
            return "";
        }
    }

    public String getClassName(JmmNode node) {
        while(!Objects.equals(node.getKind(), "ClassDeclaration")) {
            node = node.getJmmParent();
        }

        return node.get("className");
    }

    public List<Type> getArgTypes(String methodName, SymbolTableCR symbolTable) {
        List<Symbol> args = symbolTable.getParameters(methodName);
        List<Type> argTypes = new ArrayList<>();
        for(Symbol s: args) {
            argTypes.add(s.getType());
        }
        return argTypes;
    }

    public List<String> parsedImports(SymbolTableCR symbolTable) {
        List <String> importList = symbolTable.getImports();
        List <String> parsedImports = new ArrayList<>();

        for (String s : importList) {
            int lastDotIndex = s.lastIndexOf(".");
            parsedImports.add(s.substring(lastDotIndex + 1));
        }

        return parsedImports;
    }

    public void addReport(Report rep) {
        this.reports.add(rep);
    }

    public List<Report> getReports() {
        return this.getReports();
    }
}
