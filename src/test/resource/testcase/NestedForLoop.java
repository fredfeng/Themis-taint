package testcase;

public class NestedForLoop {
    public static int f(int n, int m) {
        int s = 0;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j)
                s = s + (i+1) * (j+1);
        }
        return s;
    }
}
