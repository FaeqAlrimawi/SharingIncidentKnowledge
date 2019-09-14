package ie.lero.spare.pattern_instantiation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.json.JSONObject;

import cyberPhysical_Incident.IncidentDiagram;
import environment.CyberPhysicalSystemPackage;
import environment.EnvironmentDiagram;
import ie.lero.spare.franalyser.utility.BigrapherHandler;
import ie.lero.spare.franalyser.utility.FileManipulator;
import ie.lero.spare.franalyser.utility.FileNames;
import ie.lero.spare.franalyser.utility.JSONTerms;
import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.ModelsHandler;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.pattern_extraction.IncidentPatternExtractor;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator.InstancesSaver;
import it.uniud.mads.jlibbig.core.Signature;
import it.uniud.mads.jlibbig.core.std.Bigraph;

public class IncidentPatternInstantiator {

	// private String xqueryFile = "etc/match_query.xq";

	// parallelism parameters
	// sets how many asset sets can run in parallel
	private int threadPoolSize = 1;

	// sets how many activities can be
	// analysed in parallel
	private int numberOfparallelActivities = 1;

	private ExecutorService executor;
	private ForkJoinPool mainPool = new ForkJoinPool();

	// waiting time for executor before firing an exception
	int maxWaitingTime = 7;
	TimeUnit timeUnit = TimeUnit.DAYS;

	// GUI
	IncidentPatternInstantiationListener listener;
	int incrementValue = 10; // for GUI progress bar
	boolean isSetsSelected = false;
	LinkedList<Integer> assetSetsSelected;

	// system initialisation (creating bigraph signature, loading states)
	// indication
	private boolean isSystemInitialised = false;

	// incident model file
	private String incidentModelFile;

	// incident model
	IncidentDiagram incidentModel;

	// system model file
	private String systemModelFile;

	// system model
	EnvironmentDiagram systemModel;

	// system handler
	private SystemInstanceHandler systemHandler;
	private TransitionSystem transitionSystem;

	// Logging
	private Logger logger;
	private boolean isPrintToScreen = true;
	private boolean isSaveLog = true;
	// private boolean dummy = true;

	private String outputFolder = ".";

	// used to hold the map between system classes and controls
	// key is the system class while the value are Controls (which should be
	// found in the .big file provided when analysing an incident pattern)
	// the first control is the one with the highest priority, if not found then
	// the next is used
	private Map<String, List<String>> assetControlMap;
	String[] incidentAssetNames;
	LinkedList<String[]> combinations;

	// BRS executor
	SystemExecutor brsExecutor;

	private void runLogger() {

		logger = new Logger();

		logger.setListener(listener);
		logger.setPrintToScreen(isPrintToScreen);
		logger.setSaveLog(isSaveLog);
		logger.setLogFolder(outputFolder + "/log");

		logger.createLogFile();

		logger.start();

	}

	/**
	 * Maps given incident pattern to the given system model. Bigraph
	 * representation of the system and generated states are considered to have
	 * the same name as the system model file name
	 * 
	 * @param incidentPatternFile
	 *            incident pattern file path
	 * @param systemModelFile
	 *            system model file path
	 */
	public void execute(String incidentPatternFile, String systemModelFile) {

		// brs output folder (containing states) has the same name as the system
		// model file name
		String BRS_outputFolder = systemModelFile.substring(0, systemModelFile.lastIndexOf("."));

		// brs file has the same name as the system model file name but with
		// .big extension instead of .cps
		String BRS_file = BRS_outputFolder + ".big";

		execute(incidentPatternFile, systemModelFile, BRS_file, BRS_outputFolder, null);
	}

	/**
	 * Maps given incident pattern to the given system model. Bigraph
	 * representation of the system and generated states are considered to have
	 * the same name as the system model file name
	 * 
	 * @param incidentPatternFile
	 *            incident pattern file path
	 * @param systemModelFile
	 *            system model file path
	 * @param GUIlistener
	 *            a GUI listener used to send messages to the GUI
	 */
	public void execute(String incidentPatternFile, String systemModelFile,
			IncidentPatternInstantiationListener GUIlistener) {

		// brs output folder (containing states) has the same name as the system
		// model file name
		String BRS_outputFolder = systemModelFile.substring(0, systemModelFile.lastIndexOf("."));

		// brs file has the same name as the system model file name but with
		// .big extension instead of .cps
		String BRS_file = BRS_outputFolder + ".big";

		execute(incidentPatternFile, systemModelFile, BRS_file, BRS_outputFolder, GUIlistener);
	}

