package edu.utexas.libra.themis.impl.func;

import edu.utexas.libra.themis.ast.func.Function;
import edu.utexas.libra.themis.ast.func.Param;
import edu.utexas.libra.themis.ast.stmt.Stmt;
import edu.utexas.libra.themis.ast.type.Type;

import java.util.Collections;
import java.util.List;

class ThemisFunction implements Function {
    private String name;
    private List<Param> params;
    private Type retType;
    private Stmt body;

    ThemisFunction(String name, List<Param> params, Type retType, Stmt body) {
        if (name == null || params == null || retType == null || body == null)
            throw new IllegalArgumentException();

        this.name = name;
        this.params = Collections.unmodifiableList(params);
        this.retType = retType;
        this.body = body;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Param> getParameters() {
        return params;
    }

    @Override
    public Type getReturnType() {
        return retType;
    }

    @Override
    public Stmt getFunctionBody() {
        return body;
    }
}
