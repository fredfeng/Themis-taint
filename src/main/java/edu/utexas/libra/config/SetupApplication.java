/*******************************************************************************
\ * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package edu.utexas.libra.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.UnsupportedDataTypeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import edu.utexas.libra.utils.SootUtils;
import soot.Main;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.AbstractInfoflow;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.cfg.BiDirICFGFactory;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.ipc.IIPCManager;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.rifl.RIFLSourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.data.ISourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.data.SourceSinkDefinition;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.options.Options;

public class SetupApplication {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private SrcSinkParser sourceSinkProvider;
	private final Map<String, Set<SootMethodAndClass>> callbackMethods =
			new HashMap<String, Set<SootMethodAndClass>>(10000);

	private InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
	
	private Set<String> entrypoints = new HashSet<>();
	private Set<String> callbackClasses = null;

	private String appPackageName = "";
	private String entryLoc = "entry.txt";

	private final String androidJar;
	private final boolean forceAndroidJar;
	private final String apkFileLocation;
	private final String additionalClasspath;
	private ITaintPropagationWrapper taintWrapper;
	
	private ThemisSourceSinkManager sourceSinkManager = null;
	private AndroidEntryPointCreator entryPointCreator = null;


	private IInfoflowConfig sootConfig = new SootConfigForThemis();
	private BiDirICFGFactory cfgFactory = null;

	private IIPCManager ipcManager = null;
	
	private long maxMemoryConsumption = -1;
	
	private Set<Stmt> collectedSources = null;
	private Set<Stmt> collectedSinks = null;

	private String callbackFile = "AndroidCallbacks.txt"; 
	
	/**
	 * Creates a new instance of the {@link SetupApplication} class
	 * 
	 * @param androidJar
	 *            The path to the Android SDK's "platforms" directory if Soot shall automatically select the JAR file to
	 *            be used or the path to a single JAR file to force one.
	 * @param apkFileLocation
	 *            The path to the APK file to be analyzed
	 */
	public SetupApplication(String androidJar, String apkFileLocation) {
		this(androidJar, apkFileLocation, "", null, "");
	}

	/**
	 * Creates a new instance of the {@link SetupApplication} class
	 * 
	 * @param androidJar
	 *            The path to the Android SDK's "platforms" directory if
	 *            Soot shall automatically select the JAR file to
	 *            be used or the path to a single JAR file to force one.
	 * @param apkFileLocation
	 *            The path to the APK file to be analyzed
	 * @param ipcManager
	 *            The IPC manager to use for modelling inter-component and inter-application data flows
	 */
	public SetupApplication(String androidJar, String apkFileLocation,
			IIPCManager ipcManager) {
		this(androidJar, apkFileLocation, "", ipcManager, "");
	}
	
	/**
	 * Creates a new instance of the {@link SetupApplication} class
	 * 
	 * @param androidJar
	 *            The path to the Android SDK's "platforms" directory if
	 *            Soot shall automatically select the JAR file to
	 *            be used or the path to a single JAR file to force one.
	 * @param apkFileLocation
	 *            The path to the APK file to be analyzed
	 * @param ipcManager
	 *            The IPC manager to use for modelling inter-component and inter-application data flows
	 */
	public SetupApplication(String androidJar, String apkFileLocation,
			String additionalClasspath,
			IIPCManager ipcManager, String entry) {
		System.out.println("androidJar: " + androidJar);
		System.out.println("apkFileLocation:" + apkFileLocation);
		System.out.println("additionalClassPath" + additionalClasspath);
		File f = new File(androidJar);
		this.forceAndroidJar = f.isFile();

		this.androidJar = androidJar;
		this.apkFileLocation = apkFileLocation;

		this.ipcManager = ipcManager;
		//FIXME
		if(entry != null) entryLoc = entry;
		System.out.println("current entry:" + entryLoc);
		this.additionalClasspath = additionalClasspath;

		this.entrypoints.addAll(SootUtils.readLines(entryLoc));
		assert !entrypoints.isEmpty();
	}
	
	/**
	 * Gets the set of sinks loaded into FlowDroid These are the sinks as
	 * they are defined through the SourceSinkManager.
	 * 
	 * @return The set of sinks loaded into FlowDroid
	 */
	public Set<String> getSinks() {
		return this.sourceSinkProvider == null ? null
				: this.sourceSinkProvider.getSinks();
	}
	
	/**
	 * Gets the concrete instances of sinks that have been collected inside
	 * the app. This method returns null if source and sink logging has not
	 * been enabled (see InfoflowConfiguration.setLogSourcesAndSinks()).
	 * @return The set of concrete sink instances in the app
	 */
	public Set<Stmt> getCollectedSinks() {
		return collectedSinks;
	}

	/**
	 * Prints the list of sinks registered with FlowDroud to stdout
	 */
	public void printSinks() {
		if (this.sourceSinkProvider == null) {
			System.err.println("Sinks not calculated yet");
			return;
		}
		System.out.println("Sinks:");
		for (String am : getSinks()) {
			System.out.println(am.toString());
		}
		System.out.println("End of Sinks");
	}

	/**
	 * Gets the set of sources loaded into FlowDroid. These are the sources as
	 * they are defined through the SourceSinkManager.
	 * 
	 * @return The set of sources loaded into FlowDroid
	 */
	public Set<String> getSources() {
		return this.sourceSinkProvider == null ? null
				: this.sourceSinkProvider.getSources();
	}
	
	/**
	 * Gets the concrete instances of sources that have been collected inside
	 * the app. This method returns null if source and sink logging has not
	 * been enabled (see InfoflowConfiguration.setLogSourcesAndSinks()).
	 * @return The set of concrete source instances in the app
	 */
	public Set<Stmt> getCollectedSources() {
		return collectedSources;
	}

	/**
	 * Prints the list of sources registered with FlowDroud to stdout
	 */
	public void printSources() {
		if (this.sourceSinkProvider == null) {
			System.err.println("Sources not calculated yet");
			return;
		}
		System.out.println("Sources:");
		for (String am : getSources()) {
			System.out.println(am.toString());
		}
		System.out.println("End of Sources");
	}

	/**
	 * Gets the set of classes containing entry point methods for the lifecycle
	 * 
	 * @return The set of classes containing entry point methods for the lifecycle
	 */
	public Set<String> getEntrypointClasses() {
		return entrypoints;
	}

	/**
	 * Prints list of classes containing entry points to stdout
	 */
	public void printEntrypoints() {
		if (this.entrypoints == null)
			System.out.println("Entry points not initialized");
		else {
			System.out.println("Classes containing entry points:");
			for (String className : entrypoints)
				System.out.println("\t" + className);
			System.out.println("End of Entrypoints");
		}
	}

	/**
	 * Sets the class names of callbacks.
	 *  If this value is null, it automatically loads the names from AndroidCallbacks.txt as the default behavior.
	 * @param callbackClasses
	 * 	        The class names of callbacks or null to use the default file.
	 */
	public void setCallbackClasses(Set<String> callbackClasses) {
		this.callbackClasses = callbackClasses;
	}

	public Set<String> getCallbackClasses() {
		return callbackClasses;
	}

	/**
	 * Sets the taint wrapper to be used for propagating taints over unknown (library) callees. If this value is null,
	 * no taint wrapping is used.
	 * 
	 * @param taintWrapper
	 *            The taint wrapper to use or null to disable taint wrapping
	 */
	public void setTaintWrapper(ITaintPropagationWrapper taintWrapper) {
		this.taintWrapper = taintWrapper;
	}

	/**
	 * Gets the taint wrapper to be used for propagating taints over unknown (library) callees. If this value is null,
	 * no taint wrapping is used.
	 * 
	 * @return The taint wrapper to use or null if taint wrapping is disabled
	 */
	public ITaintPropagationWrapper getTaintWrapper() {
		return this.taintWrapper;
	}

	
	/**
	 * Calculates the sets of sources, sinks, entry points, and callbacks methods for the given APK file.
	 * 
	 * @param sourcesAndSinks
	 *            A provider from which the analysis can obtain the list of
	 *            sources and sinks
	 * @throws IOException
	 *             Thrown if the given source/sink file could not be read.
	 * @throws XmlPullParserException
	 *             Thrown if the Android manifest file could not be read.
	 */
	public void calculateSourcesSinksEntrypoints(SrcSinkParser sourcesAndSinks)
			throws IOException, XmlPullParserException {
		// To look for callbacks, we need to start somewhere. We use the Android
		// lifecycle methods for this purpose.
		this.sourceSinkProvider = sourcesAndSinks;
		System.out.println("Entry point calculation done." + sourceSinkProvider.getSources().size());

		// Clean up everything we no longer need
		soot.G.reset();
        initializeSoot(true);

		// Create the SourceSinkManager
		{
			Set<SootMethodAndClass> callbacks = new HashSet<>();
			for (Set<SootMethodAndClass> methods : this.callbackMethods.values())
				callbacks.addAll(methods);
			
			List<String> sources = new ArrayList<>();
			for(String ssd : sourceSinkProvider.getSources()) {
				sources.add(ssd);
			}
			
			List<String> sinks = new ArrayList<>();
			for(String ssd : sourceSinkProvider.getSinks()) {
				sinks.add(ssd);
			}

			sourceSinkManager = new ThemisSourceSinkManager(
					sources, sinks);
		}

		entryPointCreator = createEntryPointCreator();
	}
	
	public void calculateSourcesSinksEntrypoints(String sourceSinkFile)
			throws IOException, XmlPullParserException {
		SrcSinkParser parser = null;

		String fileExtension = sourceSinkFile.substring(sourceSinkFile.lastIndexOf("."));
		fileExtension = fileExtension.toLowerCase();

		if (fileExtension.equals(".txt"))
			parser = SrcSinkParser.fromFile(sourceSinkFile);
		else
			throw new UnsupportedDataTypeException("The Inputfile isn't a .txt or .xml file.");

		calculateSourcesSinksEntrypoints(parser);
	}



	/**
	 * Creates the main method based on the current callback information, injects it into the Soot scene.
	 */
	private void createMainMethod() {
		// Always update the entry point creator to reflect the newest set
		// of callback methods
		SootMethod entryPoint = createEntryPointCreator().createDummyMain();
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
		if (Scene.v().containsClass(entryPoint.getDeclaringClass().getName()))
			Scene.v().removeClass(entryPoint.getDeclaringClass());
		Scene.v().addClass(entryPoint.getDeclaringClass());
		
		// addClass() declares the given class as a library class. We need to
		// fix this.
		entryPoint.getDeclaringClass().setApplicationClass();
	}

	/**
	 * Gets the source/sink manager constructed for FlowDroid. Make sure to call calculateSourcesSinksEntryPoints()
	 * first, or you will get a null result.
	 * 
	 * @return FlowDroid's source/sink manager
	 */
	public ThemisSourceSinkManager getSourceSinkManager() {
		return sourceSinkManager;
	}
	
	/**
	 * Builds the classpath for this analysis
	 * @return The classpath to be used for the taint analysis
	 */
	private String getClasspath() {
		String classpath = forceAndroidJar ? androidJar
				: Scene.v().getAndroidJarPath(androidJar, apkFileLocation);
		if (this.additionalClasspath != null && !this.additionalClasspath.isEmpty())
			classpath += File.pathSeparator + this.additionalClasspath;
		logger.debug("soot classpath: " + classpath);
		System.out.println("Acutal classpath:" + classpath);
		return classpath;
	}

	/**
	 * Initializes soot for running the soot-based phases of the application metadata analysis
	 * @param constructCallgraph True if a callgraph shall be constructed, otherwise false
	 */
	private void initializeSoot(boolean constructCallgraph) {
//		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_whole_program(constructCallgraph);
		Options.v().set_process_dir(Collections.singletonList(apkFileLocation));
		if (forceAndroidJar)
			Options.v().set_force_android_jar(androidJar);
		else
			Options.v().set_android_jars(androidJar);
		Options.v().set_src_prec(Options.src_prec_apk_class_jimple);
		Options.v().set_keep_line_number(false);
		Options.v().set_keep_offset(false);
		
		// Set the Soot configuration options. Note that this will needs to be
		// done before we compute the classpath.
		if (sootConfig != null)
			sootConfig.setSootOptions(Options.v());
		
		Options.v().set_soot_classpath(getClasspath());
		Main.v().autoSetOptions();
		
		// Configure the callgraph algorithm
		if (constructCallgraph) {
			switch (config.getCallgraphAlgorithm()) {
			case AutomaticSelection:
			case SPARK:
				Options.v().setPhaseOption("cg.spark", "on");
				break;
			case GEOM:
				Options.v().setPhaseOption("cg.spark", "on");
				AbstractInfoflow.setGeomPtaSpecificOptions();
				break;
			case CHA:
				Options.v().setPhaseOption("cg.cha", "on");
				break;
			case RTA:
				Options.v().setPhaseOption("cg.spark", "on");
				Options.v().setPhaseOption("cg.spark", "rta:true");
				Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");
				break;
			case VTA:
				Options.v().setPhaseOption("cg.spark", "on");
				Options.v().setPhaseOption("cg.spark", "vta:true");
				break;
			default:
				throw new RuntimeException("Invalid callgraph algorithm");
			}
		}

		// Load whetever we need
		Scene.v().loadNecessaryClasses();
	}

	/**
	 * Runs the data flow analysis
	 * 
	 * @return The results of the data flow analysis
	 */
	public InfoflowResults runInfoflow() {
		return runInfoflow(null);
	}

	/**
	 * Runs the data flow analysis. Make sure to populate the sets of sources, sinks, and entry points first.
	 * 
	 * @param onResultsAvailable
	 *            The callback to be invoked when data flow results are available
	 * @return The results of the data flow analysis
	 */
	public InfoflowResults runInfoflow(ResultsAvailableHandler onResultsAvailable) {
		if (this.sourceSinkProvider == null)
			throw new RuntimeException("Sources and/or sinks not calculated yet");

		System.out.println("Running data flow analysis on " + apkFileLocation + " with " + getSources().size()
				+ " sources and " + getSinks().size() + " sinks...");
		Infoflow info;
		if (cfgFactory == null)
			info = new Infoflow(androidJar, forceAndroidJar, null,
					new DefaultPathBuilderFactory(config.getPathBuilder(),
							config.getComputeResultPaths()));
		else
			info = new Infoflow(androidJar, forceAndroidJar, cfgFactory,
					new DefaultPathBuilderFactory(config.getPathBuilder(),
							config.getComputeResultPaths()));

		//FIXME: replace it with the actual classpath.
		final String path = this.getClasspath();

		info.setTaintWrapper(taintWrapper);
		if (onResultsAvailable != null)
			info.addResultsAvailableHandler(onResultsAvailable);

		System.out.println("Starting infoflow computation...");
		info.setConfig(config);
		info.setSootConfig(sootConfig);
		
		if (null != ipcManager) {
			info.setIPCManager(ipcManager);
		}

//		info.computeInfoflow(apkFileLocation, path, entryPointCreator, sourceSinkManager);
		info.computeInfoflow(apkFileLocation, path, entrypoints.iterator().next(), sourceSinkManager);

		this.maxMemoryConsumption = info.getMaxMemoryConsumption();
		this.collectedSources = info.getCollectedSources();
		this.collectedSinks = info.getCollectedSinks();

		return info.getResults();
	}

	private AndroidEntryPointCreator createEntryPointCreator() {
		AndroidEntryPointCreator entryPointCreator = new AndroidEntryPointCreator(new ArrayList<String>(
				this.entrypoints));
		Map<String, List<String>> callbackMethodSigs = new HashMap<String, List<String>>();
		for (String className : this.callbackMethods.keySet()) {
			List<String> methodSigs = new ArrayList<String>();
			callbackMethodSigs.put(className, methodSigs);
			for (SootMethodAndClass am : this.callbackMethods.get(className))
				methodSigs.add(am.getSignature());
		}
		entryPointCreator.setCallbackFunctions(callbackMethodSigs);
		return entryPointCreator;
	}

	/**
	 * Gets the entry point creator used for generating the dummy main method emulating the Android lifecycle and the
	 * callbacks. Make sure to call calculateSourcesSinksEntryPoints() first, or you will get a null result.
	 * 
	 * @return The entry point creator
	 */
	public AndroidEntryPointCreator getEntryPointCreator() {
		return entryPointCreator;
	}
	
	/**
	 * Gets the extra Soot configuration options to be used when running the analysis
	 * 
	 * @return The extra Soot configuration options to be used when running the analysis, null if the defaults shall be
	 *         used
	 */
	public IInfoflowConfig getSootConfig() {
		return this.sootConfig;
	}

	/**
	 * Sets the extra Soot configuration options to be used when running the analysis
	 * 
	 * @param config
	 *            The extra Soot configuration options to be used when running the analysis, null if the defaults shall
	 *            be used
	 */
	public void setSootConfig(IInfoflowConfig config) {
		this.sootConfig = config;
	}

	/**
	 * Sets the factory class to be used for constructing interprocedural control flow graphs
	 * 
	 * @param factory
	 *            The factory to be used. If null is passed, the default factory is used.
	 */
	public void setIcfgFactory(BiDirICFGFactory factory) {
		this.cfgFactory = factory;
	}
	
	/**
	 * Gets the maximum memory consumption during the last analysis run
	 * 
	 * @return The maximum memory consumption during the last analysis run if available, otherwise -1
	 */
	public long getMaxMemoryConsumption() {
		return this.maxMemoryConsumption;
	}
	
	/**
	 * Gets the data flow configuration
	 * @return The current data flow configuration
	 */
	public InfoflowAndroidConfiguration getConfig() {
		return this.config;
	}
	
	/**
	 * Sets the data flow configuration
	 * @param config The new data flow configuration
	 */
	public void setConfig(InfoflowAndroidConfiguration config) {
		this.config = config;
	}
	
	public void setCallbackFile(String callbackFile) {
		this.callbackFile = callbackFile;
	}
	
}