	/**
	 * Maps given incident pattern to the given system model.
	 * 
	 * @param incidentPatternFile
	 *            incident pattern file path
	 * @param systemModelFile
	 *            system model file path
	 * @param BRS_file
	 *            Bigraphical Reactive System representation file path
	 * @param BRS_statesFolder
	 *            folder containing generated states using BRS
	 * @param GUIlistener
	 *            a GUI listener used to send messages to the GUI
	 */
	public void execute(String incidentPatternFile, String systemModelFile, String BRS_file, String BRS_statesFolder,
			IncidentPatternInstantiationListener GUIlistener) {

		listener = GUIlistener;

		executeScenario(incidentPatternFile, systemModelFile, BRS_file, BRS_statesFolder);
		// String xQueryMatcherFile = xqueryFile;

		// try {
		//
		// // System.out.println(BRS_statesFolder);
		// // currently creates a folder named "log" where the states folder is
		// outputFolder = BRS_statesFolder.substring(0,
		// BRS_statesFolder.lastIndexOf(File.separator));
		//
		// runLogger();
		//
		// // XqueryExecuter.SPACE_DOC = systemModelFile;
		// // XqueryExecuter.INCIDENT_DOC = incidentPatternFile;
		//
		// // add the models to the ModelsHandler class (which can be used by
		// // other objects like the Mapper to
		// // access the models
		// ModelsHandler.addIncidentModel(incidentPatternFile);
		// ModelsHandler.addSystemModel(systemModelFile);
		//
		// // StopWatch timer = new StopWatch();
		//
		// logger.putMessage("////Executing Scenario1\\\\\\\\");
		// logger.putMessage("*Incident pattern file \"" + incidentPatternFile +
		// "\"");
		// logger.putMessage("*System model file \"" + systemModelFile + "\"");
		// logger.putMessage("*BRS file \"" + BRS_file + "\" & states folder \""
		// + BRS_statesFolder + "\"");
		//
		// // start a timer
		// // timer.start();
		//
		// //// start executing the scenario \\\\
		// Mapper m = new Mapper();
		//
		// logger.putMessage(">>Matching incident pattern entities to system
		// assets");
		//
		// AssetMap am = m.findMatches();
		//
		// // if there are incident assets with no matches from space model
		// // then exit
		// if (am == null || am.hasEntitiesWithNoMatch()) {
		// if (am != null) {
		// logger.putMessage(">>Some incident entities have no matches in the
		// system assets. These are:");
		// // getIncidetnAssetWithNoMatch method has some issues
		// List<String> asts = am.getIncidentAssetsWithNoMatch();
		// logger.putMessage(asts.toString());
		// } else {
		// logger.putMessage(
		// ">>Some incident entities have no matches in the system assets.
		// Exiting execution.");
		// }
		//
		// return; // execution stops if there are incident entities with
		// // no matching
		// }
		//
		// // print matched assets
		// logger.putMessage(">>Number of Assets (also entities) = " +
		// am.getIncidentEntityNames().length);
		// logger.putMessage(">>Incident entities order: " +
		// Arrays.toString(am.getIncidentEntityNames()));
		// logger.putMessage(">>Entity-Asset map:");
		// logger.putMessage(am.toString());
		// logger.putMessage(">>Generating asset sets..");
		//
		// listener.updateAssetMapInfo(am.toStringCompact());
		//
		// // generate sequences
		// boolean isStrict = true;
		// LinkedList<String[]> lst = am.generateUniqueCombinations(isStrict);
		//
		// listener.updateProgress(10);
		// listener.updateAssetSetInfo(lst);
		//
		// // checks if there are sequences generated or not. if not, then
		// // execution is terminated
		// // this can be loosened to allow same asset to be mapped to two
		// // entities
		// if (lst == null || lst.isEmpty()) {
		// logger.putMessage(">>No combinations found. Terminating execution");
		// return;
		// }
		//
		// logger.putMessage(">>Asset sets (" + lst.size() + "):");
		//
		// // print the sets only if there are less than 200. Else, print a 100
		// // but save the rest to a file
		// boolean oldIsPrintToScreen = isPrintToScreen;
		//
		// // print the sets only if there are less than 200. Else, print a 100
		// // but save the rest to a file
		// for (int i = 0; i < lst.size(); i++) {// adjust the length
		// if (isPrintToScreen && i >= 100) {
		// isPrintToScreen = false;
		// System.out.println("-... [See log file (" + logger.getLogFolder() +
		// "/" + logger.getLogFileName()
		// + ") for the rest]");
		// }
		// logger.putMessage("-Set[" + i + "]: " + Arrays.toString(lst.get(i)));
		// }
		//
		// isPrintToScreen = oldIsPrintToScreen;
		//
		// logger.putMessage(
		// ">>Initialising the Bigraphical Reactive System (Loading states &
		// creating the state transition graph)...");
		//
		// // initialise the system (load states and transition system)
		// boolean isInitialised = initialiseBigraphSystem(BRS_file,
		// BRS_statesFolder);
		//
		// if (isInitialised) {
		// logger.putMessage(">>Initialisation completed successfully");
		// } else {
		// logger.putMessage(">>Initialisation was NOT completed successfully.
		// Execution is terminated");
		// }
		//
		// logger.putMessage(">>Number of States= " +
		// transitionSystem.getNumberOfStates());
		// // logger.putMessage(">>State Transitions:");
		// //
		// logger.putMessage(TransitionSystem.getTransitionSystemInstance().getDigraph().toString());
		//
		// PotentialIncidentInstance[] incidentInstances = new
		// PotentialIncidentInstance[lst.size()];
		//
		// String[] incidentAssetNames = am.getIncidentEntityNames();
		//
		// while (!isSetsSelected) {
		// // wait user input
		// Thread.sleep(100);
		// }
		//
		// if (assetSetsSelected.size() > 0) {
		// incrementValue = (int) Math.ceil(90.0 / assetSetsSelected.size());
		// }
		//
		// // create threads that handle each sequence generated from asset
		// // matching
		// executor = Executors.newFixedThreadPool(threadPoolSize);
		//
		// logger.putMessage(">>Creating [" + assetSetsSelected.size() + "]
		// threads for asset sets. [" + threadPoolSize
		// + "] thread(s) are running in parallel.");
		//
		// for (int i = 0; i < assetSetsSelected.size(); i++) {// adjust the
		// // length
		// incidentInstances[i] = new
		// PotentialIncidentInstance(lst.get(assetSetsSelected.get(i)),
		// incidentAssetNames, i);
		// executor.submit(incidentInstances[i]);
		// }
		//
		// try {
		// executor.shutdown();
		//
		// // if it returns false then maximum waiting time is reached
		// if (!executor.awaitTermination(maxWaitingTime, timeUnit)) {
		// logger.putMessage("Time out! tasks took more than specified maximum
		// time [" + maxWaitingTime + " "
		// + timeUnit + "]");
		// }
		//
		// mainPool.shutdown();
		//
		// // if it returns false then maximum waiting time is reached
		// if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
		// logger.putMessage("Time out! saving instances took more than
		// specified maximum time ["
		// + maxWaitingTime + " " + timeUnit + "]");
		// }
		//
		// } catch (InterruptedException e) {
		//
		// e.printStackTrace();
		// }
		//
		// // timer.stop();
		//
		// // long timePassed = timer.getEllapsedMillis();
		// // int hours = (int) (timePassed / 3600000) % 60;
		// // int mins = (int) (timePassed / 60000) % 60;
		// // int secs = (int) (timePassed / 1000) % 60;
		// // int secMils = (int) timePassed % 1000;
		//
		// logger.putMessage("////Execution finished\\\\\\\\");
		// // execution time
		// // logger.putMessage("Execution time: " + timePassed + "ms [" +
		// // hours + "h:" + mins + "m:" + secs + "s:"
		// // + secMils + "ms]");
		//
		// // logger.putMessage(Logger.terminatingString);
		// logger.terminateLogging();
		//
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	/**
	 * This method intialises the BRS system by executing the BRS file, then
	 * loading states as Bigraph objects
	 * 
	 * @param BRSFileName
	 *            The BRS file describing the system components and its
	 *            evolution
	 * @param outputFolder
	 *            Output folder
	 * @return
	 */
	private boolean initialiseBigraphSystem(String BRSFileName, String outputFolder) {

		systemHandler = new SystemInstanceHandler();
		// create a handler for bigrapher tool
		brsExecutor = new BigrapherHandler(BRSFileName, outputFolder);

		// read states from the output folder then create Bigraph signature and
		// convert states from JSON objects to Bigraph (from LibBig library)
		// objects
		systemHandler.setLogger(logger);
		systemHandler.setExecutor(brsExecutor);

		boolean isDone = systemHandler.analyseBRS();

		if (isDone) {
			transitionSystem = systemHandler.getTransitionSystem();
			// add to the list of system handlers for other objects to access
			SystemsHandler.addSystemHandler(systemHandler);
		}

		return isDone;
	}

	private void executeLeroScenario() {

		String interruptionPattern = "D:/Bigrapher data/incident patterns/infectWithMalware-pattern.cpi";
		String dataCollectionPattern = "D:/Bigrapher data/incident patterns/collectData-pattern.cpi";

		String NIIncidentInstance = "D:/Bigrapher data/NII/incident instances/incidentInstance_steal.cpi";
		String NIIgeneratedIncidentPattern = "D:/Bigrapher data/NII/incident instances/incidentInstance_steal_abstract.cpi";
		String NIIgeneratedIncidentPattern_ubuntu = "/home/faeq/Desktop/NII/incident instances/incidentInstance_steal_abstract.cpi";

		String leroSystemModel = "D:/Bigrapher data/lero/lero.cps";
		String leroSystemModel_ubuntu = "/home/faeq/Desktop/lero/lero.cps";

		String NIISystemModel = "D:/Bigrapher data/NII/NII_ext.cps";
		String NIISystemModel_ubuntu = "/home/faeq/Desktop/NII/NII_ext.cps";

		String systemModelFile = NIISystemModel;
		String incidentPatternFile = dataCollectionPattern;

		executeScenario(incidentPatternFile, systemModelFile);
	}

	private void executeScenarioFromConsole() {

		Scanner scanner = new Scanner(System.in);

		System.out.println("Enter Incident pattern file path:");
		String incidentPatternFile = scanner.nextLine();

		System.out.println(
				"Enter System model file path (*.big file and output folder containing states should have the same name as the system):");
		String systemModelFile = scanner.nextLine();

		executeScenario(incidentPatternFile, systemModelFile);
	}

	private void executeScenario(String incidentPatternFile, String systemModelFile) {

		// brs output folder (containing states) has the same name as the system
		// model file name
		String BRS_outputFolder = systemModelFile.substring(0, systemModelFile.lastIndexOf("."));

		// brs file has the same name as the system model file name but with
		// .big extension instead of .cps
		String BRS_file = BRS_outputFolder + ".big";

		executeScenario(incidentPatternFile, systemModelFile, BRS_file, BRS_outputFolder);
	}

	protected void executeScenario(String incidentPatternFile, String systemModelFile, String BRS_file,
			String BRS_outputFolder) {

		long startTime = Calendar.getInstance().getTimeInMillis();

		this.systemModelFile = systemModelFile;
		this.incidentModelFile = incidentPatternFile;

		try {

			// currently creates a folder named "log" where the states folder is
			// int count = 10000;
			// while(BRS_outputFolder.contains("\\") && count>0) {
			// BRS_outputFolder = BRS_outputFolder.replace("\\",
			// File.separator);
			// count--;
			// }

			if (BRS_outputFolder.contains("\\")) {
				outputFolder = BRS_outputFolder.substring(0, BRS_outputFolder.lastIndexOf("\\"));
			} else if (BRS_outputFolder.contains("/")) {
				outputFolder = BRS_outputFolder.substring(0, BRS_outputFolder.lastIndexOf("/"));
			} else {
				outputFolder = BRS_outputFolder.substring(0, BRS_outputFolder.lastIndexOf(File.separator));
			}

			// create output folder
			File tmp = new File(outputFolder + "/output");

			if (!tmp.exists()) {
				tmp.mkdir();
			}

			// set attributes for logger and run it
			runLogger();

			logger.putMessage("#########Executing Scenario#########");
			logger.putMessage("*Incident pattern file \"" + incidentPatternFile + "\"");
			logger.putMessage("*System model file \"" + systemModelFile + "\"");
			logger.putMessage("*BRS file \"" + BRS_file + "\" & states folder \"" + BRS_outputFolder + "\"");

			// memory used
			Runtime runtime = Runtime.getRuntime();

			// Run the garbage collector
			runtime.gc();
			// Calculate the used memory
			long memory = runtime.totalMemory() - runtime.freeMemory();
			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Memory used at the start: " + memory + "Bytes");

			// start a timer to see how long it takes for the whole execution to
			// finish
			// timer.start();

			//// start executing the scenario \\\\
			Mapper m = new Mapper();

			// finds components in a system representation (space.xml) that
			// match the entities identified in an incident (incident.xml)
			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Matching incident pattern entities to system assets");

			// add the models to the ModelsHandler class (which can be used by
			// other objects like the Mapper to
			// access the models
			incidentModel = ModelsHandler.addIncidentModel(incidentPatternFile);
			systemModel = ModelsHandler.addSystemModel(systemModelFile);

			/**
			 * finding matches also can be accomplished using Xquery (but more
			 * // strict criteria is applied) AssetMap am =
			 * m.findMatchesUsingXquery(xqueryFilePath);
			 **/
			AssetMap am = m.findMatches(incidentModel, systemModel);

			// if there are incident assets with no matches from space model
			// then exit
			List<String> asts = am.getIncidentAssetsWithNoMatch();
			if (asts == null || !asts.isEmpty()) {
				logger.putError(Logger.SEPARATOR_BTW_INSTANCES
						+ "Some incident entities have no matches in the system assets. These are:");
				logger.putError(asts.toString());

				// execution stops if there are incident entities with
				// no matching
				return;
			}

			// print matched assets
			logger.putMessage(
					Logger.SEPARATOR_BTW_INSTANCES + "Incident Entities =  " + am.getIncidentEntityNames().length);
			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Incident entities order: "
					+ Arrays.toString(am.getIncidentEntityNames()));
			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Entity-Asset map:");
			logger.putMessage(am.toString());
			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Generating asset sets..");

			if (listener != null) {
				listener.updateAssetMapInfo(am.toStringCompact());
			}

			// generate sequences. If isStrict is false then generated sequences
			// is only based on having unique sets with unique assets mapped to
			// an entities in each set. if isStrict is set to true, then
			// relationships between entities in the incident pattern are
			// considered when generating sequences
			boolean isStrict = true;
			combinations = am.generateUniqueCombinations(isStrict, incidentModel, systemModel);

			if (listener != null) {
				listener.updateProgress(10);
				listener.updateAssetSetInfo(combinations);
			}

			// checks if there are sequences generated or not. if not, then
			// execution is terminated
			// this can be loosened to allow same asset to be mapped to two
			// entities
			if (combinations == null || combinations.isEmpty()) {
				logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "No combinations found. Terminating execution");
				return;
			}

			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Number of Asset Sets generated = " + combinations.size()
					+ " Sets");
			logger.putMessage("Incident entity set:" + Arrays.toString(am.getIncidentEntityNames()));

