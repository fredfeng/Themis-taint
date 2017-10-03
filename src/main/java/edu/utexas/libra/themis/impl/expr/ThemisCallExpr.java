package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.CallExpr;
import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.visitor.ExprVisitor;

import java.util.Collections;
import java.util.List;

class ThemisCallExpr extends ThemisExpr implements CallExpr {
    private String calleeName;
    private List<Expr> args;

    ThemisCallExpr(String calleeName, List<Expr> args) {
        if (calleeName == null || args == null)
            throw new IllegalArgumentException();

        this.calleeName = calleeName;
        this.args = Collections.unmodifiableList(args);
    }

    @Override
    public String getCalleeName() {
        return calleeName;
    }

    @Override
    public List<Expr> getArguments() {
        return args;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
