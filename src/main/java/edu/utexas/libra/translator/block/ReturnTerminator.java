package edu.utexas.libra.translator.block;

import soot.Value;

public class ReturnTerminator implements Terminator {
    private Value value;

    public ReturnTerminator(Value value) {
        this.value = value;
    }

    public Value getValue() { return value; }

    @Override
    public <T> T accept(TerminatorVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
