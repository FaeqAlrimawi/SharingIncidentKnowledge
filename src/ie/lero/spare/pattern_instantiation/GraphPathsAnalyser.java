package ie.lero.spare.pattern_instantiation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator.PotentialIncidentInstance;

public class GraphPathsAnalyser {

	private List<GraphPath> paths;
	private int topPercent = 10;

	// key is action name, and list of integer represent different freqs
	// at list(0) is the action frequency against the total number of actions
	// at list(1) is the action frequency against the number of transitions
	private HashMap<String, List<Integer>> actionsFrequency;
	private static final int ACTIONS_FREQ = 0;
	private static final int TRANSITIONS_FREQ = 1;

	private LinkedList<String> commonAssets;
	private LinkedList<Integer> topPaths;
	// private LinkedList<Integer> allShortestPaths;
	private LinkedList<Integer> shortestPaths;
	private LinkedList<Integer> longestPaths;
	private ForkJoinPool mainPool;
	private int maxWaitingTime = 24;
	private TimeUnit timeUnit = TimeUnit.HOURS;
	
	// threshold for the number of states on which task is further subdivided into halfs
	private final static int THRESHOLD = 100;
	
	
	private double percentageFrequency;
	
	//used to convert double into integer for comparison, and also for printing in % format
	private static final int PRECISION = 1000000;
	private TransitionSystem transitionSystem;

	private int totalNumberOfActions;
	private int totalNumberOfTransitions;
	
	//used to idenfity 'Most' actions to satisfy
	private static final double ACTION_SATISFACTION_FACTOR = 0.5;

	private Logger logger;
	private int threadID;
	private String instanceName;
	public static final String PATHS_ANALYSER_NAME = "Graph-Paths-Analyser";

	private static int traceID  =0;
	
	public GraphPathsAnalyser(List<GraphPath> paths, TransitionSystem transSys) {
		this.paths = paths;
		transitionSystem = transSys;
	}

	public GraphPathsAnalyser(List<GraphPath> paths, TransitionSystem transSys, int threadID, Logger logger) {
		this.paths = paths;
		transitionSystem = transSys;
		this.logger = logger;
		instanceName = PotentialIncidentInstance.INSTANCE_GLOABAL_NAME + "[" + threadID + "]"
				+ Logger.SEPARATOR_BTW_INSTANCES + PATHS_ANALYSER_NAME + Logger.SEPARATOR_BTW_INSTANCES;
		
		createIDs();
	}
	
	protected void createIDs() {
		
		for(GraphPath trace : paths) {
//			if(trace.getInstanceID() == -1) {
//				trace.setInstanceID(traceID);
//				traceID++;
//			}
			trace.setInstanceID(traceID);
			traceID++;
			
		}
	}

