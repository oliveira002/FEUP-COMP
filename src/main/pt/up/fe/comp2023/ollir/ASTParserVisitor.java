package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.SymbolTableCR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ASTParserVisitor extends AJmmVisitor<StringBuilder,List<String>> {
    private final SymbolTableCR symbolTable;
    private int indent = 0;
    private String method = "";

    private final List<String> statements = List.of(ASTDict.THEN_STATEMENT,
            ASTDict.CONDITIONAL_STATEMENT,
            ASTDict.EXP_STATEMENT,
            ASTDict.VAR_ASSIGN,
            ASTDict.ARRAY_ASSIGN);
    private final List<String> expressions = List.of(ASTDict.PARENTHESES,
            ASTDict.NOT_OP,
            ASTDict.BINARY_OP,
            ASTDict.COMPARE_OP,
            ASTDict.LOGICAL_OP,
            ASTDict.ARRAY_INDEX,
            ASTDict.ARRAY_LENGTH,
            ASTDict.METHOD_CALL,
            ASTDict.INTEGER,
            ASTDict.IDENTIFIER,
            ASTDict.NEW_INT_ARRAY,
            ASTDict.NEW_OBJECT,
            ASTDict.BOOL,
            ASTDict.THIS);

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
        addVisit(ASTDict.EXP_STATEMENT, this::expressionStatementVisit);
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
        addVisit(ASTDict.IDENTIFIER, this::identifierVisit);
        addVisit(ASTDict.NEW_INT_ARRAY, this::newIntArrayVisit);
        //addVisit(ASTDict.NEW_OBJECT, this::newObjectVisit);
        addVisit(ASTDict.BOOL, this::booleanVisit);
        //addVisit(ASTDict.THIS, this::thisVisit);

    }

    private List<String> defaultVisit(JmmNode jmmNode, StringBuilder ollirCode) {
        for(JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals(ASTDict.CLASS_DECL))
                visit(child, ollirCode);
        }
        return null;
    }


    private List<String> classDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){

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

    private List<String> methodDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){

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

        for(JmmNode child : jmmNode.getChildren()){
            //Statements
            if(statements.contains(child.getKind()))
                visit(child, ollirCode);

            //Expressions
            if(expressions.contains(child.getKind()))
                switch(child.getKind()){
                    case ASTDict.BINARY_OP -> {
                        List<String> binary_op_code = visit(child, ollirCode);
                        ollirCode.append("\n\n")
                                 .append(binary_op_code.get(1));
                        ollirCode.deleteCharAt(ollirCode.length() - 1); //Remove last \n
                        //Return statement
                        ollirCode.append("\n")
                                 .append("\t".repeat(indent))
                                 .append("ret.i32 ")
                                 .append(binary_op_code.get(0)).append(".i32;\n");
                    }
                    case ASTDict.INTEGER, ASTDict.IDENTIFIER ->
                        //Return statement
                        ollirCode.append("\n")
                                 .append("\t".repeat(indent))
                                 .append("ret")
                                 .append(Utils.toOllirType(return_type.getName(), return_type.isArray()))
                                 .append(" ")
                                 .append(visit(child, ollirCode).get(0))
                                 .append(Utils.toOllirType(return_type.getName(), return_type.isArray()))
                                 .append(";\n");
                }
        }

        if(return_type.getName().equals("void")){
            ollirCode.append("\n").append("\t".repeat(indent)).append("ret.V;\n");
        }

        ollirCode.append("\t".repeat(--indent))
                 .append("}\n");

        return null;
    }


    //TODO: Cp3
    private List<String> condStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        String conditional_type = jmmNode.get("conditional");
        this.indent++;
        switch (conditional_type){
            case "if" -> {

            }
            case "while" -> {

            }
        }
        this.indent--;
        return null;
    }

    private  List<String> expressionStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        for(JmmNode child : jmmNode.getChildren())
            visit(child, ollirCode);
        return null;
    }

    //TODO: Cp2
    private List<String> varAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        String var_name = jmmNode.get("var");
        List<Object> local_var_info = symbolTable.getLocalVarType(var_name, this.method);
        String var_type = Utils.toOllirType((String) local_var_info.get(0), (boolean) local_var_info.get(1));

        ollirCode.append("\n");

        JmmNode child = jmmNode.getChildren().get(0);
        switch(child.getKind()){
            case ASTDict.BINARY_OP, ASTDict.METHOD_CALL -> {
                List<String> code = visit(child, ollirCode);
                ollirCode.append(code.get(1).replace(code.get(0), var_name));
                ollirCode.deleteCharAt(ollirCode.length() - 1); //Remove x2 last \n
                Utils.currentTemp--;
            }
            case ASTDict.INTEGER, ASTDict.IDENTIFIER, ASTDict.BOOL -> ollirCode.append("\t".repeat(indent))
                                                                 .append(var_name)
                                                                 .append(var_type)
                                                                 .append(" :=")
                                                                 .append(var_type)
                                                                 .append(" ")
                                                                 .append(visit(child, ollirCode).get(0))
                                                                 .append(var_type)
                                                                 .append(";");
            case ASTDict.NEW_INT_ARRAY -> {

                List<String> code = visit(child, ollirCode);
                ollirCode.append(code.get(1))
                         .append("\t".repeat(indent))
                         .append(var_name)
                         .append(var_type)
                         .append(" :=")
                         .append(var_type)
                         .append(" ")
                         .append(code.get(0))
                         .append("\n");
            }
            case ASTDict.NEW_OBJECT -> {
                ollirCode.append("\t".repeat(indent))
                         .append(var_name)
                         .append(var_type)
                         .append(" :=")
                         .append(var_type)
                         .append(" new(")
                         .append(var_type.replace(".", ""))
                         .append(")")
                         .append(var_type).append(";\n")
                         .append("\t".repeat(indent))
                         .append("invokespecial(")
                         .append(var_name)
                         .append(var_type)
                         .append(",\"<init>\").V;\n");
            }
        }
        return null;
    }

    private List<String> arrayAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){

        var index = jmmNode.getChildren().get(0);
        var value = jmmNode.getChildren().get(1);
        var name = jmmNode.get("var");

        List<String> index_code = new ArrayList<>();
        List<String> value_code;

        switch(index.getKind()){
            case ASTDict.BINARY_OP, ASTDict.IDENTIFIER -> {
                index_code = visit(index, ollirCode);
            }
            case ASTDict.INTEGER ->{
                List<String> int_var = visit(index, ollirCode);
                String temp = Utils.nextTemp();
                String prefix_code = "\t".repeat(indent)+temp + ".i32 :=.i32 "+int_var.get(0)+".i32;\n";
                index_code = List.of(temp, prefix_code);
            }
        }

        value_code = visit(value, ollirCode);

        ollirCode.append("\n")
                 .append(index_code.get(1))
                 .append(value_code.get(1))
                 .append("\t".repeat(indent))
                 .append(name)
                 .append("[")
                 .append(index_code.get(0))
                 .append(".i32].i32 :=.i32 ")
                 .append(value_code.get(0))
                 .append(".i32;\n");

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
    private List<String> methodCallVisit(JmmNode jmmNode, StringBuilder ollirCode){
        String called = jmmNode.getJmmChild(0).get("var");
        String method = jmmNode.get("var");
        List<JmmNode> params = jmmNode.getChildren();
        params = params.subList(1, params.size());
        String return_type =".V";

        //Var assign
        if(jmmNode.getJmmParent().getKind().equals(ASTDict.VAR_ASSIGN)){
            String temp = Utils.nextTemp();
            Type return_type_aux = symbolTable.getReturnType(this.method);
            return_type = Utils.toOllirType(return_type_aux.getName(), return_type_aux.isArray());

            List<Object> var_type = symbolTable.getLocalVarType(called, this.method);

            if(var_type == null){
                var_type = symbolTable.getParamType(called, this.method);
            }

            StringBuilder prefix_code = new StringBuilder("\t".repeat(indent) + temp + return_type + " :=" + return_type + " invokevirtual("
                    + called + Utils.toOllirType((String) var_type.get(0), (boolean) var_type.get(1)) + ", " + "\"" + method + "\"");

            for(JmmNode param : params){

                String param_value = visit(param, ollirCode).get(0);
                String param_type;

                //Int
                if(param.getKind().equals(ASTDict.INTEGER)){
                    param_type = ".i32";
                }
                //Identifier
                else{

                    List<Object> param_type_aux = symbolTable.getFieldType(param_value);
                    if(param_type_aux == null){
                        param_type_aux = symbolTable.getLocalVarType(param_value, this.method);
                    }
                    param_type = Utils.toOllirType((String) param_type_aux.get(0), (boolean) param_type_aux.get(1));
                }

                prefix_code.append(", ").append(param_value).append(param_type);
            }

            prefix_code.append(")").append(return_type).append(";\n");
            return List.of(temp, prefix_code.toString());
        }

        //Import, class -> invokestatic
        if(symbolTable.getParsedImports().contains(called) || symbolTable.getClassName().equals(called)){
            ollirCode.append("\n")
                     .append("\t".repeat(indent))
                     .append("invokestatic(").append(called);

        }
        //local var, method params -> invokevirtual
        else{

            Type return_type_aux = symbolTable.getReturnType(this.method);
            return_type = Utils.toOllirType(return_type_aux.getName(), return_type_aux.isArray());

            List<Object> var_type = symbolTable.getLocalVarType(called, this.method);

            if(var_type == null){
                var_type = symbolTable.getParamType(called, this.method);
            }

            ollirCode.append("\t".repeat(indent))
                     .append("invokevirtual(")
                     .append(called).append(Utils.toOllirType((String) var_type.get(0), (boolean) var_type.get(1)));
        }

         ollirCode.append(", \"")
                  .append(method)
                  .append("\"");

        for(JmmNode param : params){

            String param_value = visit(param, ollirCode).get(0);
            String param_type;

            //Int
            if(param.getKind().equals(ASTDict.INTEGER)){
                 param_type = ".i32";
            }
            //Identifier
            else{

                List<Object> param_type_aux = symbolTable.getFieldType(param_value);
                if(param_type_aux == null){
                    param_type_aux = symbolTable.getLocalVarType(param_value, this.method);
                }
                param_type = Utils.toOllirType((String) param_type_aux.get(0), (boolean) param_type_aux.get(1));
            }

            ollirCode.append(", ")
                     .append(param_value).append(param_type);
        }

        ollirCode.append(")")
                 .append(return_type)
                 .append(";");

        return null;
    }

    private List<String> integerVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return List.of(jmmNode.get("value"), "");
    }

    private List<String> identifierVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return List.of(jmmNode.get("var"), "");
    }

    private List<String> newIntArrayVisit(JmmNode jmmNode, StringBuilder ollirCode){

        String inside = "";
        String before = "";

        JmmNode child = jmmNode.getChildren().get(0);
        switch(child.getKind()){
            case ASTDict.BINARY_OP, ASTDict.IDENTIFIER -> {
                List<String> code = visit(child, ollirCode);

                inside = code.get(0);
                before = code.get(1);

            }
            case ASTDict.INTEGER -> {
                List<String> int_code = visit(child, ollirCode);
                String temp = Utils.nextTemp();

                inside = temp;
                before = "\t".repeat(indent) + temp+".i32 :=.i32 "+int_code.get(0)+".i32;\n";
            }
        }
        return List.of("new(array, %s.i32).array.i32;".formatted(inside), "\n"+before);
    }

    private List<String> booleanVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return List.of(jmmNode.get("value").equals("true") ? "1" : "0", "");
    }

}
