package edu.utexas.libra.themis.ast.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;

public interface IfStmt extends Stmt {
    Expr getBranchCondition();
    Stmt getTrueBranch();
    Stmt getFalseBranch();
}