	public String analyse() {

		mainPool = new ForkJoinPool();

		// getActionsFrequencyOriginal();
		// getTopPathsOriginal();

		// returns the topPaths that has actions with at least frequencey
		// percentage more than or equal to [e.g., 50%]. If true it will return
		// only the paths that contain actions with
		// frequency more than [e.g., 50%]

		// parallelism based functionalities
		//
		 getShortestPaths();
		// getLongestPaths();

		// sets the percentage of the frequency that the actio
		percentageFrequency = 0.9;

		// set operation to perform (>=,>,<=,<,==,!=)
		AnalyserOperation operation = AnalyserOperation.GTE;

		// sets if all/most/any actions in the path has the sepcified frequency
		// in percentageFrequency variable
		ActionsToSatisfy actionsToSatisfy = ActionsToSatisfy.ALL;

//		getTopPaths(percentageFrequency, operation, actionsToSatisfy);

		mainPool.shutdown();

		try {
			if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
				// msgQ.put("Time out! tasks took more than specified maximum
				// time [" + maxWaitingTime + " " + timeUnit + "]");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";// print();
	}

	protected synchronized void increaseNumberOfTotalActions(int numOfActions) {

		totalNumberOfActions += numOfActions;
	}

	public List<GraphPath> getPaths() {
		return paths;
	}

	public void setPaths(LinkedList<GraphPath> paths) {
		this.paths = paths;
	}

	public int getTopPercent() {
		return topPercent;
	}

	public void setTopPercent(int topPercent) {
		this.topPercent = topPercent;
	}

	/**
	 * Returns all actions with their frequency i.e. how many times they
	 * appeared in the different paths
	 * 
	 * @return a Map in which the key is the action and the value is the
	 *         frequency
	 */
	public HashMap<String, List<Integer>> getActionsFrequency() {

		if (actionsFrequency == null || actionsFrequency.isEmpty()) {
			analyseActionsFrequency();
		}

		return actionsFrequency;
	}

	private void analyseActionsFrequency() {

		actionsFrequency = mainPool.invoke(new ActionsFrequencyAnalyser(0, paths.size()));

		// return actionsFrequency;
	}

	/**
	 * Returns all actions with their frequency i.e. how many times they
	 * appeared in the different paths
	 * 
	 * @return
	 */
	// private HashMap<String, Integer> getActionsFrequencyOriginal() {
	//
	// actionsFrequency = new HashMap<String, Integer>();
	// LinkedList<String> actions;
	// int cnt;
	//
	// for(GraphPath path : paths) {
	// actions = path.getPathActions(transitionSystem);
	// for(String action : actions) {
	// //if the action exists in the hashmap, then add one to its counter
	// if(actionsFrequency.containsKey(action)) {
	// cnt = actionsFrequency.get(action);
	// cnt++;
	// actionsFrequency.put(action, cnt);
	// } else {//if it is a new action
	// actionsFrequency.put(action, 1);
	// }
	// }
	// }
	//
	// //sort the map from the most frequent to the least
	// actionsFrequency =
	// (HashMap<String,Integer>)sortByComparator(actionsFrequency, false);
	//
	// return actionsFrequency;
	// }

//	private Map<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, final boolean order) {
//
//		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());
//
//		// Sorting the list based on values
//		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
//			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
//				if (order) {
//					// System.out.println(o1.getValue()+" "+o2.getValue());
//					return o1.getValue().compareTo(o2.getValue());
//				} else {
//					return o2.getValue().compareTo(o1.getValue());
//
//				}
//			}
//		});
//
//		// Maintaining insertion order with the help of LinkedList
//		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
//		for (Entry<String, Integer> entry : list) {
//
//			sortedMap.put(entry.getKey(), entry.getValue());
//		}
//
//		return sortedMap;
//	}

//	private LinkedList<String> getCommonAssets() {
//
//		// to be implemented
//		//
//		return commonAssets;
//	}

	/**
	 * Returns all paths that contain the given actions
	 * 
	 * @param actions
	 *            the list of actions that paths should have
	 * @param isExact
	 *            if true it returns all paths that have exactly the given
	 *            actions in any order. If false it returns all paths that have
	 *            at least the actions given
	 * @return List of path IDs
	 */
	public LinkedList<Integer> getTopPaths(LinkedList<String> actions, boolean isExact) {

		if (topPaths == null || topPaths.isEmpty()) {
			analyseTopPaths(actions, isExact);
		}

		return topPaths;
	}

	private void analyseTopPaths(LinkedList<String> actions, boolean isExact) {

		topPaths = mainPool.invoke(new TopPathsAnalyser(0, paths.size(), actions, isExact));

		// return topPaths;
	}

	/**
	 * Returns all paths that contain actions which has a frequency percentage
	 * >= to the given frequencey percentage (percentage =
	 * action_frequency/num_of_paths)
	 * 
	 * @param actionsFrequencyPercentage
	 *            the percentage that actions should have at least
	 * @param isExact
	 *            if true it returns all paths that have exactly the actions
	 *            with a percentage >= given percentage. If false it returns all
	 *            paths that have at least the actions with percentage >= given
	 *            percentage
	 * @return List of path IDs
	 */
	public LinkedList<Integer> getTopPaths(double actionsFrequencyPercentage, AnalyserOperation operation,
			ActionsToSatisfy actionsToSatisfy) {

		if (topPaths == null || topPaths.isEmpty()) {
			analyseTopPaths(actionsFrequencyPercentage, operation, actionsToSatisfy);
		}

		return topPaths;
	}

	private void analyseTopPaths(double actionsFrequencyPercentage, AnalyserOperation operation,
			ActionsToSatisfy actionsToSatisfy) {

		// if actions frequency are not calculated then calculate them
		if (actionsFrequency == null || actionsFrequency.isEmpty()) {
			logger.putMessage(instanceName + "Generating actions frequencey...");
			actionsFrequency = mainPool.invoke(new ActionsFrequencyAnalyser(0, paths.size()));
		}

		totalNumberOfTransitions = paths.size();

		logger.putMessage(instanceName + "Total number of actions = " + totalNumberOfActions);
		logger.putMessage(instanceName + "Total number of transitions = " + totalNumberOfTransitions);

//		for (Entry<String, List<Integer>> entry : actionsFrequency.entrySet()) {
//			logger.putMessage(instanceName + "action [" + entry.getKey() + "] actions-freq = "
//					+ entry.getValue().get(ACTIONS_FREQ) + ", trans-freq = " + entry.getValue().get(TRANSITIONS_FREQ));
//		}

		//analyse transitions
		logger.putMessage(instanceName + "Analysing top transitions based on percentage (" + actionsFrequencyPercentage
				+ "), operation (" + operation.toString() + "), actionsToSatisfy (" + actionsToSatisfy.toString()
				+ ")");
		
		topPaths = mainPool.invoke(
				new TopActionsAnalyser(0, paths.size(), actionsFrequencyPercentage, operation, actionsToSatisfy));

		logger.putMessage(
				instanceName + "# of top transitions = " + topPaths.size());

	}

	// protected int getNumberOfActions() {
	//
	// int num = 0;
	//
	// for (GraphPath path : paths) {
	// num += path.getPathActions(transitionSystem).size();
	// }
	//
	// return num;
	// }

	// protected Map<String, List<Integer>> getActionsFrequencyIterative() {
	//
	// Map<String, List<Integer>> freqs = new HashMap<String, List<Integer>>();
	// List<String> visited = new LinkedList<String>();
	//
	// for (GraphPath path : paths) {
	//
	// visited.clear();
	// for (String action : path.getPathActions(transitionSystem)) {
	//
	// if (freqs.containsKey(action)) {
	// List<Integer> vals = freqs.get(action);
	// int act = vals.get(ACTIONS_FREQ);
	// act++;
	// vals.set(ACTIONS_FREQ, act);
	//
	// if (!visited.contains(action)) {
	// int trans = vals.get(TRANSITIONS_FREQ);
	// trans++;
	// vals.set(TRANSITIONS_FREQ, trans);
	// visited.add(action);
	// }
	//
	// } else {
	// List<Integer> tmp = new LinkedList<Integer>();
	// tmp.add(ACTIONS_FREQ, 1);
	// tmp.add(TRANSITIONS_FREQ, 1);
	// freqs.put(action, tmp);
	// }
	// }
	// }
	//
	// return freqs;
	//
	// }
	/**
	 * Returns all paths that contain all common actions
	 * 
	 * @return
	 */
	// private LinkedList<Integer> getTopPathsOriginal() {
	//
	// topPaths = new LinkedList<Integer>();
	// LinkedList<String> pathActions;
	// LinkedList<String> actions = new LinkedList<String>();
	//
	// //for testing
	// double perc = 0.85;
	// int numOfPaths = paths.size();
	//
	// for(Entry<String, Integer> set : actionsFrequency.entrySet()) {
	// if((double)set.getValue()/(double)numOfPaths >= perc) {
	// actions.add(set.getKey());
	// }
	// }
	//
	// System.out.println("actions with >= 0.8 : "+ actions);
	//
	// for(int i=0;i< paths.size();i++) {
	// pathActions = paths.get(i).getPathActions(transitionSystem);
	//
	// //if the path contains all common actions then the path is considered a
	// top one
	// if(pathActions.containsAll(actions)) {
	// topPaths.add(i);
	// }
	// }
	//
	// return topPaths;
	// }

	/**
	 * Returns the shortest paths i.e. paths that has the minimum number of
	 * states/actions
	 * 
	 * @return IDs of the shortest paths
	 */
	public LinkedList<Integer> getShortestPaths() {

		if (shortestPaths == null || shortestPaths.isEmpty()) {
			analyseShortestPaths();
		}

		return shortestPaths;
	}

	private void analyseShortestPaths() {

		// shortestPaths = new LinkedList<Integer>();

		// if(paths == null || paths.size() == 0) {
		// return shortestPaths;
		// }

		logger.putMessage(instanceName+"Identifiying shortest transitions...");
		
		shortestPaths = mainPool
				.invoke(new PathsAnalyserParallelism(0, paths.size(), PathsAnalyserParallelism.SHORTEST));

		if(shortestPaths!=null && !shortestPaths.isEmpty()) {
			logger.putMessage(instanceName+"# of identified shortest transitions = " + shortestPaths.size());	
		} else {
			logger.putMessage(instanceName+"# of identified shortest transitions = 0");
		}
		
	}

	/**
	 * Returns the shortest paths i.e. paths that has the minimum number of
	 * states/actions
	 * 
	 * @return IDs of the shortest paths
	 */
	private LinkedList<Integer> getShortestPathsOriginal() {
		shortestPaths = new LinkedList<Integer>();

		// initial smallest size

		if (paths == null || paths.size() == 0) {
			return shortestPaths;
		}

		int numOfStates = paths.get(0).getStateTransitions().size();
		int size;

		shortestPaths.add(0);

		for (int i = 1; i < paths.size(); i++) {
			// if current path has a number of states smaller than the set one
			// then the set one is changed
			size = paths.get(i).getStateTransitions().size();
			if (size < numOfStates) {
				numOfStates = size;
				shortestPaths.clear();
				shortestPaths.add(i);
			} else if (size == numOfStates) {
				shortestPaths.add(i);
			}
		}

		// this sets the number of actions for the shortest paths
		// shortestPaths.add(numOfStates-1);

		return shortestPaths;
	}

	public LinkedList<Integer> getLongestPaths() {

		if (longestPaths == null || longestPaths.isEmpty()) {
			analyseLongestPaths();
		}

		return longestPaths;
	}

	private void analyseLongestPaths() {
		
		longestPaths = mainPool.invoke(new PathsAnalyserParallelism(0, paths.size(), PathsAnalyserParallelism.LONGEST));

	}

	private LinkedList<Integer> getLongestPathsOriginal() {
		longestPaths = new LinkedList<Integer>();

		if (paths == null || paths.size() == 0) {
			return shortestPaths;
		}

		// initial smallest size
		int numOfStates = paths.get(0).getStateTransitions().size();
		int size;

		longestPaths.add(0);

		for (int i = 1; i < paths.size(); i++) {
			// if current path has a number of states smaller than the set one
			// then the set one is changed
			size = paths.get(i).getStateTransitions().size();
			if (size > numOfStates) {
				numOfStates = size;
				longestPaths.clear();
				longestPaths.add(i);
			} else if (size == numOfStates) {
				longestPaths.add(i);
			}
		}

		// this sets the number of actions for the longest paths
		// longestPaths.add(numOfStates-1);

		return longestPaths;
	}

	public String print() {

		StringBuilder str = new StringBuilder();
		String newLine = "\n";
		int actionsNum = 0;

		if (paths == null || paths.size() == 0) {
			return null;
		}

		// get common paths
		if (actionsFrequency != null) {
			str.append(newLine).append("-Actions Frequency: [");

			 for(Entry<String, List<Integer>> freq : actionsFrequency.entrySet()) {
			 double perc =
			 ((int)(((double)freq.getValue().get(TRANSITIONS_FREQ)/(double)totalNumberOfTransitions)*(PRECISION*1.0))/(PRECISION*1.0))*100.0;
			 str.append(freq.getKey()).append("=").append(freq.getValue()).append("(").append(perc).append("%), ");
			 }

			// removes the last comma and space
			str.replace(str.length() - 2, str.length(), "");
			str.append("]").append(newLine);
		}

		// get top paths based on the common actions i.e. paths that contain all
		// common actions
		if (topPaths != null) {
			str.append("-top paths [" + topPaths.size() + "] (based on actions Frequency >= ")
					.append(percentageFrequency).append("):").append(topPaths).append(newLine);
		}

		// get shortest paths
		if (shortestPaths != null && shortestPaths.size() > 0) {
			actionsNum = paths.get(shortestPaths.getFirst()).getStateTransitions().size() - 1;
			str.append("-Shortest Paths [").append(shortestPaths.size()).append("] (").append(actionsNum)
					.append(" actions): ").append(shortestPaths).append(newLine);
		} else {
			str.append("-Shortest Paths: [NONE]");
		}

		// get longest paths
		if (longestPaths != null && longestPaths.size() > 0) {
			actionsNum = paths.get(longestPaths.getFirst()).getStateTransitions().size() - 1;
			str.append("-Longest Paths [").append(longestPaths.size()).append("] (").append(actionsNum)
					.append(" actions): ").append(longestPaths).append(newLine);
		} else {
			str.append("-Longest Paths: [NONE]");
		}

		return str.toString();
	}

	public String convertToJSONStr() {

		StringBuilder str = new StringBuilder();
		int actionsNum = 0;

		if (paths == null || paths.size() == 0) {
			return null;
		}

		str.append("{");

		// get common paths
		if (actionsFrequency != null) {
			str.append("\"actions_frequency\": [");
			 for(Entry<String, List<Integer>> freq : actionsFrequency.entrySet()) {
			 double perc =
			 (int)(((double)freq.getValue().get(TRANSITIONS_FREQ)/(double)totalNumberOfTransitions)*(PRECISION*1.0))/(PRECISION*1.0);
			 str.append("{")
			 .append("\"action\":").append(freq.getKey()).append(",")
			 .append("\"frequency\":").append(freq.getValue()).append(",")
			 .append("\"precentage\":").append(perc)
			 .append("},");
			 }

			if (actionsFrequency.size() > 0) {
				str.deleteCharAt(str.length() - 1);// remove comma
			}

			str.append("],");

		} else {
			str.append("\"actions_frequency\": [],");
		}

		// get top paths based on the common actions i.e. paths that contain all
		// common actions
		if (topPaths != null) {
			str.append("\"top_paths\": {").append("\"frequency\":").append(percentageFrequency).append(",")
					.append("\"transitions_ids\":").append(topPaths).append("},");
		} else {
			str.append("\"top_paths\": {").append("\"frequency\":").append(0).append(",")
					.append("\"transitions_ids\":[]").append("},");
		}

		// get shortest paths
		if (shortestPaths != null && shortestPaths.size() > 0) {
			actionsNum = paths.get(shortestPaths.getFirst()).getStateTransitions().size() - 1;
			str.append("\"shortest_transitions\":{").append("\"number_of_actions\":").append(actionsNum).append(",")
					.append("\"transitions_ids\":").append(shortestPaths).append("},");
		} else {
			str.append("\"shortest_transitions\":{").append("\"number_of_actions\":0,").append("\"transitions_ids\":[]")
					.append("},");
		}

		// get longest paths
		if (longestPaths != null && longestPaths.size() > 0) {
			actionsNum = paths.get(longestPaths.getFirst()).getStateTransitions().size() - 1;
			str.append("\"longest_transitions\":{").append("\"number_of_actions\":").append(actionsNum).append(",")
					.append("\"transitions_ids\":").append(longestPaths).append("}");
		} else {
			str.append("\"longest_transitions\":{").append("\"number_of_actions\":0,").append("\"transitions_ids\":[]")
					.append("}");
		}

		str.append("}");

		return str.toString();
	}

	class PathsAnalyserParallelism extends RecursiveTask<LinkedList<Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private LinkedList<Integer> sPaths;
		protected static final int SHORTEST = 1;
		protected static final int LONGEST = 2;
		private int operation = SHORTEST;
		// for testing
		// protected int numOfParts = 0;

		public PathsAnalyserParallelism(int indexStart, int indexEnd, int operation) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			sPaths = new LinkedList<Integer>();
			this.operation = operation;
		}

