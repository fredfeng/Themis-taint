package testcase;

public class ForLoop {
    public static int f(int n) {
        int s = 0;
        for (int i = 0; i < n; ++i)
            s = s + i;
        return s;
    }
}
