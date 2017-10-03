package edu.utexas.libra.themis.impl.lvalue;

import edu.utexas.libra.themis.ast.expr.Expr;

public class ThemisLvalueBuilder {
    public static ThemisVariableRef makeVariableRef(String name) {
        return new ThemisVariableRef(name);
    }
    public static ThemisArrayRef makeArrayRef(String baseName, Expr index) {
        return new ThemisArrayRef(baseName, index);
    }
}
