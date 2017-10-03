package edu.utexas.libra.themis.ast.func;

public enum ParamAnnotation {
    NONE(0x0),
    LOW(0x1),
    HIGH(0x2),
    LOWHIGH(0x3);

    private static int LOW_MASK = 0x1;
    private static int HIGH_MASK = 0x2;

    private int flags;
    ParamAnnotation(int flags) {
        this.flags = flags;
    }

    public boolean isLow() {
        return (flags & LOW_MASK) != 0;
    }

    public boolean isHigh() {
        return (flags & HIGH_MASK) != 0;
    }
}
