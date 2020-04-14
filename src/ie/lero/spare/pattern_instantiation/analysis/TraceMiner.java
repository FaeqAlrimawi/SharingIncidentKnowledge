package ie.lero.spare.pattern_instantiation.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import com.beust.jcommander.internal.Lists;

import ca.pfv.spmf.algorithms.clustering.dbscan.AlgoDBSCAN;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoBisectingKMeans;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;
import ca.pfv.spmf.algorithms.clustering.optics.AlgoOPTICS;
import ca.pfv.spmf.algorithms.clustering.optics.DoubleArrayOPTICS;
import ca.pfv.spmf.algorithms.clustering.text_clusterer.TextClusterAlgo;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map;
import ca.pfv.spmf.algorithms.sequentialpatterns.occur.AlgoOccur;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoTKS;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.PatternTKS;
import ca.pfv.spmf.patterns.cluster.Cluster;
import ca.pfv.spmf.patterns.cluster.ClusterWithMean;
import cyberPhysical_Incident.Entity;
import ie.lero.spare.forensics.assessment.brs.parser.ActionWrapper;
import ie.lero.spare.forensics.assessment.brs.parser.BRSParser;
import ie.lero.spare.forensics.assessment.brs.parser.BRSWrapper;
import ie.lero.spare.forensics.assessment.brs.parser.BigraphWrapper;
import ie.lero.spare.forensics.assessment.brs.parser.utilities.BigraphNode;
import ie.lero.spare.forensics.assessment.brs.parser.utilities.JSONTerms;
//import controller.instantiation.analysis.TraceViewerController;
//import core.brs.parser.ActionWrapper;
//import core.brs.parser.BRSParser;
//import core.brs.parser.BRSWrapper;
//import core.brs.parser.BigraphWrapper;
//import core.brs.parser.utilities.JSONTerms;
//import core.utilities.Query;
//import cyberPhysical_Incident.Entity;
//import ie.lero.spare.franalyser.utility.BigraphNode;
//import ie.lero.spare.franalyser.utility.Digraph;
//import ie.lero.spare.franalyser.utility.TransitionSystem;
//import ie.lero.spare.franalyser.utility.FileManipulator;
//import ie.lero.spare.franalyser.utility.JSONTerms;
import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator.InstancesSaver;
import ie.lero.spare.pattern_instantiation.LabelExtractor;
import ie.lero.spare.utility.Digraph;
import ie.lero.spare.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.Site;

public class TraceMiner {

	// instances (traces). key is trace ID value is the trace as GraphPath
	// object
	Map<Integer, GraphPath> traces;

	String instanceFileName;
	String convertedInstancesFileName = "convertedInstances.txt";
	int numberOFClusters = 10;
	DistanceFunction distanceFunction;
	// AlgoKMeans kmean;

	// space is the separator
	public final static String DATA_SEPARATOR = ",";
	public final static String WEKA_DATA_SEPARATOR = ",";
	// cloud be the number of states
	public static int PADDING_STATE = -1;
	public static String PADDING_ACTION = "NULL";
	public static int PADDING_ACTION_INT = -1;
	// what value to give? probably larger would be better to get a noticable
	// difference
	public final static int ACTION_PERFORMED = 1;
	public final static int ACTION_NOT_PERFORMED = 0;

	// used for as a result for assessing causality between two actions
	public final static int CAUSALLY_DEPENDENT = 1;

	// this means that action (b) is not causally dependent on action (a) as
	// determined by the LTS and Bigraph matching
	public final static int NOT_CAUSALLY_DEPENDENT = 2;
	// this means that action (b) is not causally dependent on action (a) as
	// determined by Bigraph matching (not the LTS)
	public final static int POTENTIALLY_NOT_CAUSALLY_DEPENDENT = 3;
	// this means that action (b) [maybe] is not causally dependent on action
	// (a) as determined by Bigraph matching and the the LTS. It's maybe because
	// by Bigraph matching, it shows that applying action (a) would increase the
	// number of matches for action(b) i.e. triggering action (a) gives more
	// ways that action (b) can be triggered in the system
	public final static int NOT_CAUSALLY_DEPENDENT_BY_LTS = 4;

	public final static int NOT_NECESSARILY_CAUSALLY_DEPENDENT = 5;
	// same as previous, however, causality depended solely on Bigraph matching
	// as LTS did not determine causality
	public final static int POTENTIALLY_NOT_NECESSARILY_CAUSALLY_DEPENDENT = 6;

	public final static String ATTRIBUTE_STATE_NAME = "state-";
	public final static String ATTRIBUTE_ACTION_NAME = "action-";

	// errors
	public final static int TRACES_NOT_LOADED = -1;
	public final static int SHORTEST_FILE_NOT_SAVED = -2;
	public final static int ACTIONS_CAUSAL_DEPENDENCY_ERROR = -1;

	// constants for mining operators
	public final static int SHORTEST = 0;
	public final static int ALL = 1;
	public final static int CLASP = 2;

	String clusterFolder = "clusters generated";
	String clustersOutputFileName = "clustersGenerated.txt";
	String clustersOutputFolder;

	int longestTransition = -1;
	int shortestTransition = -1;
	int numberOfTraces = -1;

	// notify listener every n traces loaded
	int tracesLoadedNotifierNumber = 30;

	// key:action name, value: id
	Map<String, Integer> tracesActions;

	// key:action name, value: occurrence (at least one occurrence in a trace)
	Map<String, Integer> tracesActionsOccurence;

	int totalNumberOfActionOccurrences = 0;

	// key:state number, value: occurrence (occurrence in a trace)
	Map<Integer, Integer> statesOccurrences;

	int totalNumberOfStateOccurrences = 0;

	// key:action name, value: occurrence
	Map<String, Integer> shortestActionsOccurence;

	// key:state, value: occurrence
	Map<Integer, Integer> shortestStatesOccurence;

	// shortest traces
	Map<Integer, GraphPath> shortestTraces;

	// prints a number of instances for each cluster
	int lengthToPrint = 5;

	// weka attributes
	String wekaInstancesFilePath = "wekaInstances.arff";

	// holds clusters generated
	List<ClusterWithMean> clusters;

	boolean isAlreadyChecked = false;

	String shortestTracesFileName;

	List<Integer> shortestTraceIDs;
	List<Integer> claSPTraceIDs;
	List<Integer> customeFilteringTraceIDs;

	List<Integer> currentShownTraces;

	String outputFolder;

	// min action length
	static final int MIN_LENGTH_INITIAL_VALUE = 1000000;
	int minimumTraceLength = MIN_LENGTH_INITIAL_VALUE;

	// max action length
	static final int MAX_LENGTH_INITIAL_VALUE = -1;
	int maximumTraceLength = -1;

	// system data
	int numberOfStates = 10000; // should be adjusted

	// listener (from GUI)
	TraceMinerListener listener;

	boolean isLoaded = false;

	// used for analysing common entities
	BRSParser brsParser;

	// bigraphER file
	private String bigraphERFile;

	// holds information about the .big file
	BRSWrapper brsWrapper;

	// map of all actions in the bigraphER file
	// key is action name, value is an ActionWrapper object containing action
	// info (pre, post)
	// taken from BRSWrapper
	Map<String, ActionWrapper> bigraphERActions;

	// specifies how many entities are there in the current selected traces that
	// are processed
	int totalNumberOfEntitiesInCurrentTraces = 0;

	// folder where states are
	private String statesFolder;

	// folder where to save traces
	private String traceFolder;

	// key is trace id, value is path
	Map<Integer, String> tracesSaved;

	// transition system info
	private String transitionSystemFilePath;
	private TransitionSystem transitionSystem;

	private URL defaultOutputFolder = getClass().getResource("../../../resources/example");

	// temp storage for loaded traces
	Map<Integer, Bigraph> loadedStateBigraphs;

	// incident pattern handler
	// provides functionalities such as finding states matching the condition
	private IncidentPatternHandler incidentPatternHandler;

	public TraceMiner() {

		tracesActions = new HashMap<String, Integer>();
		tracesActionsOccurence = new HashMap<String, Integer>();
		shortestActionsOccurence = new HashMap<String, Integer>();

		statesOccurrences = new HashMap<Integer, Integer>();

		shortestTraces = new HashMap<Integer, GraphPath>();
		shortestStatesOccurence = new HashMap<Integer, Integer>();

		// instances = new HashMap<Integer, GraphPath>();

		shortestTraceIDs = new LinkedList<Integer>();
		claSPTraceIDs = new LinkedList<Integer>();
		customeFilteringTraceIDs = new LinkedList<Integer>();

		loadedStateBigraphs = new HashMap<Integer, Bigraph>();

		incidentPatternHandler = new IncidentPatternHandler(this);

		tracesSaved = new HashMap<Integer, String>();

		numberOfStates = 10000;

		PADDING_STATE = -1 * numberOfStates;

		PADDING_ACTION_INT = -1; // initial. should be changed according to
									// actions in the system or actions in the
									// traces

		/** Need to set system actions (all possible system actions) **/
		// some actions
		// systemActions.put("EnterRoom", 0);
		// systemActions.put("ConnectIPDevice", 1);
		// systemActions.put("DisconnectIPDevice", 2);
		// systemActions.put("ConnectBusDevice", 3);
		// systemActions.put("DisconnectBusDevice", 4);
		// systemActions.put("SendData", 5);
		// systemActions.put("SendMalware", 6);
		// systemActions.put("DisableHVAC", 7);
		// systemActions.put("EnterRoomWithoutCardReader", 8);
		// systemActions.put("ChangeAccessToCardRequired", 9);
		// systemActions.put("ChangeAccessToCardNotRequired", 10);
		// systemActions.put("ChangeContextToOutSideWorkingHours", 11);
		// systemActions.put("ChangeContextToWorkingHours", 12);
		// systemActions.put("TurnOnHVAC", 13);
		// systemActions.put("TurnOffHVAC", 14);
		// systemActions.put("TurnOnSmartTV", 15);
		// systemActions.put("TurnOffSmartTV", 16);
		// systemActions.put("GenerateData", 17);
		// systemActions.put("CollectData", 18);
		// systemActions.put("TurnONTVMicrophone", 19);
		// systemActions.put("TurnOffTVMicrophone", 20);
		// systemActions.put("TurnONTVCamera", 21);
		// systemActions.put("TurnOffTVCamera", 22);

		// set value of actions as more than the max number of states. This is
		// done to avoid mixing with states numbering

		// int index = 1;
		// int increment = (int) (numberOfStates * .05); // 1% of the number of
		// // states
		//
		// if (increment == 0) {
		// increment = 1;
		// }

	}

	public boolean checkFile(String fileName) {

		if (fileName == null || fileName.isEmpty()) {
			System.err.println("Given file name is NULL");
			return false;
		}

		if (!fileName.endsWith("json")) {
			System.err.println("file should be in JSON format (i.e. *.json)");
			return false;
		}

		File file = new File(fileName);

		if (!file.exists()) {
			System.err.println("file [" + fileName + "] does NOT exist");
			return false;
		}

		if (!file.isFile()) {
			System.err.println("[" + fileName + "] is NOT a file");
			return false;
		}

		return true;
	}

	/**
	 * Identify relevant traces (currently defined as shortest and has common
	 * patterns)
	 * 
	 * @param fileName given file path (*.json or a folder containing JSON files)
	 */
	void identifyRelevantTraces(String fileName) {

		File inputFile = new File(fileName);

		if (inputFile.isFile()) {

			identifyRelevantTracesFromFile(fileName);

		} else if (inputFile.isDirectory()) {

			identifyRelevantTracesFromFolder(fileName);

		} else {
			System.err.println(fileName + " given file name is niether a FILE nor a FOLDER. Exiting");
			return;
		}

	}

	void identifyRelevantTracesFromFolder(String folderPath) {

		File inputFolder = new File(folderPath);

		StringBuilder content = new StringBuilder();

		// numberOfRelevantTraces = new LinkedList<Integer>();

		String linSeparator = System.getProperty("line.separator");

		content.append(
				"*Summary containing relevant traces (shortest & have common sequential patterns) identified from each file in folder ["
						+ folderPath + "]*")
				.append(linSeparator).append(linSeparator);
		content.append("Format: ").append(linSeparator);
		content.append("[File-name]").append(linSeparator);
		content.append("[# of relevant traces]").append(linSeparator);
		content.append("[relevant traces IDs]").append(linSeparator).append(linSeparator);

		int numOfRelevantTraces = 0;
		for (File file : inputFolder.listFiles()) {

			if (file.isFile()) {
				String filePath = file.getAbsolutePath();

				if (filePath.endsWith(".json")) {
					System.out.println("###### Identifying Relevant Traces ######");
					System.out.println("*File: " + filePath);
					System.out.println("\n");

					// identify relevant traces
					numOfRelevantTraces = identifyRelevantTracesFromFile(filePath);

					// file path
					content.append("#").append(filePath).append(linSeparator)
							// number of relevant traces
							.append(numOfRelevantTraces).append(linSeparator);
					// IDs
					// Integer[] ary = traceIDs.toArray(new
					// Integer[traceIDs.size()]);
					// content.append(Arrays.toString(ary)).append(linSeparator).append(linSeparator);

				}
			}
		}

		// store a summary into file
		String outputFile = folderPath + "/relevantTraces_Summary.txt";

		writeToFile(content.toString(), outputFile);

	}

	public int identifyRelevantTracesFromFile(String fileName) {

		if (!checkFile(fileName)) {
			return -1;
		}

		int numOfRelevantTraces = 0;

		instanceFileName = fileName;

		// clustersOutputFileName = instanceFileName.replace(".json",
		// "_relevantTraces.txt");// clustersOutputFolder
		// // clustersOutputFileName;
		// convertedInstancesFileName = instanceFileName.replace(".json",
		// "_convertedInstances.txt");// clustersOutputFolder
		// // +
		// shortestTracesFileName = instanceFileName.replace(".json",
		// "_shortestTracesIDs.txt");

		// loads instances(or traces) from given file name
		// and finds shortest transitions
		loadTracesFromFile();

		// find shortest traces
		findShortestTraces();

		/**
		 * ======Mine Frequent sequential patterns using the prefixspan algo
		 **/
		// numOfRelevantTraces =
		// mineSequencesUsingPrefixSpanAlgo(shortestTraces.values());

		// int minimumNumOfTracesForPattern = 5;
		// numOfRelevantTraces =
		// mineSequencesUsingPrefixSpanAlgo(shortestTraces.values(),minimumNumOfTracesForPattern);

		/** ======Mine Closed Frequent sequential patterns using the ClaSP **/
		// numOfRelevantTraces =
		// mineClosedSequencesUsingClaSPAlgo(shortestTraces.values());

		int minimumNumOfTracesForPattern = 5;
		numOfRelevantTraces = mineClosedSequencesUsingClaSPAlgo(shortestTraces.values(), minimumNumOfTracesForPattern);

		/** ======Mine Frequent sequential patterns using the SPADE algo **/
		// mineSequentialPatternsUsingSPADE();

		/** ======Mine Frequent sequential patterns using the TKS **/
		// finds top-k sequential patterns
		// allows to find contiguous sequence patterns
		// mineSequentialPatternsUsingTKSAlgo(shortestTraces.values());

		/** ======Mine Frequent Itemsets using FP-Growth algo **/
		// mineFrequentItemsetsUsingFP_GrowthAlgo();

		System.out.println("\n>>DONE");

		return numOfRelevantTraces;

	}

	public int loadTracesFromFile() {

		return loadTracesFromFile(instanceFileName);

	}

	public int loadTracesFromFile(String filePath) {

		System.out.println(">>Reading instances from [" + filePath + "]");

		// load instances from file
		// List<Integer> minMaxLengths = new LinkedList<Integer>();
		// List<String> tracesActs = new LinkedList<String>();

		// reset
		tracesActionsOccurence.clear();
		statesOccurrences.clear();

		minimumTraceLength = 100000;
		maximumTraceLength = -1;

		traces = readInstantiatorInstancesFile(filePath);

		// set traces actions
		if (tracesActionsOccurence.size() > 0) {
			tracesActions.clear();
			int index = 0;
			for (String action : tracesActionsOccurence.keySet()) {
				tracesActions.put(action, index);
				index++;
			}
		}

		System.out.println(">>Number of instances read = " + traces.size() + "\n>>Min trace length: "
				+ minimumTraceLength + "\n>>Max trace length: " + maximumTraceLength + "\n>>Actions: " + tracesActions
				+ "\n>>Occurrences: " + tracesActionsOccurence);

		// used when converting traces to mining format
		PADDING_ACTION_INT = -1 * tracesActions.size();

		// System.out.println(traces.get(0).getTransitionActions());
		if (traces == null) {
			System.out.println("traces are null! Exiting");
			isLoaded = false;
			return TRACES_NOT_LOADED;

		}

		isLoaded = true;

		return traces.size();

	}

	
	public int loadTracesFromList(List<GraphPath> newTraces) {

		System.out.println(">>Reading instances from [" + instanceFileName + "]");

		// load instances from file
		// List<Integer> minMaxLengths = new LinkedList<Integer>();
		// List<String> tracesActs = new LinkedList<String>();

		// reset
		tracesActionsOccurence.clear();
		statesOccurrences.clear();

		minimumTraceLength = 100000;
		maximumTraceLength = -1;

//		traces = readInstantiatorInstancesFile(instanceFileName);
		setTraces(newTraces);
		generateActionsOccurrences();

		// set traces actions
		if (tracesActionsOccurence.size() > 0) {
			tracesActions.clear();
			int index = 0;
			for (String action : tracesActionsOccurence.keySet()) {
				tracesActions.put(action, index);
				index++;
			}
		}

		System.out.println(">>Number of instances read = " + traces.size() + "\n>>Min trace length: "
				+ minimumTraceLength + "\n>>Max trace length: " + maximumTraceLength + "\n>>Actions: " + tracesActions
				+ "\n>>Occurrences: " + tracesActionsOccurence);

		// used when converting traces to mining format
		PADDING_ACTION_INT = -1 * tracesActions.size();

		// System.out.println(traces.get(0).getTransitionActions());
		if (traces == null) {
			System.out.println("traces are null! Exiting");
			isLoaded = false;
			return TRACES_NOT_LOADED;

		}

		isLoaded = true;

		return traces.size();

	}

