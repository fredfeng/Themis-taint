package edu.utexas.libra.themis.ast.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;

public interface AssertStmt extends Stmt {
    Expr getAssertion();
}
