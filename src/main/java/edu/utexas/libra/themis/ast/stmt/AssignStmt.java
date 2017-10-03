package edu.utexas.libra.themis.ast.stmt;

import edu.utexas.libra.themis.ast.lvalue.Lvalue;
import edu.utexas.libra.themis.ast.expr.Expr;

public interface AssignStmt extends Stmt {
    Lvalue getTarget();
    Expr getSource();
}
