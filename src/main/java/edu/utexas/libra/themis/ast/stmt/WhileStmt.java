package edu.utexas.libra.themis.ast.stmt;

import edu.utexas.libra.themis.ast.expr.Expr;

public interface WhileStmt extends Stmt {
    Expr getLoopCondition();
    Stmt getLoopBody();
}
