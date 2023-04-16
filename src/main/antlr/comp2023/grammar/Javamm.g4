grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : '0' | [1-9][0-9]* ;
ID : [$a-zA-Z_][$a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
COMMENT: '/*' .*? '*/' -> skip;

START_PAR: '(';
END_PAR: ')';
MULT: '*';
DIV: '/';
SUM: '+';
DIFFERENCE: '-';
LOGICAL_AND: '&&';
LOGICAL_OR: '||';
LESS: '<';

program
    : (importDeclaration)* classDeclaration EOF
    ;

importDeclaration
    : 'import' importModule+=ID ('.' importModule+=ID)* ';'
    ;

classDeclaration
    : 'class' className=ID ('extends' extendsName=ID)? '{' (varDeclaration)* (methodDeclaration)* '}'
    ;

varDeclaration
    : type var=ID ';' #VarCreation
    | type var=ID '=' expression ';' #VarCreationWithAssign
    ;

methodDeclaration
    : ('public')? type name=ID '(' (type param+=ID (',' type param+=ID )*)? ')' '{' (varDeclaration)* (statement)* 'return' expression ';' '}'
    | ('public')? 'static' 'void' name='main' '(' type '[' ']' param+=ID ')' '{' (varDeclaration)* (statement)* '}'
    ;

type locals[boolean isArray = false, boolean isClass = false]
    : value='int' ('['']' {$isArray=true;})?
    | value='boolean'
    | value='String'
    | value='int'
    | value=ID
    ;

statement
    : '{' (statement)* '}' #ThenStmt
    | 'if' '(' expression ')' statement 'else' statement #ConditionStmt
    | 'while' '(' expression ')' statement #ConditionStmt
    | expression ';' #ExpStmt
    | var=ID '=' expression ';' #VarAssign
    | var=ID '[' expression ']' '=' expression ';' #ArrayAssign
    ;

expression
    : START_PAR expression END_PAR #Parentheses
    | '!' expression #Not
    | expression op=(MULT | DIV) expression #BinaryOp
    | expression op=(SUM | DIFFERENCE) expression #BinaryOp
    | expression op=LESS expression #CompareOp
    | expression op=(LOGICAL_AND | LOGICAL_OR) expression #LogicalOp
    | expression '[' expression ']' #ArrayIndex
    | expression '.' 'length' #ArrayLength
    | expression '.' var=ID '(' (expression (',' expression)*)? ')' #MethodCall
    | value=INTEGER #Integer
    | var=ID #Identifier
    | 'new' 'int' '[' expression ']' #NewIntArray
    | 'new' var=ID '('')' #NewObj
    | value=('true' | 'false') #Boolean
    | 'this' #This
    ;
