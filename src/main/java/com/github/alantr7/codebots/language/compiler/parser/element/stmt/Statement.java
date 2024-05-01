package com.github.alantr7.codebots.language.compiler.parser.element.stmt;

public interface Statement {

    int getStatementType();

    int VARIABLE_DECLARE = 0;

    int VARIABLE_ASSIGN = 1;

    int FUNCTION_CALL = 2;

    int IF_STATEMENT = 3;

    int RETURN = 4;

    int WHILE_LOOP = 5;

    int DO_WHILE_LOOP = 6;

    int FOR_LOOP = 7;

}
