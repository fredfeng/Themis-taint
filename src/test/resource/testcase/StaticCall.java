package testcase;

public class StaticCall {
    public static int f(int n) {
        int s = 0;
        for (int i = 0; i < g(n); ++i) {
            s = s + i;
        }
        return s;
    }

    private static int g(int n) {
        return n + n;
    }
}
