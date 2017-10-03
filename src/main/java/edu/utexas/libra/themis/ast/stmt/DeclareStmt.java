package edu.utexas.libra.themis.ast.stmt;

import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.expr.Expr;

public interface DeclareStmt extends Stmt {
    VarDecl getDelcaration();

    // Return null if not exist
    Expr getInitExpr();
}
