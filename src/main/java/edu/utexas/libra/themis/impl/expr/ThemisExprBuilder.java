package edu.utexas.libra.themis.impl.expr;

import edu.utexas.libra.themis.ast.expr.BinaryOperator;
import edu.utexas.libra.themis.ast.expr.Expr;
import edu.utexas.libra.themis.ast.expr.UnaryOperator;
import edu.utexas.libra.themis.ast.lvalue.Lvalue;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.ast.type.Type;

import java.util.List;

public class ThemisExprBuilder {

    public static ThemisUnitConstantExpr makeUnitConst() {
        return new ThemisUnitConstantExpr();
    }

    public static ThemisBoolConstantExpr makeBoolConst(boolean value) {
        return new ThemisBoolConstantExpr(value);
    }

    public static ThemisIntConstantExpr makeIntConst(int value) {
        return new ThemisIntConstantExpr(value);
    }

    public static ThemisNondetConstantExpr makeNondetConst(Type type) {
        return new ThemisNondetConstantExpr(type);
    }

    public static ThemisValueFetchExpr makeValueFetch(Lvalue lvalue) {
        return new ThemisValueFetchExpr(lvalue);
    }

    public static ThemisNewArrayExpr makeNewArray(BasicType elemType, Expr size) {
        return new ThemisNewArrayExpr(elemType, size);
    }

    public static ThemisMakeArrayExpr makeMakeArray(BasicType elemType, List<Expr> elems) {
        return new ThemisMakeArrayExpr(elemType, elems);
    }

    public static ThemisUnaryExpr makeUnary(UnaryOperator operator, Expr operand) {
        return new ThemisUnaryExpr(operator, operand);
    }

    public static ThemisBinaryExpr makeBinary(BinaryOperator operator, Expr first, Expr second) {
        return new ThemisBinaryExpr(operator, first, second);
    }

    public static ThemisCallExpr makeCall(String name, List<Expr> args) {
        return new ThemisCallExpr(name, args);
    }
}
