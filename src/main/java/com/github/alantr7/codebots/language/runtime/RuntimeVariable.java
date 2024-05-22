package com.github.alantr7.codebots.language.runtime;

import lombok.Getter;
import lombok.Setter;

public class RuntimeVariable {

    private final DataType<?> type;

    private Object value;

    private RuntimeVariable variable;

    private byte valueProvider = 0;

    @Getter @Setter
    private boolean isConstant = false;

    @Getter
    private boolean isInitialized = false;

    public RuntimeVariable(DataType<?> type) {
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
        isInitialized = true;
    }

    public void setPointer(RuntimeVariable variable) {
        this.value = null;
        this.variable = variable;
        valueProvider = 1;
        isInitialized = true;
    }

    public Object getValue() {
        return valueProvider == 0 ? value : variable.getValue();
    }

    public DataType<?> getAcceptedType() {
        return type;
    }

    public DataType<?> getType() {
        return DataType.of(value);
    }

}
