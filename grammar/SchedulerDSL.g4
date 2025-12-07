grammar SchedulerDSL;

@header {
    package com.scheduler.chatbot.parser;
}

// Parser Rules
program
    : statement+ EOF
    ;

statement
    : addSubjectStatement
    | setAvailabilityStatement
    | generateScheduleStatement
    | showScheduleStatement
    | clearStatement
    | deleteSubjectStatement 
    | listSubjectsStatement 
    | listAvailabilityStatement 
    | updateSubjectStatement 
    | updatePriorityStatement 
    | showHistoryStatement
    | loadScheduleStatement 
    ;

addSubjectStatement
    : 'add' 'subject' subjectName=STRING 'hours' hours=NUMBER 'priority' priority=PRIORITY
    ;

setAvailabilityStatement
    : 'set' 'availability' 'on' date=DATE 'capacity' capacity=NUMBER 'hours'
    ;

generateScheduleStatement
    : 'generate' 'schedule'
    ;

showScheduleStatement
    : 'show' 'schedule'
    ;

clearStatement
    : 'clear' ('all' | 'subjects' | 'schedule')
    ;

deleteSubjectStatement 
    : 'delete' 'subject' STRING 
    ;

listSubjectsStatement 
    : 'list' 'subjects' 
    ;

listAvailabilityStatement 
    : 'list' 'availability' 
    ;

updateSubjectStatement 
    : 'update' 'subject' STRING 'hours' NUMBER 
    ;

updatePriorityStatement 
    : 'update' 'subject' STRING 'priority' PRIORITY 
    ;

showHistoryStatement 
    : 'show' 'history' 
    ;

loadScheduleStatement 
    : 'load' 'schedule' STRING 
    ;

// Lexer Rules
PRIORITY
    : 'LOW'
    | 'MEDIUM'
    | 'MED'
    | 'HIGH'
    ;

DATE
    : DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT  // YYYY-MM-DD
    | DIGIT DIGIT '/' DIGIT DIGIT '/' DIGIT DIGIT DIGIT DIGIT  // DD/MM/YYYY
    ;

NUMBER
    : DIGIT+ ('.' DIGIT+)?
    ;

STRING
    : '"' (~["\r\n])* '"'
    | '\'' (~['\r\n])* '\''
    ;

fragment DIGIT
    : [0-9]
    ;

// Skip whitespace and comments
WS
    : [ \t\r\n]+ -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;
