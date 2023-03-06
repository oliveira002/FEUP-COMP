grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

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
    : 'import' ID ('.' ID)* ';' #ImportModule //Como dizer que importModule=ID ('.' ID)*   ?
    ;

classDeclaration
    : 'class' class_name=ID ('extends' extends_name=ID)? '{' (varDeclaration)* (methodDeclaration)* '}'
    ;

varDeclaration
    : type var=ID ';'
    ;

methodDeclaration
    : ('public')? type ID '(' (type ID (',' type ID)*)? ')' '{' (varDeclaration)* (statement)* 'return' expression ';' '}'
    | ('public')? 'static' 'void' 'main' '(' type '[' ']' ID ')' '{' (varDeclaration)* (statement)* '}'
    ;

type locals[boolean isArray = false, boolean isClass = false]
    : 'int' ('['']' {$isArray=true;})?
    | 'boolean'
    | 'int'
    | ID
    ;


statement
    : '{' (statement)* '}'
    | 'if' '(' expression ')' statement 'else' statement
    | 'while' '(' expression ')' statement
    | expression ';'
    | var=ID '=' expression ';'
    | var=ID '[' expression ']' '=' expression ';'
    ;

expression
    : START_PAR expression END_PAR #Parentheses
    | '!' expression #Negative
    | expression op=(MULT | DIV) expression #BinaryOp
    | expression op=(SUM | DIFFERENCE) expression #BinaryOp
    | expression op=LESS expression #CompareOp
    | expression op=(LOGICAL_AND | LOGICAL_OR) expression #BinaryOp
    | expression '[' expression ']' #ArrayIndex
    | expression '.' 'length' #ArrayLength
    | expression '.' ID '(' (expression (',' expression)*)? ')' #MethodCall
    | value=INTEGER #Integer
    | var=ID #Identifier
    | 'new' 'int' '[' expression ']' #NewIntArray
    | 'new' ID '('')' #NewObj
    | value=('true' | 'false') #Boolean
    | 'this' #This
    ;
