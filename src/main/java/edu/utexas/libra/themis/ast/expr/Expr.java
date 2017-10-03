package edu.utexas.libra.themis.ast.expr;

import edu.utexas.libra.themis.visitor.ExprVisitor;

public interface Expr {
    <T> T accept(ExprVisitor<T> visitor);
}
