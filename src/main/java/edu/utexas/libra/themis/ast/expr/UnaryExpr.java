package edu.utexas.libra.themis.ast.expr;

public interface UnaryExpr extends Expr {
    UnaryOperator getOperator();
    Expr getOperand();
}
