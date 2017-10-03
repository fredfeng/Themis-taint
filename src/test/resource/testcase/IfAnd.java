package testcase;

public class IfAnd {
    public static int f(int x, int y) {
        int ret = 0;
        if (x > 0 && y > 0)
            ret = x + y;
        else
            ret = x - y;
        return ret;
    }
}
