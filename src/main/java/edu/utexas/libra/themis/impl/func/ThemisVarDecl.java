package edu.utexas.libra.themis.impl.func;

import edu.utexas.libra.themis.ast.func.VarDecl;
import edu.utexas.libra.themis.ast.type.Type;

class ThemisVarDecl implements VarDecl {
    private Type type;
    private String name;

    ThemisVarDecl(Type type, String name) {
        if (type == null || name == null)
            throw new IllegalArgumentException();
        this.type = type;
        this.name = name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }
}
