package com.github.alantr7.codebots.language.compiler.parser.element.exp;

import com.github.alantr7.codebots.language.compiler.parser.element.stmt.Statement;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FunctionCall extends Expression implements Statement {

    @Getter
    private final MemberAccess target;

    @Getter
    protected final Expression[] arguments;

    public FunctionCall(MemberAccess target, String name, Expression[] arguments) {
        super(name);
        this.target = target;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    protected String toString(int indent) {
        return " ".repeat(indent) + target + "." + this.getValue() + "(" + (
                arguments.length != 0 ? "\n" + Arrays.stream(arguments).map(a -> a.toString(indent + 2)).collect(Collectors.joining(",\n")) + "\n" + " ".repeat(indent) : ""
        ) + ")";
    }

    @Override
    public String getType() {
        return "function_call";
    }

    @Override
    public int getStatementType() {
        return Statement.FUNCTION_CALL;
    }

}
