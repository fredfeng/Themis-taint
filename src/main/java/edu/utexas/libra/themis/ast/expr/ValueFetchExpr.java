package edu.utexas.libra.themis.ast.expr;

import edu.utexas.libra.themis.ast.lvalue.Lvalue;

public interface ValueFetchExpr extends Expr {
    Lvalue getSource();
}
