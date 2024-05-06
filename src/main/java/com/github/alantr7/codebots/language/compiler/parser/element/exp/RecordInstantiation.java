package com.github.alantr7.codebots.language.compiler.parser.element.exp;

import com.github.alantr7.codebots.language.compiler.parser.element.stmt.Statement;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RecordInstantiation extends Expression implements Statement {

    @Getter
    private final VariableAccess target;

    @Getter
    protected final Expression[] arguments;

    public RecordInstantiation(VariableAccess target, Expression[] arguments) {
        super(target);
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
        return "new_record";
    }

    @Override
    public int getStatementType() {
        return Statement.FUNCTION_CALL;
    }

}
