package edu.utexas.libra.themis.ast.type;

import edu.utexas.libra.themis.visitor.TypeVisitor;

public interface Type {
    <T> T accept(TypeVisitor<T> visitor);

    boolean isUnit();
    boolean isBool();
    boolean isInt();
    boolean isArray();
}
