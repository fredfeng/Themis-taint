package edu.utexas.libra.themis.ast.lvalue;

import edu.utexas.libra.themis.ast.expr.Expr;

public interface ArrayRef extends Lvalue {
    String getName();
    Expr getIndex();
}
