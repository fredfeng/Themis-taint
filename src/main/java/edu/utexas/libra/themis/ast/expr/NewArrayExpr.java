package edu.utexas.libra.themis.ast.expr;

import edu.utexas.libra.themis.ast.type.BasicType;

public interface NewArrayExpr extends Expr {
    BasicType getElementType();
    Expr getSize();
}
