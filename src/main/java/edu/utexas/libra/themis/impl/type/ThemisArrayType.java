package edu.utexas.libra.themis.impl.type;

import edu.utexas.libra.themis.ast.type.ArrayType;
import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.visitor.TypeVisitor;

class ThemisArrayType extends ThemisType implements ArrayType {
    private BasicType elemType;

    ThemisArrayType(BasicType elemType) {
        if (elemType == null)
            throw new IllegalArgumentException();

        this.elemType = elemType;
    }

    @Override
    public BasicType getElementType() {
        return elemType;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean isUnit() {
        return false;
    }

    @Override
    public boolean isBool() {
        return false;
    }

    @Override
    public boolean isInt() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ThemisArrayType))
            return false;
        if (obj == this)
            return true;

        ThemisArrayType rhs = (ThemisArrayType) obj;
        return elemType.equals(rhs.elemType);
    }

    @Override
    public int hashCode() {
        return elemType.hashCode() * 31;
    }

    @Override
    public String toString() {
        return elemType.toString() + "_array";
    }
}