			for (int i = 0; i < combinations.size(); i++) {
				logger.putMessage("-Set[" + i + "]: " + Arrays.toString(combinations.get(i)));
			}

			/** Initialise system **/
			if (!isSystemInitialised) {
				logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES
						+ "Initialising the Bigraphical Reactive System (Loading states & creating the state transition graph)...");

				// initialise BRS system
				isSystemInitialised = initialiseBigraphSystem(BRS_file, BRS_outputFolder);

				if (isSystemInitialised) {
					logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Initialisation completed successfully");
				} else {
					logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES
							+ "Initialisation was NOT completed successfully. Execution is terminated");
					return;
				}
			}

			// logger.putMessage(transitionSystem.getDigraph().toString());

			logger.putMessage(
					Logger.SEPARATOR_BTW_INSTANCES + "Number of States= " + transitionSystem.getNumberOfStates());

			/***************/

			// create a new transition file with labels
			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Labelling transition system...");

			String[] actionNames = brsExecutor != null ? brsExecutor.getActionNames() : null;

			String outputFile = createNewLabelledTransitionFile(actionNames);
			//
			if (outputFile != null) {
				logger.putMessage(
						Logger.SEPARATOR_BTW_INSTANCES + "New Labelled transitions is created: " + outputFile);
			} else {
				logger.putError(Logger.SEPARATOR_BTW_INSTANCES + "Failed to create a new labelled transition file");
			}

			// load systemClass-Control map
			loadAssetControlMap(BRS_file);

			// check gui
			int size = 0;

			if (listener != null) {
				while (!isSetsSelected) {
					// wait user input
					Thread.sleep(100);
				}

				if (assetSetsSelected.size() > 0) {
					incrementValue = (int) Math.ceil(90.0 / assetSetsSelected.size());
					size = assetSetsSelected.size();
				}
			} else {
				size = combinations.size();
			}
			// create threads that handle each sequence generated from asset
			// matching
			executor = Executors.newFixedThreadPool(threadPoolSize);

			PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[size];

			incidentAssetNames = am.getIncidentEntityNames();

			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Creating threads for asset sets. [" + threadPoolSize
					+ "] thread(s) are running in parallel.");

			List<Future<Integer>> instances = new LinkedList<Future<Integer>>();

			// print separator
			logger.putSeparator();

			// Run the garbage collector
			runtime.gc();
			// Calculate the used memory
			memory = runtime.totalMemory() - runtime.freeMemory();
			logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Memory before executing sets: " + memory + "Bytes");

			if (listener == null) {// no gui
				for (int i = 0; i < combinations.size(); i++) {// adjust the
																// length
					incidentInstances[i] = new PotentialIncidentInstance(combinations.get(i), incidentAssetNames, i);
					instances.add(executor.submit(incidentInstances[i]));
				}
			} else {// with gui
				for (int i = 0; i < size; i++) {
					incidentInstances[i] = new PotentialIncidentInstance(combinations.get(assetSetsSelected.get(i)),
							incidentAssetNames, i);
					executor.submit(incidentInstances[i]);
				}
			}

			/** for testing **/
			// incidentInstances[1] = new PotentialIncidentInstance(lst.get(1),
			// incidentAssetNames, 1);
			// instances.add(executor.submit(incidentInstances[1]));
			// incidentInstances[2] = new PotentialIncidentInstance(lst.get(2),
			// incidentAssetNames, 2);
			// instances.add(executor.submit(incidentInstances[2]));

			// for (Future<Integer> fut : instances) {
			// if (fut != null && !fut.isDone()) {
			// fut.get();
			// }
			// }

			// no more tasks will be added so it will execute the submitted ones
			// and then terminate
			executor.shutdown();

			// if it returns false then maximum waiting time is reached
			if (!executor.awaitTermination(maxWaitingTime, timeUnit)) {
				logger.putError(
						Logger.SEPARATOR_BTW_INSTANCES + "Time out! tasks took more than specified maximum time ["
								+ maxWaitingTime + " " + timeUnit + "]");
			}

			mainPool.shutdown();

			// if it returns false then maximum waiting time is reached
			if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
				logger.putError(Logger.SEPARATOR_BTW_INSTANCES
						+ "Time out! saving instances took more than specified maximum time [" + maxWaitingTime + " "
						+ timeUnit + "]");
			}

			// calculate execution time
			long stopTime = Calendar.getInstance().getTimeInMillis();

			long duration = stopTime - startTime;

			int secMils2 = (int) duration % 1000;
			int hours2 = (int) (duration / 3600000) % 60;
			int mins2 = (int) (duration / 60000) % 60;
			int secs2 = (int) (duration / 1000) % 60;

			logger.putMessage("################## Execution Completed ##################");

			// execution time
			logger.putMessage("Execution time: " + duration + "ms [" + hours2 + "h:" + mins2 + "m:" + secs2 + "s:"
					+ secMils2 + "ms]");

			// clear models and system states
			clearData();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (logger != null) {
				logger.terminateLogging();
			}

			if (executor != null && !executor.isShutdown()) {
				executor.shutdownNow();
			}

			if (mainPool != null && !mainPool.isShutdown()) {
				mainPool.shutdownNow();
			}

		}

	}

	public String createNewLabelledTransitionFile(String[] actionNames) {

		LabelExtractor ext = new LabelExtractor(systemHandler);
		ext.updateDigraphLabels(actionNames);
		return ext.createNewLabelledTransitionFile();
	}

	public void generateAssetControlMap() {

		Method[] packageMethods = CyberPhysicalSystemPackage.class.getDeclaredMethods();

		Map<String, List<String>> classMap = new HashMap<String, List<String>>();

		String className = null;

		for (Method mthd : packageMethods) {

			className = mthd.getName();
			Class cls = mthd.getReturnType();

			// only consider EClass as the classes
			if (!cls.getSimpleName().equals("EClass")) {
				continue;
			}

			// remove [get] at the beginning
			// if it contains __ then it is not a class its an attribute
			if (className.startsWith("get")) {
				className = className.replace("get", "");
			}

			// create a class from the name
			String fullClassName = "environment.impl." + className + "Impl";

			int numOfLevels = 100;

			try {

				Class potentialClass = Class.forName(fullClassName);

				List<String> classHierarchy = new LinkedList<String>();
				int cnt = 0;

				// System.out.println("generated class:
				// "+potentialClass.getSimpleName());

				do {
					classHierarchy.add(potentialClass.getSimpleName().replace("Impl", ""));
					potentialClass = potentialClass.getSuperclass();
					cnt++;
				} while (potentialClass != null && !potentialClass.getSimpleName().equals("Container")
						&& cnt < numOfLevels);

				// add new entry to the map
				classMap.put(className, classHierarchy);

			} catch (ClassNotFoundException e) {

				// if there's no such class then continue
				continue;
			}
		}

		StringBuilder str = new StringBuilder();

		str.append("{\"systemclass-control-map\":[");
		for (Entry<String, List<String>> entry : classMap.entrySet()) {

			str.append("{\"class-name\":").append(entry.getKey()).append(", \"controls\":[");
			for (String nm : entry.getValue()) {
				str.append("\"").append(nm).append("\",");
			}
			// remove comma
			str.deleteCharAt(str.length() - 1);
			str.append("]},");
			// System.out.println(entry.getKey() + "::" +
			// Arrays.toString(entry.getValue().toArray()));

		}

		str.deleteCharAt(str.length() - 1);
		str.append("]}");

		JSONObject obj = new JSONObject(str.toString());

		String fileName = "AssetControl_map.json";

		File file = new File(fileName);

		try (final BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
			writer.write(obj.toString(4));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void clearData() {

		// // Run the garbage collector
		// Runtime runtime = Runtime.getRuntime();
		//
		// runtime.gc();
		// // Calculate the used memory
		// long memory = runtime.totalMemory() - runtime.freeMemory();

		// logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Used memory
		// before: " + memory + "Bytes");

		if (mainPool != null && !mainPool.isShutdown()) {
			mainPool.shutdownNow();
		}

		if (executor != null && !executor.isShutdown()) {
			executor.shutdownNow();
		}

		// clear system data
		Map<Integer, Bigraph> tmp = systemHandler.getStates();
		if (tmp != null) {
			tmp.clear();
			tmp = null;
		}

		SystemsHandler.removeSystemHandler(systemHandler.getSysID());

		systemHandler = null;
		transitionSystem = null;

		// remove models
		ModelsHandler.removeIncidentModel(incidentModelFile);
		ModelsHandler.removeSystemModel(systemModelFile);

		systemModel = null;
		incidentModel = null;

		// runtime.gc();
		// memory = runtime.totalMemory() - runtime.freeMemory();
		//
		// logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "Used memory
		// after: " + memory + "Bytes");

	}

	protected void loadAssetControlMap(String systemFileName) {

		assetControlMap = new HashMap<String, List<String>>();

		List<String> unMatchedControls = new LinkedList<String>();
		Signature signature = systemHandler.getGlobalBigraphSignature();

		InputStream systemControlMapFileName = IncidentPatternInstantiator.class.getClassLoader()
				.getResourceAsStream("ie/lero/spare/resources/" + FileNames.ASSET_CONTROL_MAP);
		// .getResource("ie/lero/spare/resources/" +
		// FileNames.ASSET_CONTROL_MAP);

		if (systemControlMapFileName == null) {
			logger.putError("System to Control map file [" + FileNames.ASSET_CONTROL_MAP + "] is not found");
			return;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(systemControlMapFileName));

		// String path = systemControlMapFileName.getPath();
		// String[] lines = FileManipulator.readFileNewLine(path);

		String assetClass = null;
		String[] controlNames = null;
		String[] tmp;
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				tmp = line.split(FileNames.ASSET_CONTROL_SEPARATOR);

				if (tmp.length < 2) {
					continue;
				}

				assetClass = tmp[0]; // system class
				controlNames = tmp[1] != null ? tmp[1].split(FileNames.CONTROLS_SEPARATOR) : null; // bigrapher
																									// controls
				String primariyControl = controlNames.length > 0 ? controlNames[0] : null;

				if (signature != null && signature.getByName(primariyControl) == null) {
					unMatchedControls.add(primariyControl);
				}

				assetControlMap.put(assetClass, Arrays.asList(controlNames));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES + "SystemClass<->Control map is created.");
		if (!unMatchedControls.isEmpty()) {
			logger.putMessage(
					Logger.SEPARATOR_BTW_INSTANCES + "Some Primariy Controls in the map have no equivalent in ("
							+ systemFileName + "):" + Arrays.toString(unMatchedControls.toArray()));
		}
	}

	public Map<String, List<String>> getAssetControlMap() {

		if (assetControlMap == null || assetControlMap.isEmpty()) {
			loadAssetControlMap(null);
		}

		return assetControlMap;
	}

	public boolean saveGeneratedTraces(int instanceID, List<GraphPath> traces, String outputFile) {

		InstancesSaver saver = null;
		String[] assetNames = null;
		String[] incidentEntityNames = null;

		if (listener != null) {

			if (instanceID >= 0 && assetSetsSelected != null && instanceID < assetSetsSelected.size()) {
				assetNames = combinations.get(assetSetsSelected.get(instanceID));
			}

			if (assetNames == null) {
				assetNames = new String[1];
				assetNames[0] = "";
			}

			if (incidentAssetNames == null) {
				incidentEntityNames = new String[1];
				incidentEntityNames[0] = "";
			} else {
				incidentEntityNames = incidentAssetNames;
			}

			saver = new InstancesSaver(instanceID, outputFile, incidentAssetNames, assetNames, traces);

			try {
				int res = mainPool.submit(saver).get();

				if (res == InstancesSaver.SUCCESSFUL) {
					return true;
				} else if (res == InstancesSaver.UNSUCCESSFUL) {
					return false;

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return true;
	}

	class PotentialIncidentInstance implements Callable<Integer> {

		private String[] systemAssetNames;
		private String[] incidentEntityNames;
		private int threadID;
		private String outputFileName;
		private String[] systemAssetControls;
		private String instanceName;
		public static final String INSTANCE_GLOABAL_NAME = "Instance";

		public PotentialIncidentInstance(String[] sa, String[] ie, int id) {

			systemAssetNames = Arrays.copyOf(sa, sa.length);
			incidentEntityNames = Arrays.copyOf(ie, ie.length);
			;
			threadID = id;
			instanceName = INSTANCE_GLOABAL_NAME + "[" + id + "]" + Logger.SEPARATOR_BTW_INSTANCES;
			// default output
			setOutputFileName(
					outputFolder + "/output/" + threadID + "_" + transitionSystem.getNumberOfStates() + ".json");
		}

		public PotentialIncidentInstance(String[] sa, String[] ia, int id, String outputFileName) {

			this(sa, ia, id);
			setOutputFileName(outputFileName);
		}

		@Override
		public Integer call() throws Exception {

			// timing
			long startTime = Calendar.getInstance().getTimeInMillis();

			GraphPathsAnalyser pathsAnalyser = null;

			try {

				// this object allows the conversion of incident activities
				// conditions into bigraphs
				// which later can be matched against states of the system (also
				// presented in Bigraph)
				boolean isSuccessful = updateAssetControls();

				if (!isSuccessful) {
					return -1;
				}

				PredicateGenerator predicateGenerator = new PredicateGenerator(systemAssetNames, incidentEntityNames,
						systemAssetControls, assetControlMap, logger, systemHandler, incidentModelFile);
				PredicateHandler predicateHandler = predicateGenerator.generatePredicates(incidentModel);

				StringBuilder str = new StringBuilder();

				str.append("[");
				for (int i = 0; i < systemAssetNames.length; i++) {
					str.append(systemAssetNames[i]).append(":").append(incidentEntityNames[i]).append(", ");
				}

				if (str.length() > 0) {
					str.deleteCharAt(str.length() - 1);
					str.deleteCharAt(str.length() - 1);
					str.append("]");
				}
				logger.putMessage(instanceName + "Mapping asset set :" + str.toString());
				logger.putMessage(instanceName + "Identifying states and their transitions...");

				/**
				 * this object identifies states and state transitions that
				 * satisfy the conditions of activities state transitions are
				 * updated in the predicates, which can be accessed through
				 * predicateHandler
				 **/
				BigraphAnalyser analyser = new BigraphAnalyser(predicateHandler, threadID, logger, systemHandler);

				// set logger for bigraph analyser
				// analyser.setLogger(logger);

				// set the number of activities to execute in parallel
				analyser.setNumberofParallelActivity(getNumberOfParallelActivities());

				// analyser.setThreshold(matchingThreshold);

				// identify states that satisfy the pre-/post-conditions of each
				// activity. Default Execution of analyse will use number of
				// threads equal to the number of
				// available processes for matching states. To set number of
				// threads use analyse(NumberOfThreads)
				analyser.analyse();

				// for GUI
				if (listener != null) {
					listener.updateProgress(incrementValue / 3);
				}

				// print all possible state transitions satisfying conditions
				/*
				 * if(!predicateHandler.areAllSatisfied()){
				 * logger.putMessage("Thread["+
				 * threadID+"]>>Activities are not satisfied:" +
				 * predicateHandler.getActivitiesNotSatisfied());
				 * logger.putMessage("Thread["+threadID+"]>>Terminating thread"
				 * ); threadWriter.close(); return; }
				 */

				/**
				 * how to represent all possible paths to the given sequence of
				 * assets? incidentpath can be used to hold one path, but now it
				 * is holding everything IncidentPath inc = new
				 * IncidentPath(predicateHandler);
				 **/
				// inc.generateDistinctPaths();

				/**
				 * this gives details about the states and their transitions
				 * that satisfy the conditions of each activity it prints
				 * transitions between pre and post within one activity, post of
				 * current to pre of next activity, pre of current to pre of
				 * next
				 **/
				// predicateHandler.printAll();

				// one way to find all possible paths between activities is to
				// find all transitions from the precondition of the initial
				// activity to the postconditions of the final activity
				logger.putMessage(instanceName + "Generating potential incident instances...");

				// LinkedList<GraphPath> paths =
				// predicateHandler.getPathsBetweenActivities(predicateHandler.getInitialActivity(),
				// predicateHandler.getFinalActivity());
				// LinkedList<GraphPath> paths = predicateHandler.getPaths();

				// using threading to find transitions. insure that prallelism
				// will not cause problems!
				// (might have some issues, more testing is required)
				List<GraphPath> paths = predicateHandler.findTransitions(threadID);

				// updated gui
				if (listener != null) {
					listener.updateProgress(incrementValue / 3);
				}

				// save and analyse generated paths there are any
				if (paths != null && paths.size() > 0) {

					// // create and run an instance saver to store instances to
					// a
					// // file
					InstancesSaver saver = new InstancesSaver(threadID, outputFileName, incidentEntityNames,
							systemAssetNames, paths);
					mainPool.submit(saver);

					// for(GraphPath path : paths) {
					// logger.putMessage(path.toPrettyString(transitionSystem));
					// }
					//
					// logger.putMessage(instanceName + "Analysing [" +
					// paths.size()
					// + "] of generated potential incident instances...");
					//
					// /** Analyse generated transitions **/
					// // create an analysis object for the identified paths
					pathsAnalyser = new GraphPathsAnalyser(paths, transitionSystem, threadID, logger);
					// String result = pathsAnalyser.analyse();
					//
					// logger.putMessage(result);
					//
					// // save analysis result
					// String jsonStr = pathsAnalyser.convertToJSONStr();
					// String analyseFileName = outputFolder + "/output/" +
					// threadID + "_analysis_"
					// + transitionSystem.getNumberOfStates() + ".json";
					// File threadFile = new File(analyseFileName);
					//
					// JSONObject obj = new JSONObject(jsonStr);
					//
					// if (!threadFile.exists()) {
					// threadFile.createNewFile();
					// }
					//
					// // write paths to a file
					// try (final BufferedWriter writer =
					// Files.newBufferedWriter(threadFile.toPath())) {
					// writer.write(obj.toString(4));
					// }

					// logger.putMessage(instanceName + "Analysis result is
					// stored in:" + analyseFileName);
					/**********************/

				} else {
					logger.putMessage(instanceName + "NO potential incident instances generated");

				}

				// print(pathsAnalyser.print());
				// another way is to combine the transitions found for each
				// activity from the initial one to the final one
				// predicateHandler.printAll();

				// System.out.println("\nThread["+threadID+"]>>Summary of the
				// incident pattern activities");
				// System.out.println(predicateHandler.getSummary());

				long endTime = Calendar.getInstance().getTimeInMillis();

				long timeLapsed = endTime - startTime;

				int hours = (int) (timeLapsed / 3600000) % 60;
				int mins = (int) (timeLapsed / 60000) % 60;
				int secs = (int) (timeLapsed / 1000) % 60;
				int secMils = (int) timeLapsed % 1000;
				String strTime = timeLapsed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]";
				// execution time
				logger.putMessage(instanceName + "Execution time: " + strTime);

				logger.putMessage(instanceName + "Finished Successfully");

				// memory used
				Runtime runtime = Runtime.getRuntime();

				// Run the garbage collector
				runtime.gc();
				// Calculate the used memory
				long memory = runtime.totalMemory() - runtime.freeMemory();

				logger.putMessage(instanceName + "Used memory: " + memory + "Bytes");

				// print separator
				logger.putSeparator();

				if (listener != null) {
					listener.updateProgress(incrementValue / 3 + incrementValue % 3);
					listener.updateResult(threadID, pathsAnalyser, getOutputFileName(), strTime);
				}

				return 0;

			} catch (Exception e) {
				e.printStackTrace();

				return -1;
			}
		}

		protected boolean updateAssetControls() {

			// EnvironmentDiagram systemModel =
			// ModelsHandler.getCurrentSystemModel();

			environment.Asset tmpAst;

			// holds the names of the assets that their classes have no control
			// map in the .txt file
			List<String> unMatchedAssets = new LinkedList<String>();

			// hold the controls that have don't exist in the .big file
			List<String> unMatchedControls = new LinkedList<String>();

			Signature sig = systemHandler.getGlobalBigraphSignature();

			boolean isSuccessful = true;

			systemAssetControls = new String[systemAssetNames.length];

			for (int i = 0; i < systemAssetNames.length; i++) {

				tmpAst = systemModel.getAsset(systemAssetNames[i]);

				if (tmpAst == null) {
					continue;
				}

				String className = tmpAst.getClass().getSimpleName().replace("Impl", "");
				List<String> controls = assetControlMap.get(className);

				if (controls == null || controls.isEmpty()) {
					unMatchedAssets.add(systemAssetNames[i] + "<<" + className + ">>");
					continue;
				}

				boolean isControlSet = false;

				for (String control : controls) {
					// identify control to use. Priority is set to be the order
					// 0 has the highest priority
					if (sig != null && sig.contains(control)) {
						systemAssetControls[i] = control;
						isControlSet = true;
						break;
					}
				}

				// if none of the class controls is found in the bigraph
				// signature
				// then add class to the unmatched assets
				if (!isControlSet) {
					unMatchedAssets.add(systemAssetNames[i] + "<<" + className + ">>");
				}

			}

			// if an asset class has no entry in the map of systemClass to
			// controls
			if (!unMatchedAssets.isEmpty()) {
				logger.putError(instanceName + "Some assets have no map to controls. These are:"
						+ Arrays.toString(unMatchedAssets.toArray()));
				isSuccessful = false;
			}

			// if (!unMatchedAssets.isEmpty()) {
			// logger.putError(instanceName + "Some Controls of asset Classes in
			// (" + systemControlMapFileName
			// + ") have no equivalent in the .big file. Asset Classes with no
			// control matches:"
			// + Arrays.toString(unMatchedAssets.toArray()));
			// isSuccessful = false;
			// }

			return isSuccessful;
		}

		public String[] getSystemAssetNames() {
			return systemAssetNames;
		}

		public void setSystemAssetNames(String[] systemAssetNames) {
			this.systemAssetNames = systemAssetNames;
		}

		public String[] getIncidentAssetNames() {
			return incidentEntityNames;
		}

		public void setIncidentAssetNames(String[] incidentAssetNames) {
			this.incidentEntityNames = incidentAssetNames;
		}

		public int getThreadID() {
			return threadID;
		}

		public void setThreadID(int threadID) {
			this.threadID = threadID;
		}

		public String getOutputFileName() {
			return outputFileName;
		}

		public void setOutputFileName(String outputFileName) {
			this.outputFileName = outputFileName;
		}

	}

	public boolean isSetsSelected() {
		return isSetsSelected;
	}

	public void setSetsSelected(boolean isSetsSelected) {
		this.isSetsSelected = isSetsSelected;
	}

	public LinkedList<Integer> getAssetSetsSelected() {
		return assetSetsSelected;
	}

	public void setAssetSetsSelected(LinkedList<Integer> assetSetsSelected) {
		this.assetSetsSelected = assetSetsSelected;
	}

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public int getNumberOfParallelActivities() {
		return numberOfparallelActivities;
	}

	public void setNumberOfParallelActivities(int parallelActivities) {
		this.numberOfparallelActivities = parallelActivities;
	}

	// public int getMatchingThreshold() {
	// return matchingThreshold;
	// }
	//
	// public void setMatchingThreshold(int matchingThreshold) {
	// this.matchingThreshold = matchingThreshold;
	// }

	public class InstancesSaver implements Callable<Integer> {

		private String outputFileName;
		private String[] systemAssetNames;
		private String[] incidentEntityNames;
		private List<GraphPath> paths;
		private int threadID;
		public static final String instanceSaverNameGLobal = "Instance-Saver";
		private String instanceSaverName;
		private BlockingQueue<String> instancesQ;

		public static final String INCIDENT_ENTITY_NAME = JSONTerms.INSTANCE_MAP_INCIDENT_ENTITY_NAME;
		public static final String SYSTEM_ASSET_NAME = JSONTerms.INSTANCE_MAP_SYSTEM_ASSET_NAME;
		public static final String POTENTIAL_INCIDENT_ISNTANCES = JSONTerms.INSTANCE_POTENTIAL;
		public static final String INSTNACES_COUNT = JSONTerms.INSTANCE_POTENTIAL_COUNT;
		public static final String INSTANCES = JSONTerms.INSTANCE_POTENTIAL_INSTANCES;
		public static final String MAP = "maps";

		public static final int SUCCESSFUL = 1;
		public static final int UNSUCCESSFUL = -1;

		public InstancesSaver(int threadID, String file, String[] entityNames, String[] astNames,
				List<GraphPath> paths) {

			this.threadID = threadID;
			outputFileName = file;
			incidentEntityNames = entityNames;
			systemAssetNames = astNames;
			this.paths = paths;
			instancesQ = new ArrayBlockingQueue<String>(4000);

			instanceSaverName = PotentialIncidentInstance.INSTANCE_GLOABAL_NAME + "[" + threadID + "]"
					+ Logger.SEPARATOR_BTW_INSTANCES + instanceSaverNameGLobal + Logger.SEPARATOR_BTW_INSTANCES;

			// mainPool = new ForkJoinPool();
		}

		@Override
		public Integer call() throws Exception {

			try {

				if (logger != null) {
					logger.putMessage(instanceSaverName + "Storing generated instances...");
				}

				File threadFile = new File(outputFileName);

				if (!threadFile.exists()) {
					threadFile.createNewFile();
				}

				StringBuilder jsonStr = new StringBuilder();

				jsonStr.append("{\"").append(MAP).append("\":[");

				for (int i = 0; i < systemAssetNames.length; i++) {
					jsonStr.append("{\"").append(INCIDENT_ENTITY_NAME).append("\":\"").append(incidentEntityNames[i])
							.append("\",").append("\"").append(SYSTEM_ASSET_NAME).append("\":\"")
							.append(systemAssetNames[i]).append("\"}");

					if (i < systemAssetNames.length - 1) {
						jsonStr.append(",");
					}

				}

				jsonStr.append("],");

				int size = paths.size();

				jsonStr.append("\"").append(POTENTIAL_INCIDENT_ISNTANCES).append("\":{").append("\"")
						.append(INSTNACES_COUNT).append("\":").append(size).append(",").append("\"").append(INSTANCES)
						.append("\":[");

				BufferedWriter writer = Files.newBufferedWriter(threadFile.toPath());

				// write meta information (# of instance, entity-asset map)
				writer.write(jsonStr.toString());
				writer.newLine();

				jsonStr.setLength(0);

				// if (mainPool == null) {
				// mainPool = new ForkJoinPool();
				// }

				ForkJoinTask<String> result = mainPool
						.submit(new GraphPathsToStringConverter(0, size, paths, instancesQ));

				// number of paritions =
				// #-of-transitions/Threshold
				int numOfPartitions = size / GraphPathsToStringConverter.THRESHOLD;

				// logger.putMessage(instanceSaverName + "Number of partitions =
				// " + numOfPartitions);

				int cnt = 0;

				while (cnt < numOfPartitions - 1) {

					// write chunck of instances (based on the threshold in the
					// graphPathsToStringConverter)
					String tmp = instancesQ.take();
					writer.write(tmp);
					writer.write(",");
					writer.newLine();
					// logger.putMessage(instanceSaverName+tmp);
					cnt++;

				}

				// get last chunck
				String tmp = instancesQ.take();
				writer.write(tmp);
				tmp = null;
				cnt++;

				if (result != null && result.isCompletedAbnormally()) {
					if (logger != null) {
						logger.putError(instanceSaverName + "Something went wrong while storing generated instances.");
					}
					writer.close();
					return UNSUCCESSFUL;
				}

				writer.write("]}}");

				if (logger != null) {
					logger.putMessage(
							instanceSaverName + "Instances are stored in file: " + threadFile.getAbsolutePath());
				}

				writer.close();

				Runtime.getRuntime().gc();

				return SUCCESSFUL;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return UNSUCCESSFUL;
			}
		}
	}

	class GraphPathsToStringConverter extends RecursiveTask<String> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		public static final int THRESHOLD = 100;
		private List<GraphPath> paths;
		// private StringBuilder result;
		private static final String INSTANCE_id = JSONTerms.INSTANCE_POTENTIAL_INSTANCES_ID;
		private BlockingQueue<String> queue;
		private int residue;
		public static final String GRAPH_PATHS_STRING = "Graph-Paths-String";
		private String instanceName;

		public GraphPathsToStringConverter(int start, int end, List<GraphPath> paths, BlockingQueue<String> q) {
			this.paths = paths;
			this.indexStart = start;
			this.indexEnd = end;
			queue = q;
			// result = new StringBuilder();
			residue = (int) Math.ceil(paths.size() % (THRESHOLD * 1.0));
			instanceName = GRAPH_PATHS_STRING;
		}

		@Override
		protected String compute() {

			if ((indexEnd - indexStart) > (THRESHOLD + residue)) {

				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<GraphPathsToStringConverter, String>() {

							@Override
							public String apply(GraphPathsToStringConverter arg0) {
								return null;
							}

						}).reduce(null, new BinaryOperator<String>() {

							@Override
							public String apply(String arg0, String arg1) {

								return arg0;
							}
						});

			} else {

				// logger.putMessage(instanceName+"Creating a part [" +
				// indexStart+", "+indexEnd+"]");
				StringBuilder result = new StringBuilder();
				for (int i = indexStart; i < indexEnd; i++) {

					result.append("{\"").append(INSTANCE_id).append("\":").append(i).append(",")
							.append(paths.get(i).toJSONCompact()).append("}");
					result.append(",");

				}

				result.deleteCharAt(result.length() - 1);

				try {

					queue.put(result.toString());
					result.setLength(0);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		}

		private Collection<GraphPathsToStringConverter> createSubTasks() {

			List<GraphPathsToStringConverter> dividedTasks = new LinkedList<GraphPathsToStringConverter>();

			int part = (indexEnd - indexStart) / THRESHOLD;

			for (int i = 0; i < part - 1; i++) {
				dividedTasks.add(new GraphPathsToStringConverter(indexStart, indexStart + THRESHOLD, paths, queue));
				indexStart += THRESHOLD;
			}

			dividedTasks.add(new GraphPathsToStringConverter(indexStart, indexEnd, paths, queue));

			return dividedTasks;

		}
	}

	public static void main(String[] args) {

		// IncidentPatternInstantiator ins = new IncidentPatternInstantiator();

		// ins.executeLeroScenario();

		// ins.executeScenarioFromConsole();
		// ins.executeScenario1();
		// ins.executeStealScenario();
		// ins.test1();

		// test
		// test();
//		executeFromPrompt();
		// lero10();
		// test100K();
	}

	public static void test() {

		// setting tests
		String interruptionPattern = "/home/faeq/Desktop/lero/int.cpi";

		String leroSystemModel = "/home/faeq/Desktop/lero/lero.cps";

		String BRS_file = "/home/faeq/Desktop/lero/lero.big";

		String interruptionPatternWin = "D:/Bigrapher data/incident patterns/collectData-pattern.cpi";

		String leroSystemModelWin = "D:/Bigrapher data/lero/lero.cps";

		String BRS_fileWin = "D:/Bigrapher data/lero/lero.big";

		String[] states = new String[10];

		for (int i = 0; i < states.length; i++) {

			states[i] = "/D:/Bigrapher data/lero/lero" + (i + 1);
		}

		for (int i = 0; i < 1; i++) {

			IncidentPatternInstantiator ins = new IncidentPatternInstantiator();
			ins.executeScenario(interruptionPatternWin, leroSystemModelWin, BRS_fileWin, states[i]);

			Runtime.getRuntime().gc();
			System.out.println("Complete...");
			System.out.println("\n\n");

			// wait 3 seconds
			// try {
			// Thread.sleep(3000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

		}

	}

	public static void test100K() {
		// setting tests
		String interruptionPattern = "/home/faeq/Desktop/lero/int.cpi";

		String leroSystemModel = "/home/faeq/Desktop/lero/lero.cps";

		String BRS_file = "/home/faeq/Desktop/lero/lero.big";
		String[] states = new String[10];
		String mainStatesFolder = "/home/faeq/Desktop/lero/lero100K/states";

		for (int i = 0; i < states.length; i++) {

			String statesFolder = mainStatesFolder + ((i + 1) * 10);

			IncidentPatternInstantiator ins = new IncidentPatternInstantiator();
			ins.executeScenario(interruptionPattern, leroSystemModel, BRS_file, statesFolder);

			// reset
			ins = null;
			ModelsHandler.clearAll();
			SystemsHandler.clearAll();

			// clear memory
			Runtime.getRuntime().gc();

			System.out.println("Complete...");
			System.out.println("\n\n");

			// wait 3 seconds
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public static void lero10() {

		// setting tests
		String interruptionPattern = "/home/faeq/Desktop/lero/int.cpi";

		String leroSystemModel = "/home/faeq/Desktop/lero/lero.cps";

		String BRS_file = "/home/faeq/Desktop/lero/lero.big";
		String states = "/home/faeq/Desktop/lero/lero10";

		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();
		ins.executeScenario(interruptionPattern, leroSystemModel, BRS_file, states);
		System.out.println("Complete...");
		System.out.println("\n\n");

	}

	
}
