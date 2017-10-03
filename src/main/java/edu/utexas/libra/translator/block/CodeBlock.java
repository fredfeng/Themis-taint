package edu.utexas.libra.translator.block;

public interface CodeBlock {
    <T> T accept(CodeBlockVisitor<T> visitor);
}
