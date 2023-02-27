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
    : 'import' ID ('.' ID)* ';'
    ;

classDeclaration
    : 'class' ID ('extends' ID)? '{' (varDeclaration)* (methodDeclaration)* '}'
    ;

varDeclaration
    : type ID ';'
    ;

methodDeclaration
    : ('public')? type ID '(' (type ID (',' type ID)*)? ')' '{' (varDeclaration)* (statement)* 'return' expression ';' '}'
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' (varDeclaration)* (statement)* '}'
    ;

type
    : 'int' '['']'
    | 'String'
    | 'boolean'
    | 'int'
    | ID
    ;


statement
    : '{' (statement)* '}'
    | 'if' '(' expression ')' statement 'else' statement
    | 'while' '(' expression ')' statement
    | expression ';'
    | ID '=' expression ';'
    | ID '[' expression ']' '=' expression ';'
    ;

expression
    : START_PAR expression END_PAR #Parethenses
    | expression op=(MULT | DIV) expression #BinaryOp
    | expression op=(SUM | DIFFERENCE) expression #BinaryOp
    | expression op=(LOGICAL_AND | LOGICAL_OR) expression #BinaryOp
    | expression op=LESS expression #BinaryOp
    | expression '[' expression ']' #ArrayIndex
    | expression '.' 'length' #ArrayLength
    | expression '.' ID '(' (expression (',' expression)*)? ')' #Smth
    | value = INTEGER #Integer
    | ID #Identifier
    | 'new' 'int' '[' expression ']' #NewInt
    | 'new' ID '('')' #NewPar
    | '!' expression #Negative
    | INTEGER #Integer
    | 'true' #True
    | 'false' #False
    | 'this' #This
    ;
