package com.github.alantr7.codebots.language.runtime;

import lombok.Getter;

public class RuntimeVariable {

    private final ValueType type;

    private Object value;

    private RuntimeVariable variable;

    private byte valueProvider = 0;

    public RuntimeVariable(ValueType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public void setValue(Object value) {
        this.value = value;
        this.variable = null;
        valueProvider = 0;
    }

    public void setPointer(RuntimeVariable variable) {
        this.value = null;
        this.variable = variable;
        valueProvider = 1;
    }

    public Object getValue() {
        return valueProvider == 0 ? value : variable.getValue();
    }

    public ValueType getAcceptedType() {
        return type;
    }

    public ValueType getType() {
        return ValueType.of(value);
    }

}
