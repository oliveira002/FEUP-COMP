package pt.up.fe.comp2023.analysis;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Objects;
import java.util.stream.Collectors;

public class SymbolTableVisitor extends PreorderJmmVisitor <SymbolTableCR,Integer> {

    private final List<Report> reports;
    public SymbolTableVisitor() {
        this.buildVisitor();
        this.reports = new ArrayList<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("ImportDeclaration", this::visitImport);
        addVisit("ClassDeclaration", this::visitClass);
        addVisit("MethodDeclaration", this::visitMethod);
        addVisit("VarDeclaration", this::visitVarDeclaration);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }


    private Integer visitImport(JmmNode jmmNode, SymbolTableCR symbolTable) {

        List<Object> importModule = jmmNode.getObjectAsList("importModule");
        List<String> importModuleString = new ArrayList<>(importModule.size());
        for(Object object : importModule){
            importModuleString.add(Objects.toString(object, null));
        }

        String importName = String.join(".",importModuleString);

        symbolTable.addImport(importName);
        return 1;
    }

    private Integer visitMethod(JmmNode jmmNode, SymbolTableCR symbolTable) {

        // return type node
        int numChildren = jmmNode.getChildren().size();
        String methodName = jmmNode.get("name");
        List<Symbol> params = new ArrayList<>();
        List<JmmNode> paramsList = jmmNode.getChildren().subList(1,numChildren);
        List<String> paramsName = (ArrayList<String>) jmmNode.getOptionalObject("param").get();

        if(!methodName.equals("main")) {
            JmmNode returnNode = jmmNode.getJmmChild(0);
            Type returnType = new Type(returnNode.get("value"),returnNode.get("isArray").equals("true"));
            for(int i = 0; i < paramsList.size(); i++) {
                JmmNode child = paramsList.get(i);
                if(child.getKind().equals("Type")) {
                    String typeName = child.get("value");
                    boolean isArray = child.get("isArray").equals("true");
                    Type type = new Type(typeName,isArray);
                    String varName = paramsName.get(i);
                    Symbol symb = new Symbol(type,varName);
                    params.add(symb);
                }
            }
            symbolTable.addMethod(methodName,returnType,params);
            return 1;
        }
        Type returnType = new Type("void",false);
        Type paramType = new Type("String",true);
        String paramName = "args";
        Symbol mainSymbol = new Symbol(paramType,paramName);
        params.add(mainSymbol);
        symbolTable.addMethod(methodName,returnType,params);
        return 1;
    }

    private Integer visitClass(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String className = jmmNode.get("className");
        symbolTable.setClassName(className);
        if(jmmNode.hasAttribute("extendsName")) {
            symbolTable.setSuper(jmmNode.get("extendsName"));
        }
        return 1;
    }

    private Integer visitVarDeclaration(JmmNode jmmNode, SymbolTableCR symbolTable) {
        // need to check if it's local variable or not, can do it by checking parent
        JmmNode parent = jmmNode.getJmmParent();

        String varName = jmmNode.get("var");
        String varType = jmmNode.getJmmChild(0).get("value");
        boolean isArray = jmmNode.getJmmChild(0).get("isArray").equals("true");
        Type type = new Type(varType,isArray);
        Symbol symb = new Symbol(type,varName);


        // if it's a field else it's a local variable
        if(parent.getKind().equals("ClassDeclaration")) {
            symbolTable.addField(symb);
        }
        else {
            String methodName = parent.get("name");
            symbolTable.addLocalVar(methodName,symb);
        }
        return 1;
    }

}
