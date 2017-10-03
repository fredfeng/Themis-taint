package edu.utexas.libra.themis.impl.type;

import edu.utexas.libra.themis.ast.type.ArrayType;
import edu.utexas.libra.themis.ast.type.BasicType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThemisTypeBuilder {
    private static final ThemisBasicType UNIT_TYPE;
    private static final ThemisBasicType BOOL_TYPE;
    private static final ThemisBasicType INT_TYPE;
    private static final Map<ThemisBasicType, ThemisArrayType> arrayTypeMap;
    static {
        UNIT_TYPE = new ThemisBasicType(BasicType.Tag.UNIT);
        BOOL_TYPE = new ThemisBasicType(BasicType.Tag.BOOL);
        INT_TYPE = new ThemisBasicType(BasicType.Tag.INT);
        Map<ThemisBasicType, ThemisArrayType> aMap = new HashMap<>();
        aMap.put(UNIT_TYPE, new ThemisArrayType(UNIT_TYPE));
        aMap.put(BOOL_TYPE, new ThemisArrayType(BOOL_TYPE));
        aMap.put(INT_TYPE, new ThemisArrayType(INT_TYPE));
        arrayTypeMap = Collections.unmodifiableMap(aMap);
    }

    public static ThemisBasicType makeUnitType() {
        return UNIT_TYPE;
    }

    public static ThemisBasicType makeBoolType() {
        return BOOL_TYPE;
    }

    public static ThemisBasicType makeIntType() {
        return INT_TYPE;
    }

    public static ThemisArrayType makeArrayType(BasicType elemType) {
        ThemisArrayType ret = arrayTypeMap.get(elemType);
        if (ret == null)
            throw new RuntimeException("Missing entry in ThemisTypeBuilder.arrayTypeMap");
        return ret;
    }
}
