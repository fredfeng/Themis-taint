package test;

import edu.utexas.libra.themis.ast.Program;
import edu.utexas.libra.translator.HotspotMethod;
import edu.utexas.libra.translator.SootMethodTranslator;
import org.junit.Before;
import org.junit.BeforeClass;
import soot.SootMethod;
import edu.utexas.libra.utils.SootHelper;
import edu.utexas.libra.utils.TempDirectory;
import edu.utexas.libra.utils.SourceCompiler;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class BaseTest {
    protected static final Logger logger = Logger.getLogger(TranslationTest.class.getName());
    private final static String MODEL_JAR = "model.jar";
    protected static TempDirectory tmpRoot;

    protected static String getModelJarLocation() {
        return TranslationTest.class.getClassLoader().getResource(MODEL_JAR).getPath();
    }

    protected static String getCompilerRootLocation() {
        return tmpRoot.getPath().toString();
    }

    @BeforeClass
    public static void testSetup() throws IOException {
        tmpRoot = new TempDirectory("libraCompilerTestRoot");
        tmpRoot.deleteOnExit();
        logger.info("Temporary compiler root created at " + getCompilerRootLocation());
    }

    @Before
    public void resetSoot() {
        soot.G.reset();
    }

    private Path getTestCase(String packageName, String name) {
        return Paths.get(getTestcaseRootLocation(packageName), name + ".java");
    }
    private String getTestcaseRootLocation(String packageName) {
        String loc = packageName.replace('.', '/');
        return TranslationTest.class.getClassLoader().getResource(loc).getPath();
    }

    private String getTestcaseClassName(String packageName, String name) {
        return packageName + "." + name;
    }

    protected Program doTranslate(String packageName, String className, String methodName, Integer... highIndices) {
        Path testcasePath = getTestCase(packageName, className);
        String fullClassName = getTestcaseClassName(packageName, className);
        List<String> classNames = Collections.singletonList(fullClassName);
        String classPath = String.join(":", getCompilerRootLocation(), getModelJarLocation());

        SourceCompiler.compileToDirectory(testcasePath, tmpRoot.getPath().toString());
        SootHelper.loadClasses(classPath, classNames);
        SootMethod method = SootHelper.getMethodInClass(methodName, fullClassName);
        SootHelper.setEntryPoint(method);
        SootHelper.runSootPacks();

        HotspotMethod hsMethod = HotspotMethod.fromMethodWithHigh(method, Arrays.asList(highIndices));
        return SootMethodTranslator.translateFromHotspot(hsMethod, Collections.emptySet());
    }
}
