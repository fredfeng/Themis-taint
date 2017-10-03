package edu.utexas.libra.themis.visitor;

import edu.utexas.libra.themis.ast.type.ArrayType;
import edu.utexas.libra.themis.ast.type.BasicType;

public interface TypeVisitor<T> {
    T visit(BasicType type);
    T visit(ArrayType type);
}
