package edu.utexas.libra.themis.ast.type;

public interface ArrayType extends Type {
    BasicType getElementType();
}
