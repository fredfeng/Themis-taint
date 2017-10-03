package edu.utexas.libra.themis.ast.expr;

import edu.utexas.libra.themis.ast.type.BasicType;

import java.util.List;

public interface MakeArrayExpr extends Expr {
    BasicType getElementType();
    List<Expr> getElements();
}
