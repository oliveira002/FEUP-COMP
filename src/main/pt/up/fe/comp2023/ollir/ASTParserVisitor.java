package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.SymbolTableCR;

import java.util.Arrays;
import java.util.List;

public class ASTParserVisitor extends AJmmVisitor<StringBuilder,List<String>> {
    private final SymbolTableCR symbolTable;
    private int indent = 0;
    private String method = "";

    public ASTParserVisitor(SymbolTableCR symbolTable){
        this.symbolTable = symbolTable;
        this.buildVisitor();
    }
    @Override
    protected void buildVisitor() {

        setDefaultVisit(this::defaultVisit);

        addVisit(ASTDict.CLASS_DECL,this::classDeclarationVisit);
        addVisit(ASTDict.METHOD_DECL, this::methodDeclarationVisit);
        //addVisit(ASTDict.VAR_TYPE, this::varTypeVisit);
        //addVisit(ASTDict.THEN_STATEMENT, this::thenStatementVisit);
        addVisit(ASTDict.CONDITIONAL_STATEMENT, this::condStatementVisit);
        //addVisit(ASTDict.EXP_STATEMENT, this::expressionStatementVisit);
        addVisit(ASTDict.VAR_ASSIGN, this::varAssignVisit);
        addVisit(ASTDict.ARRAY_ASSIGN, this::arrayAssignVisit);
        //addVisit(ASTDict.PARENTHESES, this::parenthesesVisit);
        //addVisit(ASTDict.NOT_OP, this::notOperatorVisit);
        addVisit(ASTDict.BINARY_OP, this::binaryOperatorVisit);
        //addVisit(ASTDict.COMPARE_OP, this::comparisonOperatorVisit);
        //addVisit(ASTDict.ARRAY_INDEX, this::arrayIndexVisit);
        //addVisit(ASTDict.ARRAY_LENGTH, this::arrayLengthVisit);
        addVisit(ASTDict.METHOD_CALL, this::methodCallVisit);
        addVisit(ASTDict.INTEGER, this::integerVisit);
        //addVisit(ASTDict.IDENTIFIER, this::identifierVisit);
        //addVisit(ASTDict.NEW_INT_ARRAY, this::newIntArrayVisit);
        //addVisit(ASTDict.NEW_OBJECT, this::newObjectVisit);
        //addVisit(ASTDict.BOOL, this::booleanVisit);
        //addVisit(ASTDict.THIS, this::thisVisit);

    }

    private List<String> defaultVisit(JmmNode jmmNode, StringBuilder ollirCode) {
        for(JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals(ASTDict.CLASS_DECL))
                visit(child, ollirCode);
        }
        return null;
    }


    private List<String>  classDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){

        //Imports
        for(String importModule : this.symbolTable.getImports())
            ollirCode.append("import %s;\n".formatted(importModule));
        ollirCode.append("\n");

        //Class declaration with "extends"
        ollirCode.append(symbolTable.getClassName())
                 .append(symbolTable.getSuper().equals("") ? " {" : " extends %s {\n".formatted(symbolTable.getSuper()));
        this.indent++;

        //Field declaration
        for(Symbol field : symbolTable.getFields()){
            Type field_type = field.getType();
            String field_name = field_type.getName();
            boolean is_array = field_type.isArray();
            String type = Utils.toOllirType(field_name, is_array);

            ollirCode.append("\t".repeat(this.indent))
                     .append(".field ")
                     .append(field.getName())
                     .append(type)
                     .append(";\n");
        }
        ollirCode.append("\n");

        //Class constructor
        ollirCode.append("\t".repeat(this.indent))
                 .append(".construct ")
                 .append(symbolTable.getClassName())
                 .append("().V {\n")
                 .append("\t".repeat(++this.indent))
                 .append("invokespecial(this, \"<init>\").V;\n")
                 .append("\t".repeat(--this.indent))
                 .append("}\n");

        //Visit children: Only need to visit method declarations because fields already dealt with
        for(JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals(ASTDict.METHOD_DECL))
                visit(child, ollirCode);
        }

        ollirCode.append("}");

        return null;
    }

    private List<String>  methodDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){

        this.method = jmmNode.get("name");
        Type return_type = symbolTable.getReturnType(this.method);

        ollirCode.append("\n")
                 .append("\t".repeat(indent))
                 .append(".method ")
                 .append(jmmNode.get("modifier"))
                 .append(this.method.equals("main")? " static main(" : " %s(".formatted(this.method));

        List<Symbol> params = symbolTable.getParameters(this.method);

        for(Symbol param : params) {
            String param_name = param.getName();
            boolean is_array = param.getType().isArray();
            String param_type = Utils.toOllirType(param.getType().getName(), is_array);

            ollirCode.append(param_name)
                    .append(param_type)
                    .append(", ");
        }

        //Remove trailing ", "
        if(!params.isEmpty())
            ollirCode.deleteCharAt(ollirCode.length() - 1).deleteCharAt(ollirCode.length() - 1);

        ollirCode.append(")")
                 .append(Utils.toOllirType(return_type.getName(), return_type.isArray()))
                 .append(" {");

        this.indent++;

        //Visit children: Only need to visit statements because types already dealt with, local var declarations don't matter and expressions are dealt after
        for(JmmNode child : jmmNode.getChildren()){
            //if(child.getKind().equals(ASTDict.STATEMENT))
                visit(child, ollirCode);
        }

        //Return statement
        ollirCode.append("\n")
                 .append("\t".repeat(indent))
                 .append("ret")
                 .append(Utils.toOllirType(return_type.getName(), return_type.isArray()))
                 .append(";");

        //Visit children: Only need to visit expressions, because types, local var declarations and statements already dealt with
        for(JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals(ASTDict.EXPRESSION))
                visit(child, ollirCode);
        }

        ollirCode.append("\n")
                 .append("\t".repeat(--indent))
                 .append("}\n");

        return null;
    }


    //TODO: Cp3
    private List<String>  condStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        String conditional_type = jmmNode.get("conditional");

        switch (conditional_type){
            case "if" -> {

            }
            case "while" -> {

            }
        }

        return null;
    }

    //TODO: Cp2
    private List<String>  varAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        String var_name = jmmNode.get("var");
        String var_type = symbolTable.getLocalVarType(var_name, this.method);

        JmmNode child = jmmNode.getChildren().get(0);
        if(child.getKind().equals(ASTDict.BINARY_OP)){
            List<String> binary_op_code = visit(child, ollirCode);
            ollirCode.append("\n")
                     .append(binary_op_code.get(1).replace(binary_op_code.get(0), var_name));
        }

        return null;
    }

    //TODO: Cp2
    private List<String>  arrayAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private List<String> binaryOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        var lhs = jmmNode.getJmmChild(0);
        var rhs = jmmNode.getJmmChild(1);

        List<String> lhsCode = visit(lhs);
        List<String> rhsCode = visit(rhs);
        StringBuilder prefixCode = new StringBuilder();

        String temp = Utils.nextTemp();

        prefixCode.append(lhsCode.get(1))
                  .append(rhsCode.get(1))
                  .append("\t".repeat(indent))
                  .append(temp)
                  .append(".i32 :=.i32 ")
                  .append(lhsCode.get(0))
                  .append(".i32 ")
                  .append(jmmNode.get("op"))
                  .append(".i32 ")
                  .append(rhsCode.get(0))
                  .append(".i32;")
                  .append("\n");

        return List.of(temp, prefixCode.toString());
    }

    //TODO: Cp2
    private List<String>  methodCallVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private List<String>  integerVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return List.of(jmmNode.get("value"), "");
    }

}
