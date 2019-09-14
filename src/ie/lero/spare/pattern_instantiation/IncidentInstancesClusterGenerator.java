package ie.lero.spare.pattern_instantiation;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.UnsupportedEncodingException;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.PriorityQueue;
//import java.util.Random;
//
//import com.beust.jcommander.internal.Lists;
//
//import ca.pfv.spmf.algorithms.clustering.dbscan.AlgoDBSCAN;
//import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
//import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoBisectingKMeans;
//import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;
//import ca.pfv.spmf.algorithms.clustering.optics.AlgoOPTICS;
//import ca.pfv.spmf.algorithms.clustering.optics.DoubleArrayOPTICS;
//import ca.pfv.spmf.algorithms.clustering.text_clusterer.TextClusterAlgo;
//import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
//import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoClaSP;
//import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;
//import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
//import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase;
//import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator;
//import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map;
//import ca.pfv.spmf.algorithms.sequentialpatterns.occur.AlgoOccur;
//import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoPrefixSpan;
//import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoTKS;
//import ca.pfv.spmf.algorithms.sequentialpatterns.spam.PatternTKS;
//import ca.pfv.spmf.patterns.cluster.Cluster;
//import ca.pfv.spmf.patterns.cluster.ClusterWithMean;
//import ca.pfv.spmf.patterns.cluster.DoubleArray;
//import ie.lero.spare.franalyser.utility.FileManipulator;
//import weka.clusterers.ClusterEvaluation;
//import weka.clusterers.Clusterer;
//import weka.clusterers.Cobweb;
//import weka.clusterers.EM;
//import weka.clusterers.SimpleKMeans;
//import weka.core.Instances;
//import weka.core.converters.ConverterUtils.DataSource;
//import weka.filters.Filter;
//import weka.filters.unsupervised.attribute.Remove;
//
public class IncidentInstancesClusterGenerator {
}
	//
