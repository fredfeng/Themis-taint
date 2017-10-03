package testcase;

public class InstanceField {

    private int data;
    private InstanceField other;

    private InstanceField(int data, InstanceField other) {
        this.data = data;
        this.other = other;
    }

    public static int f(int d) {
        InstanceField insField = new InstanceField(d, null);
        InstanceField insField2 = new InstanceField(d+1, insField);

        insField.other = insField2;
        insField2.data = d + 2;
        return insField.data + insField2.data;
    }
}
