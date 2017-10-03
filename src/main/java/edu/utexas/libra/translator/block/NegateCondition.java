package edu.utexas.libra.translator.block;

import soot.Value;

public class NegateCondition implements Condition {
    private Value value;

    public NegateCondition(Value value) {
        this.value = value;
    }

    public Value getValue() { return value; }

    @Override
    public <T> T accept(ConditionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Condition negate() {
        return new ValueCondition(value);
    }
}
