package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2023.SymbolTableCR;
import pt.up.fe.comp2023.ollir.ASTDict;

public class ASTParserVisitor extends PreorderJmmVisitor<StringBuilder,Integer> {
    private final SymbolTableCR symbolTable;
    public ASTParserVisitor(SymbolTableCR symbolTable){
        this.symbolTable = symbolTable;
        this.buildVisitor();
    }
    @Override
    protected void buildVisitor() {

        addVisit(ASTDict.IMPORT_DECL, this::importDeclarationVisit);
        addVisit(ASTDict.CLASS_DECL,this::classDeclarationVisit);
        addVisit(ASTDict.VAR_DECL, this::varDeclarationVisit);
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
        addVisit(ASTDict.INTEGER, this::integerVisit);
        addVisit(ASTDict.IDENTIFIER, this::identifierVisit);
        addVisit(ASTDict.NEW_INT_ARRAY, this::newIntArrayVisit);
        addVisit(ASTDict.NEW_OBJECT, this::newObjectVisit);
        addVisit(ASTDict.BOOL, this::booleanVisit);
        addVisit(ASTDict.THIS, this::thisVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private Integer defaultVisit(JmmNode jmmNode, StringBuilder ollirCode) {
        return null;
    }

    private Integer importDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer classDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer varDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer methodDeclarationVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer varTypeVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer thenStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer ifStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer expressionStatementVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer varAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer arrayAssignVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer parenthesesVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer notOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer binaryOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer comparisonOperatorVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer arrayIndexVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer arrayLengthVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer methodCallVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer integerVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer identifierVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer newIntArrayVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer newObjectVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer booleanVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }
    private Integer thisVisit(JmmNode jmmNode, StringBuilder ollirCode){
        return null;
    }

}
