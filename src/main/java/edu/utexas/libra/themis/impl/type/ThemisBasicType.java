package edu.utexas.libra.themis.impl.type;

import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.visitor.TypeVisitor;

final class ThemisBasicType extends ThemisType implements BasicType {
    private Tag tag;

    ThemisBasicType(Tag t) {
        if (t == null)
            throw new IllegalArgumentException();

        tag = t;
    }

    @Override
    public <T> T accept(TypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Tag getTag() {
        return tag;
    }

    @Override
    public boolean isUnit() {
        return tag == Tag.UNIT;
    }

    @Override
    public boolean isBool() {
        return tag == Tag.BOOL;
    }

    @Override
    public boolean isInt() {
        return tag == Tag.INT;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ThemisBasicType))
            return false;
        if (obj == this)
            return true;

        ThemisBasicType rhs = (ThemisBasicType) obj;
        return tag == rhs.tag;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        switch (tag) {
            case UNIT:
                return "unit";
            case BOOL:
                return "bool";
            case INT:
                return "int";
            default:
                throw new RuntimeException("Unrecognized basictype tag: " + tag);
        }

    }
}
