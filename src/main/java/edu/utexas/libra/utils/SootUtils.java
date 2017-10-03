package edu.utexas.libra.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.Files;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.SourceLocator;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.options.Options;
import soot.util.Chain;

public class SootUtils {

    private static final Pattern comment = Pattern.compile("(.*?)(\\s*//.*)?$");

    public static Local addNewLocal(String name, Type type, SootMethod method) {
        Local local = Jimple.v().newLocal(name, type);
        method.getActiveBody().getLocals().add(local);
        return local;
    }

    public static void addNewStmt(Stmt stmt, SootMethod method) {
        method.getActiveBody().getUnits().add(stmt);
    }

    public static void makeEmptyActiveBody(SootMethod method) {
        Body body = Jimple.v().newBody(method);
        method.setActiveBody(body);
    }

    public static SootMethodRef methodRef(SootMethod sinkMethod) {
        return sinkMethod.makeRef();
    }

    public static SootFieldRef fieldRef(SootField field) {
        return field.makeRef();
    }

    public static void dumpClass(SootClass sootClass) {
        String fileName = SourceLocator.v().getFileNameFor(sootClass, Options.output_format_class);
        IOException ex = null;
        FileOutputStream stream = null;
        try {
            File f = new File(fileName);
            Files.createParentDirs(f);
            Files.touch(f);
            stream = new FileOutputStream(fileName);
            new soot.baf.BafASMBackend(sootClass, 3).generateClassFile(stream);
        } catch (IOException e) {
            ex = e;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    ex = e;
                }
            }
        }
    }

    public static Long getValueFromConstant(Value arg) {
        if (arg instanceof LongConstant) {
            return ((LongConstant) arg).value;
        } else if (arg instanceof IntConstant) {
            return (long) ((IntConstant) arg).value;
        }
        return null;
    }

    private static List<String> stripEmptyLinesAndComments(List<String> lines) {
        List<String> stripped = new ArrayList<>();
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }

            Matcher m = comment.matcher(line);
            if (!m.matches()) {
                continue;
            }
            line = m.group(1);

            if (line.isEmpty()) {
                continue;
            }

            stripped.add(line);
        }
        return stripped;
    }

	public static List<String> readLines(String filename) {
		try {
			return SootUtils.stripEmptyLinesAndComments(
					java.nio.file.Files.readAllLines(Paths.get(filename), Charset.defaultCharset()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static SootMethod pickEntry(Set<Stmt> stmts) {
		System.out.println("Taint stmt:" + stmts);
		assert !Scene.v().getEntryPoints().isEmpty();
		SootMethod entryMethod = Scene.v().getEntryPoints().get(0);
		CallGraph cg = Scene.v().getCallGraph();
		Set<SootMethod> reachableSet = new HashSet<>();
		Queue<SootMethod> workList = new ArrayDeque<>();
		workList.add(entryMethod);

		while (!workList.isEmpty()) {
			SootMethod currMethod = workList.poll();
			if (reachableSet.contains(currMethod))
				continue;
			reachableSet.add(currMethod);
			for (Stmt s : stmts) {
				if (currMethod.hasActiveBody() && hasStmt(currMethod, s)) {
					return currMethod;
				}
			}

			for (Iterator<Edge> it = cg.edgesOutOf(currMethod); it.hasNext();) {
				Edge mmc = it.next();

				SootMethod tgtMethod = mmc.tgt();
				if (tgtMethod.isPhantom())
					continue;

				workList.add(tgtMethod);
			}
		}
		return null;
	}
	
	public static boolean hasStmt(SootMethod meth, Stmt stmtCheck) {
		Chain<Unit> units = meth.getActiveBody().getUnits();
		Iterator<Unit> uit = units.snapshotIterator();
		while (uit.hasNext()) {
			Stmt stmt = (Stmt) uit.next();
			if (stmt.equals(stmtCheck))
				return true;
		}
		return false;
	}
	
	public static void printSummary() {
		int total = 0;
		ReachableMethods reachableMethods = Scene.v().getReachableMethods();
		for (Iterator<MethodOrMethodContext> iter = reachableMethods.listener(); iter.hasNext();) {
			SootMethod meth = iter.next().method();
			if(meth.hasActiveBody()) {
//				System.out.println("Debug method:" + meth);
				Chain<Unit> units = meth.getActiveBody().getUnits();
				Iterator<Unit> uit = units.snapshotIterator();
				while (uit.hasNext()) {
					uit.next();
					total++;
				}
			}
		}
		System.out.println("[Total lines of Jimple]:" + total);
	}
	
}
