package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.SymbolTableCR;

import java.util.ArrayList;
import java.util.List;

public class JmmOptimizerVisitor extends AJmmVisitor<StringBuilder,List<String>> {
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

    public JmmOptimizerVisitor(SymbolTableCR symbolTable){
        this.symbolTable = symbolTable;
        this.buildVisitor();
    }
    @Override
    protected void buildVisitor() {

        setDefaultVisit(this::defaultVisit);

        addVisit(ASTDict.CLASS_DECL,this::classDeclarationVisit);
        addVisit(ASTDict.METHOD_DECL, this::methodDeclarationVisit);
        //addVisit(ASTDict.VAR_TYPE, this::varTypeVisit);
        addVisit(ASTDict.THEN_STATEMENT, this::statementVisit);
        addVisit(ASTDict.CONDITIONAL_STATEMENT, this::condStatementVisit);
        addVisit(ASTDict.EXP_STATEMENT, this::statementVisit);
        addVisit(ASTDict.VAR_ASSIGN, this::varAssignVisit);
        addVisit(ASTDict.ARRAY_ASSIGN, this::arrayAssignVisit);
        //addVisit(ASTDict.PARENTHESES, this::parenthesesVisit);
        addVisit(ASTDict.NOT_OP, this::notOperatorVisit);
        addVisit(ASTDict.BINARY_OP, this::binaryOperatorVisit);
        addVisit(ASTDict.LOGICAL_OP, this::logicalOperatorVisit);
        addVisit(ASTDict.COMPARE_OP, this::comparisonOperatorVisit);
        addVisit(ASTDict.ARRAY_INDEX, this::arrayIndexVisit);
        addVisit(ASTDict.ARRAY_LENGTH, this::arrayLengthVisit);
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
                    case ASTDict.INTEGER, ASTDict.IDENTIFIER, ASTDict.BOOL ->{

                        List<String> code = visit(child, ollirCode);

                        //Return statement
                        ollirCode.append("\n").append(code.get(1))
                                 .append("\t".repeat(indent))
                                 .append("ret")
                                 .append(Utils.toOllirType(return_type.getName(), return_type.isArray()))
                                 .append(" ")
                                 .append(code.get(0))
                                 .append(Utils.toOllirType(return_type.getName(), return_type.isArray()))
                                 .append(";\n");
                    }
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
        switch (conditional_type){
            case "if" -> {
                JmmNode condition = jmmNode.getJmmChild(0);
                JmmNode ifThen = jmmNode.getJmmChild(1);
                JmmNode elseThen = jmmNode.getJmmChild(2);
                List<String> thenEndif = Utils.nextThenEndIf();
                List<String> cond_code = visit(condition,ollirCode);
                //List<String> ifThen_code = visit(ifThen,ollirCode);
                //List<String> elseThen_code = visit(elseThen,ollirCode);

                ollirCode.append("\n")
                         .append(cond_code.get(1))
                         .append("\t".repeat(indent))
                         .append("if(")
                         .append(cond_code.get(0))
                         .append(".bool) goto ")
                         .append(thenEndif.get(0))
                         .append(";\n");
                this.indent++;
                visit(elseThen,ollirCode);
                ollirCode.append("\t".repeat(indent)).append("goto ").append(thenEndif.get(1)).append(";\n");
                ollirCode.append("\t".repeat(--indent)).append(thenEndif.get(0)).append(":\n");
                this.indent++;
                visit(ifThen,ollirCode);
                ollirCode.append("\t".repeat(--indent)).append(thenEndif.get(1)).append(":\n");
            }
            case "while" -> {
                JmmNode condition = jmmNode.getJmmChild(0);
                JmmNode whileDo = jmmNode.getJmmChild(1);
            }
        }
        return null;
    }