//	// instances (traces). key is trace ID value is the trace as GraphPath
//	// object
//	Map<Integer, GraphPath> instances;
//
//	String instanceFileName;
//	String convertedInstancesFileName = "convertedInstances.txt";
//	int numberOFClusters = 10;
//	DistanceFunction distanceFunction;
//	// AlgoKMeans kmean;
//
//	// space is the separator
//	public final static String DATA_SEPARATOR = ",";
//	public final static String WEKA_DATA_SEPARATOR = ",";
//	// cloud be the number of states
//	public static int PADDING_STATE = -1;
//	public static String PADDING_ACTION = "NULL";
//	public static int PADDING_ACTION_INT = -1;
//	// what value to give? probably larger would be better to get a noticable
//	// difference
//	public final static int ACTION_PERFORMED = 1;
//	public final static int ACTION_NOT_PERFORMED = 0;
//	public final static String ATTRIBUTE_STATE_NAME = "state-";
//	public final static String ATTRIBUTE_ACTION_NAME = "action-";
//
//	String clusterFolder = "clusters generated";
//	String clustersOutputFileName = "clustersGenerated.txt";
//	String clustersOutputFolder;
//
//	int longestTransition = -1;
//	int shortestTransition = -1;
//
//	Map<String, Integer> systemActions;
//
//	// shortest traces
//	Map<Integer, GraphPath> shortestTraces;
//
//	// prints a number of instances for each cluster
//	int lengthToPrint = 5;
//
//	// weka attributes
//	String wekaInstancesFilePath = "wekaInstances.arff";
//
//	// holds clusters generated
//	List<ClusterWithMean> clusters;
//
//	boolean isAlreadyChecked = false;
//
//	String shortestTracesFileName;
//
//	List<Integer> traceIDs;
//
//	public IncidentInstancesClusterGenerator() {
//
//		systemActions = new HashMap<String, Integer>();
//
//		shortestTraces = new HashMap<Integer, GraphPath>();
//
//		int numberOfStates = 10000;
//		// PADDING_ACTION = 0;
//		PADDING_STATE = -1 * numberOfStates;
//
//		// some actions
//		systemActions.put("EnterRoom", 0);
//		systemActions.put("ConnectIPDevice", 1);
//		systemActions.put("DisconnectIPDevice", 2);
//		systemActions.put("ConnectBusDevice", 3);
//		systemActions.put("DisconnectBusDevice", 4);
//		systemActions.put("SendData", 5);
//		systemActions.put("SendMalware", 6);
//		systemActions.put("DisableHVAC", 7);
//		systemActions.put("EnterRoomWithoutCardReader", 8);
//		systemActions.put("ChangeAccessToCardRequired", 9);
//		systemActions.put("ChangeAccessToCardNotRequired", 10);
//		systemActions.put("ChangeContextToOutSideWorkingHours", 11);
//		systemActions.put("ChangeContextToWorkingHours", 12);
//		systemActions.put("TurnOnHVAC", 13);
//		systemActions.put("TurnOffHVAC", 14);
//		systemActions.put("TurnOnSmartTV", 15);
//		systemActions.put("TurnOffSmartTV", 16);
//		systemActions.put("GenerateData", 17);
//		systemActions.put("CollectData", 18);
//		systemActions.put("TurnONTVMicrophone", 19);
//		systemActions.put("TurnOffTVMicrophone", 20);
//		systemActions.put("TurnONTVCamera", 21);
//		systemActions.put("TurnOffTVCamera", 22);
//
//		PADDING_ACTION_INT = -1 * systemActions.size();
//
//		// set value of actions as more than the max number of states. This is
//		// done to avoid mixing with states numbering
//
//		int index = 1;
//		int increment = (int) (numberOfStates * .05); // 1% of the number of
//														// states
//
//		if (increment == 0) {
//			increment = 1;
//		}
//
//		// for(Entry<String, Integer> set : systemActions.entrySet()) {
//		// set.setValue(index);
//		//
//		// index +=100;
//		// }
//
//	}
//
//	boolean checkFile(String fileName) {
//
//		if (fileName == null || fileName.isEmpty()) {
//			System.err.println("Given file name is NULL");
//			return false;
//		}
//
//		if (!fileName.endsWith("json")) {
//			System.err.println("file should be in JSON format (i.e. *.json)");
//			return false;
//		}
//
//		File file = new File(fileName);
//
//		if (!file.exists()) {
//			System.err.println("file [" + fileName + "] does NOT exist");
//			return false;
//		}
//
//		if (!file.isFile()) {
//			System.err.println("[" + fileName + "] is NOT a file");
//			return false;
//		}
//
//		return true;
//	}
//
//	void generateClustersFromFolder(String folderPath) {
//
//		File inputFolder = new File(folderPath);
//
//		for (File file : inputFolder.listFiles()) {
//
//			if (file.isFile()) {
//				String filePath = file.getAbsolutePath();
//
//				if (filePath.endsWith(".json")) {
//					System.out.println("###### Generating clusters ######");
//					System.out.println("*File: " + filePath);
//
//					generateClustersFromFile(filePath);
//
//					System.out.println("\n");
//				}
//
//			}
//		}
//	}
//
//	void generateClustersFromFile(String fileName) {
//
//		if (!checkFile(fileName)) {
//
//			return;
//		}
//
//		instanceFileName = fileName;
//
//		File outputFolder = new File(clustersOutputFolder);
//
//		if (!outputFolder.exists()) {
//			outputFolder.mkdir();
//		}
//
//		clustersOutputFileName = instanceFileName.replace(".json", "_relevantTraces.txt");// clustersOutputFolder
//		// clustersOutputFileName;
//		convertedInstancesFileName = instanceFileName.replace(".json", "_convertedInstances.txt");// clustersOutputFolder
//		// +
//		shortestTracesFileName = instanceFileName.replace(".json", "_shortestTracesIDs.txt");
//
//		// loads instances(or traces) from given file name
//		// and finds shortest transitions
//		readTracesFromFile();
//
//		// System.out.println(">>Converting instances to data mining tech
//		// format...");
//
//		// convertedInstancesFileName =
//		// convertInstancesToMiningFormat(instances);
//
//		// ==For text mining (i.e. clustering based on actions names)
//		// convertedInstancesFileName =
//		// convertInstancesToTextMiningFormat(instances);
//
//		// jaccard distance function is used for vectors that has only 0,1
//		// values
//		// distanceFunction = new DistanceEuclidian();
//
//		/** apply cluster algorithm (K-mean) **/
//		// clusters = generateClustersUsingKMean();
//		// printClustersWithMean(clusters);
//
//		/** apply cluster algorithm (BiSect implementation) **/
//		// generateClustersUsingKMeanUsingBiSect();
//		// printClusters();
//
//		/** using OPTIC algorithm to find clusters **/
//		List<Cluster> clus = generateClustersUsingOPTICS();
//		printClustersOPTIC(clus);
//
//		/** using DBSCAN algorithm **/
//		// List<Cluster> clus = generateClustersUsingDBSCAN();
//		// printClustersOPTIC(clus);
//
//		/** ======text based clustering **/
//		// convertedInstancesFileName = convertInstancesToTextMiningFormat();
//		// generateClustersUsingTextMining();
//
//		System.out.println("\n>>DONE");
//
//	}
//
//	void generateClusters(String fileName) {
//
//		// instanceFileName = fileName;
//
//		File inputFile = new File(fileName);
//
//		if (inputFile.isFile()) {
//			clustersOutputFolder = fileName.substring(0, fileName.lastIndexOf("/")) + "/" + clusterFolder;
//
//			instanceFileName = fileName;
//			generateClustersFromFile(fileName);
//
//		} else if (inputFile.isDirectory()) {
//			clustersOutputFolder = fileName + "/" + clusterFolder;
//			generateClustersFromFolder(fileName);
//
//		} else {
//			System.err.println(fileName + " given file name is niether a FILE nor a FOLDER. Exiting");
//			return;
//		}
//
//	}
//
//	/**
//	 * Identify relevant traces (currently defined as shortest and has common
//	 * patterns)
//	 * 
//	 * @param fileName
//	 *            given file path (*.json or a folder containing JSON files)
//	 */
//	void identifyRelevantTraces(String fileName) {
//
//		File inputFile = new File(fileName);
//
//		if (inputFile.isFile()) {
//
//			identifyRelevantTracesFromFile(fileName);
//
//		} else if (inputFile.isDirectory()) {
//
//			identifyRelevantTracesFromFolder(fileName);
//
//		} else {
//			System.err.println(fileName + " given file name is niether a FILE nor a FOLDER. Exiting");
//			return;
//		}
//
//	}
//
//	void identifyRelevantTracesFromFolder(String folderPath) {
//
//		File inputFolder = new File(folderPath);
//
//		StringBuilder content = new StringBuilder();
//
//		// numberOfRelevantTraces = new LinkedList<Integer>();
//
//		String linSeparator = System.getProperty("line.separator");
//
//		content.append("*Summary containing relevant traces (shortest & have common sequential patterns) identified from each file in folder [" + folderPath + "]*")
//				.append(linSeparator).append(linSeparator);
//		content.append("Format: ").append(linSeparator);
//		content.append("[File-name]").append(linSeparator);
//		content.append("[# of relevant traces]").append(linSeparator);
//		content.append("[relevant traces IDs]").append(linSeparator).append(linSeparator);
//		
//		int numOfRelevantTraces = 0;
//		for (File file : inputFolder.listFiles()) {
//
//			if (file.isFile()) {
//				String filePath = file.getAbsolutePath();
//
//				if (filePath.endsWith(".json")) {
//					System.out.println("###### Identifying Relevant Traces ######");
//					System.out.println("*File: " + filePath);
//					System.out.println("\n");
//
//					//identify relevant traces
//					numOfRelevantTraces = identifyRelevantTracesFromFile(filePath);
//
//					//file path
//					content.append("#").append(filePath).append(linSeparator)
//					//number of relevant traces
//					.append(numOfRelevantTraces).append(linSeparator);
//					//IDs
//					Integer[] ary = traceIDs.toArray(new Integer[traceIDs.size()]);
//					content.append(Arrays.toString(ary)).append(linSeparator).append(linSeparator);
//					
//				}
//			}
//		}
//
//		// store a summary into file
//		String outputFile = folderPath + "/relevantTraces_Summary.txt";
//
//		writeToFile(content.toString(), outputFile);
//	
//	}
//
//	public int identifyRelevantTracesFromFile(String fileName) {
//
//		if (!checkFile(fileName)) {
//			return -1;
//		}
//
//		int numOfRelevantTraces = 0;
//
//		instanceFileName = fileName;
//
//		clustersOutputFileName = instanceFileName.replace(".json", "_relevantTraces.txt");// clustersOutputFolder
//		// clustersOutputFileName;
//		convertedInstancesFileName = instanceFileName.replace(".json", "_convertedInstances.txt");// clustersOutputFolder
//		// +
//		shortestTracesFileName = instanceFileName.replace(".json", "_shortestTracesIDs.txt");
//
//		// loads instances(or traces) from given file name
//		// and finds shortest transitions
//		readTracesFromFile();
//
//		// find shortest traces
//		findShortestTraces();
//
//		/**
//		 * ======Mine Frequent sequential patterns using the prefixspan algo
//		 **/
////		numOfRelevantTraces = mineSequencesUsingPrefixSpanAlgo(shortestTraces.values());
//
////		 int minimumNumOfTracesForPattern = 5;
////		 numOfRelevantTraces = mineSequencesUsingPrefixSpanAlgo(shortestTraces.values(),minimumNumOfTracesForPattern);
//
//		/** ======Mine Closed Frequent sequential patterns using the ClaSP **/
////		numOfRelevantTraces = mineClosedSequencesUsingClaSPAlgo(shortestTraces.values());
//
//		int minimumNumOfTracesForPattern = 5;
//		numOfRelevantTraces = mineClosedSequencesUsingClaSPAlgo(shortestTraces.values(), minimumNumOfTracesForPattern);
//
//		/** ======Mine Frequent sequential patterns using the SPADE algo **/
//		// mineSequentialPatternsUsingSPADE();
//
//		/** ======Mine Frequent sequential patterns using the TKS **/
//		// finds top-k sequential patterns
//		// allows to find contiguous sequence patterns
//		// mineSequentialPatternsUsingTKSAlgo(shortestTraces.values());
//
//		/** ======Mine Frequent Itemsets using FP-Growth algo **/
//		// mineFrequentItemsetsUsingFP_GrowthAlgo();
//
//		System.out.println("\n>>DONE");
//
//		return numOfRelevantTraces;
//
//	}
//
//	void readTracesFromFile() {
//
//		// File fileConvertedInstances = new File(convertedInstancesFileName);
//		// File fileShortestInstances = new File(shortestTracesFileName);
//
//		// if the traces already read before and converted instances are
//		// generated as a file then skip loading
//		// if (fileConvertedInstances.exists() &&
//		// fileShortestInstances.exists()) {
//		// isAlreadyChecked = true;
//		// System.out.println(">>Traces were loaded before and shortest traces
//		// are identified."
//		// + "\n>>Converted traces from [" + convertedInstancesFileName + "]
//		// will be used."
//		// + "\n>>Shortest traces from [" + shortestTracesFileName + "] will be
//		// used");
//		// return;
//		// }
//
//		System.out.println(">>Reading instances from [" + instanceFileName + "]");
//
//		// load instances from file
//		instances = FileManipulator.readInstantiatorInstancesFile(instanceFileName);
//
//		System.out.println(">>Number of instances read = " + instances.size());
//		// System.out.println(instances.get(0).getTransitionActions());
//		if (instances == null) {
//			System.out.println("Instances are null! Exiting");
//			return;
//		}
//
//	}
//
//	void findShortestTraces() {
//
//		// shortest trace is set to be 3 actions (or 4 states (i.e. actions+1)
//
//		int numberOfStates = 4;
//
//		String separator = " ";
//		StringBuilder bldr = new StringBuilder();
//
//		if (shortestTraces != null) {
//			shortestTraces.clear();
//		}
//
//		// contains the ids separated by spaces
//		// String shortestTracesFileName =
//		// instanceFileName.substring(instanceFileName.lastIndexOf("/")+1);
//
//		// remove .json and add extension .txt
//		// String shortestTracesFileName = instanceFileName.replace(".json",
//		// "_shortestTracesIDs.txt");
//
//		// File file = new File(shortestTracesFileName);
//
//		// if shortest traces already defined for the given traces then read the
//		// file
//		// if (file.isFile()) {
//		//
//		// // read shortest traces file (contains ids separated by space)
//		// System.out.println(">>Loading shortest traces IDs from [" +
//		// shortestTracesFileName + "]");
//		// String[] tracesIDs =
//		// FileManipulator.readFileNewLine(shortestTracesFileName);
//		//
//		// tracesIDs = tracesIDs[0].split(separator);
//		//
//		// for (String id : tracesIDs) {
//		// int idInt = Integer.parseInt(id);
//		//
//		// shortestTraces.put(idInt, instances.get(idInt));
//		// }
//		//
//		// System.out.println(shortestTraces.size());
//		//
//		// } else {
//		System.out.println(">>Identifying shortest traces in [" + instanceFileName + "]");
//		for (GraphPath trace : instances.values()) {
//
//			if (trace.getStateTransitions().size() == numberOfStates) {
//				shortestTraces.put(trace.getInstanceID(), trace);
//				bldr.append(trace.getInstanceID()).append(separator);
//			}
//		}
//
//		if (bldr.length() > 0) {
//			bldr.deleteCharAt(bldr.length() - 1);// remove extra space
//		}
//
//		// store to file
//		writeToFile(bldr.toString(), shortestTracesFileName);
//		System.out.println(">>Shortest traces IDs are stored in [" + shortestTracesFileName + "]");
//		// }
//
//	}
//
//	public List<ClusterWithMean> generateClustersUsingKMean() {
//
//		AlgoKMeans kmean = new AlgoKMeans();
//
//		try {
//
//			numberOFClusters = 6;
//
//			System.out.println(">>Generating clusters using K-mean algorithm" + " with K = " + numberOFClusters
//					+ ", distance function is " + distanceFunction.getName());
//
//			// generate clusters
//			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
//					DATA_SEPARATOR);
//
//			// store clusters (each line is a cluster in the output file)
//			kmean.saveToFile(clustersOutputFileName);
//
//			kmean.printStatistics();
//
//			return clusters;
//		} catch (NumberFormatException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	public List<ClusterWithMean> generateClustersUsingKMeanUsingBiSect() {
//
//		AlgoBisectingKMeans kmean = new AlgoBisectingKMeans();
//
//		int iteratorForSplit = numberOFClusters * 2;
//
//		try {
//			System.out.println(">>Generating clusters using K-mean algorithm with K = " + numberOFClusters
//					+ ", distance function is " + distanceFunction.getName());
//			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
//					iteratorForSplit, DATA_SEPARATOR);
//
//			kmean.saveToFile(clustersOutputFileName);
//
//			return clusters;
//		} catch (NumberFormatException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	public List<Cluster> generateClustersUsingDBSCAN() {
//
//		AlgoDBSCAN algo = new AlgoDBSCAN();
//
//		// minimum number of points/instances in a cluster
//		int minPoints = 10;
//		// distance between points/instances in a cluster
//		double epsilon = 10d;
//		// double epsilonPrime = epsilon;
//
//		try {
//			System.out.println(">>Generating clusters using DBSCAN algorithm");
//
//			// generate clusters
//			List<Cluster> clusters = algo.runAlgorithm(convertedInstancesFileName, minPoints, epsilon, DATA_SEPARATOR);
//
//			// store clusters (each line is a cluster in the output file)
//			algo.saveToFile(clustersOutputFileName);
//
//			algo.printStatistics();
//
//			return clusters;
//		} catch (NumberFormatException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	public List<Cluster> generateClustersUsingOPTICS() {
//
//		AlgoOPTICS algo = new AlgoOPTICS();
//
//		// minimum number of points/instances in a cluster
//		int minPoints = 10;
//		// distance between points/instances in a cluster
//		double epsilon = 2d;
//		double epsilonPrime = epsilon;
//
//		try {
//			System.out.println(">>Generating clusters using OPTIC algorithm");
//
//			// generate clusters
//			List<DoubleArrayOPTICS> clusters = algo.computerClusterOrdering(convertedInstancesFileName, minPoints,
//					epsilon, DATA_SEPARATOR);
//
//			// generate dbscan clusters from the cluster ordering:
//			List<Cluster> dbScanClusters = algo.extractDBScan(minPoints, epsilonPrime);
//
//			// store clusters (each line is a cluster in the output file)
//			algo.saveToFile(clustersOutputFileName);
//
//			algo.printStatistics();
//
//			return dbScanClusters;
//		} catch (NumberFormatException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	public void generateClustersUsingTextMining() {
//
//		TextClusterAlgo algo = new TextClusterAlgo();
//
//		boolean stemFlag = true;
//		boolean stopWordFlag = true;
//
//		System.out.println(">>Generating clusters using Text Clustering algorithm");
//
//		// generate clusters
//		algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, stemFlag, stopWordFlag);
//
//		// clusters generated are stored in a file (e.g.,
//		// clustersOutputFileName)
//
//		algo.printStatistics();
//	}
//
//	public String convertInstancesToMiningFormat(List<GraphPath> instances) {
//
//		// convert instances to a format compatible with that of the data mining
//		// library used (i.e. SPMF)
//		// line format could have:
//		// @NAME="instance name"
//		String instanceName = "@NAME=";
//		// @ATTRIBUTEDEF="attribute name"
//		String attributeName = "@ATTRIBUTEDEF=";
//		// #, % are used for comments and any meta-data respectively
//		// data1 [separator] data2 [separator] data3 ... (actual data treated as
//		// double array)
//
//		/**
//		 * all data array should be of the same length so states that are short
//		 * than the longest are padded with -1
//		 **/
//		// create a text file to hold the data
//
//		String fileLinSeparator = System.getProperty("line.separator");
//
//		StringBuilder builder = new StringBuilder();
//
//		if (instances != null && !instances.isEmpty()) {
//			shortestTransition = instances.get(0).getStateTransitions().size();
//		}
//
//		// find longest and shortest transitions
//		for (GraphPath path : instances) {
//			List<Integer> tmp = path.getStateTransitions();
//
//			if (tmp.size() > longestTransition) {
//				longestTransition = tmp.size();
//			} else if (tmp.size() < shortestTransition) {
//				shortestTransition = tmp.size();
//			}
//		}
//
//		// numberOFClusters = (longestTransition - shortestTransition) + 1;
//
//		int numberOfActions = longestTransition - 1;
//
//		int i = 0;
//
//		// ========states attributes (state-0, state-1, number of maximum
//		// states)
//		for (i = 0; i < longestTransition - 1; i++) {
//
//			// add attribute name e.g., "state-0 state-1 ..."
//			builder.append(attributeName).append(ATTRIBUTE_STATE_NAME).append(i).append(fileLinSeparator);
//			builder.append(attributeName).append(ATTRIBUTE_ACTION_NAME).append(i).append(fileLinSeparator);
//		}
//
//		builder.append(attributeName).append(ATTRIBUTE_STATE_NAME).append(i).append(fileLinSeparator);
//
//		// ========actions attribute (actions names)
//		// for (String action : systemActions.keySet()) {
//		// builder.append(attributeName).append(action).append(fileLinSeparator);
//		// }
//
//		// ========set data
//		for (GraphPath path : instances) {
//
//			// set instance name to be the instance id
//			builder.append(instanceName).append(path.getInstanceID()).append(fileLinSeparator);
//
//			// set data to be the states and actions of transitions
//			List<Integer> states = path.getStateTransitions();
//			List<String> transitionActions = path.getTransitionActions();
//
//			for (i = 0; i < states.size() - 1; i++) {
//
//				// add state
//				builder.append(states.get(i)).append(DATA_SEPARATOR);
//
//				// add action
//				builder.append(systemActions.get(transitionActions.get(i))).append(DATA_SEPARATOR);
//			}
//
//			// add last state
//			builder.append(states.get(i));
//			// builder.append(systemActions.get(transitionActions.get(i)));
//
//			// pad the transition with -1
//			if (states.size() < longestTransition) {
//
//				int numOfExtraStates = longestTransition - states.size();
//
//				builder.append(DATA_SEPARATOR);
//
//				// add dummy action
//				builder.append(PADDING_ACTION_INT).append(DATA_SEPARATOR);
//
//				for (i = 0; i < numOfExtraStates - 1; i++) {
//
//					builder.append(PADDING_STATE).append(DATA_SEPARATOR);
//					builder.append(PADDING_ACTION_INT).append(DATA_SEPARATOR);
//				}
//
//				builder.append(PADDING_STATE);
//				// builder.append(PADDING_ACTION);
//			}
//
//			// add action data
//			// 0 for missing the action from the transition actions. 1 if it
//			// exists
//
//			// if (transitionActions != null && !transitionActions.isEmpty()) {
//			//
//			// builder.append(DATA_SEPARATOR);
//			//
//			//// for (i = 0; i < systemActions.size() - 1; i++) {
//			////
//			//// if (transitionActions.contains(systemActions.get(i))) {
//			//// builder.append(ACTION_PERFORMED).append(DATA_SEPARATOR);
//			//// } else {
//			//// builder.append(ACTION_NOT_PERFORMED).append(DATA_SEPARATOR);
//			//// }
//			//// }
//			//
//			// //add action as an index in the system actions
//			// for(i=0;i< transitionActions.size()-1;i++) {
//			// builder.append(systemActions.get(transitionActions.get(i))).append(DATA_SEPARATOR);
//			// }
//			//
//			// // check last action
//			//// if (transitionActions.contains(systemActions.get(i))) {
//			//// builder.append(ACTION_PERFORMED);
//			//// } else {
//			//// builder.append(ACTION_NOT_PERFORMED);
//			//// }
//			//
//			// builder.append(systemActions.get(transitionActions.get(i)));
//			// }
//
//			builder.append(fileLinSeparator);
//		}
//
//		// save string to file
//
//		writeToFile(builder.toString(), convertedInstancesFileName);
//
//		return convertedInstancesFileName;
//	}
//
//	public String convertInstancesToTextMiningFormat(List<GraphPath> instances) {
//
//		String fileLinSeparator = System.getProperty("line.separator");
//
//		StringBuilder builder = new StringBuilder();
//
//		// ========set data
//		for (GraphPath path : instances) {
//
//			// === get states as string
//			// String statesStr = path.getStateTransitions().toString();
//			// // remove brackets
//			// statesStr = statesStr.replaceAll("\\[", "");
//			// statesStr = statesStr.replaceAll("\\]", "");
//			// // remove commas
//			// statesStr = statesStr.replaceAll(",", "");
//			// statesStr = statesStr.trim();
//
//			// === get actions as string
//			String actionsStr = path.getTransitionActions().toString();
//			actionsStr = actionsStr.replaceAll("\\[", "");
//			actionsStr = actionsStr.replaceAll("\\]", "");
//			actionsStr = actionsStr.replaceAll(",", "");
//			actionsStr = actionsStr.trim();
//
//			// === set record(instance_id [states (1 2 3) actions (enterRoom)]
//			builder.append(path.getInstanceID()).append("\t")
//					// .append(statesStr)
//					// .append(" ")
//					.append(actionsStr).append(fileLinSeparator);
//
//		}
//		writeToFile(builder.toString(), convertedInstancesFileName);
//
//		return convertedInstancesFileName;
//	}
//
//	protected void writeToFile(String text, String fileName) {
//
//		try {
//			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
//
//			writer.write(text);
//
//			writer.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	/**************************************/
//	/***** SEQUENTIAL PATTERN MINING ******/
//	/**************************************/
//
//	public int mineSequencesUsingPrefixSpanAlgo(Collection<GraphPath> traces) {
//
//		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);
//
//		// Create an instance of the algorithm with minsup = 50 %
//		AlgoPrefixSpan algo = new AlgoPrefixSpan();
//
//		try {
//
//			// run several times till you find max minsup that which after there
//			// will be no result
//
//			boolean isMaxFound = false;
//			int tries = traces.size() / 2 + 1;
//
//			// binary search
//			int left = 0;
//			int right = traces.size() - 1;
//			int mid = -1;
//
//			while ((left <= right) & tries > 0) {
//
//				mid = (int) Math.floor((left + right) / 2);
//
//				algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
//				String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);
//
//				// if there is output, then increase minsup. Else, decrease
//				if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
//					left = mid + 1;
//					System.out
//							.println(">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
//					isMaxFound = true;
//				} else {
//					isMaxFound = false;
//					right = mid - 1;
//					System.out
//							.println(">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
//				}
//
//				tries--;
//			}
//
//			// if the last mid (or minimum trace/minsup) has zero patterns, then
//			// decrement till a value is returned
//			if (!isMaxFound) {
//				while (mid > 0) {
//
//					mid--;
//
//					algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
//					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);
//
//					// if there is output, then increase minsup. Else, decrease
//					if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
//						break;
//					}
//				}
//			} // else increase mid until there's no output.
//			else {
//				while (mid < right) {
//
//					mid++;
//
//					algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, mid);
//					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);
//
//					// if there is output, then increase minsup. Else, decrease
//					if (lines == null || lines.length == 0 || lines[0].isEmpty()) {
//						mid--;
//						break;
//					}
//				}
//			}
//
//			System.out.println(">>Minimum traces is " + mid);
//			
//			return mineSequencesUsingPrefixSpanAlgo(traces, mid);
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return 0;
//	}
//
//	public int mineSequencesUsingPrefixSpanAlgo(Collection<GraphPath> traces, int minimumTraces) {
//
//		File file = new File(convertedInstancesFileName);
//
//		if (!file.exists()) {
//			convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);
//		}
//
//		AlgoPrefixSpan algo = new AlgoPrefixSpan();
//
//		int minsup = minimumTraces; // use a minimum support of x sequences.
//
//		// if you set the following parameter to true, the sequence ids of the
//		// sequences where
//		// each pattern appears will be shown in the result
//		algo.setShowSequenceIdentifiers(true);
//
//		// execute the algorithm
//		try {
//
//			algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, minsup);
//			algo.printStatistics();
//
//			// analysis of the generated sequential patterns
//			String analysisFile = instanceFileName.replace(".json", "_PerfixSpan_analysis.txt");
//
//			analyseGeneratedSequencePatterns(convertedInstancesFileName, clustersOutputFileName, analysisFile);
//
//			return getTracesIDsFromOutputFile(clustersOutputFileName);
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return 0;
//	}
//
//	public int mineClosedSequencesUsingClaSPAlgo(Collection<GraphPath> traces, int minimumTraces) {
//
//		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);
//
//		int numberOfTraces = minimumTraces;
//		int numOfRelevantTraces = 0;
//
//		// Load a sequence database
//		double support = numberOfTraces * 1.0 / traces.size();
//
//		boolean keepPatterns = true;
//		boolean verbose = true;
//		boolean findClosedPatterns = true;
//		boolean executePruningMethods = true;
//		// if you set the following parameter to true, the sequence ids of the
//		// sequences where
//		// each pattern appears will be shown in the result
//		boolean outputSequenceIdentifiers = true;
//
//		AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
//		IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();
//
//		SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);
//
//		double relativeSupport;
//
//		try {
//
//			relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);
//
//			// double relativeSupport =
//			// sequenceDatabase.loadFile(fileToPath("gazelle.txt"), support);
//
//			AlgoClaSP algorithm = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
//					executePruningMethods);
//
//			algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
//					outputSequenceIdentifiers);
//
//			String msg = "Minimum percentage of traces to appear in: ";
//			if (isAlreadyChecked) {
//				System.out.println(msg + support);
//			} else {
//				System.out.println(msg + support + " [" + Math.ceil(support * traces.size()) + "]");
//			}
//
//			System.out.println(algorithm.getNumberOfFrequentPatterns() + "patterns found.");
//
//			if (verbose && keepPatterns) {
//				System.out.println(algorithm.printStatistics());
//			}
//
//			// extracts traces ids from generated file
//			numOfRelevantTraces = getTracesIDsFromOutputFile(clustersOutputFileName);
//
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return numOfRelevantTraces;
//
//	}
//
//	public int mineClosedSequencesUsingClaSPAlgo(Collection<GraphPath> traces) {
//
//		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);
//
//		// Load a sequence database
//		double support = 0;
//
//		boolean keepPatterns = true;
//		boolean verbose = false;
//		boolean findClosedPatterns = true;
//		boolean executePruningMethods = true;
//		// if you set the following parameter to true, the sequence ids of the
//		// sequences where
//		// each pattern appears will be shown in the result
//		boolean outputSequenceIdentifiers = true;
//
//		double relativeSupport;
//
//		int size = traces.size();
//		try {
//
//			// run several times till you find max minsup that which after there
//			// will be no result
//
//			boolean isMaxFound = false;
//			int tries = traces.size() / 2 + 1;
//
//			// binary search
//			int left = 0;
//			int right = traces.size() - 1;
//			int mid = -1;
//
//			while ((left <= right) & tries > 0) {
//
//				mid = (int) Math.floor((left + right) / 2);
//
//				support = mid * 1.0 / size;
//
//				AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
//				IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();
//
//				SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);
//
//				relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);
//
//				AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
//						executePruningMethods);
//
//				algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
//						outputSequenceIdentifiers);
//				// String[] lines =
//				// FileManipulator.readFileNewLine(clustersOutputFileName);
//				//
//				// // if there is output, then increase minsup. Else, decrease
//				// if (lines != null && lines.length > 0 && !lines[0].isEmpty())
//				// {
//				// left = mid + 1;
//				// System.out
//				// .println(">>[L] trying min-traces (i.e. minsup) " + mid + " l
//				// = " + left + " r = " + right);
//				// isMaxFound = true;
//				// } else {
//				// isMaxFound = false;
//				// right = mid - 1;
//				// System.out
//				// .println(">>[R] trying min-traces (i.e. minsup) " + mid + " l
//				// = " + left + " r = " + right);
//				// }
//
//				if (findClosedPatterns) {
//					if (algo.getNumberOfFrequentClosedPatterns() > 0) {
//						left = mid + 1;
//						System.out.println(
//								">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
//						isMaxFound = true;
//					} else {
//						isMaxFound = false;
//						right = mid - 1;
//						System.out.println(
//								">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
//					}
//				} else {
//					if (algo.getNumberOfFrequentPatterns() > 0) {
//						left = mid + 1;
//						System.out.println(
//								">>[L] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
//						isMaxFound = true;
//					} else {
//						isMaxFound = false;
//						right = mid - 1;
//						System.out.println(
//								">>[R] trying min-traces (i.e. minsup) " + mid + " l = " + left + " r = " + right);
//					}
//				}
//
//				tries--;
//			}
//
//			// if the last mid (or minimum trace/minsup) has zero patterns, then
//			// decrement till a value is returned
//			if (!isMaxFound) {
//				while (mid > 0) {
//
//					mid--;
//
//					support = mid * 1.0 / size;
//
//					AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
//					IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();
//
//					SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);
//
//					relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);
//
//					AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
//							executePruningMethods);
//
//					algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
//							outputSequenceIdentifiers);
//					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);
//
//					// if there is output, then increase minsup. Else, decrease
//					if (lines != null && lines.length > 0 && !lines[0].isEmpty()) {
//						break;
//					}
//				}
//			} // else increase mid until there's no output.
//			else {
//				while (mid < right) {
//
//					mid++;
//
//					support = mid * 1.0 / size;
//
//					AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
//					IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();
//
//					SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);
//
//					relativeSupport = sequenceDatabase.loadFile(convertedInstancesFileName, support);
//
//					AlgoClaSP algo = new AlgoClaSP(relativeSupport, abstractionCreator, findClosedPatterns,
//							executePruningMethods);
//
//					algo.runAlgorithm(sequenceDatabase, keepPatterns, verbose, clustersOutputFileName,
//							outputSequenceIdentifiers);
//					String[] lines = FileManipulator.readFileNewLine(clustersOutputFileName);
//
//					// if there is output, then increase minsup. Else, decrease
//					if (lines == null || lines.length == 0 || lines[0].isEmpty()) {
//						mid--;
//						break;
//					}
//				}
//			}
//
//			System.out.println(">>Minimum traces is " + mid);
//
//			return mineClosedSequencesUsingClaSPAlgo(traces, mid);
//
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return 0;
//	}
//
//	protected int getTracesIDsFromOutputFile(String fileName) {
//
//		// read the file generated by sequence pattern mining algorithm
//
//		String outputFile = fileName.replace(".txt", "_IDs.txt");
//
//		String[] lines = FileManipulator.readFileNewLine(fileName);
//
//		StringBuilder str = new StringBuilder();
//		// String separator = " ";
//		String fileLinSeparator = System.getProperty("line.separator");
//		traceIDs = new LinkedList<Integer>();
//
//		str.append("trace-ID").append(fileLinSeparator);
//
//		for (String line : lines) {
//			// itemSet -1(separator between item sets) #SUP: num(number of
//			// traces repeated in) #SID: ids(traces ids)
//			// e.g., 36 -1 #SUP: 5 #SID: 403 404 406 409 413
//
//			if (line.isEmpty()) {
//				continue;
//			}
//
//			// get ids
//			String tracesIDsSet = line.split(":")[2];
//
//			String[] tracesIDs = tracesIDsSet.trim().split(" ");
//
//			for (String id : tracesIDs) {
//				int idInt = Integer.parseInt(id);
//
//				if (!traceIDs.contains(idInt)) {
//					traceIDs.add(idInt);
//					str.append(id).append(fileLinSeparator);
//				}
//
//			}
//		}
//
//		System.out
//				.println(">>Number of traces identified as relevant (Shortest & has common partial-traces or states) = "
//						+ traceIDs.size());
//
//		writeToFile(str.toString(), outputFile);
//
//		return traceIDs.size();
//
//	}
//
//	protected void mineSequentialPatternsUsingSPADE(Collection<GraphPath> traces) {
//
//		// convertedInstancesFileName =
//		// toSPMFsequentialPatternFormat(traces);
//		//
//		// String outputPath = clustersOutputFileName;
//		// // Load a sequence database
//		// double support = 0.01;
//		//
//		// boolean keepPatterns = true;
//		// boolean verbose = false;
//		//
//		// AbstractionCreator abstractionCreator =
//		// AbstractionCreator_Qualitative.getInstance();
//		// boolean dfs = true;
//		//
//		// // if you set the following parameter to true, the sequence ids of
//		// the
//		// // sequences where
//		// // each pattern appears will be shown in the result
//		// boolean outputSequenceIdentifiers = true;
//		//
//		// IdListCreator idListCreator = IdListCreator_FatBitmap.getInstance();
//		//
//		// CandidateGenerator candidateGenerator =
//		// CandidateGenerator_Qualitative.getInstance();
//		//
//		// SequenceDatabase sequenceDatabase = new
//		// SequenceDatabase(abstractionCreator, idListCreator);
//		//
//		// try {
//		//
//		// sequenceDatabase.loadFile(convertedInstancesFileName, support);
//		//
//		//// System.out.println(sequenceDatabase.toString());
//		//
//		// AlgoSPADE algorithm = new AlgoSPADE(support, dfs,
//		// abstractionCreator);
//		//
//		// algorithm.runAlgorithm(sequenceDatabase, candidateGenerator,
//		// keepPatterns, verbose, outputPath,
//		// outputSequenceIdentifiers);
//		// System.out.println("Minimum support (relative) = " + support);
//		// System.out.println(algorithm.getNumberOfFrequentPatterns() + "
//		// frequent patterns.");
//		//
//		// System.out.println(algorithm.printStatistics());
//		//
//		// // analysis of the generated sequential patterns
//		// String analysisFile = clustersOutputFolder +
//		// "/sequentialPatternAnalysis.txt";
//		//
//		// analyseGeneratedSequencePatterns(convertedInstancesFileName,
//		// clustersOutputFileName, analysisFile);
//		//
//		// } catch (UnsupportedEncodingException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// } catch (IOException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//
//	}
//
//	protected void mineSequentialPatternsUsingTKSAlgo(Collection<GraphPath> traces) {
//
//		/**
//		 * http://www.philippe-fournier-viger.com/spmf/TKS.php fastest Top-K
//		 * sequential pattern recognition algo
//		 */
//
//		convertedInstancesFileName = toSPMFsequentialPatternFormat(traces);
//
//		// Load a sequence database
//		String input = convertedInstancesFileName;
//		String output = clustersOutputFileName;
//
//		int k = 10; // number of sequential patterns to find
//
//		// Create an instance of the algorithm
//		AlgoTKS algo = new AlgoTKS();
//
//		// This optional parameter allows to specify the minimum pattern length:
//		algo.setMinimumPatternLength(4); // optional
//
//		// This optional parameter allows to specify the maximum pattern length:
//		// algo.setMaximumPatternLength(4); // optional
//
//		// This optional parameter allows to specify constraints that some
//		// items MUST appear in the patterns found by TKS
//		// E.g.: This requires that items 1 and 3 appears in every patterns
//		// found
//		// algo.setMustAppearItems(new int[] {63});
//
//		// This optional parameter allows to specify the max gap between two
//		// itemsets in a pattern. If set to 1, only patterns of contiguous
//		// itemsets
//		// will be found (no gap).
//		algo.setMaxGap(1);
//
//		// if you set the following parameter to true, the sequence ids of the
//		// sequences where
//		// each pattern appears will be shown in the result
//		algo.showSequenceIdentifiersInOutput(true);
//
//		// execute the algorithm, which returns some patterns
//		try {
//
//			PriorityQueue<PatternTKS> patterns = algo.runAlgorithm(input, output, k);
//
//			// save results to file
//			algo.writeResultTofile(output);
//			algo.printStatistics();
//
//			// analysis of the generated sequential patterns
//			String analysisFile = clustersOutputFolder + "/sequentialPatternAnalysis.txt";
//
//			analyseGeneratedSequencePatterns(convertedInstancesFileName, clustersOutputFileName, analysisFile);
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	protected void analyseGeneratedSequencePatterns(String convertedInstancesFile, String patternsFile,
//			String outputFile) {
//
//		// post analysis of output generated by a sequential pattern mining
//		// algorithm such as PrefixSpan
//
//		// Create an instance of the algorithm with minsup = 50 %
//		AlgoOccur algo = new AlgoOccur();
//
//		// execute the algorithm
//		try {
//
//			File file = new File(outputFile);
//
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//
//			algo.runAlgorithm(convertedInstancesFile, patternsFile, outputFile);
//			algo.printStatistics();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	// public static String fileToPath(String filename) throws
//	// UnsupportedEncodingException {
//	// URL url = IncidentInstancesClusterGenerator.class.getResource(filename);
//	// System.out.println("tst " + url.toExternalForm());
//	// return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
//	// }
//
//	protected String toSPMFsequentialPatternFormat(Collection<GraphPath> instances) {
//
//		// the format is as follows:
//		// state-0 <space> -1 <space> state-1 ... state-n -2
//		// where: -1 is a separator between states and -2 indicates the end of
//		// sequence
//
//		int stateSeparator = -1;
//		int sequenceEndIndicator = -2;
//		int i = 0;
//		String fileLinSeparator = System.getProperty("line.separator");
//		final String DATA_SEPARATOR = " ";
//
//		StringBuilder str = new StringBuilder();
//
//		for (GraphPath sequence : instances) {
//
//			List<Integer> states = sequence.getStateTransitions();
//
//			for (i = 0; i < states.size(); i++) {
//				// add state
//				str.append(states.get(i)).append(DATA_SEPARATOR).append(stateSeparator).append(DATA_SEPARATOR);
//			}
//
//			// add end of sequence
//			str.append(sequenceEndIndicator).append(fileLinSeparator);
//		}
//
//		writeToFile(str.toString(), convertedInstancesFileName);
//
//		return convertedInstancesFileName;
//	}
//
//	/**************************************/
//	/****** FREQUENT ITEMSETS MINING *******/
//	/**************************************/
//
//	protected void mineFrequentItemsetsUsingFP_GrowthAlgo() {
//
//		convertedInstancesFileName = toSPMFFrequentItemsetsFormat(instances.values());
//
//		// percentage of traces in which the item set appears
//		// e.g., 10% in a 100 traces means that an item set should appear in at
//		// least 10 traces out of the 100
//		double minsup = 0.5; // means a minsup of 2 transaction (we used a
//								// relative support)
//
//		// Applying the FPGROWTH
//		AlgoFPGrowth algo = new AlgoFPGrowth();
//
//		// Uncomment the following line to set the maximum pattern length
//		// (number of items per itemset, e.g. 3 )
//		// algo.setMaximumPatternLength(3);
//
//		try {
//
//			algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, minsup);
//			algo.printStats();
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	protected String toSPMFFrequentItemsetsFormat(Collection<GraphPath> instances) {
//
//		// the format is as follows:
//		// state-0 <space> state-1 ... state-n
//
//		int i = 0;
//		String fileLinSeparator = System.getProperty("line.separator");
//		final String DATA_SEPARATOR = " ";
//
//		StringBuilder str = new StringBuilder();
//
//		for (GraphPath sequence : instances) {
//
//			List<Integer> states = sequence.getStateTransitions();
//
//			for (i = 0; i < states.size() - 1; i++) {
//				// add state
//				str.append(states.get(i)).append(DATA_SEPARATOR);
//			}
//
//			// add last state
//			str.append(states.get(i)).append(fileLinSeparator);
//		}
//
//		writeToFile(str.toString(), convertedInstancesFileName);
//
//		return convertedInstancesFileName;
//	}
//
//	/**************** WEKA *********************/
//	/*******************************************/
//	/*******************************************/
//
//	protected void clusterUsingWeka() {
//
//		System.out.println(">>Reading instances from [" + instanceFileName + "]");
//		instances = FileManipulator.readInstantiatorInstancesFile(instanceFileName);
//
//		System.out.println(">>Converting instances into ARFF");
//
//		// convert instances to ARFF (Attribute Relation File Format)
//		// generated file contains as the first field in a row the instance id
//		wekaInstancesFilePath = convertInstancesActionsToARFF(instances.values());
//
//		try {
//
//			// ===== get weka instances
//			DataSource source = new DataSource(wekaInstancesFilePath);
//			Instances wekaInstances = source.getDataSet();
//
//			// ===== remove first field (i.e. instance id)
//			String[] options = new String[2];
//			options[0] = "-R"; // remove
//			options[1] = "1"; // first item
//
//			Remove remove = new Remove();
//			remove.setOptions(options);
//			remove.setInputFormat(wekaInstances);
//
//			// new weka instances without the id field
//			wekaInstances = Filter.useFilter(wekaInstances, remove);
//
//			// ==== converts string to vector so that it can be processed by
//			// other algorithms
//			// StringToWordVector vector = new StringToWordVector();
//
//			// String[] fliterOptions = Utils.splitOptions("-R first-last -W
//			// 5000 -prune-rate 20.0 -T -I -N 0 -L -stemmer weka" +
//			// ".core.stemmers.NullStemmer -M 1 -tokenizer
//			// \"weka.core.tokenizers.WordTokenizer -delimiters \\\"
//			// \\\\r\\\\n\\\\t.,;:\\\\\\'\\\\\\\"()?!\\\"\"");
//
//			// vector.setInputFormat(wekaInstances);
//			// vector.setOptions(fliterOptions);
//			// vector.setIDFTransform(true);
//			// vector.setLowerCaseTokens(true);
//			// vector.setStemmer(new LovinsStemmer());
//			// vector.setAttributeIndices("first-last");
//			// vector.setTokenizer(new WordTokenizer());
//
//			// System.out.println(wekaInstances.get(0));
//			// System.out.println(wekaInstances.get(4));
//			//
//			// wekaInstances = Filter.useFilter(wekaInstances, vector);
//			// System.out.println(wekaInstances.get(0));
//			// System.out.println(wekaInstances.get(4));
//
//			// ===== cluster using CobWeb algorithm
//			// clusterUsingWekaCobWeb(wekaInstances);
//
//			// ===== cluster using EM algorithm
//			clusterUsingWekaEM(wekaInstances);
//
//			// ===== cluster using KMEANS
//			// clusterUsingWekaKMEANS(wekaInstances);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println("\n>>DONE");
//	}
//
//	void clusterUsingWekaCobWeb(Instances wekaInstances) {
//
//		// ====== generate clusters using CobWeb algorithm
//		Cobweb cobWebClusterer = new Cobweb();
//		try {
//
//			cobWebClusterer.buildClusterer(wekaInstances);
//
//			/**
//			 * A way to incremently cluster instances. Taken from:
//			 * https://waikato.github.io/weka-wiki/use_weka_in_your_java_code/
//			 **/
//			// for (Instance wekaInstance : wekaInstances) {
//			// cobWebClusterer.updateClusterer(wekaInstance);
//			// }
//			// cobWebClusterer.updateFinished();
//
//			// System.out.println(cobWebClusterer);
//
//			evaluateWekaClusterer(cobWebClusterer, wekaInstances);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	void clusterUsingWekaEM(Instances wekaInstances) {
//
//		// ====== generate clusters using EM (expectation maximisation)
//		EM emClusterer = new EM();
//
//		String[] options = new String[2];
//
//		try {
//
//			options[0] = "-I"; // iterations
//			options[1] = "100"; // -I 100 sets max iterations to 100
//
//			emClusterer.setOptions(options);
//
//			// generate clusters
//			emClusterer.buildClusterer(wekaInstances);
//
//			// System.out.println(emClusterer);
//
//			evaluateWekaClusterer(emClusterer, wekaInstances);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	void clusterUsingWekaKMEANS(Instances wekaInstances) {
//
//		SimpleKMeans clusterer = new SimpleKMeans();
//
//		try {
//
//			numberOFClusters = 5;
//			clusterer.setNumClusters(numberOFClusters);
//			clusterer.buildClusterer(wekaInstances);
//
//			// System.out.println(Arrays.toString(clusterer.getClusterSizes()));
//
//			evaluateWekaClusterer(clusterer, wekaInstances);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	void evaluateWekaClusterer(Clusterer clusterer, Instances wekaInstances) {
//
//		// evaluation
//		ClusterEvaluation eval = new ClusterEvaluation();
//
//		try {
//
//			clusterer.buildClusterer(wekaInstances);
//
//			eval.setClusterer(clusterer);
//
//			eval.evaluateClusterer(wekaInstances);
//
//			System.out.println(eval.clusterResultsToString());
//			System.out.println(Arrays.toString(eval.getClusterAssignments()));
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	public void clusterUsingWeka(String fileName) {
//
//		instanceFileName = fileName;
//
//		clustersOutputFolder = instanceFileName.substring(0, instanceFileName.lastIndexOf("/")) + "/" + clusterFolder;
//
//		File outputFolder = new File(clustersOutputFolder);
//
//		if (!outputFolder.exists()) {
//			outputFolder.mkdir();
//
//		}
//
//		clustersOutputFileName = clustersOutputFolder + "/" + clustersOutputFileName;
//		wekaInstancesFilePath = clustersOutputFolder + "/" + wekaInstancesFilePath;
//
//		clusterUsingWeka();
//
//	}
//
//	public String convertInstancesToARFF(List<GraphPath> instances) {
//
//		// convert instances to ARFF format:
//		// @RELATION="instance name"
//		String relationName = "@RELATION";
//		// @ATTRIBUTE="attribute name"
//		String attributeName = "@ATTRIBUTE";
//		// attribute can have value NUMERIC, string, nominal (e.g., {nam1,
//		// name2, ...}), and date
//		String numericAttributeValue = "NUMERIC";
//		String stringAttributeValue = "string";
//		// data is deifned by @DATA
//		String dataTag = "@DATA";
//		// data is defined in a row separated by commas. Each row is an
//		// instance. Each comma-separated value corresponds to an attribute
//		// column
//		// e.g., 1,2,weka,{up, down}
//		// % are used for comments
//
//		// a dummy relation value
//		String relationValue = "\"Potential Incident Instance\"";
//
//		// instance id attribute name
//		String instanceIDAttribute = "Instance_ID";
//		/**
//		 * all data array should be of the same length so states that are short
//		 * than the longest are padded with -1
//		 **/
//		// create a text file to hold the data
//
//		String fileLinSeparator = System.getProperty("line.separator");
//
//		StringBuilder builder = new StringBuilder();
//
//		if (instances != null && !instances.isEmpty()) {
//			shortestTransition = instances.get(0).getStateTransitions().size();
//		}
//
//		// find longest and shortest transitions
//		for (GraphPath path : instances) {
//			List<Integer> tmp = path.getStateTransitions();
//
//			if (tmp.size() > longestTransition) {
//				longestTransition = tmp.size();
//			} else if (tmp.size() < shortestTransition) {
//				shortestTransition = tmp.size();
//			}
//		}
//
//		numberOFClusters = longestTransition - shortestTransition + 1;
//
//		// ========relation
//		builder.append(relationName).append(" ").append(relationValue).append(fileLinSeparator);
//
//		// ========instance id attribute
//		builder.append(attributeName).append(" ").append(instanceIDAttribute).append(" ").append(numericAttributeValue)
//				.append(fileLinSeparator);
//
//		// ========states attributes (state-0, state-1, number of maximum
//		// states)
//		for (int i = 0; i < longestTransition; i++) {
//
//			// add attribute name e.g., "state-0 state-1 ..."
//			builder.append(attributeName).append(" ") // attribute tag
//					.append(ATTRIBUTE_STATE_NAME).append(i).append(" ") // attribute
//					// name
//					.append(numericAttributeValue) // attribute type
//					.append(fileLinSeparator);
//		}
//
//		// ========actions attribute (actions names)
//		for (String action : systemActions.keySet()) {
//			builder.append(attributeName).append(" ").append(action).append(" ").append(numericAttributeValue)
//					.append(fileLinSeparator);
//		}
//
//		// ========set data
//		// set data tag (i.e. @DATA)
//		builder.append(dataTag).append(fileLinSeparator);
//
//		for (GraphPath path : instances) {
//
//			// set instance id
//			builder.append(path.getInstanceID()).append(WEKA_DATA_SEPARATOR);
//
//			// set data to be the state transitions
//			List<Integer> states = path.getStateTransitions();
//
//			int i = 0;
//			for (i = 0; i < states.size() - 1; i++) {
//				// add state
//				builder.append(states.get(i)).append(WEKA_DATA_SEPARATOR);
//			}
//			// add last state
//			builder.append(states.get(states.size() - 1));
//
//			// pad the transition with -1
//			if (states.size() < longestTransition) {
//
//				builder.append(WEKA_DATA_SEPARATOR);
//
//				for (i = 0; i < longestTransition - states.size() - 1; i++) {
//
//					builder.append(PADDING_STATE).append(WEKA_DATA_SEPARATOR);
//				}
//
//				builder.append(PADDING_STATE);
//			}
//
//			// add action data
//			// 0 for missing the action from the transition actions. 1 if it
//			// exists
//			List<String> transitionActions = path.getTransitionActions();
//
//			if (transitionActions != null && !transitionActions.isEmpty()) {
//
//				builder.append(WEKA_DATA_SEPARATOR);
//
//				for (i = 0; i < systemActions.size() - 1; i++) {
//
//					if (transitionActions.contains(systemActions.get(i))) {
//						builder.append(ACTION_PERFORMED).append(WEKA_DATA_SEPARATOR);
//					} else {
//						builder.append(ACTION_NOT_PERFORMED).append(WEKA_DATA_SEPARATOR);
//					}
//				}
//
//				// check last action
//				if (transitionActions.contains(systemActions.get(i))) {
//					builder.append(ACTION_PERFORMED);
//				} else {
//					builder.append(ACTION_NOT_PERFORMED);
//				}
//			}
//
//			builder.append(fileLinSeparator);
//		}
//
//		// save string to file
//
//		try {
//			BufferedWriter writer = new BufferedWriter(
//					new OutputStreamWriter(new FileOutputStream(wekaInstancesFilePath), "utf-8"));
//
//			writer.write(builder.toString());
//
//			writer.close();
//
//			return wekaInstancesFilePath;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	public String convertInstancesActionsToARFF(Collection<GraphPath> instances) {
//
//		/**
//		 * Convert instances action into ARFF
//		 * 
//		 */
//
//		// convert instances to ARFF format:
//		// @RELATION="instance name"
//		String relationName = "@RELATION";
//		// @ATTRIBUTE="attribute name"
//		String attributeName = "@ATTRIBUTE";
//		// attribute can have value NUMERIC, string, nominal (e.g., {nam1,
//		// name2, ...}), and date
//		String numericAttributeValue = "NUMERIC";
//		String stringAttributeValue = "string";
//		// data is deifned by @DATA
//		String dataTag = "@DATA";
//		// data is defined in a row separated by commas. Each row is an
//		// instance. Each comma-separated value corresponds to an attribute
//		// column
//		// e.g., 1,2,weka,{up, down}
//		// % are used for comments
//
//		// a dummy relation value
//		String relationValue = "\"Potential Incident Instance\"";
//
//		// instance id attribute name
//		String instanceIDAttribute = "Instance_ID";
//
//		/**
//		 * all data array should be of the same length so states that are short
//		 * than the longest are padded with -1
//		 **/
//		// create a text file to hold the data
//
//		String fileLinSeparator = System.getProperty("line.separator");
//
//		StringBuilder builder = new StringBuilder();
//
//		if (instances != null && !instances.isEmpty()) {
//			shortestTransition = 100;// instances.get(0).getStateTransitions().size();
//		}
//
//		// find longest and shortest transitions
//		for (GraphPath path : instances) {
//			List<Integer> tmp = path.getStateTransitions();
//
//			if (tmp.size() > longestTransition) {
//				longestTransition = tmp.size();
//			} else if (tmp.size() < shortestTransition) {
//				shortestTransition = tmp.size();
//			}
//		}
//
//		numberOFClusters = longestTransition - shortestTransition + 1;
//
//		int numberOfActions = longestTransition - 1;
//
//		// ========relation
//		builder.append(relationName).append(" ").append(relationValue).append(fileLinSeparator);
//
//		// ========instance id attribute
//		builder.append(attributeName).append(" ").append(instanceIDAttribute).append(" ").append(numericAttributeValue)
//				.append(fileLinSeparator);
//
//		// ========actions attribute (action-0, action-1)
//		// number of actions = longest transition-1
//		// instances with less actions are padded with NULL as action
//		for (int i = 0; i < numberOfActions; i++) {
//			builder.append(attributeName).append(" ").append(ATTRIBUTE_ACTION_NAME).append(i).append(" ")
//					.append(numericAttributeValue).append(fileLinSeparator);
//		}
//
//		// ========set data
//		// set data tag (i.e. @DATA)
//		builder.append(dataTag).append(fileLinSeparator);
//
//		for (GraphPath path : instances) {
//
//			// ===== instance ID
//			builder.append(path.getInstanceID()).append(WEKA_DATA_SEPARATOR);
//
//			// ==== set data to be the actions
//			List<String> actions = path.getTransitionActions();
//
//			int i = 0;
//			for (i = 0; i < actions.size() - 1; i++) {
//				// add action
//				builder.append(systemActions.get(actions.get(i))).append(WEKA_DATA_SEPARATOR);
//			}
//			// add last action
//			builder.append(systemActions.get(actions.get(i)));
//
//			// pad the transition with NULL
//			if (actions.size() < numberOfActions) {
//
//				builder.append(WEKA_DATA_SEPARATOR);
//
//				for (i = 0; i < numberOfActions - actions.size() - 1; i++) {
//
//					builder.append(PADDING_ACTION).append(WEKA_DATA_SEPARATOR);
//				}
//
//				builder.append(PADDING_ACTION);
//			}
//
//			builder.append(fileLinSeparator);
//		}
//
//		// ===== save string to file
//		try {
//			BufferedWriter writer = new BufferedWriter(
//					new OutputStreamWriter(new FileOutputStream(wekaInstancesFilePath), "utf-8"));
//
//			writer.write(builder.toString());
//
//			writer.close();
//
//			return wekaInstancesFilePath;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	/********* Utilities *********/
//	/**************************/
//	/**************************/
//	/**************************/
//
//	/******* printers *********/
//	/**************************/
//	/**************************/
//	/**************************/
//
//	public void printClusters(List<Cluster> clusters) {
//
//		int id = 0;
//
//		// used to print a few sets
//		// int length = 5;
//		Random rand = new Random();
//
//		for (Cluster cluster : clusters) {
//			System.out.println("Cluster " + id++);
//			// For each data point: [first entry is the instance name]
//			List<DoubleArray> dataPoints = cluster.getVectors();
//			System.out.println("  number of instances = " + dataPoints.size());
//
//			for (int i = 0; i < lengthToPrint && i < dataPoints.size(); i++) {
//				System.out.println("   " + dataPoints.get(rand.nextInt(dataPoints.size())));
//			}
//
//			if (dataPoints.size() > lengthToPrint) {
//				System.out.println("   ...");
//			}
//		}
//
//	}
//
//	public void printClustersWithMean(List<ClusterWithMean> clusters) {
//
//		int id = 0;
//
//		// used to print a few sets
//		// int length = 5;
//		Random rand = new Random();
//
//		for (ClusterWithMean cluster : clusters) {
//			System.out.println("Cluster " + id++);
//			// For each data point: [first entry is the instance name]
//			List<DoubleArray> dataPoints = cluster.getVectors();
//			System.out.println("  number of instances = " + dataPoints.size());
//
//			for (int i = 0; i < lengthToPrint && i < dataPoints.size(); i++) {
//				System.out.println("   " + dataPoints.get(rand.nextInt(dataPoints.size())));
//			}
//
//			if (dataPoints.size() > lengthToPrint) {
//				System.out.println("   ...");
//			}
//		}
//
//	}
//
//	public void printClustersOPTIC(List<Cluster> clusters) {
//
//		int id = 0;
//
//		// used to print a few sets
//		// int length = 5;
//		Random rand = new Random();
//
//		for (Cluster cluster : clusters) {
//			System.out.println("Cluster " + id++);
//			// For each data point: [first entry is the instance name]
//			List<DoubleArray> dataPoints = cluster.getVectors();
//			System.out.println("  number of instances = " + dataPoints.size());
//
//			for (int i = 0; i < lengthToPrint && i < dataPoints.size(); i++) {
//				System.out.println("   " + dataPoints.get(rand.nextInt(dataPoints.size())));
//			}
//
//			if (dataPoints.size() > lengthToPrint) {
//				System.out.println("   ...");
//			}
//		}
//
//	}
//
//	public static void main(String[] args) {
//
//		IncidentInstancesClusterGenerator tester = new IncidentInstancesClusterGenerator();
//
//		String fileName = "D:/Bigrapher data/lero/lero100K/output";
//
//		// using SPMF library
//		tester.identifyRelevantTraces(fileName);
//
//		// using Weka
//		// tester.clusterUsingWeka(fileName);
//	}
//
//}
