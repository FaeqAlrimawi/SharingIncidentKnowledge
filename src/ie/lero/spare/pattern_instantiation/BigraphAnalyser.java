package ie.lero.spare.pattern_instantiation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import cyberPhysical_Incident.Activity;
import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator.PotentialIncidentInstance;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;

public class BigraphAnalyser {

	private PredicateHandler predicateHandler;
	private LinkedList<GraphPath> paths;
	private HashMap<Integer, Bigraph> states;
	private Matcher matcher;

	/** Threading parameters **/
	// determines if activities should run in parallel
	private boolean isThreading = true;

	// determine if predicates should run in parallel
	private boolean isPredicateThreading = false;

	// determines how many activities should be threaded
	private int numberofActivityParallelExecution = 1; // set by the
														// PotentialIncidentInstance
														// class in the
														// instantiator class

	private ExecutorService predicateExecutor = Executors.newCachedThreadPool();
	/************************/

	private int threadID;
	private int maxWaitingTime = 24;
	private TimeUnit timeUnit = TimeUnit.HOURS;

	// used to calculate average time of execution
	private int numberOfConditions = 0;

	// threshold for the number of states on which task is further subdivided
	// into halfs
	private int threshold;
	private int thresholdResidue;
	private static final int Adjust_Threshold_State_Number = 1000;

	// 1.25%, this is related to having 2 threads available in a system (i.e. 2
	// CPUs available)
	private static final double PERCENTAGE__OF_STATES = 0.0125;

	private static final int DEFAULT_THRESHOLD = 100;
	private static final int MINIMUM_THRESHOLD = 10;

	// system handler
	private SystemInstanceHandler systemHandler;
	private TransitionSystem transitionSystem;

	// used to reduce the number of states to match against
	List<Integer> statesWithNoEdges;

	// using forkjoin threading for dividing the matching process of states to a
	// condition
	private ForkJoinPool mainPool;

	private boolean isTestingTime = true;

	private String bigraphAnalyserInstanceName;
	public static final String BIGRAPH_ANALYSER_NAME = "Bigraph-Analyser";

	protected int numberOfPartitions = 0;

	private LinkedList<Future<Integer>> predicateResults = new LinkedList<Future<Integer>>();
	private Logger logger;

	public BigraphAnalyser() {

		this(null, -1);
	}

	public BigraphAnalyser(PredicateHandler predHandler) {
		this(predHandler, -1);

	}

	public BigraphAnalyser(PredicateHandler predHandler, int threadID) {

		this(predHandler, threadID, null);
	}

	public BigraphAnalyser(PredicateHandler predHandler, int threadID, Logger logger) {

		this(predHandler, threadID, logger, SystemsHandler.getCurrentSystemHandler());
	}

	public BigraphAnalyser(PredicateHandler predHandler, int threadID, Logger logger,
			SystemInstanceHandler sysHandler) {

		systemHandler = sysHandler;
		transitionSystem = systemHandler != null ? systemHandler.getTransitionSystem() : null;
		states = systemHandler.getStates();
		matcher = new Matcher();
		this.logger = logger;
		predicateHandler = predHandler;
		this.threadID = threadID;
		statesWithNoEdges = new LinkedList<Integer>();

		bigraphAnalyserInstanceName = PotentialIncidentInstance.INSTANCE_GLOABAL_NAME + "[" + threadID + "]"
				+ Logger.SEPARATOR_BTW_INSTANCES + BIGRAPH_ANALYSER_NAME + Logger.SEPARATOR_BTW_INSTANCES;
	}

