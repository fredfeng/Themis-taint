package edu.utexas.libra.themis.ast.lvalue;

import edu.utexas.libra.themis.visitor.LvalueVisitor;

public interface Lvalue {
    <T> T accept(LvalueVisitor<T> visitor);
}
