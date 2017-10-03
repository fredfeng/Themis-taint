package testcase;

public class WhileBreak {
    public static boolean f(int[] a, int[] b) {
        if (a.length != b.length)
            return false;

        boolean flag = true;
        for (int i = 0; i < a.length; ++i) {
            if (a[i] != b[i]) {
                flag = false;
                break;
            }
        }
        return flag;
    }
}