	/**
	 * Return Actions with the highest occurrence in all given traces
	 * 
	 * @return Map with the key as action name and value as occurrence
	 */
	public Map<String, Integer> getHighestActionOccurrence() {

		if (tracesActionsOccurence == null || tracesActionsOccurence.isEmpty()) {
			return null;
		}

		Map<String, Integer> result = new HashMap<String, Integer>();

		int index = 0;

		// sort occurrences
		Collection<Integer> values = tracesActionsOccurence.values();
		List<Integer> list = new LinkedList<Integer>(values);
		Collections.sort(list);// ascending order

		// List<Integer> topN = new LinkedList<Integer>();

		if (list.size() == 0) {
			return null;
		}

		int highestOccur = list.get(list.size() - 1);

		// for now get the first n
		for (Entry<String, Integer> entry : tracesActionsOccurence.entrySet()) {

			String action = entry.getKey();
			int occur = entry.getValue();

			if (occur == highestOccur) {
				result.put(action, occur);
				index++;
			}

		}

		return result;
	}

//	public Map<String, Integer> getTopActionOccurrences(int numberofActions, String tracesToFilter) {
//
//		Map<String, Integer> result = new HashMap<String, Integer>();
//		Map<String, Integer> occurrences = null;
//
//		int index = 0;
//
//		// for now get the first n from traces
//		switch (tracesToFilter) {
//
//		case TraceViewerController.ALL_TRACES:
//			occurrences = tracesActionsOccurence;
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			if (shortestActionsOccurence != null && !shortestActionsOccurence.isEmpty()) {
//				occurrences = shortestActionsOccurence;
//			} else {
//				occurrences = getOccurrence(shortestTraces);
//				shortestActionsOccurence = occurrences;
//			}
//
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
//				occurrences = getOccurrence(traces);
//			}
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
//				occurrences = getOccurrence(traces);
//			}
//			break;
//
//		default:
//			break;
//		}
//
//		if (occurrences != null) {
//			// sort occurrences
//			Collection<Integer> values = occurrences.values();
//			List<Integer> list = new LinkedList<Integer>(values);
//			Collections.sort(list);// ascending order
//
//			List<Integer> topN = new LinkedList<Integer>();
//
//			for (int i = list.size() - 1; i > 0; i--) {
//
//				if (!topN.contains(list.get(i))) {
//					topN.add(list.get(i));
//					index++;
//
//					if (index == numberofActions) {
//						break;
//					}
//				}
//
//			}
//
//			for (Entry<String, Integer> entry : occurrences.entrySet()) {
//
//				String action = entry.getKey();
//				int occur = entry.getValue();
//
//				if (topN.contains(occur)) {
//					result.put(action, occur);
//					index++;
//				}
//
//			}
//		}
//
//		return result;
//	}

//	public Map<String, Long> getTopEntitiesOccurrences(int numberofEntities, String tracesToFilter) {
//
//		Map<String, Long> result = new HashMap<String, Long>();
//		// Map<String, Integer> occurrences = null;
//		List<Map.Entry<String, Long>> entitiesOccur = new LinkedList<Map.Entry<String, Long>>();
//
//		int index = 0;
//		List<String> omitList = JSONTerms.BIG_IRRELEVANT_TERMS;
//		// for now get the first n from traces
//		switch (tracesToFilter) {
//
//		case TraceViewerController.ALL_TRACES:
//			entitiesOccur = findTopCommonEntities(traces.values(), omitList, numberofEntities);
//			// occurrences = tracesActionsOccurence;
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			// if (shortestActionsOccurence != null &&
//			// !shortestActionsOccurence.isEmpty()) {
//			// occurrences = shortestActionsOccurence;
//			// } else {
//			// occurrences = getOccurrence(shortestTraces);
//			// shortestActionsOccurence = occurrences;
//			// }
//			entitiesOccur = findTopCommonEntities(getTraces(shortestTraceIDs).values(), omitList, numberofEntities);
//
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
//				entitiesOccur = findTopCommonEntities(traces.values(), omitList, numberofEntities);
//			}
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
//				entitiesOccur = findTopCommonEntities(traces.values(), omitList, numberofEntities);
//			}
//			break;
//
//		default:
//			break;
//		}
//
//		// System.out.println("Res: " + entitiesOccur);
//
//		for (Entry<String, Long> ent : entitiesOccur) {
//			result.put(ent.getKey(), ent.getValue());
//
//		}
//
//		// if (occurrences != null) {
//		// // sort occurrences
//		// Collection<Integer> values = occurrences.values();
//		// List<Integer> list = new LinkedList<Integer>(values);
//		// Collections.sort(list);// ascending order
//		//
//		// List<Integer> topN = new LinkedList<Integer>();
//		//
//		// for (int i = list.size() - 1; i > 0; i--) {
//		//
//		// if (!topN.contains(list.get(i))) {
//		// topN.add(list.get(i));
//		// index++;
//		//
//		// if (index == numberofActions) {
//		// break;
//		// }
//		// }
//		//
//		// }
//		//
//		// for (Entry<String, Integer> entry : occurrences.entrySet()) {
//		//
//		// String action = entry.getKey();
//		// int occur = entry.getValue();
//		//
//		// if (topN.contains(occur)) {
//		// result.put(action, occur);
//		// index++;
//		// }
//		//
//		// }
//		// }
//
//		return result;
//	}

//	public Map<Integer, Integer> getTopStatesOccurrences(int numberofActions, String tracesToFilter) {
//
//		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
//		Map<Integer, Integer> occurrences = null;
//
//		int index = 0;
//
//		// for now get the first n from traces
//		switch (tracesToFilter) {
//
//		case TraceViewerController.ALL_TRACES:
//			occurrences = statesOccurrences;
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			if (shortestStatesOccurence != null && !shortestStatesOccurence.isEmpty()) {
//				occurrences = shortestStatesOccurence;
//			} else {
//				occurrences = getStateOccurrence(shortestTraces);
//				shortestStatesOccurence = occurrences;
//			}
//
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
//				occurrences = getStateOccurrence(traces);
//			}
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
//				occurrences = getStateOccurrence(traces);
//			}
//			break;
//
//		default:
//			break;
//		}
//
//		if (occurrences != null) {
//			// sort occurrences
//			Collection<Integer> values = occurrences.values();
//			List<Integer> list = new LinkedList<Integer>(values);
//			Collections.sort(list);// ascending order
//
//			List<Integer> topN = new LinkedList<Integer>();
//
//			for (int i = list.size() - 1; i > 0; i--) {
//
//				if (!topN.contains(list.get(i))) {
//					topN.add(list.get(i));
//					index++;
//
//					if (index == numberofActions) {
//						break;
//					}
//				}
//
//			}
//
//			// for now get the first n
//			for (Entry<Integer, Integer> entry : occurrences.entrySet()) {
//
//				Integer state = entry.getKey();
//				int occur = entry.getValue();
//
//				if (topN.contains(occur)) {
//					result.put(state, occur);
//					index++;
//				}
//
//			}
//
//		}
//
//		return result;
//	}

//	public Map<String, Integer> getActionsWithOccurrencePercentage(double percentage, String operation,
//			String tracesToFilter) {
//
//		Map<String, Integer> result = new HashMap<String, Integer>();
//		Map<String, Integer> occurrences = null;
//
//		double localPerc = 0;
//		int numOfTraces = -1;
//
//		switch (tracesToFilter) {
//		case TraceViewerController.ALL_TRACES:
//			occurrences = tracesActionsOccurence;
//			numOfTraces = traces.size();
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			if (shortestActionsOccurence != null && !shortestActionsOccurence.isEmpty()) {
//				occurrences = shortestActionsOccurence;
//			} else {
//				occurrences = getOccurrence(shortestTraces);
//				shortestActionsOccurence = occurrences;
//			}
//
//			numOfTraces = shortestTraces.size();
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			Map<Integer, GraphPath> traces1 = null;
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				traces1 = getTraces(claSPTraceIDs);
//				occurrences = getOccurrence(traces1);
//			}
//
//			if (traces1 != null) {
//				numOfTraces = traces1.size();
//			}
//
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			Map<Integer, GraphPath> traces = null;
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				traces = getTraces(customeFilteringTraceIDs);
//				occurrences = getOccurrence(traces);
//			}
//
//			if (traces != null) {
//				numOfTraces = traces.size();
//			}
//
//			break;
//		default:
//			break;
//		}
//
//		if (occurrences != null && !occurrences.isEmpty() && numOfTraces > 0) {
//
//			// numOfTraces = occurrences.size();
//			// System.out.println("miner " + occurrences);
//			// for now get the first n
//			for (Entry<String, Integer> entry : occurrences.entrySet()) {
//
//				String action = entry.getKey();
//				int occur = entry.getValue();
//
//				localPerc = occur * 1.0 / numOfTraces;
//
//				switch (operation) {
//				case TraceViewerController.EQUAL:
//					if (localPerc == percentage) {
//						result.put(action, occur);
//						// index++;
//					}
//					break;
//
//				case TraceViewerController.MORE_THAN_EQUAL:
//					if (localPerc >= percentage) {
//						result.put(action, occur);
//						// index++;
//					}
//					break;
//
//				case TraceViewerController.LESS_THAN_EQUAL:
//					if (localPerc <= percentage) {
//						result.put(action, occur);
//						// index++;
//					}
//					break;
//
//				default:
//					break;
//				}
//
//			}
//		}
//
//		// tracesActionsOccurence.s
//		return result;
//	}

//	public Map<String, Long> getEntitiesWithOccurrencePercentage(double percentage, String operation,
//			String tracesToFilter) {
//
//		Map<String, Long> result = new HashMap<String, Long>();
//		// Map<String, Integer> occurrences = null;
//		List<Map.Entry<String, Long>> entitiesOccur = new LinkedList<Map.Entry<String, Long>>();
//
//		// int index = 0;
//		List<String> omitList = JSONTerms.BIG_IRRELEVANT_TERMS;
//		// for now get the first n from traces
//		switch (tracesToFilter) {
//
//		case TraceViewerController.ALL_TRACES:
//			entitiesOccur = findAllEntities(traces.values(), omitList);
//			// occurrences = tracesActionsOccurence;
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			// if (shortestActionsOccurence != null &&
//			// !shortestActionsOccurence.isEmpty()) {
//			// occurrences = shortestActionsOccurence;
//			// } else {
//			// occurrences = getOccurrence(shortestTraces);
//			// shortestActionsOccurence = occurrences;
//			// }
//			entitiesOccur = findAllEntities(getTraces(shortestTraceIDs).values(), omitList);
//
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
//				entitiesOccur = findAllEntities(traces.values(), omitList);
//			}
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
//				entitiesOccur = findAllEntities(traces.values(), omitList);
//			}
//			break;
//
//		default:
//			break;
//		}
//
//		for (Entry<String, Long> entry : entitiesOccur) {
//
//			String action = entry.getKey();
//			long occur = entry.getValue();
//
//			double localPerc = 1;
//
//			if (totalNumberOfEntitiesInCurrentTraces > 0) {
//				localPerc = occur * 1.0 / totalNumberOfEntitiesInCurrentTraces;
//			} else {
//				localPerc = occur;
//			}
//
//			switch (operation) {
//			case TraceViewerController.EQUAL:
//				if (localPerc == percentage) {
//					result.put(action, occur);
//					// index++;
//				}
//				break;
//
//			case TraceViewerController.MORE_THAN_EQUAL:
//				if (localPerc >= percentage) {
//					result.put(action, occur);
//					// index++;
//				}
//				break;
//
//			case TraceViewerController.LESS_THAN_EQUAL:
//				if (localPerc <= percentage) {
//					result.put(action, occur);
//					// index++;
//				}
//				break;
//
//			default:
//				break;
//			}
//
//		}
//
//		return result;
//	}

//	public Map<Integer, Integer> getStatesWithOccurrencePercentage(double percentage, String operation,
//			String tracesToFilter) {
//
//		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
//		Map<Integer, Integer> occurrences = null;
//
//		int index = 0;
//		int numOfTraces = -1;
//
//		switch (tracesToFilter) {
//
//		case TraceViewerController.ALL_TRACES:
//			occurrences = statesOccurrences;
//			numOfTraces = traces.size();
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			if (shortestStatesOccurence != null && !shortestStatesOccurence.isEmpty()) {
//				occurrences = shortestStatesOccurence;
//			} else {
//				occurrences = getStateOccurrence(shortestTraces);
//				shortestStatesOccurence = occurrences;
//			}
//
//			numOfTraces = shortestTraces.size();
//
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			Map<Integer, GraphPath> traces1 = null;
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				traces1 = getTraces(claSPTraceIDs);
//				occurrences = getStateOccurrence(traces1);
//			}
//			if (traces1 != null) {
//				numOfTraces = traces1.size();
//			}
//
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			Map<Integer, GraphPath> traces = null;
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				traces = getTraces(customeFilteringTraceIDs);
//				occurrences = getStateOccurrence(traces);
//			}
//			if (traces != null) {
//				numOfTraces = traces.size();
//			}
//
//			break;
//
//		default:
//			break;
//		}
//
//		if (occurrences != null && !occurrences.isEmpty() && numOfTraces > 0) {
//
//			// numOfTraces = occurrences.size();
//			double localPerc = 0;
//
//			// for now get the first n
//			for (Entry<Integer, Integer> entry : occurrences.entrySet()) {
//
//				Integer state = entry.getKey();
//				int occur = entry.getValue();
//
//				localPerc = occur * 1.0 / numOfTraces;
//
//				switch (operation) {
//				case TraceViewerController.EQUAL:
//					if (localPerc == percentage) {
//						result.put(state, occur);
//						index++;
//					}
//					break;
//
//				case TraceViewerController.MORE_THAN_EQUAL:
//					if (localPerc >= percentage) {
//						result.put(state, occur);
//						index++;
//					}
//					break;
//
//				case TraceViewerController.LESS_THAN_EQUAL:
//					if (localPerc <= percentage) {
//						result.put(state, occur);
//						index++;
//					}
//					break;
//
//				default:
//					break;
//				}
//
//			}
//		}
//
//		return result;
//	}

//	public Map<String, Integer> getLowestActionOccurrences(int numberofActions, String tracesToFilter) {
//
//		Map<String, Integer> result = new HashMap<String, Integer>();
//		Map<String, Integer> occurrences = null;
//		int index = 0;
//
//		// for now get the first n from traces
//		switch (tracesToFilter) {
//
//		case TraceViewerController.ALL_TRACES:
//			occurrences = tracesActionsOccurence;
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			if (shortestActionsOccurence != null && !shortestActionsOccurence.isEmpty()) {
//				occurrences = shortestActionsOccurence;
//			} else {
//				occurrences = getOccurrence(shortestTraces);
//				shortestActionsOccurence = occurrences;
//			}
//
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
//				occurrences = getOccurrence(traces);
//			}
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
//				occurrences = getOccurrence(traces);
//			}
//			break;
//		default:
//			break;
//		}
//
//		if (occurrences != null) {
//			Collection<Integer> values = occurrences.values();
//			List<Integer> list = new LinkedList<Integer>(values);
//			Collections.sort(list);// ascending order
//
//			List<Integer> topN = new LinkedList<Integer>();
//			int size = list.size();
//
//			for (int i = 0; i < list.size(); i++) {
//
//				if (!topN.contains(list.get(i))) {
//					topN.add(list.get(i));
//					index++;
//
//					if (index == numberofActions) {
//						break;
//					}
//				}
//
//			}
//
//			// for now get the first n
//			for (Entry<String, Integer> entry : occurrences.entrySet()) {
//
//				String action = entry.getKey();
//				int occur = entry.getValue();
//
//				if (topN.contains(occur)) {
//					result.put(action, occur);
//					index++;
//				}
//
//			}
//		}
//
//		return result;
//	}

//	public Map<String, Long> getLowestEntitiesOccurrences(int numberofEntities, String tracesToFilter) {
//
//		Map<String, Long> result = new HashMap<String, Long>();
//		// Map<String, Integer> occurrences = null;
//		List<Map.Entry<String, Long>> entitiesOccur = new LinkedList<Map.Entry<String, Long>>();
//
//		// int index = 0;
//		List<String> omitList = JSONTerms.BIG_IRRELEVANT_TERMS;
//		// for now get the first n from traces
//		switch (tracesToFilter) {
//
//		case TraceViewerController.ALL_TRACES:
//			entitiesOccur = findLowestCommonEntities(traces.values(), omitList, numberofEntities);
//			// occurrences = tracesActionsOccurence;
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			// if (shortestActionsOccurence != null &&
//			// !shortestActionsOccurence.isEmpty()) {
//			// occurrences = shortestActionsOccurence;
//			// } else {
//			// occurrences = getOccurrence(shortestTraces);
//			// shortestActionsOccurence = occurrences;
//			// }
//			entitiesOccur = findLowestCommonEntities(getTraces(shortestTraceIDs).values(), omitList, numberofEntities);
//
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
//				entitiesOccur = findLowestCommonEntities(traces.values(), omitList, numberofEntities);
//			}
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
//				entitiesOccur = findLowestCommonEntities(traces.values(), omitList, numberofEntities);
//			}
//			break;
//
//		default:
//			break;
//		}
//
//		// System.out.println("Res: " + entitiesOccur);
//
//		for (Entry<String, Long> ent : entitiesOccur) {
//			result.put(ent.getKey(), ent.getValue());
//
//		}
//
//		return result;
//	}

//	public Map<Integer, Integer> getLowestStateOccurrences(int numberofStates, String tracesToFilter) {
//
//		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
//		Map<Integer, Integer> occurrences = null;
//
//		int index = 0;
//
//		switch (tracesToFilter) {
//
//		case TraceViewerController.ALL_TRACES:
//			occurrences = statesOccurrences;
//			break;
//
//		case TraceViewerController.SHORTEST_TRACES:
//			if (shortestStatesOccurence != null && !shortestStatesOccurence.isEmpty()) {
//				occurrences = shortestStatesOccurence;
//			} else {
//				occurrences = getStateOccurrence(shortestTraces);
//				shortestStatesOccurence = occurrences;
//			}
//
//			break;
//
//		case TraceViewerController.SHORTEST_CLASP_TRACES:
//			if (claSPTraceIDs != null && !claSPTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(claSPTraceIDs);
//				occurrences = getStateOccurrence(traces);
//			}
//			break;
//
//		case TraceViewerController.CUSTOMISED_TRACES:
//			if (customeFilteringTraceIDs != null && !customeFilteringTraceIDs.isEmpty()) {
//				Map<Integer, GraphPath> traces = getTraces(customeFilteringTraceIDs);
//				occurrences = getStateOccurrence(traces);
//			}
//			break;
//
//		default:
//			break;
//		}
//
//		if (occurrences != null) {
//			// sort occurrences
//			Collection<Integer> values = occurrences.values();
//			List<Integer> list = new LinkedList<Integer>(values);
//			Collections.sort(list);// ascending order
//
//			List<Integer> topN = new LinkedList<Integer>();
//			int size = list.size();
//
//			for (int i = 0; i < list.size(); i++) {
//
//				if (!topN.contains(list.get(i))) {
//					topN.add(list.get(i));
//					index++;
//
//					if (index == numberofStates) {
//						break;
//					}
//				}
//
//			}
//
//			// for now get the first n
//			for (Entry<Integer, Integer> entry : occurrences.entrySet()) {
//
//				Integer state = entry.getKey();
//				int occur = entry.getValue();
//
//				if (topN.contains(occur)) {
//					result.put(state, occur);
//					index++;
//				}
//
//				// if (index == numberofActions) {
//				// break;
//				// }
//			}
//		}
//
//		// tracesActionsOccurence.s
//		return result;
//	}

	/**
	 * Finds Action occurrrences for the given traces
	 * 
	 * @param traces
	 * @return
	 */
	public Map<String, Integer> getOccurrence(Map<Integer, GraphPath> traces) {

		Map<String, Integer> result = new HashMap<String, Integer>();

		for (GraphPath trace : traces.values()) {

			// get actions
			List<String> actions = trace.getTransitionActions();

			for (String action : actions) {

				// if action already exist then add one to current occurrence
				// value
				if (result.containsKey(action)) {
					int oldValue = result.get(action);
					oldValue++;
					result.put(action, oldValue);

					// else create new entry for the action
				} else {
					result.put(action, 1);
				}
			}
		}

		return result;
	}

	/**
	 * Finds States occurrences for the given traces
	 * 
	 * @param traces
	 * @return
	 */
	public Map<Integer, Integer> getStateOccurrence(Map<Integer, GraphPath> traces) {

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();

		for (GraphPath trace : traces.values()) {

			// get actions
			List<Integer> states = trace.getStateTransitions();

			for (Integer state : states) {

				// if action already exist then add one to current occurrence
				// value
				if (result.containsKey(state)) {
					int oldValue = result.get(state);
					oldValue++;
					result.put(state, oldValue);

					// else create new entry for the action
				} else {
					result.put(state, 1);
				}
			}
		}

		for (Entry<Integer, Integer> entr : result.entrySet()) {
			if (entr.getValue() == 5) {
				System.out.println(entr);
			}

		}
		// System.out.println(result);

		return result;
	}

	public int findShortestTraces() {

		// shortest trace is set to be 3 actions (or 4 states (i.e. actions+1)

		int minimumStates = minimumTraceLength + 1;

//		System.out.println("min actions: " +minimumTraceLength);

		String separator = " ";
		StringBuilder bldr = new StringBuilder();

		if (shortestTraces != null) {
			shortestTraces.clear();
		} else {
			shortestTraces = new HashMap<Integer, GraphPath>();
		}

		if (shortestTracesFileName != null) {
			System.out.println(">>Identifying shortest traces in [" + instanceFileName + "]");
		} else {
			bldr = null;
			System.out.println(">>Identifying shortest traces...");
		}
		for (GraphPath trace : traces.values()) {

			if (trace.getStateTransitions().size() == minimumStates) {
				shortestTraces.put(trace.getInstanceID(), trace);

				if (bldr != null) {
					bldr.append(trace.getInstanceID()).append(separator);
				}
			}
		}

		if (bldr != null && bldr.length() > 0) {
			bldr.deleteCharAt(bldr.length() - 1);// remove extra space
		}

		// store to file
		if (shortestTracesFileName != null && bldr != null) {
			writeToFile(bldr.toString(), shortestTracesFileName);
			System.out.println(">>Shortest traces IDs are stored in [" + shortestTracesFileName + "]");
		}

		shortestTraceIDs = Arrays.asList(shortestTraces.keySet().toArray(new Integer[shortestTraces.size()]));

		return shortestTraces.size();

	}

	public int findShortestTraces(boolean saveToFile) {

		// shortest trace is set to be 3 actions (or 4 states (i.e. actions+1)

		int numberOfStates = 4;

		String separator = " ";
		StringBuilder bldr = new StringBuilder();

		if (shortestTraces != null) {
			shortestTraces.clear();
		}

		System.out.println(">>Identifying shortest traces in [" + instanceFileName + "]");
		for (GraphPath trace : traces.values()) {

			if (trace.getStateTransitions().size() == numberOfStates) {
				shortestTraces.put(trace.getInstanceID(), trace);
				bldr.append(trace.getInstanceID()).append(separator);
			}
		}

		if (bldr.length() > 0) {
			bldr.deleteCharAt(bldr.length() - 1);// remove extra space
		}

		// store to file
		if (saveToFile) {

			if (shortestTracesFileName == null) {
				if (instanceFileName != null) {
					shortestTracesFileName = instanceFileName.replace(".json", "_shortestTracesIDs.txt");
				}

			}

			if (shortestTracesFileName == null) {
				return SHORTEST_FILE_NOT_SAVED;
			}

			writeToFile(bldr.toString(), shortestTracesFileName);
			System.out.println(">>Shortest traces IDs are stored in [" + shortestTracesFileName + "]");
		}

		return shortestTraces.size();

	}

	public List<ClusterWithMean> generateClustersUsingKMean() {

		AlgoKMeans kmean = new AlgoKMeans();

		try {

			numberOFClusters = 6;

			System.out.println(">>Generating clusters using K-mean algorithm" + " with K = " + numberOFClusters
					+ ", distance function is " + distanceFunction.getName());

			// generate clusters
			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
					DATA_SEPARATOR);

			// store clusters (each line is a cluster in the output file)
			kmean.saveToFile(clustersOutputFileName);

			kmean.printStatistics();

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<ClusterWithMean> generateClustersUsingKMeanUsingBiSect() {

		AlgoBisectingKMeans kmean = new AlgoBisectingKMeans();

		int iteratorForSplit = numberOFClusters * 2;

		try {
			System.out.println(">>Generating clusters using K-mean algorithm with K = " + numberOFClusters
					+ ", distance function is " + distanceFunction.getName());
			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
					iteratorForSplit, DATA_SEPARATOR);

			kmean.saveToFile(clustersOutputFileName);

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<Cluster> generateClustersUsingDBSCAN() {

		AlgoDBSCAN algo = new AlgoDBSCAN();

		// minimum number of points/traces in a cluster
		int minPoints = 10;
		// distance between points/traces in a cluster
		double epsilon = 10d;
		// double epsilonPrime = epsilon;

		try {
			System.out.println(">>Generating clusters using DBSCAN algorithm");

			// generate clusters
			List<Cluster> clusters = algo.runAlgorithm(convertedInstancesFileName, minPoints, epsilon, DATA_SEPARATOR);

			// store clusters (each line is a cluster in the output file)
			algo.saveToFile(clustersOutputFileName);

			algo.printStatistics();

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<Cluster> generateClustersUsingOPTICS() {

		AlgoOPTICS algo = new AlgoOPTICS();

		// minimum number of points/traces in a cluster
		int minPoints = 10;
		// distance between points/traces in a cluster
		double epsilon = 2d;
		double epsilonPrime = epsilon;

		try {
			System.out.println(">>Generating clusters using OPTIC algorithm");

			// generate clusters
			List<DoubleArrayOPTICS> clusters = algo.computerClusterOrdering(convertedInstancesFileName, minPoints,
					epsilon, DATA_SEPARATOR);

			// generate dbscan clusters from the cluster ordering:
			List<Cluster> dbScanClusters = algo.extractDBScan(minPoints, epsilonPrime);

			// store clusters (each line is a cluster in the output file)
			algo.saveToFile(clustersOutputFileName);

			algo.printStatistics();

			return dbScanClusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void generateClustersUsingTextMining() {

		TextClusterAlgo algo = new TextClusterAlgo();

		boolean stemFlag = true;
		boolean stopWordFlag = true;

		System.out.println(">>Generating clusters using Text Clustering algorithm");

		// generate clusters
		algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, stemFlag, stopWordFlag);

		// clusters generated are stored in a file (e.g.,
		// clustersOutputFileName)

		algo.printStatistics();
	}

	public String convertInstancesToMiningFormat(List<GraphPath> traces) {

		// convert traces to a format compatible with that of the data mining
		// library used (i.e. SPMF)
		// line format could have:
		// @NAME="instance name"
		String instanceName = "@NAME=";
		// @ATTRIBUTEDEF="attribute name"
		String attributeName = "@ATTRIBUTEDEF=";
		// #, % are used for comments and any meta-data respectively
		// data1 [separator] data2 [separator] data3 ... (actual data treated as
		// double array)

		/**
		 * all data array should be of the same length so states that are short than the
		 * longest are padded with -1
		 **/
		// create a text file to hold the data

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		if (traces != null && !traces.isEmpty()) {
			shortestTransition = traces.get(0).getStateTransitions().size();
		}

		// find longest and shortest transitions
		for (GraphPath path : traces) {
			List<Integer> tmp = path.getStateTransitions();

			if (tmp.size() > longestTransition) {
				longestTransition = tmp.size();
			} else if (tmp.size() < shortestTransition) {
				shortestTransition = tmp.size();
			}
		}

		// numberOFClusters = (longestTransition - shortestTransition) + 1;

		int numberOfActions = longestTransition - 1;

		int i = 0;

		// ========states attributes (state-0, state-1, number of maximum
		// states)
		for (i = 0; i < longestTransition - 1; i++) {

			// add attribute name e.g., "state-0 state-1 ..."
			builder.append(attributeName).append(ATTRIBUTE_STATE_NAME).append(i).append(fileLinSeparator);
			builder.append(attributeName).append(ATTRIBUTE_ACTION_NAME).append(i).append(fileLinSeparator);
		}

		builder.append(attributeName).append(ATTRIBUTE_STATE_NAME).append(i).append(fileLinSeparator);

		// ========actions attribute (actions names)
		// for (String action : systemActions.keySet()) {
		// builder.append(attributeName).append(action).append(fileLinSeparator);
		// }

		// ========set data
		for (GraphPath path : traces) {

			// set instance name to be the instance id
			builder.append(instanceName).append(path.getInstanceID()).append(fileLinSeparator);

			// set data to be the states and actions of transitions
			List<Integer> states = path.getStateTransitions();
			List<String> transitionActions = path.getTransitionActions();

			for (i = 0; i < states.size() - 1; i++) {

				// add state
				builder.append(states.get(i)).append(DATA_SEPARATOR);

				// add action
				builder.append(tracesActions.get(transitionActions.get(i))).append(DATA_SEPARATOR);
			}

			// add last state
			builder.append(states.get(i));
			// builder.append(systemActions.get(transitionActions.get(i)));

			// pad the transition with -1
			if (states.size() < longestTransition) {

				int numOfExtraStates = longestTransition - states.size();

				builder.append(DATA_SEPARATOR);

				// add dummy action
				builder.append(PADDING_ACTION_INT).append(DATA_SEPARATOR);

				for (i = 0; i < numOfExtraStates - 1; i++) {

					builder.append(PADDING_STATE).append(DATA_SEPARATOR);
					builder.append(PADDING_ACTION_INT).append(DATA_SEPARATOR);
				}

				builder.append(PADDING_STATE);
				// builder.append(PADDING_ACTION);
			}

			// add action data
			// 0 for missing the action from the transition actions. 1 if it
			// exists

			// if (transitionActions != null && !transitionActions.isEmpty()) {
			//
			// builder.append(DATA_SEPARATOR);
			//
			//// for (i = 0; i < systemActions.size() - 1; i++) {
			////
			//// if (transitionActions.contains(systemActions.get(i))) {
			//// builder.append(ACTION_PERFORMED).append(DATA_SEPARATOR);
			//// } else {
			//// builder.append(ACTION_NOT_PERFORMED).append(DATA_SEPARATOR);
			//// }
			//// }
			//
			// //add action as an index in the system actions
			// for(i=0;i< transitionActions.size()-1;i++) {
			// builder.append(systemActions.get(transitionActions.get(i))).append(DATA_SEPARATOR);
			// }
			//
			// // check last action
			//// if (transitionActions.contains(systemActions.get(i))) {
			//// builder.append(ACTION_PERFORMED);
			//// } else {
			//// builder.append(ACTION_NOT_PERFORMED);
			//// }
			//
			// builder.append(systemActions.get(transitionActions.get(i)));
			// }

			builder.append(fileLinSeparator);
		}

		// save string to file

		writeToFile(builder.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	public String convertInstancesToTextMiningFormat(List<GraphPath> traces) {

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		// ========set data
		for (GraphPath path : traces) {

			// === get states as string
			// String statesStr = path.getStateTransitions().toString();
			// // remove brackets
			// statesStr = statesStr.replaceAll("\\[", "");
			// statesStr = statesStr.replaceAll("\\]", "");
			// // remove commas
			// statesStr = statesStr.replaceAll(",", "");
			// statesStr = statesStr.trim();

			// === get actions as string
			String actionsStr = path.getTransitionActions().toString();
			actionsStr = actionsStr.replaceAll("\\[", "");
			actionsStr = actionsStr.replaceAll("\\]", "");
			actionsStr = actionsStr.replaceAll(",", "");
			actionsStr = actionsStr.trim();

			// === set record(instance_id [states (1 2 3) actions (enterRoom)]
			builder.append(path.getInstanceID()).append("\t")
					// .append(statesStr)
					// .append(" ")
					.append(actionsStr).append(fileLinSeparator);

		}
		writeToFile(builder.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	public String convertUsingActionEntities(List<GraphPath> traces) {

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		// ========set data
		for (GraphPath path : traces) {

			// === get states as string
			// String statesStr = path.getStateTransitions().toString();
			// // remove brackets
			// statesStr = statesStr.replaceAll("\\[", "");
			// statesStr = statesStr.replaceAll("\\]", "");
			// // remove commas
			// statesStr = statesStr.replaceAll(",", "");
			// statesStr = statesStr.trim();

			// === get actions as string
			List<String> actions = path.getTransitionActions();

			int count = 0;

			for (String act : actions) {
				List<String> actionEntities = new LinkedList<String>();

				ActionWrapper actionDetails = bigraphERActions.get(act);

				// allow repetition of entities

				if (actionDetails != null) {

					// add entities from pre
					BigraphWrapper pre = actionDetails.getPrecondition();

					if (pre != null) {
						Set<Entity> ents = pre.getControlMap().keySet();

						for (Entity ent : ents) {
							actionEntities.add(ent.getName());
						}
					}

					BigraphWrapper post = actionDetails.getPostcondition();

					if (post != null) {
						Set<Entity> ents = post.getControlMap().keySet();

						for (Entity ent : ents) {
							actionEntities.add(ent.getName());
						}
					}
				}

				if (!actionEntities.isEmpty()) {
					// === set record(instance_id [states (1 2 3) actions
					// (enterRoom)]
					String actionsStr = actionEntities.toString();

					actionsStr = actionsStr.replaceAll("\\[", "");
					actionsStr = actionsStr.replaceAll("\\]", "");
					actionsStr = actionsStr.replaceAll(",", "");
					actionsStr = actionsStr.trim();

					// id
					builder.append(count).append("\t");
					// data separated by space
					builder.append(actionsStr).append(fileLinSeparator);
				}

				count++;

			}

		}
		writeToFile(builder.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	protected void writeToFile(String text, String fileName) {

		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));

			writer.write(text);

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**************************************/
	/***** SEQUENTIAL PATTERN MINING ******/
	/**************************************/

	public int mineSequencesUsingPrefixSpanAlgo(Collection<GraphPath> traces) {

		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);

		// Create an instance of the algorithm with minsup = 50 %
		AlgoPrefixSpan algo = new AlgoPrefixSpan();

		try {

			// run several times till you find max minsup that which after there
			// will be no result

			boolean isMaxFound = false;
			int tries = traces.size() / 2 + 1;

			// binary search
			int left = 0;
			int right = traces.size() - 1;
			int mid = -1;

			while ((left <= right) & tries > 0) {

				mid = (int) Math.floor((left + right) / 2);

				algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
				String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

				// if there is output, then increase minsup. Else, decrease
				if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
					left = mid + 1;
					System.out
							.println(">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
					isMaxFound = true;
				} else {
					isMaxFound = false;
					right = mid - 1;
					System.out
							.println(">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
				}

				tries--;
			}

			// if the last mid (or minimum trace/minsup) has zero patterns, then
			// decrement till a value is returned
			if (!isMaxFound) {
				while (mid > 0) {

					mid--;

					algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

					// if there is output, then increase minsup. Else, decrease
					if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
						break;
					}
				}
			} // else increase mid until there's no output.
			else {
				while (mid < right) {

					mid++;

					algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

					// if there is output, then increase minsup. Else, decrease
					if (lines == null || lines.length == 0 || lines[0].isEmpty()) {
						mid--;
						break;
					}
				}
			}

			System.out.println(">>Minimum traces is " + mid);

			return mineSequencesUsingPrefixSpanAlgo(traces, mid);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public int mineSequencesUsingPrefixSpanAlgo(Collection<GraphPath> traces, int minimumTraces) {

		File file = new File(convertedInstancesFileName);

		if (!file.exists()) {
			convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);
		}

		AlgoPrefixSpan algo = new AlgoPrefixSpan();

		int minsup = minimumTraces; // use a minimum support of x sequences.

		// if you set the following parameter to true, the sequence ids of the
		// sequences where
		// each pattern appears will be shown in the result
		algo.setShowSequenceIdentifiers(true);

		// execute the algorithm
		try {

			algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, minsup);
			algo.printStatistics();

			// analysis of the generated sequential patterns
			String analysisFile = instanceFileName.replace(".json", "_PerfixSpan_analysis.txt");

			analyseGeneratedSequencePatterns(convertedInstancesFileName, clustersOutputFileName, analysisFile);

			claSPTraceIDs = getTracesIDsFromOutputFile(clustersOutputFileName);

			if (claSPTraceIDs != null) {
				return claSPTraceIDs.size();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Finds all traces using the ClaSP algorithm (sequential mining algorithm)
	 * 
	 * @param tracesToAnalyse (indicates which traces to use for analysis
	 * @return
	 */
	public int mineClosedSequencesUsingClaSPAlgo(int tracesToAnalyse) {

		switch (tracesToAnalyse) {
		case SHORTEST: // shortest
			findShortestTraces();
			return mineClosedSequencesUsingClaSPAlgo(shortestTraces.values());

		case ALL: // all
			return mineClosedSequencesUsingClaSPAlgo(traces.values());

		default:
			return -1;

		}

	}

	public int mineClosedSequencesUsingClaSPAlgo(int tracesToAnalyse, int minimumTraces) {

		switch (tracesToAnalyse) {
		case SHORTEST:
			findShortestTraces();
			return mineClosedSequencesUsingClaSPAlgo(shortestTraces.values(), minimumTraces);

		case ALL:
			return mineClosedSequencesUsingClaSPAlgo(traces.values(), minimumTraces);

		default:
			return -1;

		}

	}

	public int mineClosedSequencesUsingClaSPAlgo(Collection<GraphPath> traces, int minimumTraces) {

		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);

		int numberOfTraces = minimumTraces;
		// int numOfRelevantTraces = 0;

		// Load a sequence database
		double support = numberOfTraces * 1.0 / traces.size();

		boolean keepPatterns = true; // if set to true, then generated result
										// is stored in the given file
		boolean verbose = true;
		boolean findClosedPatterns = true;
		boolean executePruningMethods = true;
		// if you set the following parameter to true, the sequence ids of the
		// sequences where
		// each pattern appears will be shown in the result
		boolean outputSequenceIdentifiers = true;

		AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
		IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

		SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

		double relativeSupport;

		try {

			relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);

			// double relativeSupport =
			// sequenceDatabase.loadFile(fileToPath("gazelle.txt"), support);

			AlgoClaSP algorithm = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
					executePruningMethods);

			algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
					outputSequenceIdentifiers);

			String msg = "Minimum percentage of traces to appear in: ";
			if (isAlreadyChecked) {
				System.out.println(msg + support);
			} else {
				System.out.println(msg + support + " [" + Math.ceil(support * traces.size()) + "]");
			}

			System.out.println(algorithm.getNumberOfFrequentPatterns() + "patterns found.");

			if (verbose && keepPatterns) {
				System.out.println(algorithm.printStatistics());
			}

			// extracts traces ids from generated file
			claSPTraceIDs = getTracesIDsFromOutputFile(clustersOutputFileName);

			if (claSPTraceIDs != null) {
				return claSPTraceIDs.size();
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;

	}

	public int mineClosedSequencesUsingClaSPAlgo(Collection<GraphPath> traces) {

		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);

		// Load a sequence database
		double support = 0;

		boolean keepPatterns = true;
		boolean verbose = false;
		boolean findClosedPatterns = true;
		boolean executePruningMethods = true;
		// if you set the following parameter to true, the sequence ids of the
		// sequences where
		// each pattern appears will be shown in the result
		boolean outputSequenceIdentifiers = true;

		double relativeSupport;

		int size = traces.size();
		try {

			// run several times till you find max minsup that which after there
			// will be no result

			boolean isMaxFound = false;
			int tries = traces.size() / 2 + 1;

			// binary search
			int left = 0;
			int right = traces.size() - 1;
			int mid = -1;

			while ((left <= right) & tries > 0) {

				mid = (int) Math.floor((left + right) / 2);

				support = mid * 1.0 / size;

				AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
				IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

				SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

				relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);

				AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
						executePruningMethods);

				algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
						outputSequenceIdentifiers);
				// String[] lines =
				// FileManipulator.readFileNewLine(clustersOutputFileName);
				//
				// // if there is output, then increase minsup. Else, decrease
				// if (lines != null && lines.length > 0 && !lines[0].isEmpty())
				// {
				// left = mid + 1;
				// System.out
				// .println(">>[L] trying min-traces (i.e. minsup) " + mid + " l
				// = " + left + " r = " + right);
				// isMaxFound = true;
				// } else {
				// isMaxFound = false;
				// right = mid - 1;
				// System.out
				// .println(">>[R] trying min-traces (i.e. minsup) " + mid + " l
				// = " + left + " r = " + right);
				// }

				if (findClosedPatterns) {
					if (algo.getNumberOfFrequentClosedPatterns() > 0) {
						left = mid + 1;
						System.out.println(
								">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
						isMaxFound = true;
					} else {
						isMaxFound = false;
						right = mid - 1;
						System.out.println(
								">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
					}
				} else {
					if (algo.getNumberOfFrequentPatterns() > 0) {
						left = mid + 1;
						System.out.println(
								">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
						isMaxFound = true;
					} else {
						isMaxFound = false;
						right = mid - 1;
						System.out.println(
								">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
					}
				}

				tries--;
			}

			// if the last mid (or minimum trace/minsup) has zero patterns, then
			// decrement till a value is returned
			if (!isMaxFound) {
				while (mid > 0) {

					mid--;

					support = mid * 1.0 / size;

					AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
					IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

					SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

					relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);

					AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
							executePruningMethods);

					algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
							outputSequenceIdentifiers);
					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

					// if there is output, then increase minsup. Else, decrease
					if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
						break;
					}
				}
			} // else increase mid until there's no output.
			else {
				while (mid < right) {

					mid++;

					support = mid * 1.0 / size;

					AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
					IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

					SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

					relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);

					AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
							executePruningMethods);

					algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
							outputSequenceIdentifiers);
					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);

					// if there is output, then increase minsup. Else, decrease
					if (lines == null || lines.length == 0 || lines[0].isEmpty()) {
						mid--;
						break;
					}
				}
			}

			System.out.println(">>Minimum traces is " + mid);

			return mineClosedSequencesUsingClaSPAlgo(traces, mid);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	protected List<Integer> getTracesIDsFromOutputFile(String fileName) {

		// read the file generated by sequence pattern mining algorithm

		List<Integer> traceIDs = new LinkedList<Integer>();

		String outputFile = fileName.replace(".txt", "_IDs.txt");

		String[] lines = FileManipulator.readFileNewLine(fileName);

		StringBuilder str = new StringBuilder();
		// String separator = " ";
		String fileLinSeparator = System.getProperty("line.separator");
		// traceIDs = new LinkedList<Integer>();

		str.append("trace-ID").append(fileLinSeparator);

		for (String line : lines) {
			// itemSet -1(separator between item sets) #SUP: num(number of
			// traces repeated in) #SID: ids(traces ids)
			// e.g., 36 -1 #SUP: 5 #SID: 403 404 406 409 413

			if (line.isEmpty()) {
				continue;
			}

			// get ids
			String tracesIDsSet = line.split(":")[2];

			String[] tracesIDs = tracesIDsSet.trim().split(" ");

			for (String id : tracesIDs) {
				int idInt = Integer.parseInt(id);

				if (!traceIDs.contains(idInt)) {
					traceIDs.add(idInt);
					str.append(id).append(fileLinSeparator);
				}

			}
		}

		System.out
				.println(">>Number of traces identified as relevant (Shortest & has common partial-traces or states) = "
						+ traceIDs.size());

		writeToFile(str.toString(), outputFile);

		return traceIDs;

	}

	protected void mineSequentialPatternsUsingSPADE(Collection<GraphPath> traces) {

		// convertedInstancesFileName =
		// toSPMFsequentialPatternFormat(traces);
		//
		// String outputPath = clustersOutputFileName;
		// // Load a sequence database
		// double support = 0.01;
		//
		// boolean keepPatterns = true;
		// boolean verbose = false;
		//
		// AbstractionCreator abstractionCreator =
		// AbstractionCreator_Qualitative.getInstance();
		// boolean dfs = true;
		//
		// // if you set the following parameter to true, the sequence ids of
		// the
		// // sequences where
		// // each pattern appears will be shown in the result
		// boolean outputSequenceIdentifiers = true;
		//
		// IdListCreator idListCreator = IdListCreator_FatBitmap.getInstance();
		//
		// CandidateGenerator candidateGenerator =
		// CandidateGenerator_Qualitative.getInstance();
		//
		// SequenceDatabase sequenceDatabase = new
		// SequenceDatabase(abstractionCreator, idListCreator);
		//
		// try {
		//
		// sequenceDatabase.loadFile(convertedInstancesFileName, support);
		//
		//// System.out.println(sequenceDatabase.toString());
		//
		// AlgoSPADE algorithm = new AlgoSPADE(support, dfs,
		// abstractionCreator);
		//
		// algorithm.runAlgorithm(sequenceDatabase, candidateGenerator,
		// keepPatterns, verbose, outputPath,
		// outputSequenceIdentifiers);
		// System.out.println("Minimum support (relative) = " + support);
		// System.out.println(algorithm.getNumberOfFrequentPatterns() + "
		// frequent patterns.");
		//
		// System.out.println(algorithm.printStatistics());
		//
		// // analysis of the generated sequential patterns
		// String analysisFile = clustersOutputFolder +
		// "/sequentialPatternAnalysis.txt";
		//
		// analyseGeneratedSequencePatterns(convertedInstancesFileName,
		// clustersOutputFileName, analysisFile);
		//
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	protected void mineSequentialPatternsUsingTKSAlgo(Collection<GraphPath> traces) {

		/**
		 * http://www.philippe-fournier-viger.com/spmf/TKS.php fastest Top-K sequential
		 * pattern recognition algo
		 */

		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);

		// Load a sequence database
		String input = convertedInstancesFileName;
		String output = clustersOutputFileName;

		int k = 10; // number of sequential patterns to find

		// Create an instance of the algorithm
		AlgoTKS algo = new AlgoTKS();

		// This optional parameter allows to specify the minimum pattern length:
		algo.setMinimumPatternLength(4); // optional

		// This optional parameter allows to specify the maximum pattern length:
		// algo.setMaximumPatternLength(4); // optional

		// This optional parameter allows to specify constraints that some
		// items MUST appear in the patterns found by TKS
		// E.g.: This requires that items 1 and 3 appears in every patterns
		// found
		// algo.setMustAppearItems(new int[] {63});

		// This optional parameter allows to specify the max gap between two
		// itemsets in a pattern. If set to 1, only patterns of contiguous
		// itemsets
		// will be found (no gap).
		algo.setMaxGap(1);

		// if you set the following parameter to true, the sequence ids of the
		// sequences where
		// each pattern appears will be shown in the result
		algo.showSequenceIdentifiersInOutput(true);

		// execute the algorithm, which returns some patterns
		try {

			PriorityQueue<PatternTKS> patterns = algo.runAlgorithm(input, output, k);

			// save results to file
			algo.writeResultTofile(output);
			algo.printStatistics();

			// analysis of the generated sequential patterns
			String analysisFile = clustersOutputFolder + "/sequentialPatternAnalysis.txt";

			analyseGeneratedSequencePatterns(convertedInstancesFileName, clustersOutputFileName, analysisFile);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void analyseGeneratedSequencePatterns(String convertedInstancesFile, String patternsFile,
			String outputFile) {

		// post analysis of output generated by a sequential pattern mining
		// algorithm such as PrefixSpan

		// Create an instance of the algorithm with minsup = 50 %
		AlgoOccur algo = new AlgoOccur();

		// execute the algorithm
		try {

			File file = new File(outputFile);

			if (!file.exists()) {
				file.createNewFile();
			}

			algo.runAlgorithm(convertedInstancesFile, patternsFile, outputFile);
			algo.printStatistics();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// public static String fileToPath(String filename) throws
	// UnsupportedEncodingException {
	// URL url = IncidentInstancesClusterGenerator.class.getResource(filename);
	// System.out.println("tst " + url.toExternalForm());
	// return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	// }

	protected String toSPMFsequentialPatternFormat(Collection<GraphPath> traces) {

		// the format is as follows:
		// state-0 <space> -1 <space> state-1 ... state-n -2
		// where: -1 is a separator between states and -2 indicates the end of
		// sequence

		int stateSeparator = -1;
		int sequenceEndIndicator = -2;
		int i = 0;
		String fileLinSeparator = System.getProperty("line.separator");
		final String DATA_SEPARATOR = " ";

		StringBuilder str = new StringBuilder();

		for (GraphPath sequence : traces) {

			List<Integer> states = sequence.getStateTransitions();

			for (i = 0; i < states.size(); i++) {
				// add state
				str.append(states.get(i)).append(DATA_SEPARATOR).append(stateSeparator).append(DATA_SEPARATOR);
			}

			// add end of sequence
			str.append(sequenceEndIndicator).append(fileLinSeparator);
		}

		writeToFile(str.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	/**************************************/
	/****** FREQUENT ITEMSETS MINING *******/
	/**************************************/

	protected void mineFrequentItemsetsUsingFP_GrowthAlgo() {

		convertedInstancesFileName = toSPMFFrequentItemsetsFormat(traces.values());

		// percentage of traces in which the item set appears
		// e.g., 10% in a 100 traces means that an item set should appear in at
		// least 10 traces out of the 100
		double minsup = 0.5; // means a minsup of 2 transaction (we used a
								// relative support)

		// Applying the FPGROWTH
		AlgoFPGrowth algo = new AlgoFPGrowth();

		// Uncomment the following line to set the maximum pattern length
		// (number of items per itemset, e.g. 3 )
		// algo.setMaximumPatternLength(3);

		try {

			algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, minsup);
			algo.printStats();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected String toSPMFFrequentItemsetsFormat(Collection<GraphPath> traces) {

		// the format is as follows:
		// state-0 <space> state-1 ... state-n

		int i = 0;
		String fileLinSeparator = System.getProperty("line.separator");
		final String DATA_SEPARATOR = " ";

		StringBuilder str = new StringBuilder();

		for (GraphPath sequence : traces) {

			List<Integer> states = sequence.getStateTransitions();

			for (i = 0; i < states.size() - 1; i++) {
				// add state
				str.append(states.get(i)).append(DATA_SEPARATOR);
			}

			// add last state
			str.append(states.get(i)).append(fileLinSeparator);
		}

		writeToFile(str.toString(), convertedInstancesFileName);

		return convertedInstancesFileName;
	}

	/**************** WEKA *********************/
	/*******************************************/
	/*******************************************/

	/********* Utilities *********/
	/**************************/
	/**************************/
	/**************************/

	/******* printers *********/
	/**************************/
	/**************************/
	/**************************/

	public void setTracesFile(String filePath) {

		// System.out.println(filePath);
		// if file path contains \, convert to /
		int tries = 10000;

		while (filePath.contains("\\") && tries > 0) {

			// remove \
			filePath = filePath.replace("\\", File.separator);

			tries--;
		}

		// System.out.println(filePath);

		instanceFileName = filePath;

		// set it for the incident pattern handler
		incidentPatternHandler.setTracesFilePath(instanceFileName);

		if (instanceFileName == null) {
			return;
		}
		String onlyPath = "";
		String onlyName = "";
		boolean isFolderCreated = false;

		int index = instanceFileName.lastIndexOf(File.separator);

		if (index >= 0) {
			onlyPath = instanceFileName.substring(0, instanceFileName.lastIndexOf(File.separator) + 1);
			onlyName = instanceFileName.substring(instanceFileName.lastIndexOf(File.separator) + 1,
					instanceFileName.lastIndexOf("."));
			// set folder
			outputFolder = onlyPath + "miningOutput" + File.separator;

			// make sure output folder is created
			if (outputFolder != null && !outputFolder.isEmpty()) {
				File out = new File(outputFolder);

				if (!out.isDirectory()) {
					isFolderCreated = out.mkdir();
				} else {
					isFolderCreated = true;
				}
			}

		}

		// create output files in output folder
		if (outputFolder != null && !outputFolder.isEmpty() && isFolderCreated) {
			clustersOutputFileName = outputFolder + onlyName + "_relevantTraces.txt";// instanceFileName.replace(".json",
																						// "_relevantTraces.txt");
			// clustersOutputFileName;
			convertedInstancesFileName = outputFolder + onlyName + "_convertedInstances.txt";
			// +
			shortestTracesFileName = outputFolder + onlyName + "_shortestTracesIDs.txt";
		}
		// store files where the traces are
		else {
			clustersOutputFileName = instanceFileName.replace(".json", "_relevantTraces.txt");
			// clustersOutputFileName;
			convertedInstancesFileName = instanceFileName.replace(".json", "_convertedInstances.txt");
			// +
			shortestTracesFileName = instanceFileName.replace(".json", "_shortestTracesIDs.txt");

		}

		// reset data
		if (traces != null) {
			traces.clear();
		}

		resetMiner();
	}

	public void setTraces(List<GraphPath> traces) {

		if (this.traces == null) {
			this.traces = new HashMap<Integer, GraphPath>();
		} else {
			this.traces.clear();
		}

		for (GraphPath trace : traces) {
			this.traces.put(trace.getInstanceID(), trace);
		}
	}

	public void resetMiner() {
		shortestTraceIDs.clear();
		claSPTraceIDs.clear();
		tracesActionsOccurence.clear();
		shortestTraces.clear();
		shortestActionsOccurence.clear();
		shortestStatesOccurence.clear();
		isLoaded = false;
	}

	public int getMinimumTraceLength() {

		if (minimumTraceLength == MIN_LENGTH_INITIAL_VALUE) {
			return 0;
		}

		return minimumTraceLength;
	}

	public int getMaximumTraceLength() {

		if (maximumTraceLength == MAX_LENGTH_INITIAL_VALUE) {
			return 0;
		}

		return maximumTraceLength;
	}

	public int getNumberOfTraces() {

		if (numberOfTraces != -1) {
			return numberOfTraces;
		} else if (traces != null) {
			numberOfTraces = traces.size();
			return numberOfTraces;
		}

		return -1;
	}

	public List<String> getTracesActions() {

		if (tracesActions != null) {
			return Arrays.asList(tracesActions.keySet().toArray(new String[tracesActions.size()]));
		}

		return null;
	}

	public Map<Integer, GraphPath> readInstantiatorInstancesFile(String fileName) {

		if (fileName == null || fileName.isEmpty()) {
			System.err.println("Error reading file: " + fileName + ". File name is empty.");
			return null;
		}

		if (!fileName.endsWith(".json")) {
			System.err.println("Error reading file: " + fileName + ". File should be in JSON format.");
			return null;
		}

		File instancesFile = new File(fileName);

		if (!instancesFile.isFile()) {
			System.err.println(fileName + " is not a file");
			return null;
		}

		Map<Integer, GraphPath> traces = new HashMap<Integer, GraphPath>();

		// int minTraceLength = 1000000;
		// int maxTraceLength = -1;
		// int currentLoadedTracesNum = 0;

		FileReader reader;
		boolean isCompactFormat = true;

		try {

			reader = new FileReader(instancesFile);

			// reading the json file and converting each instance into a
			// GraphPath object
			JSONParser parser = new JSONParser();

			// notify listener of loading json file
			if (listener != null) {
				listener.onLoadingJSONFile();
			}

			JSONObject obj = (JSONObject) parser.parse(reader);

			// check if there are instance generated
			if (obj.containsKey(JSONTerms.INSTANCE_POTENTIAL)) {
				JSONObject objInstances = (JSONObject) obj.get(JSONTerms.INSTANCE_POTENTIAL);

				if (objInstances.containsKey(JSONTerms.INSTANCE_POTENTIAL_COUNT)) {
					// get traces number
					numberOfTraces = Integer.parseInt(objInstances.get(JSONTerms.INSTANCE_POTENTIAL_COUNT).toString());

					// if there's a listener, then notify
					if (listener != null) {
						listener.onNumberOfTracesRead(numberOfTraces);
					}

				}

				// System.out.println("nuuum " + numberOfTraces);
				// check the traces again. if there are traces then read
				// them
				if (objInstances.containsKey(JSONTerms.INSTANCE_POTENTIAL_INSTANCES)) {

					// get traces
					JSONArray aryInstances = (JSONArray) objInstances.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES);

					// each instance currently has an instance_id (integer),
					// transitions (array of integers of states), and actions
					// (sequence of strings that correspond to the sequence of
					// transitions)
					// e.g., {
					// "instance_id":0,
					// "transitions":[1,64,271,937],
					// "actions":["EnterRoom","ConnectBusDevice","CollectData"]
					// }
					// this is a compact format. Another format exists in which
					// transitions are in the format
					// "transitions": [{"action": "EnterRoom", "source":
					// 1,"target": 64},

					// get transitions
					ListIterator<JSONObject> instancesList = aryInstances.listIterator();

					while (instancesList.hasNext()) {
						JSONObject instance = instancesList.next();

						// get instance id
						int instanceID = Integer
								.parseInt(instance.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_ID).toString());

						// get transitions
						JSONArray transitions = (JSONArray) instance
								.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS);

						List<Integer> states = new LinkedList<Integer>();
						List<String> actions = new LinkedList<String>();

						for (int i = 0; i < transitions.size(); i++) {

							Object objState = transitions.get(i);

							try {

								if (isCompactFormat) {
									Integer state = Integer.parseInt(objState.toString());
									// compact format
									states.add(state);
									// totalNumberOfStateOccurrences++;
								} else {
									JSONObject objTransition = (JSONObject) objState;
									// expanded format
									// transition=[{src,trg, action}]
									Integer srcState = Integer.parseInt(objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_SOURCE).toString());
									Integer tgtState = Integer.parseInt(objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_TARGET).toString());
									String actionState = objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTION).toString();

									// add state
									if (!states.contains(srcState)) {
										states.add(srcState);

										// check state occurence
										if (statesOccurrences.containsKey(srcState)) {
											int oldOccurrence = statesOccurrences.get(srcState);
											oldOccurrence++;
											statesOccurrences.put(srcState, oldOccurrence);
										} else { // if not, then create a new
													// entry
											statesOccurrences.put(srcState, 1);
										}

										// totalNumberOfStateOccurrences++;
									}

									if (!states.contains(tgtState)) {
										states.add(tgtState);

										// check state occurence
										if (statesOccurrences.containsKey(tgtState)) {
											int oldOccurrence = statesOccurrences.get(tgtState);
											oldOccurrence++;
											statesOccurrences.put(tgtState, oldOccurrence);
										} else { // if not, then create a new
													// entry
											statesOccurrences.put(tgtState, 1);
										}

										// totalNumberOfStateOccurrences++;

									}

									// add action
									actions.add(actionState);

									totalNumberOfActionOccurrences++;

									// check action occurence
									if (tracesActionsOccurence.containsKey(actionState)) {
										int oldOccurrence = tracesActionsOccurence.get(actionState);
										oldOccurrence++;
										tracesActionsOccurence.put(actionState, oldOccurrence);
									} else { // if not, then create a new entry
										tracesActionsOccurence.put(actionState, 1);
									}
								}

							} catch (NumberFormatException e) {
								isCompactFormat = false;

								// to account for the lost iteration from
								// exception
								i = -1;
							}
						}

						// get actions (if compact)
						if (instance.containsKey(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTIONS)) {
							JSONArray actionsAry = (JSONArray) instance
									.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTIONS);

							for (Object objAction : actionsAry) {

								String tmp = objAction.toString();
								// System.out.println(tmp);

								actions.add(tmp);

								// add to the list of all actions
								// if (tracesActions != null &&
								// !tracesActions.contains(tmp)) {
								// // System.out.println("adding: "+tmp);
								// tracesActions.add(tmp);
								// }

								// check action occurence
								if (tracesActionsOccurence.containsKey(tmp)) {
									int oldOccurrence = tracesActionsOccurence.get(tmp);
									oldOccurrence++;
									tracesActionsOccurence.put(tmp, oldOccurrence);
								} else { // if not, then create a new entry
									tracesActionsOccurence.put(tmp, 1);
								}
							}
						}

						// create a new path/incident
						GraphPath tmpPath = new GraphPath();
						tmpPath.setInstanceID(instanceID);
						tmpPath.setStateTransitions(states);
						tmpPath.setTransitionActions(actions);

						// add to the list
						traces.put(instanceID, tmpPath);

						// notify listener
						if (listener != null) {

							if (traces.size() % tracesLoadedNotifierNumber == 0) {
								listener.onTracesLoaded(tracesLoadedNotifierNumber);
							} else if (traces.size() == numberOfTraces) {
								listener.onTracesLoaded(traces.size() % tracesLoadedNotifierNumber);
							}

						}

						if (actions.size() == 2) {
							System.out.println(instanceID + " " + actions);
						}

						int size = actions.size();

						// set min and max trace lengths
						if (minimumTraceLength > size) {
							minimumTraceLength = size;
						}

						if (maximumTraceLength < size) {
							maximumTraceLength = size;
						}

					}

					reader.close();
				}
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return traces;

	}

	public int getNumberOfActions() {

		if (tracesActionsOccurence != null) {
			return tracesActionsOccurence.size();
		} else {

		}

		return -1;
	}

	protected void generateActionsOccurrences() {

		// === defines the min and max trace lengths, and also actions occurrences in
		// all traces

		if (traces == null) {
			return;
		}

		tracesActionsOccurence = new HashMap<String, Integer>();
		minimumTraceLength = 10000;
		maximumTraceLength = -1;

		for (GraphPath trace : traces.values()) {

			List<String> actions = trace.getTransitionActions();

			if (actions.size() < minimumTraceLength) {
				minimumTraceLength = actions.size();
			}

			if (actions.size() > maximumTraceLength) {
				maximumTraceLength = actions.size();
			}

			for (String act : trace.getTraceActions()) {
				if (tracesActionsOccurence.containsKey(act)) {
					int occur = tracesActionsOccurence.get(act);
					occur++;
					tracesActionsOccurence.put(act, occur);
				} else {
					tracesActionsOccurence.put(act, 1);
				}
			}
		}

	}

	public int getNumberOfStates() {

		if (statesOccurrences != null) {
			return statesOccurrences.size();
		}

		return -1;
	}

	public void setListener(TraceMinerListener listener) {
		this.listener = listener;
	}

	public int getShortestTracesNumber() {

		if (shortestTraces != null) {
			return shortestTraces.size();
		}

		return -1;
	}

	public int getCustomisedTracesNumber() {

		if (customeFilteringTraceIDs != null) {
			return customeFilteringTraceIDs.size();
		}

		return -1;
	}

//	public List<Integer> getTracesWithLength(String op, int length) {
//
//		return getTracesWithLength(op, length, traces);
//	}

//	public List<Integer> getTracesWithLength(String op, int length, Map<Integer, GraphPath> traces) {
//
//		List<Integer> result = new LinkedList<Integer>();
//
//		if (length <= 0) {
//			return null;
//		}
//
//		if (traces == null) {
//			return null;
//		}
//
//		if (traces.isEmpty()) {
//			return result;
//		}
//
//		// if less than minimum
//		if (minimumTraceLength != MIN_LENGTH_INITIAL_VALUE && length < minimumTraceLength) {
//			return result;
//		}
//
//		// if more than maximum
//		if (maximumTraceLength != MAX_LENGTH_INITIAL_VALUE && length > maximumTraceLength) {
//			return result;
//		}
//
//		switch (op) {
//		case TraceViewerController.MORE_THAN_EQUAL:
//			for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
//				int numOfActions = entry.getValue().getTransitionActions().size();
//
//				if (numOfActions >= length) {
//					result.add(entry.getKey());
//				}
//			}
//			break;
//
//		case TraceViewerController.LESS_THAN_EQUAL:
//			for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
//				int numOfActions = entry.getValue().getTransitionActions().size();
//
//				if (numOfActions <= length) {
//					result.add(entry.getKey());
//				}
//			}
//			break;
//
//		case TraceViewerController.EQUAL:
//			for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
//				int numOfActions = entry.getValue().getTransitionActions().size();
//
//				if (numOfActions == length) {
//					result.add(entry.getKey());
//				}
//			}
//			break;
//
//		default:
//			break;
//		}
//
//		customeFilteringTraceIDs = result;
//
//		return result;
//	}

//	public List<Integer> getTracesWithOccurrencePercentage(String op, int percentage) {
//
//		return getTracesWithOccurrencePercentage(op, percentage, traces);
//
//	}

//	public List<Integer> getTracesWithOccurrencePercentage(String op, int percentage, Map<Integer, GraphPath> traces) {
//
//		List<Integer> result = new LinkedList<Integer>();
//
//		if (percentage < 0 || percentage > 100) {
//			return null;
//		}
//
//		if (traces == null || traces == null) {
//			return null;
//		}
//
//		if (traces.isEmpty() || percentage == 0 || traces.isEmpty()) {
//			return result;
//		}
//
//		double percGiven = (percentage * 1.0 / 100);
//		int index = 0;
//
//		System.out.println("given perc: " + percGiven);
//		// all actions of a trace should have a percentage that is [op, e.g.,
//		// more than] the given
//		switch (op) {
//		case TraceViewerController.MORE_THAN_EQUAL:
//			next_trace: for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
//
//				for (String action : entry.getValue().getTransitionActions()) {
//					int occurrence = tracesActionsOccurence.get(action);
//					double perc = (occurrence * 1.0 / traces.size());
//					if (index != 100) {
//						System.out.println("perc: " + perc);
//						index++;
//					}
//
//					// if an action does not satisfy the criterion, then skip to
//					// next trace
//					if (!(perc >= percGiven)) {
//						continue next_trace;
//					}
//				}
//				result.add(entry.getKey());
//
//			}
//			break;
//
//		case TraceViewerController.LESS_THAN_EQUAL:
//			next_trace: for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
//
//				for (String action : entry.getValue().getTransitionActions()) {
//					int occurrence = tracesActionsOccurence.get(action);
//					int perc = (int) Math.floor((occurrence * 1.0 / traces.size()) * 100);
//
//					if (!(perc <= percentage)) {
//						continue next_trace;
//					}
//				}
//				result.add(entry.getKey());
//
//			}
//			break;
//
//		case TraceViewerController.EQUAL:
//			next_trace: for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
//
//				for (String action : entry.getValue().getTransitionActions()) {
//					int occurrence = tracesActionsOccurence.get(action);
//					int perc = (int) Math.floor((occurrence * 1.0 / traces.size()) * 100);
//
//					if (!(perc == percentage)) {
//						continue next_trace;
//					}
//				}
//				result.add(entry.getKey());
//
//			}
//			break;
//
//		default:
//			break;
//		}
//
//		customeFilteringTraceIDs = result;
//
//		return result;
//	}

//	public List<Integer> getTracesWithLengthAndPerc(String lengthOp, int length, String occurOp, int perc) {
//
//		// get traces that satisfy the length first
//		List<Integer> lengthTracesIDs = getTracesWithLength(lengthOp, length);
//
//		Map<Integer, GraphPath> lengthTraces = getTraces(lengthTracesIDs);
//
//		// get the traces that saitsfy the perc using the traces that satisfy
//		// the length
//		List<Integer> result = getTracesWithOccurrencePercentage(occurOp, perc, lengthTraces);
//
//		customeFilteringTraceIDs = result;
//
//		return result;
//
//	}

//	public List<Integer> getTracesWithFilters(String lengthOp, int length, String occurOp, int perc, String query) {
//
//		Map<Integer, GraphPath> traces = null;
//
//		List<Integer> lengthTracesIDs;
//		List<Integer> occurTracesIDs;
//		List<Integer> queryTracesIDs;
//
//		// check length
//		lengthTracesIDs = getTracesWithLength(lengthOp, length);
//
//		// check occurrence
//		if (lengthTracesIDs != null) {
//			// use result from length
//			traces = getTraces(lengthTracesIDs);
//			occurTracesIDs = getTracesWithOccurrencePercentage(occurOp, perc, traces);
//		} else {
//			// use all traces
//			occurTracesIDs = getTracesWithOccurrencePercentage(occurOp, perc);
//		}
//
//		// check actions
//		if (occurTracesIDs != null) {
//			// if occurrence was set
//			traces = getTraces(occurTracesIDs);
//			queryTracesIDs = getTracesWithActions(query, traces);
//		} else if (lengthTracesIDs != null) {
//			// if length was set
//			queryTracesIDs = getTracesWithActions(query, traces);
//		} else {
//			// if no length or occurrence is set
//			queryTracesIDs = getTracesWithActions(query);
//		}
//
//		if (queryTracesIDs != null) {
//			return queryTracesIDs;
//		} else if (occurTracesIDs != null) {
//			return occurTracesIDs;
//		} else {
//			return lengthTracesIDs;
//		}
//
//		// return tracesIDs;
//	}

//	public List<Integer> getTracesWithLengthAndPercAndActions(String lengthOp, int length, String occurOp, int perc,
//			String query) {
//
//		List<Integer> tracesIDs = getTracesWithLengthAndPerc(lengthOp, length, occurOp, perc);
//
//		if (tracesIDs == null) {
//			return null;
//		}
//
//		// get the traces
//		Map<Integer, GraphPath> traces = getTraces(tracesIDs);
//
//		// find traces that match the query
//		List<Integer> result = getTracesWithActions(query, traces);
//
//		return result;
//	}

	public Map<Integer, GraphPath> getTraces(List<Integer> tracesIDs) {

		Map<Integer, GraphPath> result = new HashMap<Integer, GraphPath>();

		if (tracesIDs == null || traces == null) {
			return null;
		}

		if (tracesIDs.isEmpty() || traces.isEmpty()) {
			return result;
		}

		for (Integer id : tracesIDs) {

			if (traces.containsKey(id)) {
				result.put(id, traces.get(id));
			}
		}

		return result;
	}

	public List<Integer> getAllTracesIDs() {

		if (traces != null) {
			return Arrays.asList(traces.keySet().toArray(new Integer[traces.size()]));
		}

		return null;
	}

	public GraphPath getTrace(int traceID) {

		if (traces.containsKey(traceID)) {
			return traces.get(traceID);
		}

		return null;
	}

//	public List<Integer> getTracesWithActions(String query) {
//
//		return getTracesWithActions(query, traces);
//	}
//
//	public List<Integer> getTracesWithActions(String query, Map<Integer, GraphPath> traces) {
//
//		List<Integer> result = new LinkedList<Integer>();
//
//		if (query == null || traces == null) {
//			return null;
//		}
//
//		if (query.isEmpty() || traces.isEmpty()) {
//			return result;
//		}
//
//		// create a query object
//		Query queryEvaluator = new Query(query);
//
//		// generate pattern in query
//		queryEvaluator.generatePattern();
//
//		// int actionsLength = actions.size();
//		// String singleAction = "";
//		// StringBuilder strBldr = new StringBuilder();
//
//		// for(String act: actions) {
//		// strBldr.append(act);
//		// }
//
//		// singleAction = strBldr.toString();
//
//		// String regexString = Pattern.quote(singleAction);
//
//		// Pattern pattern = Pattern.compile(regexString); //+ "(.*?)" +
//		// Pattern.quote(pattern2); ^:new line
//
//		// ==== need to update for ? *
//		for (Entry<Integer, GraphPath> entry : traces.entrySet()) {
//
//			List<String> traceActions = entry.getValue().getTransitionActions();
//
//			if (queryEvaluator.matches(traceActions)) {
//				result.add(entry.getKey());
//			}
//			// if the trace sequence is shorter than given actions
//			// if (actions.size() > traceActions.size()) {
//			// continue;
//			// }
//
//			// strBldr.setLength(0);
//
//			// create one string of the actions in sequence from the beginning
//			// for (int i = 0; i < actionsLength; i++) {
//			// strBldr.append(traceActions.get(i));
//			// }
//
//			// if(pattern.matcher(strBldr.toString()).matches()) {
//			// result.add(entry.getKey());
//			// }
//			// check if both string are equal
//			// if(singleAction.equalsIgnoreCase(strBldr.toString())) {
//			// result.add(entry.getKey());
//			// }
//
//			// check that all actions in the list exist in the trace
//			// if (traceActions.containsAll(actions)) {
//			// result.add(entry.getKey());
//			// }
//
//		}
//
//		customeFilteringTraceIDs = result;
//
//		return result;
//	}

	public List<Integer> getShortestTracesIDs() {

		return shortestTraceIDs;
	}

	public List<Integer> getShortestClaSPTracesIDs() {

		return claSPTraceIDs;
	}

	public List<Integer> getCustomisedTracesIDs() {
		return customeFilteringTraceIDs;
	}

	/**
	 * save a trace as a Trace object
	 * 
	 * @param fileName file path
	 * @param tracesID trace ID
	 * @return
	 */
	public boolean saveTrace(int traceID, String fileName) {

		boolean isSaved = false;

		GraphPath trace = traces.get(traceID);

		if (trace == null) {
			return false;
		}

		Trace newTrace = new Trace();

		for (String actionName : trace.getTransitionActions()) {
			ActionWrapper act = getActionWrapper(actionName);
			newTrace.addAction(act);
		}

		isSaved = newTrace.save(fileName);

		if (isSaved) {
			tracesSaved.put(traceID, fileName);
		}

		return isSaved;
	}

	/**
	 * save a trace as a Trace object
	 * 
	 * @param tracesID trace ID
	 * @return
	 */
	public String saveTrace(int traceID) {

		String name = File.separator + "trace_" + traceID;
		String path = traceFolder + name;

		boolean isSaved = saveTrace(traceID, path);

		if (isSaved) {
			return path;
		}

		return null;

	}

	/**
	 * Saves the given list of trace ids into the given folder
	 * 
	 * @param targetFolder folder to save files to
	 * @param tracesIDs    List of trace ids to save
	 * @return List of trace ids that the methof Failed to save
	 */
	public List<Integer> saveSelectedTraces(String targetFolder, List<Integer> tracesIDs) {

		if (targetFolder == null || tracesIDs == null) {
			return null;
		}

		List<Integer> failedToSaveTraces = new LinkedList<Integer>();
		String name = "";

		traceFolder = targetFolder;

		for (Integer traceID : tracesIDs) {
			name = saveTrace(traceID);

			if (name == null) {
				failedToSaveTraces.add(traceID);
			}
		}

		return failedToSaveTraces;
	}

	/**
	 * Saves the given list of trace ids into the traces folder
	 * 
	 * @param targetFolder folder to save files to
	 * @param tracesIDs    List of trace ids to save
	 * @return List of trace ids that the methof Failed to save
	 */
	public List<Integer> saveSelectedTraces(List<Integer> tracesIDs) {

		if (traceFolder == null) {
			return null;
		}

		List<Integer> failedToSaveTraces = new LinkedList<Integer>();
		String name = "";

		for (Integer traceID : tracesIDs) {
			name = saveTrace(traceID);

			if (name == null) {
				failedToSaveTraces.add(traceID);
			}
		}

		// if (listener != null) {
		//
		// listener.onSavingFilteredTracesComplete(isSaved);
		// }

		return failedToSaveTraces;
	}

	public boolean saveTraces(String fileName, List<Integer> tracesIDs) {

		if (fileName == null || tracesIDs == null) {
			return false;
		}

		boolean isSaved = false;

		Map<Integer, GraphPath> traces = getTraces(tracesIDs);

		String[] dummy = new String[0];
		// dummy[0] = "dummy";
		List<GraphPath> paths = Arrays.asList(traces.values().toArray(new GraphPath[traces.size()]));

		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();

		InstancesSaver tracesSaver = ins.new InstancesSaver(-1, fileName, dummy, dummy, paths);
		try {
			// ForkJoinPool mainPool = new ForkJoinPool();
			// int res = mainPool.submit(tracesSaver).get();
			int res = tracesSaver.call();

			if (res == InstancesSaver.SUCCESSFUL) {
				isSaved = true;
			} else if (res == InstancesSaver.UNSUCCESSFUL) {
				isSaved = false;

			}

			// mainPool.shutdown();

			// if it returns false then maximum waiting time is reached
			// if (!mainPool.awaitTermination(24, TimeUnit.DAYS)) {
			// System.err.println("Time out! saving traces took more than
			// specified maximum time ["
			// + 24 + " " + TimeUnit.DAYS + "]");
			// }

			if (listener != null) {
				listener.onSavingFilteredTracesComplete(isSaved);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return isSaved;
	}

	/**
	 * Checks whether the given trace id is saved or not
	 * 
	 * @param traceID
	 * @return
	 */
	public boolean isTraceSaved(int traceID) {

		return tracesSaved.containsKey(traceID);

	}

	/**
	 * checks if traces (as instances in a json file) are loaded or not
	 * 
	 * @return true if loaded. Otherwise false
	 */
	public boolean areTracesLoaded() {

		return isLoaded;
	}

	/**
	 * Finds all entities (i.e. Classes/Controls) between all traces. It excludes
	 * entities given in the excluding list.
	 * 
	 * @param traces    The list of traces to search for common entities
	 * @param excluding The list of entities to exclude
	 * @param topK      The top K entities to find
	 * @return The list of top K entities in the given traces with their occurrence
	 *         in a descending order i.e. highest to lowest
	 */
	public List<Map.Entry<String, Long>> findAllEntities(Collection<GraphPath> traces, List<String> excluding) {

		if (!isBigraphERFileSet()) {
			setBigraphERFile(bigraphERFile);
		}

		// finds the top (with Occurrence) in the given trace
		// List<GraphPath> traces = new LinkedList<GraphPath>();
		// traces.add(trace);
		List<String> actionsEntities = new LinkedList<String>();

		// reset number of all entities
		totalNumberOfEntitiesInCurrentTraces = 0;

		for (GraphPath trace : traces) {
			actionsEntities.addAll(convertToEntities(trace, excluding));
		}

		Map<String, Long> map = actionsEntities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		List<Map.Entry<String, Long>> result = map.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());

		// convertedInstancesFileName = convertUsingActionEntities(traces);

		// generateClustersUsingTextMining();

		return result;
	}

	/**
	 * Finds top common entities (i.e. Classes/Controls) between all traces. It
	 * excludes entities given in the excluding list. It limits to topK entities
	 * 
	 * @param traces    The list of traces to search for common entities
	 * @param excluding The list of entities to exclude
	 * @param topK      The top K entities to find
	 * @return The list of top K entities in the given traces with their occurrence
	 */
	public List<Map.Entry<String, Long>> findTopCommonEntities(Collection<GraphPath> traces, List<String> excluding,
			int topK) {

		if (!isBigraphERFileSet()) {
			setBigraphERFile(bigraphERFile);
		}

		// finds the top (with Occurrence) in the given trace
		// List<GraphPath> traces = new LinkedList<GraphPath>();
		// traces.add(trace);
		List<String> actionsEntities = new LinkedList<String>();

		// reset number of all entities
		totalNumberOfEntitiesInCurrentTraces = 0;

		for (GraphPath trace : traces) {
			actionsEntities.addAll(convertToEntities(trace, excluding));
		}

		Map<String, Long> map = actionsEntities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		List<Map.Entry<String, Long>> result = map.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(topK).collect(Collectors.toList());

		// convertedInstancesFileName = convertUsingActionEntities(traces);

		// generateClustersUsingTextMining();

		return result;
	}

	/**
	 * Finds lowest common entities (i.e. Classes/Controls) between all traces. It
	 * excludes entities given in the excluding list. It limits to topK entities
	 * 
	 * @param traces    The list of traces to search for common entities
	 * @param excluding The list of entities to exclude
	 * @param topK      The top K entities to find
	 * @return The list of top K entities in the given traces with their occurrence
	 */
	public List<Map.Entry<String, Long>> findLowestCommonEntities(Collection<GraphPath> traces, List<String> excluding,
			int topK) {

		if (!isBigraphERFileSet()) {
			setBigraphERFile(bigraphERFile);
		}

		// finds the top (with Occurrence) in the given trace
		// List<GraphPath> traces = new LinkedList<GraphPath>();
		// traces.add(trace);
		List<String> actionsEntities = new LinkedList<String>();
		// List<Map.Entry<String, Long>> result = new
		// LinkedList<Map.Entry<String,Long>>();

		// reset number of all entities
		totalNumberOfEntitiesInCurrentTraces = 0;

		for (GraphPath trace : traces) {
			actionsEntities.addAll(convertToEntities(trace, excluding));
		}

		Map<String, Long> map = actionsEntities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		List<Map.Entry<String, Long>> result = map.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.naturalOrder())).limit(topK).collect(Collectors.toList());

		// convertedInstancesFileName = convertUsingActionEntities(traces);

		// generateClustersUsingTextMining();

		return result;
	}

	/**
	 * converts the given trace into a list of entities that it contains
	 * 
	 * @param trace
	 * @param excluding The list of entities to exclude from the list
	 * @return List of entities (Classes/Controls)
	 */
	public List<String> convertToEntities(GraphPath trace, List<String> excluding) {

		List<String> actions = trace.getTransitionActions();

		List<String> actionEntities = new LinkedList<String>();

		for (String act : actions) {

			ActionWrapper actionDetails = bigraphERActions.get(act);

			// allow repetition of entities

			// System.out.println("Entities of " + act);
			if (actionDetails != null) {

				// add entities from pre
				BigraphWrapper pre = actionDetails.getPrecondition();

				if (pre != null) {
					// System.out.println("Pre entities: ");

					Set<Entity> ents = pre.getControlMap().keySet();

					for (Entity ent : ents) {

						String name = ent.getName();

						// if it is a term to exclude, then continue
						if (excluding != null && excluding.contains(name)) {
							continue;
						}

						// if it is a reaction name that is used to identify the
						// action (i.e. an extra)
						if (name.equalsIgnoreCase(act)) {
							continue;
						}

						// System.out.println("\t-"+name);
						actionEntities.add(name);
					}
				}

				// add entities from post
				BigraphWrapper post = actionDetails.getPostcondition();

				if (post != null) {

					// System.out.println("Post entities: ");

					Set<Entity> ents = post.getControlMap().keySet();

					for (Entity ent : ents) {

						String name = ent.getName();

						if (excluding != null && excluding.contains(name)) {
							continue;
						}

						// if it is a reaction name that is used to identify the
						// action (i.e. an extra)
						if (name.equalsIgnoreCase(act)) {
							continue;
						}

						// System.out.println("\t-"+name);
						actionEntities.add(name);
					}
				}
			}
		}

		totalNumberOfEntitiesInCurrentTraces += actionEntities.size();

		return actionEntities;

	}

	/**
	 * Set the path to the *.big file
	 * 
	 * @param bigraphERFile file path
	 */
	public void setBigraphERFile(String bigraphERFile) {

		brsParser = new BRSParser();

		this.bigraphERFile = bigraphERFile;

		brsWrapper = brsParser.parseBigraphERFile(bigraphERFile);
		bigraphERActions = brsWrapper.getActions();
	}

	public String getBigraphERFile() {
		return bigraphERFile;
	}

	public boolean isBigraphERFileSet() {

		if (bigraphERFile == null || bigraphERFile.isEmpty()) {
			return false;
		}

		return true;

	}

	public int getTotalNumberOfEntitiesInCurrentTraces() {

		return totalNumberOfEntitiesInCurrentTraces;
	}

	public String getStatesFolder() {
		return statesFolder;
	}

	public void setStatesFolder(String statesFolder) {
		this.statesFolder = statesFolder;
	}

	public void setTraceFolder(String traceFolder) {
		this.traceFolder = traceFolder;
	}

	public String getTraceFolder() {
		return traceFolder;
	}

	public ActionWrapper getActionWrapper(String actionName) {

		if (bigraphERActions == null) {
			return null;
		}

		return bigraphERActions.get(actionName);
	}

	public TransitionSystem getTransitionSystem() {

		if (transitionSystem == null) {
			loadTransitionSystem();
		}

		return transitionSystem;
	}

	public void setTransitionSystemFilePath(String filePath) {
		transitionSystemFilePath = filePath;
	}

	public TransitionSystem loadTransitionSystem() {

		if (transitionSystemFilePath == null) {
			return null;
		}

		transitionSystem = new TransitionSystem(transitionSystemFilePath);

		return transitionSystem;

	}

	public String createNewLabelledTransitionFile() {

		String outputFolder = null;

		if (transitionSystemFilePath != null) {
			if (transitionSystemFilePath.contains(File.separator)) {
				outputFolder = transitionSystemFilePath.substring(0,
						transitionSystemFilePath.lastIndexOf(File.separator));
			} else {
				outputFolder = transitionSystemFilePath.substring(0, transitionSystemFilePath.lastIndexOf("/"));
			}

		}

		File out = new File(outputFolder);

		if (!out.isDirectory()) {
			// check states folder
			if (statesFolder != null) {
				outputFolder = statesFolder;
			} else {
				if (defaultOutputFolder != null) {
					outputFolder = defaultOutputFolder.getPath();
				} else {
					outputFolder = ".";
				}

			}

		}

		if (bigraphERActions == null) {
			System.err.println("TraceMiner::createNewLabelledTransitionFile: BigraphER file is not set");
			return null;
		}

		Set<String> actions = bigraphERActions.keySet();

		LabelExtractor ext = new LabelExtractor(transitionSystem, outputFolder);

		ext.updateDigraphLabels(actions.toArray(new String[actions.size()]));
		transitionSystemFilePath = ext.createNewLabelledTransitionFile();

		loadTransitionSystem();

		return transitionSystemFilePath;
	}

	public double getStatePercentage(int state, int tracesCategory) {

		if (!statesOccurrences.containsKey(state)) {
			return -1;
		}

		double perc = 0;

		switch (tracesCategory) {
		case ALL:
			// find a state perc in all traces

			int stateOuccr = statesOccurrences.get(state);
			int totalNumOfTraces = traces.size();
			if (totalNumOfTraces > 0) {
				perc = stateOuccr * 1.0 / totalNumOfTraces;
			} else {
				perc = 1;
			}

			break;

		case SHORTEST:

			break;
		case CLASP:

			break;
		default:
			break;
		}

		return perc;
	}

	/**
	 * Returns the action occurrence of the given action name. Occurrence is the
	 * percentage of the action appearance in the given traces (i.e tracesCategory)
	 * 
	 * @param actionName
	 * @param tracesCategory (All, Shortest, Clasp)
	 * @return percentage
	 */
	public double getActionOccurrencePercentage(String actionName, int tracesCategory) {

		if (!tracesActionsOccurence.containsKey(actionName)) {
			return -1;
		}

		double perc = 0;

		switch (tracesCategory) {
		case ALL:
			// find a state perc in all traces

			int actionOuccr = tracesActionsOccurence.get(actionName);
			int totalNumOfTraces = traces.size();

			if (totalNumOfTraces > 0) {
				perc = actionOuccr * 1.0 / totalNumOfTraces;
				// System.out.println("total: " + totalNumOfTraces + "\naction
				// occur:" + actionOuccr + "\nperc: " + perc);
			} else {
				perc = 1;
			}

			break;

		case SHORTEST:

			break;
		case CLASP:

			break;
		default:
			break;
		}

		return perc;
	}

	public int getStateOccurrence(int state, int tracesCategory) {

		switch (tracesCategory) {
		case ALL:
			// find a state perc in all traces

			if (statesOccurrences.containsKey(state)) {
				return statesOccurrences.get(state);
			} else {
				return -1;
			}

		case SHORTEST:

			break;
		case CLASP:

			break;
		default:
			return -1;
		}

		return -1;
	}

	public int getActionOccurrence(String actionName, int tracesCategory) {

		switch (tracesCategory) {
		case ALL:
			// find a state perc in all traces

			if (tracesActionsOccurence.containsKey(actionName)) {
				return tracesActionsOccurence.get(actionName);
			} else {
				return -1;
			}

		case SHORTEST:

			break;
		case CLASP:

			break;
		default:
			return -1;
		}

		return -1;
	}

	public int getStateOccurrence(int state) {

		if (statesOccurrences.containsKey(state)) {
			return statesOccurrences.get(state);
		} else {
			return -1;
		}
	}

	public int getTotalNumberOfStateOccurrences() {

		totalNumberOfStateOccurrences = 0;

		for (Integer occurs : statesOccurrences.values()) {
			totalNumberOfStateOccurrences += occurs;
		}

		return totalNumberOfStateOccurrences;
	}

	public int getTotalNumberOfActionOccurrences() {

		totalNumberOfActionOccurrences = 0;

		for (Integer occurs : tracesActionsOccurence.values()) {
			totalNumberOfActionOccurrences += occurs;
		}

		return totalNumberOfActionOccurrences;
	}

	public List<Integer> getCurrentShownTraces() {
		return currentShownTraces;
	}

	public void setCurrentShownTraces(List<Integer> currentShownTraces) {
		this.currentShownTraces = currentShownTraces;
	}

	public List<Integer> findTracesWithStates(List<Integer> states, List<Integer> tracesIDsToSearch) {

		if (states == null || tracesIDsToSearch == null) {
			return null;
		}

		List<Integer> result = new LinkedList<Integer>();

		// get traces
		Map<Integer, GraphPath> tracesToSearch = getTraces(tracesIDsToSearch);

		if (tracesToSearch == null || tracesToSearch.isEmpty()) {
			return result;
		}

		for (Entry<Integer, GraphPath> traceEntry : tracesToSearch.entrySet()) {
			List<Integer> traceStates = traceEntry.getValue().getStateTransitions();

			int subIndex = Collections.indexOfSubList(traceStates, states);

			if (subIndex != -1) {
				result.add(traceEntry.getKey());
			}
		}

		return result;
	}

	/**
	 * Finds all traces that contain the given states (not necessarly consecutive)
	 * 
	 * @param states
	 * @param tracesIDsToSearch
	 * @return
	 */
	public List<Integer> findTracesContainingStates(List<Integer> states, List<Integer> tracesIDsToSearch,
			boolean isInOrder) {

		// a trace should contain all states
		// the order should be preserved e.g., state-1 index should be more than
		// that of state-0

		if (states == null || tracesIDsToSearch == null) {
			return null;
		}

		List<Integer> result = new LinkedList<Integer>();

		// get traces
		Map<Integer, GraphPath> tracesToSearch = getTraces(tracesIDsToSearch);

		if (tracesToSearch == null || tracesToSearch.isEmpty()) {
			return result;
		}

		trace_loop: for (Entry<Integer, GraphPath> traceEntry : tracesToSearch.entrySet()) {
			List<Integer> traceStates = traceEntry.getValue().getStateTransitions();

			// int subIndex = Collections.indexOfSubList(traceStates, states);

			// it should contain all given states
			if (traceStates.containsAll(states)) {
				// it should be in order

				if (isInOrder) {
					int index = -1;
					for (int i = 0; i < states.size(); i++) {
						int newIndex = traceStates.indexOf(states.get(i));

						// if the current index of the state is less than or
						// equal
						// than the last one then skip
						if (newIndex <= index) {
							continue trace_loop;
						}
					}
				}
				// if this point is reached then the trace matches the given
				// states
				result.add(traceEntry.getKey());
			}
		}

		return result;
	}

	/**
	 * Finds all traces that have the given startState and contain the given states
	 * (not necessarly consecutive) and ends with the endState
	 * 
	 * @param states
	 * @param tracesIDsToSearch
	 * @return
	 */
	public List<Integer> findTracesContainingStates(int startState, List<Integer> inBetweenStates, int endState,
			List<Integer> tracesIDsToSearch, boolean isInOrder) {

		// a trace should contain all states
		// the order should be preserved e.g., state-1 index should be more than
		// that of state-0

		// if (states == null || tracesIDsToSearch == null) {
		// return null;
		// }

		List<Integer> result = new LinkedList<Integer>();

		// get traces
		Map<Integer, GraphPath> tracesToSearch = getTraces(tracesIDsToSearch);

		if (tracesToSearch == null || tracesToSearch.isEmpty()) {
			return result;
		}

		trace_loop: for (Entry<Integer, GraphPath> traceEntry : tracesToSearch.entrySet()) {
			LinkedList<Integer> traceStates = traceEntry.getValue().getStateTransitions();

			// int subIndex = Collections.indexOfSubList(traceStates, states);

			// it should start with the start state and end with the end state
			// and contain all given states
			if (traceStates != null && (startState == -1 || traceStates.getFirst() == startState)
					&& (endState == -1 || traceStates.getLast() == endState)
					&& (inBetweenStates == null || traceStates.containsAll(inBetweenStates))) {
				// it should be in order

				if (isInOrder && inBetweenStates != null) {
					int index = -1;
					for (int i = 0; i < inBetweenStates.size(); i++) {
						int newIndex = traceStates.indexOf(inBetweenStates.get(i));

						// if the current index of the state is less than or
						// equal
						// than the last one then skip
						if (newIndex <= index) {
							continue trace_loop;
						}
					}
				}
				// if this point is reached then the trace matches the given
				// states
				result.add(traceEntry.getKey());
			}
		}

		return result;
	}

	/**
	 * Finds all traces that contain the given actions (not necessarly in order)
	 * 
	 * @param actions
	 * @param tracesIDsToSearch
	 * @return
	 */
	public List<Integer> findTracesContainingActions(List<String> actionNames, List<Integer> tracesIDsToSearch,
			boolean isInOrder) {

		if (actionNames == null || tracesIDsToSearch == null) {
			return null;
		}

		// to lower case
		actionNames.replaceAll(String::trim);
		actionNames.replaceAll(String::toLowerCase);

		List<Integer> result = new LinkedList<Integer>();

		// get traces
		Map<Integer, GraphPath> tracesToSearch = getTraces(tracesIDsToSearch);

		if (tracesToSearch == null || tracesToSearch.isEmpty()) {
			return result;
		}

		trace_loop: for (Entry<Integer, GraphPath> traceEntry : tracesToSearch.entrySet()) {
			List<String> traceActions = new LinkedList<String>(traceEntry.getValue().getTransitionActions());

			// to lower case
			traceActions.replaceAll(String::toLowerCase);

			// it should contain all given states
			if (traceActions.containsAll(actionNames)) {

				// if should be in order
				if (isInOrder) {
					int index = -1;
					for (int i = 0; i < actionNames.size(); i++) {
						int newIndex = traceActions.indexOf(actionNames.get(i));

						// if the current index of the state is less than or
						// equal
						// than the last one then skip
						if (newIndex <= index) {
							continue trace_loop;
						}
					}
				}

				// if this point is reached then the trace matches the given
				// states
				result.add(traceEntry.getKey());
			}
		}

		return result;
	}

	/**
	 * Finds all traces that contain the given entities (not necessarly in order)
	 * 
	 * @param actions
	 * @param tracesIDsToSearch
	 * @return
	 */
	public List<Integer> findTracesContainingEntities(List<String> entityNames, List<Integer> tracesIDsToSearch,
			boolean isInOrder) {

		if (entityNames == null || tracesIDsToSearch == null) {
			return null;
		}

		// to lower case
		entityNames.replaceAll(String::trim);
		entityNames.replaceAll(String::toLowerCase);

		List<Integer> result = new LinkedList<Integer>();

		// get traces
		Map<Integer, GraphPath> tracesToSearch = getTraces(tracesIDsToSearch);

		if (tracesToSearch == null || tracesToSearch.isEmpty()) {
			return result;
		}

		trace_loop: for (Entry<Integer, GraphPath> traceEntry : tracesToSearch.entrySet()) {
			List<String> traceEntities = convertToEntities(traceEntry.getValue(), JSONTerms.BIG_IRRELEVANT_TERMS);

			// to lower case
			traceEntities.replaceAll(String::toLowerCase);

			// it should contain all given states
			if (traceEntities.containsAll(entityNames)) {

				// if should be in order
				if (isInOrder) {
					int index = -1;
					for (int i = 0; i < entityNames.size(); i++) {
						int newIndex = traceEntities.indexOf(entityNames.get(i));

						// if the current index of the state is less than or
						// equal
						// than the last one then skip
						if (newIndex <= index) {
							continue trace_loop;
						}
					}
				}

				// if this point is reached then the trace matches the given
				// states
				result.add(traceEntry.getKey());
			}
		}

		return result;
	}

	/**
	 * Finds the causal dependency chain between actions in the given incident trace
	 * 
	 * @param incidentTrace a GraphPath object representing the potential incident
	 * @return A map of the causal dependency between actions in the given potential
	 *         incident. Key is the action name, value is a boolean whether the
	 *         action depends on the previous action or not
	 */
	protected Map<String, Boolean> findCausalDependency(GraphPath incidentTrace) {

		if (incidentTrace == null) {
			return null;
		}

		Map<String, Boolean> causalDependencyMap = new HashMap<String, Boolean>();

		return causalDependencyMap;
	}

	/**
	 * Determines if the action is causally dependent on the preAction Causally
	 * dependence means that the action would not have happened if the preAction had
	 * not happened.
	 * 
	 * @param preAction the previous action
	 * @param action    the action that we want to determine if it is causally
	 *                  dependent on preAction
	 * @return True if action is causally dependent on the preAction. False
	 *         otherwise
	 */
	public int areActionsCausallyDependent(String action, String preAction, int actionPreState, int preActionPreState,
			int originalPre, int originalPost) {

		/**
		 * Causally dependence is implemented by checking if the pre-condition of the
		 * given action is matches to the state that the preAction's pre-condition
		 * matches to. If it matches then the action is NOT causally dependent.
		 * Otherwise, it is causally dependent
		 **/

		if (action == null || preAction == null || brsWrapper == null) {
			System.err.println("Error. there's a null");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		ActionWrapper actionWrapper = bigraphERActions.get(action);

		// ActionWrapper preActionWrapper = bigraphERActions.get(preAction);

		if (actionWrapper == null) {
			System.err.println("Error. there's a null of wrappers");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		BigraphWrapper preWrapper = actionWrapper.getPrecondition();

		// System.out.println(preWrapper.getContainedEntitiesMap());
		// === get Bigraph representation of the precondition of action
		Bigraph actionPre = preWrapper != null ? preWrapper.getBigraphObject(false, brsWrapper.getSignature()) : null;

		if (actionPre == null) {
			System.err.println("precondition of the given action [" + action + "] is NULL");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		// === get preAction's precondition State as Bigraph
		if (getStatesFolder() == null) {
			System.err.println("States folder is missing");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		Signature sig = brsWrapper.getSignature();
		// load the previous action pre state
		Bigraph preStateBig = loadState(preActionPreState, sig);

		// load action pre state
		Bigraph actionPreStateBig = loadState(actionPreState, sig);

		// testinggg
		Bigraph actionOrigPreStateBig = loadState(originalPre, sig);
		Bigraph actionOrigPostStateBig = loadState(originalPost, sig);

//		Bigraph actionPostStateBig = loadState(actionPostState);

		if (preStateBig == null) {
			System.err.println("preState Bigraph object is NULL");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		// add to temp storage
		// addBigraphStateToTemp(preState, preStateBig);

		// === match the action precondition to the state
		Matcher matcher = new Matcher();

		// if (action.equalsIgnoreCase("connectbusdevice")) {
		// System.out.println("+++++++++ Condition +++++++++");
		// System.out.println("-stmt: " +
		// actionWrapper.getPrecondition().getBigraphERString()+"\n");
		// System.out.println("-Bigraph:\n"+actionPre);
		// System.out.println("+++++++++\nState: " + preStateBig +
		// "\n+++++++++\n");
		// }

		int countPreAction = 0;
		int countAction = 0;
		boolean isNotCausallyDependentByLTS = false;

		// alternative way is to check if the action is triggered by the
		// preActionPreState in the transition system
		if (transitionSystem != null) {
			Digraph<Integer> graph = transitionSystem.getDigraph();

			if (graph != null) {
				List<Integer> outgoingNeighbors = graph.outboundNeighbors(preActionPreState);

				for (Integer node : outgoingNeighbors) {
					String nodeAction = graph.getLabel(preActionPreState, node);

					// the action is found to be triggered by the pre state of
					// the previous action
					if (nodeAction.equalsIgnoreCase(action)) {
						// System.out.println("traceMiner:: [" + action + "] is
						// NOT causally dependent on [" + preAction
						// + "] with pre-state [" + preActionPreState + "]");
						isNotCausallyDependentByLTS = true;
						break;
					}
				}

				// if this point is reached then the action is causally
				// dependent
				// if (!isNotCausallyDependentByLTS) {
				// System.out.println("traceMiner:: [" + action + "] is causally
				// dependent on [" + preAction
				// + "] with pre-state [" + preActionPreState + "]");
				// }
			}
		}

		// finding causality by matching bigraphs
		// if (!isCausallyDependent) {
		Iterator it = matcher.match(preStateBig, actionPre).iterator();

		Iterator itAction = matcher.match(actionPreStateBig, actionPre).iterator();

		Iterator itOrigPre = matcher.match(actionOrigPreStateBig, actionPre).iterator();
		Iterator itOrigPost = matcher.match(actionOrigPostStateBig, actionPre).iterator();

		int cntOrigPost = 0;
		int cntOrigPre = 0;

		while (itOrigPost.hasNext()) {
			// a match means that there's no dependency
			// return ACTIONS_NOT_CAUSALLY_DEPENDENT;
			itOrigPost.next();
			cntOrigPost++;
		}

		while (itOrigPre.hasNext()) {
			// a match means that there's no dependency
			// return ACTIONS_NOT_CAUSALLY_DEPENDENT;
			itOrigPre.next();
			cntOrigPre++;
		}

		System.out.println("Orignal pre[" + originalPre + "], post[" + originalPost + "]: pre = " + cntOrigPre
				+ " post = " + cntOrigPost);

		while (it.hasNext()) {
			// a match means that there's no dependency
			// return ACTIONS_NOT_CAUSALLY_DEPENDENT;
			it.next();
			countPreAction++;
		}

		while (itAction.hasNext()) {
			// a match means that there's no dependency
			// return ACTIONS_NOT_CAUSALLY_DEPENDENT;
			itAction.next();
			countAction++;
		}
		// }

		if (countPreAction > 0) {
			if (countPreAction == countAction) {
				if (isNotCausallyDependentByLTS) {
					// not causally dependent by LTS and Bigraph matching
//					System.out.println("same cnt: " + countAction +" post-cnt: " + cntPost);
					return NOT_CAUSALLY_DEPENDENT;
				} else {
					// not causally dependent only by Bigraph matching
					return POTENTIALLY_NOT_CAUSALLY_DEPENDENT;
				}
			}

			if (isNotCausallyDependentByLTS) {
				// not necessarily causally dependent by LTS and Bigraph
				// matching
				return NOT_NECESSARILY_CAUSALLY_DEPENDENT;
			} else {
				// not necessarily causally dependent only by Bigraph matching
				return POTENTIALLY_NOT_NECESSARILY_CAUSALLY_DEPENDENT;
			}

		} else if (isNotCausallyDependentByLTS) {
			// not causally dependent by LTS only
			return NOT_CAUSALLY_DEPENDENT_BY_LTS;
		}

		// action is dependent on the previous action
		return CAUSALLY_DEPENDENT;
	}

	// protected void addBigraphStateToTemp(int state, Bigraph big) {
	//
	// if(loadedStateBigraphs.containsKey(state)) {
	// return;
	// }
	//
	// loadedStateBigraphs.put(state, big);
	//
	// //check size
	// if(loadedStateBigraphs.size() > MAX_TEMP_STORAGE_SIZE) {
	// //remove 4 states
	// Random rand = new Random();
	// int index = rand.nextInt(loadedStateBigraphs.size());
	//
	// for(int i =index;i<loadedStateBigraphs.size();i++) {
	// if(i )
	// }
	//
	// }
	// }

	public Bigraph loadState(int stateID, Signature signature) {

		// get preAction's precondition State as Bigraph
		if (getStatesFolder() == null) {
			System.err.println("States folder is missing");
			return null;
		}

		JSONObject state;
		JSONParser parser = new JSONParser();

		String filePath = getStatesFolder() + "/" + stateID + ".json";
		try {
			// read state from file
			FileReader r = new FileReader(filePath);
			state = (JSONObject) parser.parse(r);
			Bigraph bigraph = convertJSONtoBigraph(state, signature);
			r.close();

			return bigraph;

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.err.println("File [" + filePath + "] is missing.");
		}

		return null;
	}

	public Bigraph loadState(int stateID) {

		if (brsWrapper == null) {
			System.err.println("TraceMiner::loadState: BRS Wrapper is missing.");
			return null;
		}

		// get preAction's precondition State as Bigraph
		if (getStatesFolder() == null) {
			System.err.println("States folder is missing");
			return null;
		}

		JSONObject state;
		JSONParser parser = new JSONParser();

		String filePath = getStatesFolder() + "/" + stateID + ".json";
		try {
			// read state from file
			FileReader r = new FileReader(filePath);
			state = (JSONObject) parser.parse(r);
			Bigraph bigraph = convertJSONtoBigraph(state, brsWrapper.getSignature());
			r.close();

			return bigraph;

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.err.println("File [" + filePath + "] is missing.");
		}

		return null;
	}

	public Bigraph convertJSONtoBigraph(JSONObject state, Signature signature) {

		String tmp;
		String tmpArity;
		JSONObject tmpObj;
		JSONObject tmpCtrl;
		HashMap<String, BigraphNode> nodes = new HashMap<String, BigraphNode>();
		BigraphNode node;
		JSONArray ary;
		JSONArray innerAry;
		JSONArray outerAry;
		JSONArray portAry;
		Iterator<JSONObject> it;
		Iterator<JSONObject> itInner;
		Iterator<JSONObject> itOuter;
		Iterator<JSONObject> itPort;
		int src, target;
		LinkedList<String> outerNames = new LinkedList<String>();
		LinkedList<String> innerNames = new LinkedList<String>();
		LinkedList<String> outerNamesFull = new LinkedList<String>();
		LinkedList<String> innerNamesFull = new LinkedList<String>();

		HashMap<String, OuterName> libBigOuterNames = new HashMap<String, OuterName>();
		HashMap<String, InnerName> libBigInnerNames = new HashMap<String, InnerName>();
		HashMap<String, Node> libBigNodes = new HashMap<String, Node>();
		LinkedList<Root> libBigRoots = new LinkedList<Root>();
		LinkedList<Site> libBigSites = new LinkedList<Site>();

		// number of roots, sites, and nodes respectively
		int numOfRoots = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH))
				.get(JSONTerms.BIGRAPHER_NUM_REGIONS).toString());
		int numOfSites = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH))
				.get(JSONTerms.BIGRAPHER_NUM_SITES).toString());
		int numOfNodes = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH))
				.get(JSONTerms.BIGRAPHER_NUM_NODES).toString());
		int edgeNumber = 0;

		// get controls & their arity [defines signature]. Controls are
		// assumed
		// to be active (i.e. true)
		ary = (JSONArray) state.get(JSONTerms.BIGRAPHER_NODES);
		it = ary.iterator();
		while (it.hasNext()) {
			node = new BigraphNode();
			tmpObj = (JSONObject) it.next(); // gets hold of node info

			tmpCtrl = (JSONObject) tmpObj.get(JSONTerms.BIGRAPHER_CONTROL);
			tmp = tmpCtrl.get(JSONTerms.BIGRAPHER_CNTRL_NAME).toString();
			tmpArity = tmpCtrl.get(JSONTerms.BIGRAPHER_CNTRL_ARITY).toString();

			// set node id
			node.setId(tmpObj.get(JSONTerms.BIGRAPHER_NODE_ID).toString());
			// set node control
			node.setControl(tmp);
			nodes.put(node.getId(), node);
		}

		// get parents for nodes from the place_graph=>
		// roots and sites numbers
		ary = (JSONArray) ((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_ROOT_NODE);
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			src = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_SOURCE).toString());
			target = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_TARGET).toString());
			nodes.get(Integer.toString(target)).setParentRoot(src);
		}

		ary = (JSONArray) ((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_ROOT_SITE);
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			src = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_SOURCE).toString());
			target = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_TARGET).toString());
			nodes.get(Integer.toString(target)).setParentRoot(src);
		}

