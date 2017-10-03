package edu.utexas.libra.themis.ast.expr;

import edu.utexas.libra.themis.ast.type.Type;

public interface NondetConstantExpr extends Expr {
    Type getType();
}
