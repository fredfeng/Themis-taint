package edu.utexas.libra.themis.impl.func;

import edu.utexas.libra.themis.ast.func.Param;
import edu.utexas.libra.themis.ast.func.ParamAnnotation;
import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.ast.type.Type;

import java.util.List;

public class ThemisFunctionBuilder {
    public static ThemisVarDecl makeVarDecl(Type type, String name) {
        return new ThemisVarDecl(type, name);
    }

    public static ThemisParam makeParam(VarDecl decl, ParamAnnotation annot) {
        return new ThemisParam(decl, annot);
    }

    public static ThemisFunction makeFunction(String name, List<Param> params, Type retType, Stmt body) {
        return new ThemisFunction(name, params, retType, body);
    }
}
