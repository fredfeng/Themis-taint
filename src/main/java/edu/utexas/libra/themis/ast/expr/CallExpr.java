package edu.utexas.libra.themis.ast.expr;

import java.util.List;

public interface CallExpr extends Expr {
    String getCalleeName();
    List<Expr> getArguments();
}
