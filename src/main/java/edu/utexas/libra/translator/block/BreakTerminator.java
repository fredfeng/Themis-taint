package edu.utexas.libra.translator.block;

public class BreakTerminator implements Terminator {
    @Override
    public <T> T accept(TerminatorVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
