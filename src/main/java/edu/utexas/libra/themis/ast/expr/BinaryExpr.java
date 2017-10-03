package edu.utexas.libra.themis.ast.expr;

public interface BinaryExpr extends Expr {
    BinaryOperator getOperator();
    Expr getFirstOperand();
    Expr getSecondOperand();
}
