package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2023.SymbolTableCR;

public class ASTParserVisitor extends AJmmVisitor<StringBuilder,Void> {
    private final SymbolTableCR symbolTable;
    private int indent = 0;

    public ASTParserVisitor(SymbolTableCR symbolTable){
        this.symbolTable = symbolTable;
        this.buildVisitor();
    }
    @Override
    protected void buildVisitor() {

        setDefaultVisit(this::defaultVisit);

        addVisit(ASTDict.CLASS_DECL,this::classDeclarationVisit);
        addVisit(ASTDict.VAR_CREATION, this::varCreationVisit);
        addVisit(ASTDict.VAR_CREATION_ASSIGN, this::varCreationAssignVisit);
        addVisit(ASTDict.METHOD_DECL, this::methodDeclarationVisit);
        addVisit(ASTDict.VAR_TYPE, this::varTypeVisit);
        addVisit(ASTDict.THEN_STATEMENT, this::thenStatementVisit);
        addVisit(ASTDict.IF_STATEMENT, this::ifStatementVisit);
        addVisit(ASTDict.EXP_STATEMENT, this::expressionStatementVisit);
        addVisit(ASTDict.VAR_ASSIGN, this::varAssignVisit);
        addVisit(ASTDict.ARRAY_ASSIGN, this::arrayAssignVisit);
        addVisit(ASTDict.PARENTHESES, this::parenthesesVisit);
        addVisit(ASTDict.NOT_OP, this::notOperatorVisit);
        addVisit(ASTDict.BINARY_OP, this::binaryOperatorVisit);
        addVisit(ASTDict.COMPARE_OP, this::comparisonOperatorVisit);
        addVisit(ASTDict.ARRAY_INDEX, this::arrayIndexVisit);
        addVisit(ASTDict.ARRAY_LENGTH, this::arrayLengthVisit);
        addVisit(ASTDict.METHOD_CALL, this::methodCallVisit);
        addVisit(ASTDict.INTEGER, this::VoidVisit);
        addVisit(ASTDict.IDENTIFIER, this::identifierVisit);
        addVisit(ASTDict.NEW_INT_ARRAY, this::newIntArrayVisit);
        addVisit(ASTDict.NEW_OBJECT, this::newObjectVisit);
        addVisit(ASTDict.BOOL, this::booleanVisit);
        addVisit(ASTDict.THIS, this::thisVisit);

    }

    private Void defaultVisit(JmmNode jmmNode, StringBuilder ollirCode) {
        for(JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals(ASTDict.CLASS_DECL))
                visit(child, ollirCode);
        }
        return null;
    }


    private Void classDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){

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

            ollirCode.append("\t".repeat(indent))
                     .append(".field ")
                     .append(field.getName())
                     .append(type)
                     .append(";\n");
        }
        ollirCode.append("\n");

        //Class constructor
        ollirCode.append("\t".repeat(indent))
                 .append(".construct ")
                 .append(symbolTable.getClassName())
                 .append("().V {\n")
                 .append("\t".repeat(++indent))
                 .append("invokespecial(this, \"<init>\").V;\n")
                 .append("\t".repeat(--indent))
                 .append("}\n\n");
        indent--;

        //Visit children: Only need to visit method declarations because fields already dealt with
        for(JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals(ASTDict.METHOD_DECL))
                visit(child, ollirCode);
        }

        ollirCode.append("}");

        return null;
    }

    //A var creation only needs to be converted to ollir if it's a field. If it's a local var it's converted to ollir only when a value is assigned
    private Void varCreationVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    
    private Void varCreationAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void methodDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){

        this.indent++;
        return null;
    }

    private Void varTypeVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void thenStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void ifStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void expressionStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void varAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void arrayAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void parenthesesVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void notOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void binaryOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void comparisonOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void arrayIndexVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void arrayLengthVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void methodCallVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void VoidVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void identifierVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void newIntArrayVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void newObjectVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void booleanVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

    private Void thisVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }


}
