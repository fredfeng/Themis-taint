package edu.utexas.libra.themis.impl.lvalue;

import edu.utexas.libra.themis.ast.lvalue.VariableRef;
import edu.utexas.libra.themis.visitor.LvalueVisitor;

class ThemisVariableRef extends ThemisLvalue implements VariableRef {

    private String name;

    ThemisVariableRef(String name) {
        if (name == null)
            throw new IllegalArgumentException();

        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(LvalueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
