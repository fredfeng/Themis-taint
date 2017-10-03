package edu.utexas.libra.themis.ast.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;

public interface ConsumeStmt extends Stmt {
    Expr getConsumption();
}