		@Override
		protected LinkedList<Integer> compute() {
			// TODO Auto-generated method stub

			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<PathsAnalyserParallelism, LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(PathsAnalyserParallelism arg0) {
								return arg0.sPaths;
							}

						}).reduce(sPaths, new BinaryOperator<LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(LinkedList<Integer> arg0, LinkedList<Integer> arg1) {

								// finds all shortest paths
								if (operation == PathsAnalyserParallelism.SHORTEST) {

									// if the first list (arg0) has less number
									// of transtions than that of the 2nd list
									// (arg1)
									if (arg0.size() > 0 && arg1.size() > 0) {
										if (paths.get(arg0.getFirst()).getStateTransitions().size() < paths
												.get(arg1.getFirst()).getStateTransitions().size()) {
											return arg0;
										} else if (paths.get(arg0.getFirst()).getStateTransitions().size() > paths
												.get(arg1.getFirst()).getStateTransitions().size()) {
											arg0.clear();
											arg0.addAll(arg1);
											return arg0;
										}
										// if they both equal then merge them
										// and return their merge
										else {
											arg0.addAll(arg1);
											return arg0;
										}

									} else if (arg0.size() > 0) {
										return arg0;
									} else {
										arg0.clear();
										arg0.addAll(arg1);
										return arg0;
									}

									// finds all longest paths
								} else if (operation == PathsAnalyserParallelism.LONGEST) {
									if (arg0.size() > 0 && arg1.size() > 0) {
										if (paths.get(arg0.getFirst()).getStateTransitions().size() > paths
												.get(arg1.getFirst()).getStateTransitions().size()) {
											return arg0;
										} else if (paths.get(arg0.getFirst()).getStateTransitions().size() < paths
												.get(arg1.getFirst()).getStateTransitions().size()) {

											arg0.clear();
											arg0.addAll(arg1);
											return arg0;
										}
										// if they both equal then merge them
										// and return their merge
										else {
											arg0.addAll(arg1);
											return arg0;
										}

									} else if (arg0.size() > 0) {
										return arg0;
									} else {
										arg0.clear();
										arg0.addAll(arg1);
										return arg0;
									}

								}
								// other than the longest or shortest it returns
								// the seed element
								else {
									return arg0;
								}
							}
						});

			} else {

				int numOfStates = paths.get(indexStart).getStateTransitions().size();
				int size;

				sPaths.add(indexStart);

				if (operation == SHORTEST) {
					for (int i = indexStart + 1; i < indexEnd; i++) {
						// if current path has a number of states smaller than
						// the set one then the set one is changed
						size = paths.get(i).getStateTransitions().size();
						if (size < numOfStates) {
							numOfStates = size;
							sPaths.clear();
							sPaths.add(i);
						} else if (size == numOfStates) {
							sPaths.add(i);
						}
					}
				} else if (operation == LONGEST) {
					for (int i = indexStart + 1; i < indexEnd; i++) {
						// if current path has a number of states smaller than
						// the set one then the set one is changed
						size = paths.get(i).getStateTransitions().size();
						if (size > numOfStates) {
							numOfStates = size;
							sPaths.clear();
							sPaths.add(i);
						} else if (size == numOfStates) {
							sPaths.add(i);
						}
					}
				}
				return sPaths;

			}

		}

		private Collection<PathsAnalyserParallelism> createSubTasks() {
			List<PathsAnalyserParallelism> dividedTasks = new LinkedList<PathsAnalyserParallelism>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new PathsAnalyserParallelism(indexStart, mid, operation));
			dividedTasks.add(new PathsAnalyserParallelism(mid, indexEnd, operation));

			return dividedTasks;
		}
	}

	class ActionsFrequencyAnalyser extends RecursiveTask<HashMap<String, List<Integer>>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private HashMap<String, List<Integer>> actionsFrequency;
		// for testing
		// protected int numOfParts = 0;

		public ActionsFrequencyAnalyser(int indexStart, int indexEnd) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			actionsFrequency = new HashMap<String, List<Integer>>();
		}

		@Override
		protected HashMap<String, List<Integer>> compute() {

			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<ActionsFrequencyAnalyser, HashMap<String, List<Integer>>>() {

							@Override
							public HashMap<String, List<Integer>> apply(ActionsFrequencyAnalyser arg0) {
								return arg0.actionsFrequency;
							}

						}).reduce(actionsFrequency, new BinaryOperator<HashMap<String, List<Integer>>>() {

							@Override
							public HashMap<String, List<Integer>> apply(HashMap<String, List<Integer>> arg0,
									HashMap<String, List<Integer>> arg1) {

								for (Entry<String, List<Integer>> set : arg1.entrySet()) {
									// if the action is already contained in the
									// result (arg0) then add the count to the
									// current count else add a new action with
									// the arg1 count
									String action = set.getKey();
									List<Integer> arg1List = set.getValue();

									if (arg0.containsKey(action)) {
										List<Integer> arg0List = arg0.get(action);
										int newActionFreq = arg0List.get(ACTIONS_FREQ) + arg1List.get(ACTIONS_FREQ);
										arg0List.set(ACTIONS_FREQ, newActionFreq);

										int newTransFreq = arg0List.get(TRANSITIONS_FREQ)
												+ arg1List.get(TRANSITIONS_FREQ);
										arg0List.set(TRANSITIONS_FREQ, newTransFreq);

										// arg0.put(action, oldValues);
									} else {

										arg0.put(action, arg1List);
									}

								}

								return arg0;
							}
						});

			} else {

				// get actions frequency
				List<String> actions;

				// int cnt = 0;
				int numOfActions = 0;

				for (int i = indexStart; i < indexEnd; i++) {

					List<String> visited = new LinkedList<String>();
					actions = paths.get(i).getTransitionActions();

					for (String action : actions) {

						// to determine number of all actions
						numOfActions++;

						// if the action exists in the hashmap, then add one to
						// its counter
						if (actionsFrequency.containsKey(action)) {
							List<Integer> vals = actionsFrequency.get(action);

							// increment actions freq
							int act = vals.get(ACTIONS_FREQ);
							act++;
							vals.set(ACTIONS_FREQ, act);

							// increment transitions freq
							int freq = vals.get(TRANSITIONS_FREQ);

							if (!visited.contains(action)) {
								freq++;
								vals.set(TRANSITIONS_FREQ, freq);
							}

							visited.add(action);

							actionsFrequency.put(action, vals);

						} else {// if it is a new action
							List<Integer> tmp = new LinkedList<Integer>();
							tmp.add(ACTIONS_FREQ, 1); // increment actions freq
							tmp.add(TRANSITIONS_FREQ, 1); // increment
															// transitions freq
							actionsFrequency.put(action, tmp);
						}
					}
				}

				// add the number of actions found
				increaseNumberOfTotalActions(numOfActions);

				return actionsFrequency;
			}
		}

		private Collection<ActionsFrequencyAnalyser> createSubTasks() {
			List<ActionsFrequencyAnalyser> dividedTasks = new LinkedList<ActionsFrequencyAnalyser>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new ActionsFrequencyAnalyser(indexStart, mid));
			dividedTasks.add(new ActionsFrequencyAnalyser(mid, indexEnd));

			return dividedTasks;
		}

		private Map<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, final boolean order) {

			List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

			// Sorting the list based on values
			Collections.sort(list, new Comparator<Entry<String, Integer>>() {
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					if (order) {
						return o1.getValue().compareTo(o2.getValue());
					} else {
						return o2.getValue().compareTo(o1.getValue());

					}
				}
			});

			// Maintaining insertion order with the help of LinkedList
			Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
			for (Entry<String, Integer> entry : list) {

				sortedMap.put(entry.getKey(), entry.getValue());
			}

			return sortedMap;
		}
	}

	enum AnalyserOperation {

		GTE, GT, LTE, LT, EQ, NQ;
	}

	enum ActionsToSatisfy {
		ALL, MOST, ANY;
	}

	/**
	 * returns all transitions that has actions (All or some, depending on
	 * isExact true or false) with frequency >= to the given percentage
	 * 
	 * @author Faeq
	 *
	 */
	class TopActionsAnalyser extends RecursiveTask<LinkedList<Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private LinkedList<Integer> topPaths;
		// private boolean isExact = false;
		private double percentage;
		private AnalyserOperation operation;
		private ActionsToSatisfy actionsToSatisfy;

		// private double frequenceyPercentage = 0.10;

		public TopActionsAnalyser(int indexStart, int indexEnd, double percentage, AnalyserOperation operation,
				ActionsToSatisfy actionsToSatisfy) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.percentage = percentage;
			// this.isExact = isExact;
			this.operation = operation;
			this.actionsToSatisfy = actionsToSatisfy;
			topPaths = new LinkedList<Integer>();
		}

		@Override
		protected LinkedList<Integer> compute() {
			// TODO Auto-generated method stub

			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<TopActionsAnalyser, LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(TopActionsAnalyser arg0) {
								return arg0.topPaths;
							}

						}).reduce(topPaths, new BinaryOperator<LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(LinkedList<Integer> arg0, LinkedList<Integer> arg1) {

								arg0.addAll(arg1);
								return arg0;
							}
						});

			} else {

				switch (actionsToSatisfy) {
				case ALL:
					satisfyAll();
					break;
				case MOST:
					satisfyMost();
					break;
				case ANY:
					satisfyAny();
					break;
				default:
					satisfyAll();

				}
			
				return topPaths;

			}

		}

		private void satisfyAll() {
			
			//all actions in a transition should satisfy the operation (i.e. actions_freq OP specified_freq is true)
			Outer_Loop: for (int i = indexStart; i < indexEnd; i++) {

				List<String> pathActions = paths.get(i).getTransitionActions();
				// boolean isTopPath = false;
				for (String action : pathActions) {

					// probably can be removed as all transitions
					// actions should be in the actions frequency map
					if (!actionsFrequency.containsKey(action)) {

						continue Outer_Loop;
					}

					int actionsFreq = actionsFrequency.get(action).get(TRANSITIONS_FREQ);

					int perc = (int) (((double) actionsFreq / (double) totalNumberOfTransitions) * (double) PRECISION);

					int modefiedPercentage = (int) ((percentage) * (double) PRECISION);

					if (!compare(perc, operation, modefiedPercentage)) {
						continue Outer_Loop;
					}

				}

				topPaths.add(i);
			}
		}

		private void satisfyMost() {

			// most is by default defined to be more than or equal to half the
			// actions in the transition
			double satisfactionFactor = ACTION_SATISFACTION_FACTOR;

			Outer_Loop: for (int i = indexStart; i < indexEnd; i++) {

				List<String> pathActions = paths.get(i).getTransitionActions();

				int most = (int) Math.ceil(pathActions.size() * satisfactionFactor);

				int numSatisfied = 0;

				boolean isTopPath = false;

				for (String action : pathActions) {

					// probably can be removed as all transitions
					// actions should be in the actions frequency map
					if (!actionsFrequency.containsKey(action)) {

						continue Outer_Loop;
					}

					int actionsFreq = actionsFrequency.get(action).get(TRANSITIONS_FREQ);

					int perc = (int) (((double) actionsFreq / (double) totalNumberOfTransitions) * (double) PRECISION);

					int modefiedPercentage = (int) ((percentage) * (double) PRECISION);

					if (compare(perc, operation, modefiedPercentage)) {
						numSatisfied++;

						if (numSatisfied >= most) {
							isTopPath = true;
							break;
						}
					}

				}

				if (isTopPath) {
					topPaths.add(i);
				}

			}
		}

		private void satisfyAny() {

			// if any action satifies the operation then it will be added (i.e.
			// at least 1 actions)
			Outer_Loop: for (int i = indexStart; i < indexEnd; i++) {

				List<String> pathActions = paths.get(i).getTransitionActions();
				boolean isTopPath = false;
				for (String action : pathActions) {

					// probably can be removed as all transitions
					// actions should be in the actions frequency map
					if (!actionsFrequency.containsKey(action)) {

						continue Outer_Loop;
					}

					int actionsFreq = actionsFrequency.get(action).get(TRANSITIONS_FREQ);

					int perc = (int) (((double) actionsFreq / (double) totalNumberOfTransitions) * (double) PRECISION);

					int modefiedPercentage = (int) ((percentage) * (double) PRECISION);

					if (compare(perc, operation, modefiedPercentage)) {
						isTopPath = true;
						break;
					}

				}

				if (isTopPath) {
					topPaths.add(i);
				}

			}

		}

		private boolean compare(int actionPercentage, AnalyserOperation operation, int specifiedPercentage) {

			switch (operation) {
			case GTE:
				if (actionPercentage >= specifiedPercentage) {
					return true;
				}
				break;
			case GT:
				if (actionPercentage > specifiedPercentage) {
					return true;
				}
				break;
			case LTE:
				if (actionPercentage <= specifiedPercentage) {
					return true;
				}
				break;
			case LT:
				if (actionPercentage < specifiedPercentage) {
					return true;
				}
				break;
			case EQ:
				if (actionPercentage == specifiedPercentage) {
					return true;
				}
				break;
			case NQ:
				if (actionPercentage != specifiedPercentage) {
					return true;
				}
				break;
			default: // greater than or equal
				if (actionPercentage >= specifiedPercentage) {
					return true;
				}
			}

			return false;

		}

		private Collection<TopActionsAnalyser> createSubTasks() {
			List<TopActionsAnalyser> dividedTasks = new LinkedList<TopActionsAnalyser>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new TopActionsAnalyser(indexStart, mid, percentage, operation, actionsToSatisfy));
			dividedTasks.add(new TopActionsAnalyser(mid, indexEnd, percentage, operation, actionsToSatisfy));

			return dividedTasks;
		}

	}

	// returns all paths that contain the actions given in the constructor
	// the actions can be set by a percentage of frequencey

	class TopPathsAnalyser extends RecursiveTask<LinkedList<Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private LinkedList<Integer> topPaths;
		private boolean isExact = false;
		private LinkedList<String> actions;

		// private double frequenceyPercentage = 0.10;

		public TopPathsAnalyser(int indexStart, int indexEnd, LinkedList<String> actions, boolean isExact) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			topPaths = new LinkedList<Integer>();
			// this.frequenceyPercentage = frequenceyPercentage;
			this.actions = actions;
			this.isExact = isExact;
		}

		@Override
		protected LinkedList<Integer> compute() {
			// TODO Auto-generated method stub

			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<TopPathsAnalyser, LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(TopPathsAnalyser arg0) {
								return arg0.topPaths;
							}

						}).reduce(topPaths, new BinaryOperator<LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(LinkedList<Integer> arg0, LinkedList<Integer> arg1) {

								arg0.addAll(arg1);
								return arg0;
							}
						});

			} else {

				List<String> pathActions;

				// all actions have a frequency >= to the specified one
				if (isExact) {
					for (int i = indexStart; i < indexEnd; i++) {
						pathActions = paths.get(i).getTransitionActions();

						if (pathActions.size() != actions.size()) {
							continue;
						}

						// compares the two list independent of order
						if (new HashSet<>(pathActions).equals(new HashSet<>(actions))) {
							topPaths.add(i);
						}
					}
					// contains the given action (could have more actions
				} else {
					for (int i = indexStart; i < indexEnd; i++) {
						pathActions = paths.get(i).getTransitionActions();

						// if the path contains all common actions then the path
						// is considered a top one
						if (pathActions.containsAll(actions)) {
							topPaths.add(i);
						}
					}
				}

				return topPaths;

			}

		}

		private Collection<TopPathsAnalyser> createSubTasks() {
			List<TopPathsAnalyser> dividedTasks = new LinkedList<TopPathsAnalyser>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new TopPathsAnalyser(indexStart, mid, actions, isExact));
			dividedTasks.add(new TopPathsAnalyser(mid, indexEnd, actions, isExact));

			return dividedTasks;
		}
	}

}