		ary = (JSONArray) ((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_NODE_NODE);
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			src = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_SOURCE).toString());
			target = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_TARGET).toString());

			// set parent node in the target node
			nodes.get(Integer.toString(target)).setParent(nodes.get(Integer.toString(src)));
			// add child node to source node
			nodes.get(Integer.toString(src)).addChildNode(nodes.get(Integer.toString(target)));

		}

		// get outer names and inner names for the nodes. Currently, focus
		// on
		// outer names
		// while inner names are extracted they are not updated in the nodes
		ary = (JSONArray) (state.get(JSONTerms.BIGRAPHER_LINK_GRAPH));
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			outerNames.clear();
			innerNames.clear();

			// get outer names
			outerAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_OUTER));
			innerAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_INNER));
			portAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_PORTS));

			// get outernames
			for (int i = 0; i < outerAry.size(); i++) {
				JSONObject tmpOuter = (JSONObject) outerAry.get(i);

				outerNames.add(tmpOuter.get(JSONTerms.BIGRAPHER_NAME).toString());
			}

			// get inner names
			for (int i = 0; i < innerAry.size(); i++) {
				JSONObject tmpInner = (JSONObject) innerAry.get(i);

				innerNames.add(tmpInner.get(JSONTerms.BIGRAPHER_NAME).toString());
			}

			// get nodes connected to outer names. Inner names should be
			// considered
			if (outerNames.size() > 0) {
				for (int i = 0; i < portAry.size(); i++) {
					JSONObject tmpPort = (JSONObject) portAry.get(i);
					node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());

					node.addOuterNames(outerNames);
					node.addInnerNames(innerNames);
				}
			} else { // if there are no outer names, then create edges by
						// creating outernames, adding them to the nodes,
						// then closing the outername

				for (int i = 0; i < portAry.size(); i++) {
					JSONObject tmpPort = (JSONObject) portAry.get(i);
					node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());

					node.addOuterName("edge" + edgeNumber, true);
				}
				edgeNumber++;
			}

			// add inner names to nodes
			if (innerNames.size() > 0) {
				for (int i = 0; i < portAry.size(); i++) {
					JSONObject tmpPort = (JSONObject) portAry.get(i);
					node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());
					;
					node.addInnerNames(innerNames);
				}
			}
		}

		outerNamesFull.addAll(outerNames);
		innerNamesFull.addAll(innerNames);

		//// Create Bigraph Object \\\\\

