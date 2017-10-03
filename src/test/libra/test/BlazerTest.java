package test;

import edu.utexas.libra.themis.ast.Program;
import edu.utexas.libra.themis.visitor.PrettyPrinter;
import org.junit.Test;

import java.io.PrintWriter;

public class BlazerTest extends BaseTest {
    private static final String PKGNAME = "blazer";

    private Program doTranslate(String className, String methodName, Integer... highIndices) {
        return super.doTranslate(PKGNAME, className, methodName, highIndices);
    }

    @Test
    public void sanityNoTaintUnsafe() {
        Program prog = doTranslate("Sanity", "notaint_unsafe", 0);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void sanityNoSecretSafe() {
        Program prog = doTranslate("Sanity", "nosecret_safe", 0);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void sanityStraightlineUnsafe() {
        Program prog = doTranslate("Sanity", "straightline_unsafe", 0, 1);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void sanityStraightlineSafe() {
        Program prog = doTranslate("Sanity", "straightline_safe", 0);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void sanitySanityUnsafe() {
        Program prog = doTranslate("Sanity", "sanity_unsafe", 0);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void sanitySanitySafe() {
        Program prog = doTranslate("Sanity", "sanity_safe", 0);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void moreSanityArrayUnsafe() {
        Program prog = doTranslate("MoreSanity", "array_unsafe", 1);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void moreSanityArraySafe() {
        Program prog = doTranslate("MoreSanity", "array_safe", 1);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void moreSanityLoopAndBranchUnsafe() {
        Program prog = doTranslate("MoreSanity", "loopAndbranch_unsafe", 1);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void moreSanityLoopAndBranchSafe() {
        Program prog = doTranslate("MoreSanity", "loopAndbranch_safe");
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void pwCheck1Unsafe() {
        Program prog = doTranslate("PWCheck", "pwcheck1_unsafe", 1);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void pwCheck2Unsafe() {
        Program prog = doTranslate("PWCheck", "pwcheck2_unsafe", 1);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void pwCheck3Safe() {
        Program prog = doTranslate("PWCheck", "pwcheck3_safe", 1);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void loginUnsafe() {
        // TODO: investigate this
        Program prog = doTranslate("Login", "login_unsafe");
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void loginSafe() {
        Program prog = doTranslate("Login", "login_safe");
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void userPasswordEqualSafe() {
        Program prog = doTranslate("User", "passwordsEqual_safe", 0);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void userPasswordEqualUnsafe() {
        Program prog = doTranslate("User", "passwordsEqual_unsafe", 0);
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }
}