	public int getNumberofActivityParallelExecution() {
		return numberofActivityParallelExecution;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public void setNumberofParallelActivity(int numberofActivityParallelExecution) {
		this.numberofActivityParallelExecution = numberofActivityParallelExecution;
	}

	public synchronized Bigraph getBigraphState(int state) {
		
		 return states.get(state);
	}
	
	protected void setThreshold() {

		/**
		 * Threshold is dynamic and # of partitions are fixed
		 **/
		numberOfPartitions = mainPool.getParallelism();

		if (numberOfPartitions == 0) {
			numberOfPartitions = 1;
		}

		// double the number of threads
		// numberOfPartitions*=2;

		threshold = (int) Math.floor(states.size() / (1.0 * numberOfPartitions));

		if (threshold != 0) {

			double percentage = (threshold / (states.size() * 1.0));

			while (percentage > PERCENTAGE__OF_STATES && threshold > MINIMUM_THRESHOLD) {

				// increase number of partitions
				numberOfPartitions += numberOfPartitions;

				// calcualte new threshold value
				threshold = (int) Math.floor(states.size() / (1.0 * numberOfPartitions));

				// update percentage
				percentage = (threshold / (states.size() * 1.0));
			}

			if (threshold < MINIMUM_THRESHOLD) {
				threshold = MINIMUM_THRESHOLD;
			}
			// threshold is zero (because you have a lot of threads, or
			// something went
			// then threshold is set to a fixed minimum value
		} else {

			// machine has a lot of threads available
			if (numberOfPartitions >= states.size()) {
				threshold = MINIMUM_THRESHOLD;
			}
			// something went wrong
			else {
				// then threshold is set to fixed value
				threshold = DEFAULT_THRESHOLD;
			}

			numberOfPartitions = (int) Math.ceil(states.size() / (threshold * 1.0));
		}

		// ****to apply this, the bigraph matching functionality should be
		// changed
		// accordingly (i.e. use createPartitions function)

		/**********************************************/

		/** Threshold is fixed and # of partitions are dynamics **/
		// threshold = DEFAULT_THRESHOLD;
		// if (states.size() > threshold) {
		//// numberOfPartitions = (int) Math.pow(2,
		//// Math.ceil(32 - Integer.numberOfLeadingZeros(states.size() /
		// threshold)));
		// numberOfPartitions = states.size()/threshold;
		// } else {
		// numberOfPartitions = 1;
		// }

		// ****to apply this, the bigraph matching functionality should be
		// changed
		// accordingly (i.e. use createSubTask function)
		/*******************************************************/

		/**
		 * Threshold is set to be a percentage of the # of states (e.g., 5%),
		 * hence, # of partitions is fixed
		 **/
		// threshold = (int) Math.ceil(states.size() * PERCENTAGE__OF_STATES);
		//
		// if(threshold == 0) {
		// threshold = states.size();
		// }
		//
		// if (states.size() > threshold) {
		//// numberOfPartitions = (int) Math.pow(2,
		//// Math.ceil(32 - Integer.numberOfLeadingZeros(states.size() /
		// threshold)));
		// numberOfPartitions = states.size()/threshold;
		// } else {
		// numberOfPartitions = 1;
		// }
		// ****to apply this, the bigraph matching functionality should be
		// changed
		// accordingly (i.e. use createSubTask function)

		/*******************************************************/

		// update threshold residue
		thresholdResidue = (int) Math.ceil(states.size() % (threshold * 1.0));

	}

	public PredicateHandler analyse() {

		int numberOfThreads = Runtime.getRuntime().availableProcessors();

		return analyse(numberOfThreads);
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public PredicateHandler analyse(int numberOfThreads) {

		try {

			long startTime = Calendar.getInstance().getTimeInMillis();

			if (isThreading) {

				mainPool = new ForkJoinPool(numberOfThreads);

				setThreshold();

				if (isTestingTime) {
					logger.putMessage(bigraphAnalyserInstanceName + "number of states: " + states.size()
							+ ", state percentage for threshold [fixed]:" + PERCENTAGE__OF_STATES * 100 + "%"
							+ ", minimum threshold: " + MINIMUM_THRESHOLD + ", default threshold: " + DEFAULT_THRESHOLD
							+ ", calculated threshold: " + threshold + " ("
							+ (int) ((threshold * 1.0 / states.size()) * 10000) / 100.0 + "%)"
							+ ", number of partitions: " + numberOfPartitions + ", Number of parallel activities: "
							+ numberofActivityParallelExecution + ", Parallelism for matching: "
							+ mainPool.getParallelism());
				}

				logger.putMessage(bigraphAnalyserInstanceName + "Identifying states...");

				// this method runs predicate in parallel if
				// isPredicateThreading is set to true
				// identifyRelevantStatesWithThreading();
				identifyRelevantStatesWithThreadingOrdered();
				// this method runs the predicates in series
				// identifyRelevantStatesWithThreadingArray();

				predicateExecutor.shutdown();
				mainPool.shutdown();

				if (!predicateExecutor.awaitTermination(maxWaitingTime, timeUnit)) {
					logger.putError(
							bigraphAnalyserInstanceName + "Time out! tasks took more than specified maximum time ["
									+ maxWaitingTime + " " + timeUnit + "]");
				}

				if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
					logger.putError(
							bigraphAnalyserInstanceName + "Time out! tasks took more than specified maximum time ["
									+ maxWaitingTime + " " + timeUnit + "]");
				}

			} else {
				logger.putMessage(bigraphAnalyserInstanceName + "Identifying states...");
				identifyRelevantStates();
			}

			// calculate time requried for finding matching states to the
			// activities conditions

			if (isTestingTime) {
				// averageTime /=numberOfConditions;
				long endTime = Calendar.getInstance().getTimeInMillis();

				long totalTime = endTime - startTime;

				int maxHours = (int) (totalTime / 3600000) % 60;
				int maxMins = (int) (totalTime / 60000) % 60;
				int maxSecs = (int) (totalTime / 1000) % 60;
				int maxMils = (int) totalTime % 1000;

				long avgTime = 0;

				if (numberOfConditions > 0) {
					avgTime = totalTime / numberOfConditions;
					;
				}

				int avgHours = (int) (avgTime / 3600000) % 60;
				int avgMins = (int) (avgTime / 60000) % 60;
				int avgSecs = (int) (avgTime / 1000) % 60;
				int avgMils = (int) avgTime % 1000;

				logger.putMessage(bigraphAnalyserInstanceName + "Total matching time of conditions = " + totalTime
						+ "ms [" + maxHours + "h:" + maxMins + "m:" + maxSecs + "s:" + maxMils + "ms]");
				logger.putMessage(bigraphAnalyserInstanceName + "Average matching time of conditions = " + avgTime
						+ "ms [" + avgHours + "h:" + avgMins + "m:" + avgSecs + "s:" + avgMils + "ms]");

			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			predicateExecutor.shutdownNow();
			mainPool.shutdownNow();
		} finally{
			if(predicateExecutor!=null) {
				predicateExecutor.shutdownNow();
			}
			
			if(mainPool!=null) {
				mainPool.shutdownNow();
			}
			
			if(matcher!=null){
				matcher = null;
			}
			
			Runtime.getRuntime().gc();
		}

		return predicateHandler;

	}

	protected synchronized void addStateWithNoEdges(Integer state) {

		statesWithNoEdges.add(state);
	}

	protected synchronized boolean hasStateWithNoEdges(Integer state) {

		return statesWithNoEdges.contains(state);
	}

	public PredicateHandler getPredicateHandler() {
		return predicateHandler;
	}

	public void setPredicateHandler(PredicateHandler predicateHandler) {

		this.predicateHandler = predicateHandler;
	}

	protected PredicateHandler identifyRelevantStates() {

		ArrayList<String> activitiesName = predicateHandler.getActivitNames();

		for (int i = 0; i < activitiesName.size(); i++) {

			identifyRelevantStates(activitiesName.get(i));

		}

		return predicateHandler;
	}

	protected PredicateHandler identifyRelevantStatesWithThreading() {

		ArrayList<String> activitiesName = predicateHandler.getActivitNames();

		try {

			int cnt = 0;
			for (int i = 0; i < activitiesName.size(); i++) {

				identifyRelevantStatesWithThreading(activitiesName.get(i));

				// used to determine when to stop parallelism of activities and
				// wait for current tasks to finish (determined by the number of
				// activities to parallel)
				cnt++;

				if ((cnt % numberofActivityParallelExecution == 0) || (i == activitiesName.size() - 1)) {
					for (int j = 0; j < predicateResults.size(); j++) {
						Future<Integer> future = predicateResults.get(j);
						if (future != null && !future.isDone()) {
							future.get();
						}

					}
					cnt = 0;
					predicateResults.clear();
				}

			}

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return predicateHandler;
	}

	protected PredicateHandler identifyRelevantStatesWithThreadingArray() {

		ArrayList<String> activitiesName = predicateHandler.getActivitNames();

		try {

			int cnt = 0;
			for (int i = 0; i < activitiesName.size(); i++) {

				identifyRelevantStatesWithThreadingArray(activitiesName.get(i));

				// used to determine when to stop parallelism of activities and
				// wait for current tasks to finish (determined by the number of
				// activities to parallel)
				cnt++;

				if ((cnt % numberofActivityParallelExecution == 0) || (i == activitiesName.size() - 1)) {
					for (int j = 0; j < predicateResults.size(); j++) {
						Future<Integer> future = predicateResults.get(j);
						if (future != null && !future.isDone()) {
							future.get();
						}

					}
					cnt = 0;
					predicateResults.clear();
				}

			}

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return predicateHandler;
	}

	/**
	 * Identifies the states of the system that matches the conditions (pre &
	 * post) of the given activity name
	 * 
	 * @param activityName
	 * @return
	 */
	protected void identifyRelevantStates(String activityName) {

		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

		// LinkedList<Future<Integer>> futurePreds = new
		// LinkedList<Future<Integer>>();
		for (int i = 0; i < preds.size(); i++) {

			logger.putMessage(bigraphAnalyserInstanceName + "Executing condition: " + preds.get(i).getName());

			identifyRelevantStates(preds.get(i));
		}

	}

	protected void identifyRelevantStatesWithThreading(String activityName) {

		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

		// LinkedList<Future<Integer>> futurePreds = new
		// LinkedList<Future<Integer>>();
		for (int i = 0; i < preds.size(); i++) {
			// futurePreds.clear();
			/*
			 * if(isLastActivity && i == preds.size()-1) { isLastCondition =
			 * true; }
			 */

			logger.putMessage(bigraphAnalyserInstanceName + "Executing condition: " + preds.get(i).getName());

			if (isPredicateThreading) {

				// parallelism
				predicateResults.add(predicateExecutor.submit(new PredicateMatcher(preds.get(i))));

			} else {

				// series
				// identifyRelevantStates(preds.get(i));
				try {
					predicateExecutor.submit(new PredicateMatcher(preds.get(i))).get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	protected void identifyRelevantStatesWithThreadingOrdered() {

		// ArrayList<String> activitiesNames =
		// predicateHandler.getActivitNames();
		// List<Activity> activites = new LinkedList<IncidentActivity>();
		// activitiesNames.add(predicateHandler.getInitialActivity().getName());
		Activity initialAct = predicateHandler.getInitialActivity();
		Activity currentAct = initialAct;
		ConditionPosition conPosition;

		try {

			int cnt = 0;

			while (currentAct != null) {

				if (currentAct == initialAct) {
					conPosition = ConditionPosition.FIRST_ACTIVITY;
				} else if (currentAct.getNextActivities() == null || currentAct.getNextActivities().isEmpty()) {
					conPosition = ConditionPosition.LAST_ACTIVITY;
				} else {
					conPosition = ConditionPosition.INTERMIDATE_ACTIVITY;
				}

				identifyRelevantStatesWithThreadingOrdered(currentAct.getName(), conPosition);

				// used to determine when to stop parallelism of activities and
				// wait for current tasks to finish (determined by the number of
				// activities to parallel)
				cnt++;

				if ((cnt % numberofActivityParallelExecution == 0)
						|| (currentAct.getNextActivities() == null || currentAct.getNextActivities().isEmpty())) {
					for (int j = 0; j < predicateResults.size(); j++) {
						Future<Integer> future = predicateResults.get(j);
						if (future != null && !future.isDone()) {
							future.get();
						}

					}
					cnt = 0;
					predicateResults.clear();
				}

				currentAct = (currentAct.getNextActivities() != null && !currentAct.getNextActivities().isEmpty())
						? currentAct.getNextActivities().get(0) : null;
			}

			// for (int i = 0; i < activitiesNames.size(); i++) {
			//
			// identifyRelevantStatesWithThreading(activitiesNames.get(i));
			//
			// // used to determine when to stop parallelism of activities and
			// // wait for current tasks to finish (determined by the number of
			// // activities to parallel)
			// cnt++;
			//
			// if ((cnt % numberofActivityParallelExecution == 0) || (i ==
			// activitiesNames.size() - 1)) {
			// for (int j = 0; j < predicateResults.size(); j++) {
			// Future<Integer> future = predicateResults.get(j);
			// if (future != null && !future.isDone()) {
			// future.get();
			// }
			//
			// }
			// cnt = 0;
			// predicateResults.clear();
			// }
			//
			// }

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void identifyRelevantStatesWithThreadingOrdered(String activityName, ConditionPosition conPosition) {

		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);
		ConditionPosition conditionPos = null;

		for (int i = 0; i < preds.size(); i++) {

			Predicate pred = preds.get(i);

			if (pred == null) {
				logger.putWarning(bigraphAnalyserInstanceName + "A condition of " + activityName + " is NULL");
				continue;
			}

			PredicateType type = pred.getPredicateType();

			if (conPosition.equals(ConditionPosition.INTERMIDATE_ACTIVITY)) {
				if (type.equals(PredicateType.Precondition)) {
					conditionPos = ConditionPosition.INTERMIDATE_PRECONDITION;
				} else if (type.equals(PredicateType.Postcondition))
					conditionPos = ConditionPosition.INTERMIDATE_POSTCONDITION;
			} else if (conPosition.equals(ConditionPosition.FIRST_ACTIVITY)) {
				if (type != null && type.equals(PredicateType.Precondition)) {
					conditionPos = ConditionPosition.FIRST_PRECONDITION;
				} else if (type != null && type.equals(PredicateType.Postcondition))
					conditionPos = ConditionPosition.FIRST_POSTCONDITION;
			} else if (conPosition.equals(ConditionPosition.LAST_ACTIVITY)) {
				if (type.equals(PredicateType.Precondition)) {
					conditionPos = ConditionPosition.LAST_PRECONDITION;
				} else if (type.equals(PredicateType.Postcondition))
					conditionPos = ConditionPosition.LAST_POSTCONDITION;
			} else {
				conditionPos = ConditionPosition.INTERMIDATE_ACTIVITY;
			}

			logger.putMessage(bigraphAnalyserInstanceName + "Executing condition: " + pred.getName());

			if (isPredicateThreading) {

				// parallelism
				predicateResults.add(predicateExecutor.submit(new PredicateMatcher(pred, conditionPos)));

			} else {

				// series
				// identifyRelevantStates(preds.get(i));
				try {
					predicateExecutor.submit(new PredicateMatcher(pred, conditionPos)).get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	protected void identifyRelevantStatesWithThreadingArray(String activityName) {

		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

		// identification is done in series
		predicateResults.add(predicateExecutor.submit(new PredicateMatcherArray(preds)));

	}

	/**
	 * Identifies the states of the system that matches the give condition
	 * 
	 * @param pred
	 *            the condition
	 * @return true if there is at least one state that matches it
	 */
	public boolean identifyRelevantStates(Predicate pred) {

		boolean areStatesIdentified = false;

		long startTime = 0;

		if (isTestingTime) {
			startTime = Calendar.getInstance().getTimeInMillis();
		}

		if (pred == null) {
			return false;
		}

		Bigraph redex = pred.getBigraphPredicate();

		if (redex == null) {
			return false;
		}

		// divide the states array for mult-threading
		LinkedList<Integer> statesResults;

		statesResults = new LinkedList<Integer>();

		for (int i = 0; i < states.size(); i++) {
			if (matcher.match(states.get(i), redex).iterator().hasNext()) {
				statesResults.add(i);
			}
		}

		// set bigraph states in the conditions
		pred.setBigraphStates(statesResults);

		logger.putMessage(bigraphAnalyserInstanceName + pred.getName() + "-states (" + pred.getBigraphStates().size()
				+ "): " + pred.getBigraphStates());

		if (isTestingTime) {
			long endTime = Calendar.getInstance().getTimeInMillis();

			long timePassed = startTime - endTime;

			int hours = (int) (timePassed / 3600000) % 60;
			int mins = (int) (timePassed / 60000) % 60;
			int secs = (int) (timePassed / 1000) % 60;
			int secMils = (int) timePassed % 1000;

			// execution time
			logger.putMessage(bigraphAnalyserInstanceName + "Condition [" + pred.getName() + "] matching time: "
					+ timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]");
			// averageTime += timePassed;
			numberOfConditions++;
		}

		return areStatesIdentified;

	}

	public PredicateHandler identifyStateTransitions() {

		for (String activityName : predicateHandler.getActivitNames()) {
			identifyStateTransitions(activityName);
		}

		return predicateHandler;
	}

	public void identifyStateTransitions(String activityName) {

		// identifyRelevantStates(activityName);
		ArrayList<Predicate> preconditions = predicateHandler.getPredicates(activityName, PredicateType.Precondition);
		ArrayList<Predicate> postconditions = predicateHandler.getPredicates(activityName, PredicateType.Postcondition);

		for (Predicate pre : preconditions) {
			pre.removeAllPaths();
			for (Predicate post : postconditions) { // this can be limited to
													// conditions that are
													// associated with each
													// other
				post.removeAllPaths();
				paths = systemHandler.getTransitionSystem().getPaths(pre, post);
				pre.addPaths(paths);
				post.addPaths(paths);
			}
		}

	}

	class BigraphMatcher extends RecursiveTask<LinkedList<Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		// private LinkedList<Bigraph> states;
		private Bigraph redex;
		private LinkedList<Integer> matchedStates;
		private ConditionPosition conditionPos;

		// for testing
		// protected int numOfParts = 0;

		public BigraphMatcher(int indexStart, int indexEnd, Bigraph redex) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.redex = redex;
			matchedStates = new LinkedList<Integer>();
		}

		public BigraphMatcher(int indexStart, int indexEnd, Bigraph redex, ConditionPosition conPos) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.redex = redex;
			matchedStates = new LinkedList<Integer>();
			conditionPos = conPos;
		}

		@Override
		protected LinkedList<Integer> compute() {
			// TODO Auto-generated method stub

			/** creates a number of partitions based on threshold size **/
			if ((indexEnd - indexStart) > (threshold + thresholdResidue)) {
				return ForkJoinTask.invokeAll(createPartitions()).stream()
						.map(new Function<BigraphMatcher, LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(BigraphMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.matchedStates;
							}

						}).reduce(matchedStates, new BinaryOperator<LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(LinkedList<Integer> arg0, LinkedList<Integer> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});
			}
			/** Divides the current task in half **/
			// if ((indexEnd - indexStart) > threshold) {
			// return ForkJoinTask.invokeAll(createSubTasks()).stream()
			// .map(new Function<BigraphMatcher, LinkedList<Integer>>() {
			//
			// @Override
			// public LinkedList<Integer> apply(BigraphMatcher arg0) {
			// // TODO Auto-generated method stub
			// return arg0.matchedStates;
			// }
			//
			// }).reduce(matchedStates, new
			// BinaryOperator<LinkedList<Integer>>() {
			//
			// @Override
			// public LinkedList<Integer> apply(LinkedList<Integer> arg0,
			// LinkedList<Integer> arg1) {
			// // TODO Auto-generated method stub
			// arg0.addAll(arg1);
			// return arg0;
			// }
			//
			// });
			//
			// }

			/** Do the matching for the given size [indexStart - indexEnd) **/
			else {
				Digraph<Integer> graph = transitionSystem != null ? transitionSystem.getDigraph() : null;

				for (int i = indexStart; i < indexEnd; i++) {

					// if the codnition being checked is not the last and the
					// current state has no edges, then skip it
					if (conditionPos != null && !conditionPos.equals(ConditionPosition.LAST_POSTCONDITION)) {
//						if(hasStateWithNoEdges(i)) {
//							continue;
//						} else 
							if(graph != null && graph.outDegree(i) == 0) {
//							addStateWithNoEdges(i);
							continue;
						}
						// logger.putMessage(bigraphAnalyserInstanceName +
						// "Skipping state-" + i);
//						continue;
					}
					Bigraph bigState = getBigraphState(i);
					if (matcher.match(bigState, redex).iterator().hasNext()) {
						matchedStates.add(i);
					}
				}
				return matchedStates;
			}

		}

		private Collection<BigraphMatcher> createSubTasks() {
			List<BigraphMatcher> dividedTasks = new LinkedList<BigraphMatcher>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new BigraphMatcher(indexStart, mid, redex, conditionPos));
			dividedTasks.add(new BigraphMatcher(mid, indexEnd, redex, conditionPos));

			return dividedTasks;
		}

		private Collection<BigraphMatcher> createPartitions() {
			List<BigraphMatcher> dividedTasks = new LinkedList<BigraphMatcher>();

			int index = indexStart;
			int localParts = (indexEnd - indexStart) / threshold;
			int i = 0;

			// create partitions
			for (i = 0; i < localParts - 1; i++) {
				dividedTasks.add(new BigraphMatcher(index, index + threshold, redex, conditionPos));
				// logger.putMessage("Thread["+threadID+"]>> part ["+i+"] = [" +
				// index+"->"+(index+threshold)+")");
				index = index + threshold;

			}

			// last partition
			dividedTasks.add(new BigraphMatcher(index, indexEnd, redex, conditionPos));
			// logger.putMessage("Thread["+threadID+"]>> part ["+i+"] = [" +
			// index+"->"+indexEnd+")");
			// logger.putMessage("Thread["+threadID+"]>>Number of partitions = "
			// + dividedTasks.size());

			return dividedTasks;
		}
	}

	class PredicateMatcher implements Callable<Integer> {

		private Predicate pred;
		private Matcher matcher;
		private ConditionPosition conPosition;

		public PredicateMatcher(Predicate pred, ConditionPosition conPosition) {
			this.pred = pred;
			matcher = new Matcher();
			this.conPosition = conPosition;
		}

		public PredicateMatcher(Predicate pred) {
			this(pred, ConditionPosition.INTERMIDATE_ACTIVITY);
		}

		@Override
		public Integer call() throws Exception {

			long startTime = 0;
			if (isTestingTime) {
				startTime = Calendar.getInstance().getTimeInMillis();
			}

			if (pred == null) {
				return -1;
			}

			Bigraph redex = pred.getBigraphPredicate();

			if (redex == null) {
				return -1;
			}

			LinkedList<Integer> statesResults;

			// divide the states array for mult-threading
			if (isThreading) {

				BigraphMatcher bigraphMatcher = new BigraphMatcher(0, states.size(), redex, conPosition);
				statesResults = mainPool.invoke(bigraphMatcher);

			}
			// this else can be removed as the fork class can decide to create
			// only 1 task if size is less than threshold
			else {
				statesResults = new LinkedList<Integer>();
				for (int i = 0; i < states.size(); i++) {
					if (matcher.match(states.get(i), redex).iterator().hasNext()) {
						statesResults.add(i);
					}
				}
			}

			// set result
			pred.setBigraphStates(statesResults);

			logger.putMessage(
					bigraphAnalyserInstanceName + pred.getName() + "-states (" + pred.getBigraphStates().size() + ")");

			if (isTestingTime) {
				long endTime = Calendar.getInstance().getTimeInMillis();

				long timePassed = endTime - startTime;

				int hours = (int) (timePassed / 3600000) % 60;
				int mins = (int) (timePassed / 60000) % 60;
				int secs = (int) (timePassed / 1000) % 60;
				int secMils = (int) timePassed % 1000;

				// execution time
				logger.putMessage(bigraphAnalyserInstanceName + "Condition [" + pred.getName() + "] matching time: "
						+ timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]");
				// averageTime += timePassed;
				numberOfConditions++;

			}

			return 1;
		}

	}

	enum ConditionPosition {
		FIRST_ACTIVITY, LAST_ACTIVITY, INTERMIDATE_ACTIVITY, FIRST_PRECONDITION, LAST_PRECONDITION, FIRST_POSTCONDITION, LAST_POSTCONDITION, INTERMIDATE_PRECONDITION, INTERMIDATE_POSTCONDITION;
	}

	/**
	 * TESTING
	 */
	class BigraphMatcherArray extends RecursiveTask<Map<Integer, LinkedList<Integer>>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		// private LinkedList<Bigraph> states;
		private List<Bigraph> redexes;

		// the key is an integer that is the index of the bigraph in the given
		// list of bigraph (i.e. redexes)
		private Map<Integer, LinkedList<Integer>> matchedStates;

		// for testing
		// protected int numOfParts = 0;

		public BigraphMatcherArray(int indexStart, int indexEnd, List<Bigraph> redex) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.redexes = redex;
			matchedStates = new HashMap<Integer, LinkedList<Integer>>();

			for (int i = 0; i < redex.size(); i++) {
				matchedStates.put(i, new LinkedList<Integer>());
			}
		}

		@Override
		protected Map<Integer, LinkedList<Integer>> compute() {
			// TODO Auto-generated method stub

			if ((indexEnd - indexStart) > threshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<BigraphMatcherArray, Map<Integer, LinkedList<Integer>>>() {

							@Override
							public Map<Integer, LinkedList<Integer>> apply(BigraphMatcherArray arg0) {
								// TODO Auto-generated method stub
								return arg0.matchedStates;
							}

						}).reduce(matchedStates, new BinaryOperator<Map<Integer, LinkedList<Integer>>>() {

							@Override
							public Map<Integer, LinkedList<Integer>> apply(Map<Integer, LinkedList<Integer>> arg0,
									Map<Integer, LinkedList<Integer>> arg1) {
								// TODO Auto-generated method stub
								for (Entry<Integer, LinkedList<Integer>> entry : arg1.entrySet()) {
									if (arg0.containsKey(entry.getKey())) {
										arg0.get(entry.getKey()).addAll(entry.getValue());
									} else {
										arg0.put(entry.getKey(), entry.getValue());
									}
								}
								// arg0.putAll(arg1);
								return arg0;
							}

						});

			} else {
				for (int i = indexStart; i < indexEnd; i++) {
					for (int j = 0; j < redexes.size(); j++) {
						if (matcher.match(getBigraphState(i), redexes.get(j)).iterator().hasNext()) {
							// if (matchedStates.containsKey(j)) {
							// matchedStates.get(j).add(i);
							// } else {
							// LinkedList<Integer> newList = new
							// LinkedList<Integer>();
							//
							// newList.add(i);
							// matchedStates.put(j, newList);
							// }
							matchedStates.get(j).add(i);
						}
					}

				}

				return matchedStates;
			}

		}

		private Collection<BigraphMatcherArray> createSubTasks() {
			List<BigraphMatcherArray> dividedTasks = new LinkedList<BigraphMatcherArray>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new BigraphMatcherArray(indexStart, mid, redexes));
			dividedTasks.add(new BigraphMatcherArray(mid, indexEnd, redexes));

			return dividedTasks;
		}
	}

	class PredicateMatcherArray implements Callable<Integer> {

		private List<Predicate> predicates;

		public PredicateMatcherArray(List<Predicate> preds) {
			predicates = preds;
			matcher = new Matcher();
		}

		@Override
		public Integer call() throws Exception {

			long startTime = 0;

			if (isTestingTime) {
				startTime = Calendar.getInstance().getTimeInMillis();
			}

			LinkedList<Bigraph> bigraphs = new LinkedList<Bigraph>();

			for (Predicate pred : predicates) {
				Bigraph redex = pred.getBigraphPredicate();

				if (redex == null) {
					logger.putError(
							bigraphAnalyserInstanceName + "Redex for condition [" + pred.getName() + "] is NULL");
					continue;
				}

				bigraphs.add(redex);
			}

			Map<Integer, LinkedList<Integer>> statesResults;

			BigraphMatcherArray bigraphMatcherArray = new BigraphMatcherArray(0, states.size(), bigraphs);
			statesResults = mainPool.invoke(bigraphMatcherArray);

			StringBuilder str = new StringBuilder();

			// set result
			for (int i = 0; i < predicates.size(); i++) {

				Predicate pred = predicates.get(i);

				pred.setBigraphStates(statesResults.get(i));

				logger.putMessage(bigraphAnalyserInstanceName + pred.getName() + "-states: " + pred.getBigraphStates());

				str.append(pred.getName()).append(", ");
			}

			if (isTestingTime) {
				long endTime = Calendar.getInstance().getTimeInMillis();

				long timePassed = endTime - startTime;

				int hours = (int) (timePassed / 3600000) % 60;
				int mins = (int) (timePassed / 60000) % 60;
				int secs = (int) (timePassed / 1000) % 60;
				int secMils = (int) timePassed % 1000;

				// execution time
				logger.putMessage(bigraphAnalyserInstanceName + "Conditions [" + str.toString() + "] matching time: "
						+ timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]");

				numberOfConditions++;

			}

			return 1;
		}

	}
}
