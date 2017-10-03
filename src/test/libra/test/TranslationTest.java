package test;

import edu.utexas.libra.themis.ast.Program;
import edu.utexas.libra.themis.visitor.PrettyPrinter;
import org.junit.Test;

import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;

public class TranslationTest extends BaseTest {

    private static final String PKGNAME = "testcase";

    private Program doTranslate(String className, String methodName, Integer... highIndices) {
        return super.doTranslate(PKGNAME, className, methodName, highIndices);
    }

    @Test
    public void testBasic() {
        Program prog = doTranslate("Basic", "f");
        logger.info("Translation successful");
    }

    @Test
    public void testBranch() {
        Program prog = doTranslate("Branch", "f");
        logger.info("Translation successful");
    }

    @Test
    public void testIfAnd() {
        Program prog = doTranslate("IfAnd", "f");
        logger.info("Translation successful");
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void testForLoop() {
        Program prog = doTranslate("ForLoop", "f");
        logger.info("Translation successful");
    }

    @Test
    public void testNestedForLoop() {
        Program prog = doTranslate("NestedForLoop", "f");
        logger.info("Translation successful");
    }

    @Test
    public void testWhileLoop() {
        Program prog = doTranslate("WhileLoop", "f");
        logger.info("Translation successful");
    }

    @Test
    public void testWhileContinue() {
        Program prog = doTranslate("WhileContinue", "f");
        logger.info("Translation successful");
    }

    @Test
    public void testWhileBreak() {
        Program prog = doTranslate("WhileBreak", "f");
        logger.info("Translation successful");
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }

    @Test
    public void testStaticCall() {
        Program prog = doTranslate("StaticCall", "f");
        logger.info("Translation successful");
        assertEquals(prog.size(), 3);
    }

    @Test
    public void testInstanceField() {
        Program prog = doTranslate("InstanceField", "f");
        logger.info("Translation successful");
        PrettyPrinter.print(new PrintWriter(System.out), prog);
    }
}
