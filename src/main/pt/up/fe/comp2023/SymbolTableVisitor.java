package pt.up.fe.comp2023;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolTableVisitor extends PreorderJmmVisitor <SymbolTableCR,Integer> {

    final List<Report> reports = new ArrayList<>();
    public SymbolTableVisitor() {

        this.buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("ImportDeclaration", this::visitImport);
        addVisit("ClassDeclaration", this::visitClass);
        addVisit("MethodDeclaration", this::visitMethod);
    }

    private Integer defaultVisit(JmmNode jmmNode, SymbolTableCR symbolTable) {
        return null;
    }


    private Integer visitImport(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String importName = jmmNode.get("importModule");
        symbolTable.addImport(importName);
        return 1;
    }

    private Integer visitMethod(JmmNode jmmNode, SymbolTableCR symbolTable) {
        // return type node
        int numChildren = jmmNode.getChildren().size();
        String methodName = jmmNode.get("name");

        List<Symbol> localVars = new ArrayList();
        List<Symbol> params = new ArrayList();
        List<JmmNode> paramsList = jmmNode.getChildren().subList(1,numChildren);
        List<String> paramsName = (ArrayList<String>) jmmNode.getOptionalObject("param").get();

        // parse param names
        for(int i = 0; i < paramsList.size(); i++) {
            JmmNode child = paramsList.get(i);
            if(child.getKind().equals("Type")) {
                String typeName = child.get("value");
                boolean isArray = child.get("isArray").equals("true");
                Type tipo = new Type(typeName,isArray);
                String varName = paramsName.get(i);
                Symbol symb = new Symbol(tipo,varName);
                params.add(symb);
            }
        }

        JmmNode returnNode = jmmNode.getJmmChild(0);
        Type returnType = new Type(returnNode.get("value"),returnNode.get("isArray").equals("true"));

        symbolTable.addMethod(methodName,returnType,params,localVars);
        return 1;
    }

    private Integer visitClass(JmmNode jmmNode, SymbolTableCR symbolTable) {
        String className = jmmNode.get("className");
        symbolTable.setClassName(className);
        if(jmmNode.hasAttribute("extendsName")) {
            symbolTable.setSuper(jmmNode.get("extendsName"));
        }

        for(JmmNode child: jmmNode.getChildren()) {
            if(child.getKind().equals("VarDeclaration")) {
                String varName = child.get("var");
                String varType = child.getJmmChild(0).get("value");
                boolean isArray = child.getJmmChild(0).get("isArray").equals("true");
                Type tipo = new Type(varType,isArray);
                Symbol symb = new Symbol(tipo,varName);
                symbolTable.addField(symb);
            }
        }
        return 1;
    }
}
