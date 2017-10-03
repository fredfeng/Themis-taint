package edu.utexas.libra.themis.impl.func;

import edu.utexas.libra.themis.ast.func.Param;
import edu.utexas.libra.themis.ast.func.ParamAnnotation;
import edu.utexas.libra.themis.ast.func.VarDecl;

class ThemisParam implements Param {
    private VarDecl varDecl;
    private ParamAnnotation annotation;

    ThemisParam(VarDecl varDecl, ParamAnnotation annotation) {
        if (varDecl == null || annotation == null)
            throw new IllegalArgumentException();

        this.varDecl = varDecl;
        this.annotation = annotation;
    }

    @Override
    public ParamAnnotation getAnnotation() {
        return annotation;
    }

    @Override
    public VarDecl getDecl() {
        return varDecl;
    }
}
