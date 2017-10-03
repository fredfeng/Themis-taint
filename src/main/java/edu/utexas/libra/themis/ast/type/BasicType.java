package edu.utexas.libra.themis.ast.type;

public interface BasicType extends Type {
    enum Tag {
        UNIT,
        BOOL,
        INT
    }

    Tag getTag();
}