//		Signature tmpSig = brsWrapper.getSignature();// getBigraphSignature();

		if (signature == null) {
			return null;
		}

		BigraphBuilder biBuilder = new BigraphBuilder(signature);

		// create roots for the bigraph
		for (int i = 0; i < numOfRoots; i++) {
			libBigRoots.add(biBuilder.addRoot(i));
		}

		// create outer names
		OuterName tmpNm;
		HashMap<String, Boolean> isClosedMap = new HashMap<String, Boolean>();

		for (BigraphNode nd : nodes.values()) {
			for (BigraphNode.OuterName nm : nd.getOuterNamesObjects()) {
				if (libBigOuterNames.get(nm.getName()) == null) {
					tmpNm = biBuilder.addOuterName(nm.getName());
					libBigOuterNames.put(nm.getName(), tmpNm);
					isClosedMap.put(nm.getName(), nm.isClosed());
				}
			}
		}

		// create inner names
		// consider closing iner names also (future work)
		for (String inner : innerNamesFull) {
			libBigInnerNames.put(inner, biBuilder.addInnerName(inner));
		}

		// initial creation of nodes
		for (BigraphNode nd : nodes.values()) {
			if (libBigNodes.containsKey(nd.getId())) {
				continue;
			}
			createNode(nd, biBuilder, libBigRoots, libBigOuterNames, libBigNodes);
		}

		// close outernames
		for (OuterName nm : libBigOuterNames.values()) {
			if (isClosedMap.get(nm.getName())) {
				biBuilder.closeOuterName(nm);
			}
		}

		// add sites to bigraph (probably for states they don't have sites)
		for (BigraphNode n : nodes.values()) {
			if (n.hasSite()) {
				biBuilder.addSite(libBigNodes.get(n.getId()));
			}
		}

		return biBuilder.makeBigraph();
	}

	private Node createNode(BigraphNode node, BigraphBuilder biBuilder, LinkedList<Root> libBigRoots,
			HashMap<String, OuterName> outerNames, HashMap<String, Node> nodes) {

		LinkedList<Handle> names = new LinkedList<Handle>();

		for (String n : node.getOuterNames()) {
			names.add(outerNames.get(n));
		}

		// if the parent is a root
		if (node.isParentRoot()) { // if the parent is a root

			Node n = biBuilder.addNode(node.getControl(), libBigRoots.get(node.getParentRoot()), names);
			nodes.put(node.getId(), n);
			return n;
		}

		// if the parent is already created as a node in the bigraph
		if (nodes.containsKey(node.getParent().getId())) {

			Node n = biBuilder.addNode(node.getControl(), nodes.get(node.getParent().getId()), names);
			nodes.put(node.getId(), n);
			return n;
		}

		Node n = biBuilder.addNode(node.getControl(),
				createNode(node.getParent(), biBuilder, libBigRoots, outerNames, nodes), names);
		nodes.put(node.getId(), n);
		return n;

	}

	public Signature getSignature() {

		if (brsWrapper != null) {
			return brsWrapper.getSignature();
		}

		return null;
	}

	/**
	 * Returns a map showing the states of the given trace that match to the
	 * incident pattern conditions
	 * 
	 * @param traceID The trace ID to look in
	 * @return A map in which the key is the condition name in the incident pattern,
	 *         while the value is a state in the given traces
	 */
	public Map<Integer, String> getStatesMatchingIncidentPatternConditions(int traceID) {

		if (incidentPatternHandler == null) {
			System.err.println("TraceMiner:: IncidentPatternHandler is null");
			return null;
		}

		GraphPath trace = getTrace(traceID);

		return incidentPatternHandler.findMatchingStates(trace);

	}

	/**
	 * Returns a map showing the states of the given trace that match to the
	 * incident pattern conditions
	 * 
	 * @param trace The trace to look in
	 * @return A map in which the key is the condition name in the incident pattern,
	 *         while the value is a state in the given traces
	 */
	public Map<Integer, String> getStatesMatchingIncidentPatternConditions(GraphPath trace) {

		if (incidentPatternHandler == null) {
			System.err.println("TraceMiner:: IncidentPatternHandler is null");
			return null;
		}

		return incidentPatternHandler.findMatchingStates(trace);

	}

	/**
	 * Returns a map showing the states of the given trace that match to the
	 * incident pattern conditions
	 * 
	 * @param traceID                 The trace ID to look in
	 * @param incidentPatternFilePath the incident pattern file path
	 * @param systemModelFilePath     the system model file path
	 * @return A map in which the key is the condition name in the incident pattern,
	 *         while the value is a state in the given traces
	 */
	public Map<Integer, String> getStatesMatchingIncidentPatternConditions(int traceID, String incidentPatternFilePath,
			String systemModelFilePath) {

		GraphPath trace = getTrace(traceID);

		return getStatesMatchingIncidentPatternConditions(trace, incidentPatternFilePath, systemModelFilePath);
	}

	/**
	 * Returns a map showing the states of the given trace that match to the
	 * incident pattern conditions
	 * 
	 * @param trace                   The trace to look in
	 * @param incidentPatternFilePath the incident pattern file path
	 * @param systemModelFilePath     the system model file path
	 * @return A map in which the key is the condition name in the incident pattern,
	 *         while the value is a state in the given traces
	 */
	public Map<Integer, String> getStatesMatchingIncidentPatternConditions(GraphPath trace,
			String incidentPatternFilePath, String systemModelFilePath) {

		Map<Integer, String> result = null;

		if (incidentPatternHandler == null) {
			incidentPatternHandler = new IncidentPatternHandler(this);
		}

		// set incident pattern file (.cpi)
		if (incidentPatternFilePath == null || incidentPatternFilePath.isEmpty()) {
			System.err.println("TraceMiner:: incident pattern file (*.cpi) is null or empty");
			return null;
		}

		incidentPatternHandler.setIncidentPatternFilePath(incidentPatternFilePath);

		// set system model file (.cps)
		if (systemModelFilePath == null || systemModelFilePath.isEmpty()) {
			System.err.println("TraceMiner:: system model file (*.cps) is null or empty");
			return null;
		}
		incidentPatternHandler.setSystemModelFilePath(systemModelFilePath);

		// set states folder
		if (getStatesFolder() == null || getStatesFolder().isEmpty()) {
			System.err.println("TraceMiner:: states folder is null or empty");
			return null;
		}

		// set traces file (.json)
		if (instanceFileName == null || instanceFileName.isEmpty()) {
			System.err.println("TraceMiner:: traces file (*.json) is null or empty");
			return null;
		}

		incidentPatternHandler.setTracesFilePath(instanceFileName);

		result = incidentPatternHandler.findMatchingStates(trace);

		return result;

	}

	public String getIncidentPatternFilePath() {

		if (incidentPatternHandler.getIncidentPatternFilePath() == null
				|| incidentPatternHandler.getIncidentPatternFilePath().isEmpty()) {
			return null;
		}

		return incidentPatternHandler.getIncidentPatternFilePath();
	}

	public void setIncidentPatternFilePath(String incidentPatternFilePath) {

		incidentPatternHandler.setIncidentPatternFilePath(incidentPatternFilePath);
	}

	public String getSystemModelFilePath() {

		if (incidentPatternHandler.getSystemModelFilePath() == null
				|| incidentPatternHandler.getSystemModelFilePath().isEmpty()) {
			return null;
		}

		return incidentPatternHandler.getSystemModelFilePath();
	}

	public void setSystemModelFilePath(String systemModelFilePath) {

		incidentPatternHandler.setSystemModelFilePath(systemModelFilePath);
	}

	public String getLastIncidentPatternCondition() {

		if (incidentPatternHandler != null) {
			return incidentPatternHandler.getLastIncidentPatternCondition();
		}

		return null;
	}

	/**
	 * Returns the number of difference between the times the redex of the given
	 * action is matched to the pre state and post state
	 * 
	 * @param action
	 * @param originalPre
	 * @param originalPost
	 * @param isRedex      if true then the redex of the action is used for
	 *                     comparison. Otherwise, the reactum is used
	 * @return The difference. Positive represents how many mores the pre-state is
	 *         matched in comparison to the post-state. Negative is the other way
	 *         around. Zero, both, pre-state and post-state, have the same mathcing
	 *         number
	 */
	public int getNumberOfRedexMatches(String action, int state1, int state2, boolean isRedex) {

		/**
		 * Causally dependence is implemented by checking if the pre-condition of the
		 * given action is matches to the state that the preAction's pre-condition
		 * matches to. If it matches then the action is NOT causally dependent.
		 * Otherwise, it is causally dependent
		 **/

		if (action == null || brsWrapper == null) {
			System.err.println("Error. there's a null");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		ActionWrapper actionWrapper = bigraphERActions.get(action);

		// ActionWrapper preActionWrapper = bigraphERActions.get(preAction);

		if (actionWrapper == null) {
			System.err.println("Error. there's a null of wrappers");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		BigraphWrapper conditionWrapper = null;

		if (isRedex) {
			conditionWrapper = actionWrapper.getPrecondition();
		} else {
			conditionWrapper = actionWrapper.getPostcondition();
		}

		// System.out.println(preWrapper.getContainedEntitiesMap());
		// === get Bigraph representation of the precondition of action
		Bigraph actionCondition = conditionWrapper != null
				? conditionWrapper.getBigraphObject(false, brsWrapper.getSignature())
				: null;

		if (actionCondition == null) {
			System.err.println("condition of the given action [" + action + "] is NULL");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		// === get preAction's precondition State as Bigraph
		if (getStatesFolder() == null) {
			System.err.println("States folder is missing");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		// testinggg
		Signature sig = brsWrapper.getSignature();
		Bigraph actionOrigPreStateBig = loadState(state1, sig);
		Bigraph actionOrigPostStateBig = loadState(state2, sig);

		// === match the action precondition to the state
		Matcher matcher = new Matcher();

		Iterator itOrigPre = matcher.match(actionOrigPreStateBig, actionCondition).iterator();
		Iterator itOrigPost = matcher.match(actionOrigPostStateBig, actionCondition).iterator();

		int cntOrigPost = 0;
		int cntOrigPre = 0;

		while (itOrigPost.hasNext()) {
			// a match means that there's no dependency
			// return ACTIONS_NOT_CAUSALLY_DEPENDENT;
			itOrigPost.next();
			cntOrigPost++;
		}

		while (itOrigPre.hasNext()) {
			// a match means that there's no dependency
			// return ACTIONS_NOT_CAUSALLY_DEPENDENT;
			itOrigPre.next();
			cntOrigPre++;
		}

		System.out.println(
				"state [" + state1 + "] matches: " + cntOrigPre + " state [" + state2 + "] matches: " + cntOrigPost);

		// action is dependent on the previous action

		return (cntOrigPre - cntOrigPost);
	}

	public int getNumberOfBigraphMatches(Bigraph bigraph, int state1, int state2) {

		/**
		 * Causally dependence is implemented by checking if the pre-condition of the
		 * given action is matches to the state that the preAction's pre-condition
		 * matches to. If it matches then the action is NOT causally dependent.
		 * Otherwise, it is causally dependent
		 **/

		if (bigraph == null) {
			System.err.println("Error. there's a null");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		// === get preAction's precondition State as Bigraph
		if (getStatesFolder() == null) {
			System.err.println("States folder is missing");
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		// unify signatures
		Signature bigSig = bigraph.getSignature();
//		Signature stateSig = brsWrapper.getSignature();
//		SignatureBuilder sigBldr = new SignatureBuilder();
//
//		Iterator<Control> bigControls = bigSig.iterator();
//
//		while (bigControls.hasNext()) {
//			Control ctrl = bigControls.next();
//
//			if (!stateSig.contains(ctrl.getName())) {
//				sigBldr.add(ctrl);
//			}
//		}
//
//		Iterator<Control> stateControls = stateSig.iterator();
//
//		while (stateControls.hasNext()) {
//			Control ctrl = stateControls.next();
//			sigBldr.add(ctrl);
//		}
//
//		Signature unifiedSig = sigBldr.makeSignature();

		Bigraph bigState1 = loadState(state1, bigSig);
		Bigraph bigState2 = loadState(state2, bigSig);

//		updateBigraphWithSignature(unifiedSig);

		if (bigState1 == null || bigState2 == null) {
			String error = "Error in getting a Bigraph of the states.";

			if (bigState1 == null) {
				error += " State1 Bigraph is Null.";
			}

			if (bigState2 == null) {
				error += " State2 Bigraph is Null.";
			}

			System.err.println(error);
			return ACTIONS_CAUSAL_DEPENDENCY_ERROR;
		}

		// === match the action precondition to the state
		Matcher matcher = new Matcher();

		Iterator itOrigPre = matcher.match(bigState1, bigraph).iterator();
		Iterator itOrigPost = matcher.match(bigState2, bigraph).iterator();

		int cntOrigPost = 0;
		int cntOrigPre = 0;

		while (itOrigPost.hasNext()) {
			// a match means that there's no dependency
			// return ACTIONS_NOT_CAUSALLY_DEPENDENT;
			itOrigPost.next();
			cntOrigPost++;
		}

		while (itOrigPre.hasNext()) {
			// a match means that there's no dependency
			// return ACTIONS_NOT_CAUSALLY_DEPENDENT;
			itOrigPre.next();
			cntOrigPre++;
		}

//		System.out.println(
//				"state [" + state1 + "] matches: " + cntOrigPre + " state [" + state2 + "] matches: " + cntOrigPost);

		// action is dependent on the previous action

		return (cntOrigPost - cntOrigPre);
	}

//	public Bigraph updateBigraphWithSignature(Bigraph bigraph, Signature signature) {
//
//		// clone the given bigraph but with the new signature
//		Bigraph res = null;
//		BigraphBuilder bigBldr = new BigraphBuilder(signature);
//
//		// controls
//		for (Node node : bigraph.getNodes()) {
//			List<Handle> handles = new LinkedList<Handle>();
//
//			//outernames (should be)
//			for (Port p : node.getPorts()) {
//				handles.add(p.getHandle());
//			}
//			
//			bigBldr.addNode(node.getControl().getName(), node.getParent(), handles);
//			
//			handles.clear();
//		}
//
//		// outernames
//		for (OuterName outer : bigraph.getOuterNames()) {
//			bigBldr.addOuterName(outer.getName());
//		}
//		
//		
//		//innernames
//		for(InnerName inner : bigraph.getInnerNames()) {
//			bigBldr.addInnerName(inner.getName());
//		}
//		
//		
//		//sites
//		for(Site s : bigraph.getSites()) {
//			bigBldr.addSite(s.getParent());
//		}
//		
//		//roots
//		for(Root root : bigraph.getRoots()) {
//			bigBldr.addRoot();
//		}
//	}
	// public static void main(String[] args) {
	//
	// TraceMiner m = new TraceMiner();
	//
	// String bigraphERFile = "D:/Bigrapher data/lero/example/lero.big";
	//
	// m.setBigraphERFile(bigraphERFile);
	//
	// List<String> actions = new LinkedList<String>();
	//
	// actions.add("enter_room_during_working_hours");
	// actions.add("disable_hvac");
	// actions.add("connect_to_hvac");
	//
	// GraphPath p = new GraphPath();
	//
	// p.setTransitionActions(actions);
	//
	// List<GraphPath> traces = new LinkedList<GraphPath>();
	//
	// traces.add(p);
	//
	// //finds the top k
	// int topK = 10;
	//
	// //exclude terms from this list
	// List<String> excluding = new LinkedList<String>();
	//
	// excluding.add("RulesKeywords");
	//
	// List<Map.Entry<String, Long>> ents = m.findTopCommonEntities(traces,
	// excluding, topK);
	//
	// System.out.println(ents);
	// }

}
