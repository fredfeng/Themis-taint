package edu.utexas.libra.translator.block;

public interface TerminatorVisitor<T> {
    T visit(BreakTerminator breakTerm);
    T visit(ReturnTerminator returnTerm);
}
