package edu.utexas.libra.translator;

import edu.utexas.libra.themis.ast.type.BasicType;
import edu.utexas.libra.themis.ast.type.Type;
import edu.utexas.libra.themis.builder.AstBuilder;
import soot.*;

import java.sql.Ref;

public class SootTypeTranslator {
    private AstBuilder astBuilder;

    public SootTypeTranslator(AstBuilder astBuilder) {
        this.astBuilder = astBuilder;
    }

    public BasicType translateBasicType(soot.Type sootType) {
        if (
                sootType instanceof BooleanType ||
                sootType instanceof ByteType ||
                        sootType instanceof CharType ||
                        sootType instanceof FloatType ||
                        sootType instanceof DoubleType ||
                        sootType instanceof ShortType ||
                        sootType instanceof IntType ||
                        sootType instanceof LongType ||
                        sootType instanceof RefType
                )
            return astBuilder.makeIntType();
        else if (sootType instanceof VoidType)
            return astBuilder.makeUnitType();
        else
            throw new RuntimeException("Basic type not supported yet: " + sootType);
    }


    public Type translateType(soot.Type sootType) {
        if (sootType instanceof BooleanType ||
                sootType instanceof ByteType ||
                sootType instanceof CharType ||
                sootType instanceof FloatType ||
                sootType instanceof DoubleType ||
                sootType instanceof ShortType ||
                sootType instanceof IntType ||
                sootType instanceof LongType
                )
            return astBuilder.makeIntType();
        else if (sootType instanceof VoidType)
            return astBuilder.makeUnitType();
        else if (sootType instanceof ArrayType) {
            ArrayType arrType = (ArrayType) sootType;
            soot.Type elemType = arrType.getElementType();
            return astBuilder.makeArrayType(translateBasicType(elemType));
        } else if (sootType instanceof RefType) {
            RefType refType = (RefType) sootType;

            // Hack: string gets treated as an int array
            if (refType.getClassName().equals("java.lang.String")) {
                return astBuilder.makeArrayType(astBuilder.makeIntType());
            }

            return astBuilder.makeIntType();
        } else
            throw new RuntimeException("Type not supported yet: " + sootType);
    }
}
