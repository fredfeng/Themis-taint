package testcase;

public class WhileContinue {
    public static int f(int n) {
        int s = 0, i = 0;
        boolean flag = false;
        while (!flag) {
            if (i > n) {
                flag = true;
                continue;
            }
            s = s + i;
            i = i + 1;
        }
        return s;
    }
}
