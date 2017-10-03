package testcase;

public class WhileLoop {
    public static int f(int n) {
        int s = 0, i = 0;
        boolean flag = false;
        while (!flag) {
            s = s + i;
            i = i + 1;
            if (i > n)
                flag = true;
            else
                flag = false;
        }
        return s;
    }
}
