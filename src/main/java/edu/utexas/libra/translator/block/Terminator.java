package edu.utexas.libra.translator.block;

public interface Terminator {
    <T> T accept(TerminatorVisitor<T> visitor);
}
