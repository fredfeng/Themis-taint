package edu.utexas.libra.themis.ast.func;

public interface Param {
    ParamAnnotation getAnnotation();
    VarDecl getDecl();
}