    private  List<String> statementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        for(JmmNode child : jmmNode.getChildren())
            visit(child, ollirCode);
        return null;
    }

    private List<String> varAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        String var_name = jmmNode.get("var");

        ollirCode.append("\n");

        //normal assign
        if(symbolTable.localVarExists(var_name,this.method)){
            List<Object> local_var_info = symbolTable.getLocalVarType(var_name, this.method);
            String var_type = Utils.toOllirType((String) local_var_info.get(0), (boolean) local_var_info.get(1));

            JmmNode child = jmmNode.getChildren().get(0);
            switch (child.getKind()) {
                case ASTDict.BINARY_OP, ASTDict.LOGICAL_OP, ASTDict.NOT_OP, ASTDict.COMPARE_OP -> {
                    List<String> code = visit(child, ollirCode);
                    ollirCode.append(code.get(1).replace(code.get(0), var_name));
                    ollirCode.deleteCharAt(ollirCode.length() - 1); //Remove x2 last \n
                    Utils.currentTemp--;
                }
                case ASTDict.METHOD_CALL -> {
                    List<String> code = visit(child, ollirCode);
                    ollirCode.append(code.get(1)).append("\t".repeat(indent)).append(var_name).append(var_type).append(" :=").append(var_type).append(code.get(0));
                }
                case ASTDict.INTEGER, ASTDict.IDENTIFIER, ASTDict.BOOL, ASTDict.ARRAY_INDEX -> {
                    List<String> code = visit(child, ollirCode);
                    ollirCode.append(code.get(1))
                            .append("\t".repeat(indent))
                            .append(var_name)
                            .append(var_type)
                            .append(" :=")
                            .append(var_type)
                            .append(" ")
                            .append(code.get(0))
                            .append(var_type)
                            .append(";\n");
                }
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
        }
        //param assign
        else if(symbolTable.paramExists(var_name, this.method)){
            List<Object> param_info = symbolTable.getParamType(var_name, this.method);
            String var_type = Utils.toOllirType((String) param_info.get(0), (boolean) param_info.get(1));

            JmmNode child = jmmNode.getChildren().get(0);
            switch (child.getKind()) {
                case ASTDict.BINARY_OP, ASTDict.LOGICAL_OP, ASTDict.NOT_OP, ASTDict.COMPARE_OP -> {
                    List<String> code = visit(child, ollirCode);
                    ollirCode.append(code.get(1).replace(code.get(0), var_name));
                    ollirCode.deleteCharAt(ollirCode.length() - 1); //Remove x2 last \n
                    Utils.currentTemp--;
                }
                case ASTDict.METHOD_CALL -> {
                    List<String> code = visit(child, ollirCode);
                    ollirCode.append(code.get(1)).append("\t".repeat(indent)).append(var_name).append(var_type).append(" :=").append(var_type).append(code.get(0));
                }
                case ASTDict.INTEGER, ASTDict.IDENTIFIER, ASTDict.BOOL, ASTDict.ARRAY_INDEX -> {
                    List<String> code = visit(child, ollirCode);
                    ollirCode.append(code.get(1))
                            .append("\t".repeat(indent))
                            .append("$")
                            .append(symbolTable.getParamIndex(var_name,this.method))
                            .append(".")
                            .append(var_name)
                            .append(var_type)
                            .append(" :=")
                            .append(var_type)
                            .append(" ")
                            .append(code.get(0))
                            .append(var_type)
                            .append(";\n");
                }
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
        }
        //field assign
        else{

            List<Object> field_info = symbolTable.getFieldType(var_name);
            String var_type = Utils.toOllirType((String) field_info.get(0), (boolean) field_info.get(1));

            JmmNode child = jmmNode.getChildren().get(0);
            switch (child.getKind()) {
                case ASTDict.BINARY_OP, ASTDict.LOGICAL_OP, ASTDict.METHOD_CALL, ASTDict.INTEGER, ASTDict.IDENTIFIER, ASTDict.BOOL, ASTDict.ARRAY_INDEX, ASTDict.NOT_OP, ASTDict.COMPARE_OP-> {

                    List<String> result = visit(child, ollirCode);

                    ollirCode.append(result.get(1))
                            .append("\t".repeat(indent))
                            .append("putfield(this, ")
                            .append(var_name)
                            .append(var_type)
                            .append(", ")
                            .append(result.get(0))
                            .append(var_type).append(").V;\n");
                }
                case ASTDict.NEW_INT_ARRAY -> {
                    //TODO
                }
                case ASTDict.NEW_OBJECT -> {
                    //TODO
                }
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

    private List<String> notOperatorVisit (JmmNode jmmNode, StringBuilder ollirCode){
        List<String> code = visit(jmmNode.getJmmChild(0), ollirCode);
        String temp = Utils.nextTemp();

        return List.of(temp, code.get(1)+"\t".repeat(indent)+temp+".bool :=.bool !.bool " + code.get(0) + ".bool;\n");
    }

    private List<String> binaryOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        var lhs = jmmNode.getJmmChild(0);
        var rhs = jmmNode.getJmmChild(1);

        List<String> lhsCode = visit(lhs, ollirCode);
        List<String> rhsCode = visit(rhs, ollirCode);
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

    private List<String> logicalOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        var lhs = jmmNode.getJmmChild(0);
        var rhs = jmmNode.getJmmChild(1);

        List<String> lhsCode = visit(lhs, ollirCode);
        List<String> rhsCode = visit(rhs, ollirCode);
        StringBuilder prefixCode = new StringBuilder();

        String temp = Utils.nextTemp();

        prefixCode.append(lhsCode.get(1))
                .append(rhsCode.get(1))
                .append("\t".repeat(indent))
                .append(temp)
                .append(".bool :=.bool ")
                .append(lhsCode.get(0))
                .append(".bool ")
                .append(jmmNode.get("op"))
                .append(".bool ")
                .append(rhsCode.get(0))
                .append(".bool;")
                .append("\n");

        return List.of(temp, prefixCode.toString());
    }

    private List<String> comparisonOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){

        var lhs = jmmNode.getJmmChild(0);
        var rhs = jmmNode.getJmmChild(1);

        List<String> lhsCode = visit(lhs, ollirCode);
        List<String> rhsCode = visit(rhs, ollirCode);
        StringBuilder prefixCode = new StringBuilder();

        String temp = Utils.nextTemp();

        prefixCode.append(lhsCode.get(1))
                .append(rhsCode.get(1))
                .append("\t".repeat(indent))
                .append(temp)
                .append(".bool :=.bool ")
                .append(lhsCode.get(0))
                .append(".i32 ")
                .append(jmmNode.get("op"))
                .append(".bool ")
                .append(rhsCode.get(0))
                .append(".i32;")
                .append("\n");

        return List.of(temp, prefixCode.toString());
    }

    private List<String> arrayIndexVisit(JmmNode jmmNode, StringBuilder ollirCode){

        JmmNode array_child = jmmNode.getJmmChild(0);
        JmmNode index_child = jmmNode.getJmmChild(1);
        StringBuilder prefix = new StringBuilder();
        StringBuilder sufix = new StringBuilder();
        List<String> result = List.of("","");

        if(array_child.getKind().equals(ASTDict.IDENTIFIER)){
            result = visit(array_child, ollirCode);
            prefix.append(result.get(1));
            sufix.append(result.get(0)).append("[");
        }

        switch(index_child.getKind()){
            case ASTDict.BINARY_OP, ASTDict.IDENTIFIER -> {
                result = visit(index_child, ollirCode);

            }
            case ASTDict.INTEGER, ASTDict.ARRAY_INDEX, ASTDict.METHOD_CALL ->{
                result = visit(index_child, ollirCode);
                String temp = Utils.nextTemp();
                result = List.of(temp, result.get(1)+"\t".repeat(indent) + temp + ".i32 :=.i32 " + result.get(0) + ".i32;\n");
            }
        }

        prefix.append(result.get(1));
        sufix.append(result.get(0)).append(".i32]");

        if(jmmNode.getJmmParent().getKind().equals(ASTDict.BINARY_OP) || jmmNode.getJmmParent().getKind().equals(ASTDict.METHOD_CALL)){
            String temp2 = Utils.nextTemp();
            String sufix2 = "\t".repeat(indent)+temp2+".i32 :=.i32 "+ sufix+".i32;\n";
            return List.of(temp2, prefix+sufix2);
        }

        return List.of(sufix.toString(), prefix.toString());
    }

    private List<String> arrayLengthVisit(JmmNode jmmNode, StringBuilder ollirCode){

        String temp = Utils.nextTemp();
        List<String> array = visit(jmmNode.getJmmChild(0), ollirCode);
        String prefix = array.get(1)+"\t".repeat(indent)+temp+".i32 :=.i32 arraylength(" + array.get(0)+  ".array.i32).i32;\n";

        return List.of(temp, prefix);
    }

    private List<String> methodCallVisit(JmmNode jmmNode, StringBuilder ollirCode){
        String called = jmmNode.getJmmChild(0).get("var");
        StringBuilder called_code = new StringBuilder();
        StringBuilder called_prefix = new StringBuilder();

        String method = jmmNode.get("var");
        String return_type = ".V";

        List<JmmNode> params = jmmNode.getChildren();
        params = params.subList(1, params.size());
        String param_value = "";
        String param_type = "";
        StringBuilder params_prefix = new StringBuilder();
        StringBuilder params_code = new StringBuilder();

        //Deal with called
        //Import, class -> invokestatic
        if(symbolTable.getParsedImports().contains(called) || symbolTable.getClassName().equals(called)){
            called_code.append("\t".repeat(indent)).append("invokestatic(").append(called).append(",").append("\"").append(method).append("\"");

            if(symbolTable.getClassName().equals(called)){
                return_type = Utils.toOllirType(symbolTable.getReturnType(method).getName(), symbolTable.getReturnType(method).isArray());
            }
        }
        //local var, method params, class field -> invokevirtual
        else{
            Type called_type_aux = symbolTable.getAnyType(called, this.method);
            String called_type = Utils.toOllirType(called_type_aux.getName(),called_type_aux.isArray());

            called_code.append("\t".repeat(indent)).append("invokevirtual(").append(called).append(called_type).append(",").append("\"").append(method).append("\"");

            if(!symbolTable.getParsedImports().contains(called_type_aux.getName()))
                return_type = Utils.toOllirType(symbolTable.getReturnType(method).getName(), symbolTable.getReturnType(method).isArray());
        }

        //Deal with parameters
        for(JmmNode param : params){

            List<String> code = visit(param, ollirCode);

            //TODO: Not, compare or logical op as param
            switch(param.getKind()){
                case ASTDict.BOOL -> param_type = ".bool";
                case ASTDict.INTEGER, ASTDict.BINARY_OP, ASTDict.ARRAY_LENGTH, ASTDict.ARRAY_INDEX-> param_type = ".i32";
                //class field, local var, method params
                case ASTDict.IDENTIFIER -> {
                    param_value = param.get("var");

                    if(symbolTable.fieldExists(param_value)){
                        List<Object> param_type_aux = symbolTable.getFieldType(param_value);
                        param_type = Utils.toOllirType((String) param_type_aux.get(0), (boolean) param_type_aux.get(1));
                    }
                    else if(symbolTable.localVarExists(param_value, this.method)){
                        List<Object> param_type_aux = symbolTable.getLocalVarType(param_value, this.method);
                        param_type = Utils.toOllirType((String) param_type_aux.get(0), (boolean) param_type_aux.get(1));
                    }
                    else{
                        List<Object> param_type_aux = symbolTable.getParamType(param_value, this.method);
                        param_type = Utils.toOllirType((String) param_type_aux.get(0), (boolean) param_type_aux.get(1));
                    }
                }
            }

            params_prefix.append(code.get(1));
            params_code.append(", ").append(code.get(0)).append(param_type);
        }

        //Var assign or binary op
        JmmNode parent = jmmNode.getJmmParent();
        if(parent.getKind().equals(ASTDict.VAR_ASSIGN)){

            String parent_type = Utils.toOllirType(symbolTable.getAnyType(parent.get("var"), this.method).getName(), symbolTable.getAnyType(parent.get("var"), this.method).isArray());

            params_code.append(")").append(parent_type).append(";\n\n");
            called_code.append(params_code);
            return List.of(" " + called_code.substring(indent),params_prefix.toString());
        }
        //This is really stupid, but I'm not changing it now
        else if(parent.getKind().equals(ASTDict.BINARY_OP) || parent.getKind().equals(ASTDict.METHOD_CALL) || parent.getKind().equals(ASTDict.ARRAY_INDEX) || parent.getKind().equals(ASTDict.NOT_OP) || parent.getKind().equals(ASTDict.COMPARE_OP) || parent.getKind().equals(ASTDict.LOGICAL_OP)) {
            String temp = Utils.nextTemp();
            params_code.append(")").append(return_type).append(";\n\n");
            called_code.append(params_code);
            return List.of(temp + return_type, params_prefix.toString() + "\t".repeat(indent) + temp + return_type + " :=" + return_type + " " + called_code.substring(indent));
        }

        params_code.append(")").append(return_type).append(";\n\n");

        //Regular statement
        ollirCode.append(params_prefix).append(called_code).append(params_code);

        return null;
    }

    private List<String> integerVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return List.of(jmmNode.get("value"), "");
    }

    private List<String> identifierVisit(JmmNode jmmNode, StringBuilder ollirCode){

        String value = jmmNode.get("var");
        StringBuilder prefix = new StringBuilder();

        //local var
        if(symbolTable.localVarExists(value, this.method)){

        }
        //Method parameter
        else if(symbolTable.paramExists(value, this.method)){
            value = "$" + symbolTable.getParamIndex(value, this.method) + "." + value;
        }
        //Class field
        else if(symbolTable.fieldExists(value)){
            String temp = Utils.nextTemp();
            List<Object> field_type_aux = symbolTable.getFieldType(value);
            String field_type = Utils.toOllirType((String) field_type_aux.get(0), (boolean) field_type_aux.get(1));

            prefix.append("\t".repeat(indent)).append(temp).append(field_type).append(" :=").append(field_type).append(" getfield(this, ").append(value).append(field_type).append(")").append(field_type).append(";\n");
            value = temp;
        }

        return List.of(value, prefix.toString());
    }

    private List<String> newIntArrayVisit(JmmNode jmmNode, StringBuilder ollirCode){

        String inside = "";
        String before = "";

        JmmNode child = jmmNode.getChildren().get(0);
        switch(child.getKind()){
            case ASTDict.BINARY_OP, ASTDict.IDENTIFIER, ASTDict.INTEGER -> {
                List<String> code = visit(child, ollirCode);

                inside = code.get(0);
                before = code.get(1);

            }
            /*case ASTDict.INTEGER -> {
                List<String> int_code = visit(child, ollirCode);
                String temp = Utils.nextTemp();

                inside = temp;
                before = "\t".repeat(indent) + temp+".i32 :=.i32 "+int_code.get(0)+".i32;\n";
            }*/
        }
        return List.of("new(array, %s.i32).array.i32;".formatted(inside), "\n"+before);
    }

    private List<String> booleanVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return List.of(jmmNode.get("value").equals("true") ? "1" : "0", "");
    }

}
