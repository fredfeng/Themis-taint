package edu.utexas.libra.translator;

import edu.utexas.libra.themis.ast.func.ParamAnnotation;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HotspotMethod {
    private SootMethod method;
    private List<ParamAnnotation> annotations;

    private HotspotMethod(SootMethod method, List<ParamAnnotation> annotations) {
        this.method = method;
        this.annotations = Collections.unmodifiableList(annotations);
    }

    public SootMethod getMethod() { return method; }
    public List<ParamAnnotation> getAnnotations() { return annotations; }
    
    private static int getParamCount(SootMethod method) {
        int cnt = method.getParameterCount();
        if (!method.isStatic())
            ++cnt;
        return cnt;
    }

    public static HotspotMethod fromMethodAndAnnotations(SootMethod method, List<ParamAnnotation> annots) {
        return new HotspotMethod(method, annots);
    }

    public static HotspotMethod fromMethodAllHigh(SootMethod method) {
        int numParams = getParamCount(method);
        List<ParamAnnotation> annots = new ArrayList<>(numParams);
        for (int i = 0; i < numParams; ++i) {
            annots.add(ParamAnnotation.HIGH);
        }
        return new HotspotMethod(method, annots);
    }

    public static HotspotMethod fromMethodAllLow(SootMethod method) {
        int numParams = getParamCount(method);
        List<ParamAnnotation> annots = new ArrayList<>(numParams);
        for (int i = 0; i < numParams; ++i) {
            annots.add(ParamAnnotation.LOW);
        }
        return new HotspotMethod(method, annots);
    }

    public static HotspotMethod fromMethodWithHigh(SootMethod method, List<Integer> highIndices) {
        int numParams = getParamCount(method);
        List<ParamAnnotation> annots = new ArrayList<>(numParams);
        for (int i = 0; i < numParams; ++i) {
            annots.add(ParamAnnotation.LOW);
        }
        for (Integer i: highIndices) {
            if (i < 0 || i >= numParams)
                throw new IllegalArgumentException("Parameter index out of bound");
            annots.set(i, ParamAnnotation.HIGH);
        }
        return new HotspotMethod(method, annots);
    }

    public static HotspotMethod fromMethodWithLow(SootMethod method, List<Integer> lowIndices) {
        int numParams = getParamCount(method);
        List<ParamAnnotation> annots = new ArrayList<>(numParams);
        for (int i = 0; i < numParams; ++i) {
            annots.add(ParamAnnotation.HIGH);
        }
        for (Integer i: lowIndices) {
            if (i < 0 || i >= numParams)
                throw new IllegalArgumentException("Parameter index out of bound");
            annots.set(i, ParamAnnotation.LOW);
        }
        return new HotspotMethod(method, annots);
    }
}
