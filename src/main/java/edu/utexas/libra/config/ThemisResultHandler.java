package edu.utexas.libra.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.utexas.libra.utils.SootUtils;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.Chain;

public class ThemisResultHandler implements ResultsAvailableHandler {
	private final BufferedWriter wr;
	
	private Set<SootMethod> hotspots = new HashSet<>();

	public ThemisResultHandler() {
		this.wr = null;
	}

	public ThemisResultHandler(BufferedWriter wr) {
		this.wr = wr;
	}

	@Override
	public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
		// Dump the results
		if (results == null) {
			print("No results found.");
		} else {
			// Report the results
			for (ResultSinkInfo sink : results.getResults().keySet()) {
				print("Found a flow to sink " + sink + ", from the following sources:");
				for (ResultSourceInfo source : results.getResults().get(sink)) {
//					print("\t- " + source.getSource() + " (in " + cfg.getMethodOf(source.getSource()).getSignature()
//							+ ")");
					if (source.getPath() != null) {
//						print("\t\ton Path(STAC) " + Arrays.toString(source.getPath()));
						Set<Stmt> mySet = new HashSet<Stmt>(Arrays.asList(source.getPath()));
						SootMethod entry = SootUtils.pickEntry(mySet);
						
						//Jia's interface.
						identifyHotSpot(entry);
					}
				}
			}
			// Serialize the results if requested
		}
		SootUtils.printSummary();
		System.out.println("[Hotspots]:" + hotspots);
	}

	/**
	 * Each entry will be fed to the Themis verifier.
	 * @param entry
	 */
	public void identifyHotSpot(SootMethod entry) {
		System.out.println("[Identify hotspot for Themis]" + entry);
		hotspots.add(entry);
		// TODO: Customize this set from a file
		// Comment out Jia's translation for now
//		Set<SootMethod> modelMethodSet = new HashSet<>();
//
//		HotspotMethod method = HotspotMethod.fromMethodAllHigh(entry);
//		Program prog = SootMethodTranslator.translateFromHotspot(method, modelMethodSet);
//		try {
//			PrettyPrinter.print(new PrintWriter(new File("out.themis")), prog);
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException("Cannot open output file");
//		}
	}

	private void print(String string) {
		try {
			System.out.println(string);
			if (wr != null)
				wr.write(string + "\n");
		} catch (IOException ex) {
			// ignore
		}
	}
}