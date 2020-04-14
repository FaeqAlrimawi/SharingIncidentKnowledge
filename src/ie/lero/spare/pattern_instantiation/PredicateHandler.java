package ie.lero.spare.pattern_instantiation;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import javax.xml.xquery.XQException;

import org.eclipse.emf.common.util.EList;

import cyberPhysical_Incident.Activity;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator.PotentialIncidentInstance;
import ie.lero.spare.utility.Digraph;
import ie.lero.spare.utility.FileManipulator;
import ie.lero.spare.utility.Logger;
import ie.lero.spare.utility.PredicateType;
import ie.lero.spare.utility.TransitionSystem;
import ie.lero.spare.utility.XqueryExecuter;

public class PredicateHandler {

	private HashMap<String, Predicate> predicates;
	private HashMap<String, Activity> incidentActivities;
	private Digraph<String> activitiesGraph;
	private LinkedList<LinkedList<String>> activitySequences;
	private Logger logger;
	// generated transitions
	List<GraphPath> transitions;

	// used for transition generation by the recursive task
	private List<Integer> preconditionStates;
	private List<Integer> postconditionStates;
	private ForkJoinPool mainPool;

	private SystemInstanceHandler systemHandler;
	private TransitionSystem transitionSystem;
	private String incidentDocument;

	// private List<Predicate> bottelNecksStatesInOrder = new
	// LinkedList<Predicate>();
	// private int bottleNeckNumber = 1;
	private String instanceName;
	public static final String INSTANCE_NAME_GLOBAL = "Predicate-Handler";

	//used to hold states that have intra transitions between each other in the precondition of the first activity
	//key is a state, and value is the list of states, which the key has transitions to (out degrees i.e. outgoing edges)
	private Map<Integer, List<Integer>> preconditionStatesWithTransitions = new HashMap<Integer, List<Integer>>();

	//used to hold states that have intra transitions between each other in the precondition of the first activity
	//key is a state, and value is the list of states that they have transitions to the key (in degrees i.e. incoming edges)
	private Map<Integer, List<Integer>> postconditionStatesInBoundTransitions = new HashMap<Integer, List<Integer>>();

	private List<Predicate> orderedConditions;

	// minimum number of actions/edges in a transition = # of activities in a
	// pattern
	private int minimumNumberOfActions;

	// sets the number of levels that the analysis for precondition intra
	// transition identification should happne
	// it could be adapted based on the max level avaialble in the transition
	// digraph
	private int maxLevel = 3;

	public PredicateHandler() {
		predicates = new HashMap<String, Predicate>();
		incidentActivities = new HashMap<String, Activity>();
		activitySequences = new LinkedList<LinkedList<String>>();
		logger = null;
		systemHandler = SystemsHandler.getCurrentSystemHandler();
		transitionSystem = systemHandler != null ? systemHandler.getTransitionSystem() : null;

	}

	public PredicateHandler(Logger logger) {
		this();
		this.logger = logger;

	}

	public PredicateHandler(Logger logger, SystemInstanceHandler sysHandler, String incidentDoc) {
		this();
		this.logger = logger;
		systemHandler = sysHandler;
		transitionSystem = systemHandler != null ? systemHandler.getTransitionSystem() : null;
		incidentDocument = incidentDoc;
	}

	public PredicateHandler(SystemInstanceHandler sysHandler) {
		this();
		systemHandler = sysHandler;
		transitionSystem = systemHandler.getTransitionSystem();
	}

	public void setLogger(Logger logger) {

		this.logger = logger;
	}

	public HashMap<String, Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(HashMap<String, Predicate> predicates) {
		this.predicates = predicates;
	}

	public boolean addPredicate(String predName, Predicate pred) {
		boolean isAdded = false;

		if (pred != null) {
			predicates.put(predName, pred);
			isAdded = true;
		}

		return isAdded;
	}

	public boolean addPredicate(Predicate pred) {

		if (pred != null) {
			predicates.put(pred.getName(), pred);
			return true;
		}

		return false;
	}

	public String toString() {
		StringBuilder res = new StringBuilder();

		if (predicates != null && !predicates.isEmpty())
			for (Predicate p : predicates.values()) {
				res.append(p.toString());
			}

		return res.toString();
	}

//	public boolean validatePredicates() {
//		boolean isValid = true;
//		// to be done...how to validate them against bigrapher file (e.g.,
//		// controls available, connections)
//
//		return isValid;
//	}

	public String insertPredicatesIntoBigraphFile(String fileName) {

		BufferedWriter writer = null;
		ArrayList<String> list = new ArrayList<String>();
		String preds = "";
		String predsBorders = "\r\n#########################Generated Predicates##############################\r\n";
		int index = -1;
		int indexRules = -1;
		boolean isPredDefined = false;
		// check file name to contain a .big file
		String outputFile = "";
		StringBuilder existingPreds = new StringBuilder();
		int indexPred = -1;
		int indexPredEnd = -1;

		// get the big pred_name = predicate statements
		preds = convertToBigraphPredicateStatement();

		try {

			String[] lines = FileManipulator.readFileNewLine(fileName);

			for (String s : lines) {
				list.add(s);
			}

			// determine the last time the keyword ctrl is used as predicates
			// cannot be defined before ctrl
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).startsWith("ctrl") || list.get(i).startsWith("atomic ctrl")
						|| list.get(i).startsWith("fun ctrl") || list.get(i).startsWith("atomic fun ctrl")) {
					index = i;
				} else if (list.get(i).startsWith("preds")) {
					isPredDefined = true;
					indexPred = i;
					existingPreds.append(list.get(i));
					if (list.get(i).contains(";")) { // there are existing
														// predicates and all on
														// the same line
						indexPredEnd = i;
					} else { // predefined predicates take more than one line
						for (int j = i + 1; j < list.size(); j++) {
							existingPreds.append(list.get(j));
							if (list.get(j).contains(";")) {
								indexPredEnd = j;
								break;
							}
						}
					}
				} else if (list.get(i).startsWith("rules")) {
					indexRules = i;
				}
			}

			// insertion of preds names before insertion of predicate statements
			// as preds names come after them
			if (isPredDefined) {
				list.add(indexPred, "\r\n####updated predicates#######\r\n"
						+ getBigraphPredicateNames(existingPreds.toString()) + "\r\n###########################");
				for (int i = indexPredEnd + 1; i >= indexPred + 1; i--) {
					list.remove(i);
				}

			} else // insert pred names after 'begin brs' section (i.e bigraph
					// definition section) and before 'end' if preds are not
					// already defined
			{
				for (int i = indexRules; i < list.size(); i++) {
					if (list.get(i).contains(";")) {
						list.add(i + 1, "\r\n####updated predicates#######\r\n" + getBigraphPredicateNames("")
								+ "\r\n###########################");
						break;
					}
				}

			}

			// check that the last ctrl statement has semicolon in the same
			// line, then insert predicates
			if (list.get(index).contains(";")) {
				list.add(index + 1, predsBorders + preds + predsBorders);
			} else {
				for (int i = index + 1; i < list.size(); i++) {
					if (list.get(i).contains(";")) {
						list.add(i + 1, predsBorders + preds + predsBorders);
						break;
					}
				}
			}

			outputFile = fileName.split("\\.")[0] + "_generated.big";
			writer = new BufferedWriter(new FileWriter(outputFile));
			for (int i = 0; i < list.size(); i++)
				writer.write(list.get(i).toString() + "\r\n");

			writer.close();

			return outputFile;
		} catch (Exception e) {
			e.printStackTrace();
			outputFile = "";
		}

		return outputFile;

	}

	public String convertToBigraphPredicateStatement() {
		StringBuilder res = new StringBuilder();

		for (Predicate pred : predicates.values()) {
			res.append(pred.getBigraphPredicateStatement());
		}
		return res.toString();
	}

	private String getBigraphPredicateNames(String existingPreds) {
		StringBuilder res = new StringBuilder();

		res.append("preds = {");
		// append existing predicates in the file
		if (!existingPreds.equals("")) {
			res.append(existingPreds.substring(existingPreds.indexOf("{") + 1, existingPreds.lastIndexOf("}")))
					.append(", ");
		}
		for (Predicate pred : predicates.values()) {
			res.append(pred.getBigraphPredicateName()).append(", ");
		}

		res.deleteCharAt(res.lastIndexOf(","));
		res.append("};");

		return res.toString();
	}

	public ArrayList<Predicate> getActivityPredicates(String activityName) {
		// ArrayList<Predicate> result = new ArrayList<Predicate>();

		/*
		 * for (Predicate p : predicates) { if
		 * (activityName.contentEquals(p.getActivityName())) { result.add(p); }
		 * }
		 */

		return ((IncidentActivity) incidentActivities.get(activityName)).getPredicates();
	}

	/*
	 * public String insertPredicatesIntoBigraphFile2(String fileName) { String
	 * outputFileName = "";
	 * 
	 * return outputFileName; }
	 */

	public ArrayList<Predicate> getPredicates(String activityName, PredicateType type) {
		ArrayList<Predicate> preds = new ArrayList<Predicate>();

		for (Predicate pred : getActivityPredicates(activityName)) {
			if (pred.getPredicateType() == type) {
				preds.add(pred);
			}
		}
		return preds;
	}

	public ArrayList<String> getActivitNames() {
		ArrayList<String> names = new ArrayList<String>();

		for (String nm : incidentActivities.keySet()) {
			names.add(nm);
		}

		return names;
	}

	public HashMap<String, Activity> getIncidentActivities() {
		return incidentActivities;
	}

	public void setIncidentActivities(HashMap<String, Activity> incidentActivities) {
		this.incidentActivities = incidentActivities;
	}

	public void updateNextPreviousActivitiesUsingXquery() {
		String[] tmp;
		String[] result;
		IncidentActivity act;

		try {
			result = XqueryExecuter.returnNextPreviousActivities(incidentDocument);

			for (String res : result) {
				tmp = res.split("##|!!");
				act = (IncidentActivity) incidentActivities.get(tmp[0]);
				/*
				 * act.setNextActivities(new ArrayList<IncidentActivity>());
				 * act.setPreviousActivities(new ArrayList<IncidentActivity>());
				 */
				if (tmp.length > 1 && tmp[1] != null) {
					for (String nxt : tmp[1].split(" ")) {
						if (nxt != null && !nxt.contentEquals("") && !nxt.contains(" ")) {
							act.addNextActivity(incidentActivities.get(nxt));
						}

					}
				}
				if (tmp.length > 2 && tmp[2] != null) {
					for (String pre : tmp[2].split(" ")) {
						if (pre != null && !pre.contentEquals("") && !pre.contains(" ")) {
							act.addPreviousActivity(incidentActivities.get(pre));
						}

					}
				}
			}

		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateNextPreviousActivities() {

		List<IncidentActivity> nxtActs = new LinkedList<IncidentActivity>();
		List<IncidentActivity> preActs = new LinkedList<IncidentActivity>();

		for (Activity act : incidentActivities.values()) {

			// update next activities
			for (Activity nxtAct : act.getNextActivities()) {
				nxtActs.add((IncidentActivity) incidentActivities.get(nxtAct.getName()));
			}

			act.getNextActivities().clear();
			act.getNextActivities().addAll(nxtActs);
			nxtActs.clear();

			// update previous activities
			for (Activity preAct : act.getPreviousActivities()) {
				preActs.add((IncidentActivity) incidentActivities.get(preAct.getName()));
			}

			act.getPreviousActivities().clear();
			act.getPreviousActivities().addAll(preActs);
			preActs.clear();

		}
	}

	public void addActivityPredicate(String activityName, Predicate pred) {
		((IncidentActivity) incidentActivities.get(activityName)).addPredicate(pred);
		addPredicate(pred);
	}

	public void addIncidentActivity(Activity activity) {

		// convert Activity object to IncidentActivity object

		incidentActivities.put(activity.getName(), activity);
	}

	/**
	 * Returns the first activity in the incident. This activity is the one
	 * without any previous activities
	 * 
	 * @return IncidentActivity object representing the initial activity, null
	 *         if there is not one
	 */
	public Activity getInitialActivity() {

		for (Activity act : incidentActivities.values()) {
			if (act.getPreviousActivities() == null || act.getPreviousActivities().isEmpty()) {
				return act;
			}
		}

		return null;
	}

	public Activity getFinalActivity() {

		for (Activity act : incidentActivities.values()) {
			if (act.getNextActivities() == null || act.getNextActivities().isEmpty()) {
				return act;
			}
		}

		return null;
	}

	public LinkedList<GraphPath> getPathsBetweenActivities(IncidentActivity sourceActivity,
			IncidentActivity destinationActivity) {

		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();

		ArrayList<Predicate> preconditions = getPredicates(sourceActivity.getName(), PredicateType.Precondition);
		ArrayList<Predicate> postconditions = getPredicates(destinationActivity.getName(), PredicateType.Postcondition);

		for (Predicate pre : preconditions) {
			pre.removeAllPaths();
			for (Predicate post : postconditions) { // this can be limited to
													// conditions that are
													// associated with each
													// other
				post.removeAllPaths();
				paths = transitionSystem.getPaths(pre, post);
				pre.addPaths(paths);
				post.addPaths(paths);
			}
		}

		LinkedList<Activity> activities = getMiddleActivities(sourceActivity, destinationActivity);

		// add the first and the last activities
		activities.addFirst(sourceActivity);
		activities.addLast(destinationActivity);

		boolean isCheckingPrecondition = true;
		// boolean isCheckingPostcondition = false;

		// check if each path contains at least one of the satisfied states for
		// each activity
		// can be parallelised
		ListIterator<GraphPath> pathsIterator = paths.listIterator();
		ListIterator<Activity> activitiesIterator = activities.listIterator();

		if (activities != null) {
			while (pathsIterator.hasNext()) {

				GraphPath path = pathsIterator.next();
				LinkedList<Integer> states = path.getStateTransitions();

				int j = 0;// first state is for the src and des activities
				// isSatisfied = true;
				isCheckingPrecondition = true;
				// isCheckingPostcondition = false;
				activitiesIterator = activities.listIterator();
				outerLoop: while (activitiesIterator.hasNext()) {
					IncidentActivity activity = (IncidentActivity) activitiesIterator.next();
					// get precondition of the activity (assumption: there is
					// only one precondition)
					Predicate pre = activity.getPredicates(PredicateType.Precondition).get(0);
					LinkedList<Integer> preStates = pre.getBigraphStates();

					// get precondition of the activity (assumption: there is
					// only one precondition)
					Predicate post = activity.getPredicates(PredicateType.Postcondition).get(0);
					LinkedList<Integer> postStates = post.getBigraphStates();

					// assumption: each predicate should satisfy different state
					// in the transition
					for (; j < states.size(); j++) { // last state is for the
														// src and des
														// activities
						int state = states.get(j);

						// if it is the last element and either it is still
						// checking the precondition or the postcondition does
						// not contain the state then remove the path and break
						// to outerloop
						if (j == states.size() - 1 && (activitiesIterator.hasNext() || // if
																						// there
																						// are
																						// still
																						// activities
																						// to
																						// iterate
																						// over
								isCheckingPrecondition || // or if it is the
															// last activity but
															// it is still
															// checking
															// precondition
								!postStates.contains(state))) { // or if it is
																// last activity
																// and and the
																// postcondition
																// does not have
																// the state as
																// one of its
																// own
							pathsIterator.remove();
							break outerLoop;
						}

						// find a match for the precondition
						if (isCheckingPrecondition) {
							if (!preStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = false;
							}

							// find a match for the postcondition
						} else {
							if (!postStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = true;
								break;
							}
						}
					}
				}
			}
		}

		return paths;
	}

	/**
	 * Returns state transitions between the first and last activities, which
	 * pass through all the other activities between the first and the last
	 * 
	 * @return
	 */
	public LinkedList<GraphPath> getPaths() {

		logger.putMessage("PredicateHandler>>Generating transitions...");
		IncidentActivity sourceActivity = (IncidentActivity) getInitialActivity();
		IncidentActivity destinationActivity = (IncidentActivity) getFinalActivity();

		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();

		ArrayList<Predicate> preconditions = getPredicates(sourceActivity.getName(), PredicateType.Precondition);
		ArrayList<Predicate> postconditions = getPredicates(destinationActivity.getName(), PredicateType.Postcondition);

		for (Predicate pre : preconditions) {
			pre.removeAllPaths();
			for (Predicate post : postconditions) { // this can be limited to
													// conditions that are
													// associated with each
													// other
				post.removeAllPaths();
				paths = transitionSystem.getPaths(pre, post);
				pre.addPaths(paths);
				post.addPaths(paths);
			}
		}

		logger.putMessage("PredicateHandler>>Analysing generated transitions [" + paths.size() + "]...");

		LinkedList<Activity> activities = getMiddleActivities(sourceActivity, destinationActivity);

		// add the first and the last activities
		activities.addFirst(sourceActivity);
		activities.addLast(destinationActivity);

		boolean isCheckingPrecondition = true;
		// boolean isCheckingPostcondition = false;

		// check if each path contains at least one of the satisfied states for
		// each activity
		// can be parallelised
		ListIterator<GraphPath> pathsIterator = paths.listIterator();
		ListIterator<Activity> activitiesIterator = activities.listIterator();

		if (activities != null) {
			while (pathsIterator.hasNext()) {

				GraphPath path = pathsIterator.next();
				LinkedList<Integer> states = path.getStateTransitions();

				int j = 0;

				isCheckingPrecondition = true;

				activitiesIterator = activities.listIterator();

				outerLoop: while (activitiesIterator.hasNext()) {
					IncidentActivity activity = (IncidentActivity) activitiesIterator.next();

					// get precondition of the activity (assumption: there is
					// only one precondition)
					Predicate pre = activity.getPredicates(PredicateType.Precondition).get(0);
					LinkedList<Integer> preStates = pre.getBigraphStates();

					// get precondition of the activity (assumption: there is
					// only one postcondition)
					Predicate post = activity.getPredicates(PredicateType.Postcondition).get(0);
					LinkedList<Integer> postStates = post.getBigraphStates();

					// assumption: each predicate should satisfy different state
					// in the transition
					for (; j < states.size(); j++) { // last state is for the
														// src and des
														// activities
						int state = states.get(j);

						// if it is the last element and either it is still
						// checking the precondition or the postcondition does
						// not contain the state then remove the path and break
						// to outerloop
						if (j == states.size() - 1 && (activitiesIterator.hasNext() || // if
																						// there
																						// are
																						// still
																						// activities
																						// to
																						// iterate
																						// over
								isCheckingPrecondition || // or if it is the
															// last activity but
															// it is still
															// checking
															// precondition
								!postStates.contains(state))) { // or if it is
																// last activity
																// and and the
																// postcondition
																// does not have
																// the state as
																// one of its
																// own
							pathsIterator.remove();
							break outerLoop;
						}

						// find a match for the precondition
						if (isCheckingPrecondition) {
							if (!preStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = false;
							}

							// find a match for the postcondition
						} else {
							if (!postStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = true;
								break;
							}
						}
					}
				}
			}
		}

		logger.putMessage("PredicateHandler>>Analysis is completed... resulted paths number [" + paths.size() + "]");

		return paths;
	}

	// not correct
	/*
	 * public LinkedList<GraphPath>
	 * getPathsBetweenActivitiesOriginal(IncidentActivity sourceActivity,
	 * IncidentActivity destinationActivity) {
	 * 
	 * //not done //
	 * 
	 * LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
	 * 
	 * ArrayList<Predicate> preconditions =
	 * getPredicates(sourceActivity.getName(), PredicateType.Precondition);
	 * ArrayList<Predicate> postconditions =
	 * getPredicates(destinationActivity.getName(),
	 * PredicateType.Postcondition);
	 * 
	 * for (Predicate pre : preconditions) { pre.removeAllPaths(); for
	 * (Predicate post : postconditions) { // this can be limited to //
	 * conditions that are // associated with each // other
	 * post.removeAllPaths(); paths =
	 * SystemInstanceHandler.getTransitionSystem().getPaths(pre, post);
	 * pre.addPaths(paths); post.addPaths(paths); } }
	 * LinkedList<IncidentActivity> middleActivities =
	 * getMiddleActivities(sourceActivity, destinationActivity);
	 * 
	 * LinkedList<Integer> indices = new LinkedList<Integer>(); GraphPath tmp;
	 * 
	 * //check if each path contains at least one of the satisfied states for
	 * each activity for(int i=0;i<paths.size();i++) { if(middleActivities !=
	 * null) { for(IncidentActivity activity: middleActivities) { tmp =
	 * paths.get(i); if (!tmp.satisfiesActivity(activity)) {
	 * //System.out.println("remove path " + tmp.toSimpleString());
	 * indices.add(i); } } } }
	 * 
	 * //if there are paths that do not go through all activities then remove
	 * them for(int i=0;i<indices.size();i++) {
	 * paths.remove((int)(indices.get(i))); //this is needed since removing an
	 * element from the list will shift the indices for(int
	 * j=i+1;j<indices.size();j++) { int v = indices.get(j)-1; indices.set(j,
	 * v); } }
	 * 
	 * return paths; }
	 */

	public LinkedList<Activity> getMiddleActivities(IncidentActivity sourceActivity,
			IncidentActivity destinationActivity) {
		LinkedList<Activity> result = new LinkedList<Activity>();

		if (sourceActivity.equals(destinationActivity)
				|| sourceActivity.getNextActivities().contains(destinationActivity)) {
			return result;
		}

		activitySequences.clear();
		LinkedList<String> visited = new LinkedList<String>();
		visited.add(sourceActivity.getNextActivities().get(0).getName());

		depthFirst(destinationActivity.getName(), visited);

		if (activitySequences.size() > 0) {
			LinkedList<String> activityNames = activitySequences.get(0);
			HashMap<String, Activity> acts = getIncidentActivities();

			for (String name : activityNames) {

				result.add(acts.get(name));
			}
		}

		return result;
	}
	/*
	 * public LinkedList<HashMap<String, LinkedList<GraphPath>>>
	 * getPathsForIncident() {
	 * 
	 * LinkedList<IncidentActivity> activities = new
	 * LinkedList<IncidentActivity>(); LinkedList<IncidentActivity>
	 * visitedActivities = new LinkedList<IncidentActivity>(); IncidentActivity
	 * tmp; LinkedList<HashMap<String, LinkedList<GraphPath>>> paths = new
	 * LinkedList<HashMap<String, LinkedList<GraphPath>>>();
	 * 
	 * IncidentActivity initialActivity = getInitialActivity();
	 * activities.add(initialActivity);
	 * 
	 * while (!activities.isEmpty()) { tmp = activities.pop();
	 * 
	 * if (tmp != null) {
	 * 
	 * // check if visited before if (visitedActivities.contains(tmp)) {
	 * continue; } paths.add(tmp.getIntraInterPaths()); for (IncidentActivity
	 * act : tmp.getNextActivities()) { activities.add(act); }
	 * 
	 * visitedActivities.add(tmp); }
	 * 
	 * }
	 * 
	 * return paths; }
	 */

	/*
	 * public void findAllPossiblePaths() {
	 * 
	 * LinkedList<HashMap<String, LinkedList<GraphPath>>> paths =
	 * getPathsForIncident(); GraphPath p = new GraphPath(); GraphPath p2 = new
	 * GraphPath(); HashMap<String, LinkedList<GraphPath>> tmpHash;
	 * 
	 * Random rand = new Random(); int tries = 0; boolean isPathSelected =
	 * false;
	 * 
	 * //get random initial path
	 * 
	 * } LinkedList<GraphPath> tmpPath =
	 * paths.get(0).get(getInitialActivity().getNextActivities()
	 * .get(rand.nextInt(getInitialActivity().getNextActivities().size())).
	 * getName());
	 * 
	 * p2 = tmpPath.get(rand.nextInt(tmpPath.size()));
	 * 
	 * System.out.println(p2); for(int i =1; i<paths.size();i++) {
	 * for(LinkedList<GraphPath> pa : paths.get(i).values()) {
	 * while(!isPathSelected && tries <10000) { p =
	 * pa.get(rand.nextInt(pa.size())); if(p2.getStateTransitions().size() > 0
	 * && p2.getStateTransitions().getLast().compareTo(p.getStateTransitions().
	 * getFirst()) == 0){ p2 = p2.combine(p);
	 * System.out.println("path selected: "+p);
	 * System.out.println(p2.toSimpleString()); isPathSelected = true; } else if
	 * (p2.getStateTransitions().size() == 0){ p2 = p2.combine(p); } else {
	 * tries++; } } isPathSelected = false; tries = 0; break; } }
	 * 
	 * System.out.println(p2.toSimpleString()); }
	 */

	public Digraph<String> createActivitiesDigraph() {
		activitiesGraph = new Digraph<String>();

		LinkedList<Activity> acts = new LinkedList<Activity>();
		LinkedList<Activity> actsVisited = new LinkedList<Activity>();
		Activity tmp;

		// assuming there is only one initial activity. can be extended to
		// multi-initials
		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			for (Activity act : tmp.getNextActivities()) {
				IncidentActivity incAct = (IncidentActivity) act;
				activitiesGraph.add(tmp.getName(), incAct.getName(), -1);
				if (!acts.contains(incAct.getName())) {
					acts.add(incAct);
				}
			}

			actsVisited.add(tmp);
		}

		return activitiesGraph;
	}

	private void depthFirst(String endActivity, LinkedList<String> visited) {
		List<String> nodes = activitiesGraph.outboundNeighbors(visited.getLast());

		// examine adjacent nodes
		for (String node : nodes) {
			if (visited.contains(node)) {
				continue;
			}
			if (node.equals(endActivity)) {
				// visited.add(node);
				// addTransitiontoList(visited);
				LinkedList<String> newList = new LinkedList<String>();
				newList.addAll(visited);
				activitySequences.add(newList);
				visited.removeLast();
				break;
			}
		}
		for (String node : nodes) {
			if (visited.contains(node) || node.equals(endActivity)) {
				continue;
			}
			visited.addLast(node);
			depthFirst(endActivity, visited);
			visited.removeLast();
		}
	}

	/*
	 * private void addTransitiontoList(List<String> transition, LinkedList<>) {
	 * LinkedList<String> newList = new LinkedList<String>(); GraphPath path =
	 * new GraphPath();
	 * 
	 * newList.addAll(transition); activitySequences.add(path);
	 * 
	 * }
	 */

	public LinkedList<LinkedList<String>> getActivitiesSequences() {
		return getActivitiesSequences(getInitialActivity().getName(), getFinalActivity().getName());
	}

	public LinkedList<LinkedList<String>> getActivitiesSequences(String initialActivity, String finalActivity) {
		LinkedList<String> visited = new LinkedList<String>();
		visited.add(initialActivity);

		if (activitySequences == null || activitySequences.size() == 0) {
			depthFirst(finalActivity, visited);
		}

		return activitySequences;
	}

	public boolean areAllSatisfied() {

		IncidentActivity act = null;

		for (Activity activity : incidentActivities.values()) {
			act = (IncidentActivity) activity;
			if (!act.isActivitySatisfied()) {
				return false;
			}
		}

		return true;
	}

	public LinkedList<String> getActivitiesNotSatisfied() {

		LinkedList<String> names = new LinkedList<String>();
		IncidentActivity act = null;

		for (Activity activity : incidentActivities.values()) {
			act = (IncidentActivity) activity;
			if (!act.isActivitySatisfied()) {
				names.add(act.getName());
			}
		}

		return names;
	}

	public void printAll() {
		LinkedList<Activity> acts = new LinkedList<Activity>();
		LinkedList<Activity> actsVisited = new LinkedList<Activity>();
		IncidentActivity tmp;

		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = (IncidentActivity) acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			System.out.println("Activity name: " + tmp.getName());
			System.out.println("**Paths from preconditions to postconditions within the activity");
			/*
			 * for(Predicate p : tmp.getPredicates(PredicateType.Precondition))
			 * { System.out.println("predicate name: "+p.getName());
			 * for(GraphPath pa : p.getPaths()) { System.out.println(pa); } }
			 */
			for (GraphPath p : tmp.getPathsBetweenPredicates()) {
				System.out.println(p);
			}
			if (tmp.getNextActivities() != null && tmp.getNextActivities().size() > 0) {

				for (Activity act : tmp.getNextActivities()) {
					IncidentActivity incAct = (IncidentActivity) act;
					System.out.println("**Paths from postconditions of current activity to preconditions of"
							+ " next activity [" + act.getName() + "] are:");
					for (GraphPath p : tmp.findPathsToNextActivity(incAct)) {
						System.out.println(p);
						if (!acts.contains(incAct)) {
							acts.add(incAct);
						}
					}

					System.out.println("**Paths from preconditions of current activity to preconditions of next"
							+ "activity are:");
					for (GraphPath p : tmp.getPathsToNextActivity(incAct)) {
						System.out.println(p);
					}
				}

			}
			System.out.println();

			actsVisited.add(tmp);
		}
	}

	public String getSummary() {

		LinkedList<Activity> acts = new LinkedList<Activity>();
		LinkedList<Activity> actsVisited = new LinkedList<Activity>();
		IncidentActivity tmp;
		StringBuilder res = new StringBuilder();
		String newLine = "\n";
		String separator = "###########################";

		updateInterStatesSatisfied();

		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = (IncidentActivity) acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			// get activity name
			res.append(newLine).append(separator).append(newLine).append(">>>Activity name: " + tmp.getName());

			ArrayList<Predicate> pre = tmp.getPredicates(PredicateType.Precondition);
			ArrayList<Predicate> post = tmp.getPredicates(PredicateType.Postcondition);

			// get preconditions
			if (pre != null && !pre.isEmpty()) {
				res.append(newLine).append(">>>Precondition: ").append(pre.get(0).getName()) // assumption
																								// made
																								// is
																								// that
																								// there
																								// is
																								// only
																								// one
																								// precondition
						.append(newLine).append("States matched: ").append(pre.get(0).getBigraphStates())
						// get states satisfying preconditions to postconditions
						// within the activity, and states that satisfy
						// condiitions between
						// the post of current activity and the precondition of
						// the next activity
						.append(newLine).append("States satisfying intra-conditions (i.e. pre-post): ")
						.append(pre.get(0).getStatesIntraSatisfied());
			}

			// get postcondition
			if (post != null && !post.isEmpty()) {
				res.append(newLine).append(">>>Postcondition: ").append(post.get(0).getName()) // assumption
																								// made
																								// is
																								// that
																								// there
																								// is
																								// only
																								// one
																								// precondition
						.append(newLine).append("States matched: ").append(post.get(0).getBigraphStates())
						// get states satisfying preconditions to postconditions
						// within the activity, and states that satisfy
						// condiitions between
						// the post of current activity and the precondition of
						// the next activity
						.append(newLine).append("States satisfying intra-conditions (i.e. pre-post): ")
						.append(post.get(0).getStatesIntraSatisfied()).append(newLine)
						.append("States satisfying inter-conditions (i.e. post-pre,next): ")
						.append(post.get(0).getStatesInterSatisfied());
			}

			res.append(newLine).append(separator).append(newLine);
			EList<Activity> next = tmp.getNextActivities();
			if (next != null && next.size() > 0) {
				for (Activity activity : next) {
					IncidentActivity incAct = (IncidentActivity) activity;
					if (!acts.contains(incAct)) {
						acts.add(incAct);
					}
				}
			}
			actsVisited.add(tmp);
		}

		return res.toString();
	}

	public void updateInterStatesSatisfied() {

		LinkedList<Activity> acts = new LinkedList<Activity>();
		LinkedList<Activity> actsVisited = new LinkedList<Activity>();
		IncidentActivity tmp;

		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = (IncidentActivity) acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			tmp.findPathsToNextActivities();

			for (Activity act : tmp.getNextActivities()) {
				IncidentActivity incAct = (IncidentActivity) act;
				if (!acts.contains(incAct)) {
					acts.add(incAct);
				}
			}

			actsVisited.add(tmp);
		}
	}

	protected synchronized List<Integer> getIntraInboundStates(Integer state) {
		
		return postconditionStatesInBoundTransitions!=null?postconditionStatesInBoundTransitions.get(state):null;
	}
	
	protected synchronized List<Integer> getIntraOutboundStates(Integer state) {
	
		return preconditionStatesWithTransitions!=null?preconditionStatesWithTransitions.get(state):null;
	}
	
	public List<GraphPath> findTransitions(int threadID) {

		long startTime = Calendar.getInstance().getTimeInMillis();

		// create instance name
		instanceName = PotentialIncidentInstance.INSTANCE_GLOABAL_NAME + "[" + threadID + "]"
				+ Logger.SEPARATOR_BTW_INSTANCES + PredicateHandler.INSTANCE_NAME_GLOBAL
				+ Logger.SEPARATOR_BTW_INSTANCES;

		// set minimum number of actions for a transition = # of activies in
		// pattern
		minimumNumberOfActions = incidentActivities.size();
		maxLevel = minimumNumberOfActions*2;
		
		logger.putMessage(instanceName + "Minimum nmber of actions = " + minimumNumberOfActions);
		// create thread pool
		mainPool = new ForkJoinPool();

		IncidentActivity sourceActivity = (IncidentActivity) getInitialActivity();
		IncidentActivity destinationActivity = (IncidentActivity) getFinalActivity();

		ArrayList<Predicate> preconditions = getPredicates(sourceActivity.getName(), PredicateType.Precondition);
		ArrayList<Predicate> postconditions = getPredicates(destinationActivity.getName(), PredicateType.Postcondition);

		Predicate precondition = preconditions.get(0);// assuming 1 precondition
		List<Integer> preconditionStates = precondition != null ? precondition.getBigraphStates() : null;

		Predicate postcondition = postconditions.get(0);// assuming 1

		// posytcondition
		List<Integer> postconditionStates = postcondition != null ? postcondition.getBigraphStates() : null;

		if (preconditionStates == null || preconditionStates.isEmpty() || postconditionStates == null
				|| postconditionStates.isEmpty()) {
			return null;
		}

		this.preconditionStates = preconditionStates;
		this.postconditionStates = postconditionStates;

		// generate nodes neighbor in the digraph to reduce processing time for
		// threads that will be created next
		logger.putMessage(instanceName + "Generating neighbor nodes in the Digraph...");
		transitionSystem.getDigraph().generateNeighborNodesMap();

		LinkedList<Activity> activities = getMiddleActivities(sourceActivity, destinationActivity);

		updateOrderedCondititions(activities);

		activities.addFirst(sourceActivity);
		activities.addLast(destinationActivity);

//		logger.putMessage(instanceName + "Identifying intra transitions between precondition states...");
		// identify transitions between states of precondition
		preconditionStatesWithTransitions = findIntraStatesTransitions(preconditionStates);

		// identify inbound transitions between states of postcondition
		postconditionStatesInBoundTransitions= findIntraInBoundStatesTransitions(postconditionStates);
		
		PreconditionMatcher preMatcher = new PreconditionMatcher(0, preconditionStates.size(), activities);

		logger.putMessage(instanceName + "Identifying transitions...");

		transitions = mainPool.invoke(preMatcher);

		// TransitionAnalyser analyser = new TransitionAnalyser(0,
		// transitions.size(), activities);
		// // analyseTransitions(sourceActivity, destinationActivity);
		//
		// logger.putMessage(instanceName + "Removing from identified
		// transitions (" + transitions.size()
		// + ") those that don't contain a state from each activity...");
		// List<GraphPath> transitionsToRemove = mainPool.invoke(analyser);
		//
		// int numOfTransitionsRemoved;
		//
		// if (transitionsToRemove != null && !transitionsToRemove.isEmpty()) {
		// transitions.removeAll(transitionsToRemove);
		// numOfTransitionsRemoved = transitionsToRemove.size();
		// } else {
		// numOfTransitionsRemoved = 0;
		// }
		//
		// logger.putMessage(instanceName + "(" + numOfTransitionsRemoved
		// + ") transitions removed. Total generated transitions is (" +
		// transitions.size() + ")");

		mainPool.shutdown();

		try {
			mainPool.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			mainPool.shutdownNow();
		}

		long endTime = Calendar.getInstance().getTimeInMillis();

		long duration = endTime - startTime;

		int secMils2 = (int) duration % 1000;
		int hours2 = (int) (duration / 3600000) % 60;
		int mins2 = (int) (duration / 60000) % 60;
		int secs2 = (int) (duration / 1000) % 60;

		// execution time
		logger.putMessage(
				instanceName + " # of transitions = " + transitions.size() + ". Transitions identification time: "
						+ duration + "ms [" + hours2 + "h:" + mins2 + "m:" + secs2 + "s:" + secMils2 + "ms]");

		preconditionStatesWithTransitions = null;
		this.preconditionStates = null;
		this.postconditionStates = null;
		transitionSystem.getDigraph().deleteNeighborNodesMap();

		return transitions;

	}

	protected void updateOrderedCondititions(List<Activity> middleActs) {

		orderedConditions = new LinkedList<Predicate>();

		for (Activity activity : middleActs) {
			IncidentActivity act = (IncidentActivity) activity;

			Predicate pre = act.getPredicates(PredicateType.Precondition) != null
					? act.getPredicates(PredicateType.Precondition).get(0) : null;
			Predicate post = act.getPredicates(PredicateType.Postcondition) != null
					? act.getPredicates(PredicateType.Postcondition).get(0) : null;

			orderedConditions.add(pre);
			orderedConditions.add(post);
		}

	}

	protected Map<Integer, List<Integer>> findIntraStatesTransitions(List<Integer> conStates) {

		if (conStates == null || conStates.isEmpty()) {
			return null;
		}

		logger.putMessage(instanceName + "Finding precondition intra transitions...");

		ConditionIntraTransitionsIdentifier condIdentifier = new ConditionIntraTransitionsIdentifier(conStates, 0,
				conStates.size());

		Map<Integer, List<Integer>> result = mainPool.invoke(condIdentifier);

		logger.putMessage(instanceName + "# of preconditions states with intra transitions = " + result.size()
				+ ". Total states = " + conStates.size());

		return result;
	}
	
	protected Map<Integer, List<Integer>> findIntraInBoundStatesTransitions(List<Integer> conStates) {

		if (conStates == null || conStates.isEmpty()) {
			return null;
		}

		logger.putMessage(instanceName + "Finding postcondition intra inbound transitions...");

		ConditionIntraInBoundTransitionsIdentifier condIdentifier = new ConditionIntraInBoundTransitionsIdentifier(conStates, 0,
				conStates.size());

		Map<Integer, List<Integer>> result = mainPool.invoke(condIdentifier);

		logger.putMessage(instanceName + "# of postconditions states with intra transitions = " + result.size()
				+ ". Total states = " + conStates.size());

//		for(Entry<Integer, List<Integer>> entry : result.entrySet()) {
//			logger.putMessage(instanceName+ "state-"+entry.getKey()+": "+entry.getValue());
//		}
		return result;
	}

	/**
	 * Finds states in a condition that have transitions to other states in this
	 * condition
	 * 
	 * @author Faeq
	 *
	 */
	class ConditionIntraTransitionsIdentifier extends RecursiveTask<Map<Integer, List<Integer>>> {

		private static final long serialVersionUID = 1L;
		List<Integer> states;
		int indexStart;
		int indexEnd;
		public static final int THRESHOLD = 100;
		private Map<Integer, List<Integer>> result;

		public ConditionIntraTransitionsIdentifier(List<Integer> states, int startIndex, int endIndex) {
			this.states = states;
			indexStart = startIndex;
			indexEnd = endIndex;
			result = new HashMap<Integer, List<Integer>>();
		}

		@Override
		protected Map<Integer, List<Integer>> compute() {
			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<ConditionIntraTransitionsIdentifier, Map<Integer, List<Integer>>>() {

							@Override
							public Map<Integer, List<Integer>> apply(ConditionIntraTransitionsIdentifier arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<Map<Integer, List<Integer>>>() {

							@Override
							public Map<Integer, List<Integer>> apply(Map<Integer, List<Integer>> arg0,
									Map<Integer, List<Integer>> arg1) {
								// TODO Auto-generated method stub
								for (Entry<Integer, List<Integer>> entry1 : arg1.entrySet()) {
									// if arg0 has this entry then add whatever
									// list to the exisitng list
									if (arg0.containsKey(entry1.getKey())) {
										arg0.get(entry1.getKey()).addAll(entry1.getValue());
									}
									// else add a new entry
									else {
										arg0.put(entry1.getKey(), entry1.getValue());
									}
								}
								return arg0;
							}

						});

			} else {

				List<ForkJoinTask<List<Integer>>> tasks = new LinkedList<ForkJoinTask<List<Integer>>>();

				for (int j = indexStart; j < indexEnd; j++) {

					Integer state = states.get(j);

					ConditionIntraTransitionsIdentifierVertical obj = new ConditionIntraTransitionsIdentifierVertical(
							states, 0, states.size(), state);

					tasks.add(mainPool.submit(obj));
				}

				int ind = 0;
				for (int j = indexStart; j < indexEnd; j++) {

					try {
						List<Integer> res = tasks.get(ind).get();

						if (res != null && !res.isEmpty()) {
							result.put(states.get(j), res);
						}

						ind++;
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				return result;
			}

		}

		protected List<ConditionIntraTransitionsIdentifier> createSubTasks() {

			List<ConditionIntraTransitionsIdentifier> dividedTasks = new LinkedList<ConditionIntraTransitionsIdentifier>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new ConditionIntraTransitionsIdentifier(states, indexStart, mid));
			dividedTasks.add(new ConditionIntraTransitionsIdentifier(states, mid, indexEnd));

			return dividedTasks;
		}

	}

	class ConditionIntraTransitionsIdentifierVertical extends RecursiveTask<List<Integer>> {

		private static final long serialVersionUID = 1L;
		List<Integer> states;
		int indexStart;
		int indexEnd;
		public static final int THRESHOLD = 100;
		private List<Integer> result;
		private int endState;
		private Digraph<Integer> transitionDigraph = transitionSystem.getDigraph();
		private boolean hasTransition = false;
		private Integer srcState;

		public ConditionIntraTransitionsIdentifierVertical(List<Integer> states, int startIndex, int endIndex,
				Integer srcState) {
			this.states = states;
			indexStart = startIndex;
			indexEnd = endIndex;
			result = new LinkedList<Integer>();
			this.srcState = srcState;
		}

		@Override
		protected List<Integer> compute() {
			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<ConditionIntraTransitionsIdentifierVertical, List<Integer>>() {

							@Override
							public List<Integer> apply(ConditionIntraTransitionsIdentifierVertical arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<List<Integer>>() {

							@Override
							public List<Integer> apply(List<Integer> arg0, List<Integer> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				for (int i = indexStart; i < indexEnd; i++) {

					Integer desState = states.get(i);

					if (srcState == desState) {
						continue;
					}

					// check if the srcState already had identified the
					// desState
					if (result.contains(desState)) {
						continue;
					}

					// tries to find a transition from src to des
					hasATransition(srcState, desState);

				}

				return result;
			}

		}

		protected boolean hasATransition(Integer srcState, Integer desState) {

			hasTransition = false;

			LinkedList<Integer> v;// = new LinkedList<Integer>();
			// this.startState = srcState;
			// v.add(srcState);
			this.endState = desState;

			// search for a transition
			// depthFirst(v);

			v = checkForTransitionBFS(srcState, endState);

			if (v == null) {
				return false;
			}

			// // add desState to srcState
			// if (hasTransition) {
			// result.add(desState);
			//
			// // remove first and last since they are the targets
			// v.removeFirst();
			// v.removeLast();
			//
			// } else {
			// // just remove the srcState
			// v.removeFirst();
			//
			// }

			// remove src state
			v.removeFirst();

			for (Integer node : v) {

				// then add desState to srcState list of states
				int index = states.indexOf(node);

				if (index >= indexStart && index < indexEnd) {

					if (!result.contains(node)) {
						result.add(node);
					}
				}
			}

			return hasTransition;

		}

		private void depthFirst(LinkedList<Integer> visited) {

			List<Integer> nodes = transitionDigraph.outboundNeighborsForTransitionGeneration(visited.getLast());

			for (Integer node : nodes) {

				if (visited.contains(node)) {
					continue;
				}

				if (node.equals(endState)) {
					visited.addLast(node);
					hasTransition = true;
					// visited.removeLast();
					return;
				}

				visited.addLast(node);
				depthFirst(visited);
				// visited.removeLast();

				if (hasTransition) {
					return;
				}
			}
		}

		private LinkedList<Integer> checkForTransitionBFS(Integer srcState, Integer endState) {

			LinkedList<Integer> queue = new LinkedList<Integer>();
			LinkedList<Integer> visited = new LinkedList<Integer>();

			queue.add(srcState);
			visited.add(srcState);

			int cnt = 0;
			while (!queue.isEmpty() && cnt < maxLevel) {

				Integer state = queue.removeFirst();

				List<Integer> states = transitionDigraph.outboundNeighborsForTransitionGeneration(state);

				for (Integer neighborState : states) {

					if (!visited.contains(neighborState)) {
						queue.add(neighborState);
						visited.add(neighborState);
					}

					// check if it the endState
					if (neighborState.equals(endState)) {
						hasTransition = true;
						return visited;
					}
				}

				cnt++;
			}

			return null;
		}

		protected List<ConditionIntraTransitionsIdentifierVertical> createSubTasks() {

			List<ConditionIntraTransitionsIdentifierVertical> dividedTasks = new LinkedList<ConditionIntraTransitionsIdentifierVertical>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new ConditionIntraTransitionsIdentifierVertical(states, indexStart, mid, srcState));
			dividedTasks.add(new ConditionIntraTransitionsIdentifierVertical(states, mid, indexEnd, srcState));

			return dividedTasks;
		}

	}

	class ConditionIntraInBoundTransitionsIdentifier extends RecursiveTask<Map<Integer, List<Integer>>> {

		private static final long serialVersionUID = 1L;
		List<Integer> states;
		int indexStart;
		int indexEnd;
		public static final int THRESHOLD = 100;
		private Map<Integer, List<Integer>> result;

		public ConditionIntraInBoundTransitionsIdentifier(List<Integer> states, int startIndex, int endIndex) {
			this.states = states;
			indexStart = startIndex;
			indexEnd = endIndex;
			result = new HashMap<Integer, List<Integer>>();
		}

		@Override
		protected Map<Integer, List<Integer>> compute() {
			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<ConditionIntraInBoundTransitionsIdentifier, Map<Integer, List<Integer>>>() {

							@Override
							public Map<Integer, List<Integer>> apply(ConditionIntraInBoundTransitionsIdentifier arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<Map<Integer, List<Integer>>>() {

							@Override
							public Map<Integer, List<Integer>> apply(Map<Integer, List<Integer>> arg0,
									Map<Integer, List<Integer>> arg1) {
								// TODO Auto-generated method stub
								for (Entry<Integer, List<Integer>> entry1 : arg1.entrySet()) {
									// if arg0 has this entry then add whatever
									// list to the exisitng list
									if (arg0.containsKey(entry1.getKey())) {
										arg0.get(entry1.getKey()).addAll(entry1.getValue());
									}
									// else add a new entry
									else {
										arg0.put(entry1.getKey(), entry1.getValue());
									}
								}
								return arg0;
							}

						});

			} else {

				List<ForkJoinTask<List<Integer>>> tasks = new LinkedList<ForkJoinTask<List<Integer>>>();

				for (int j = indexStart; j < indexEnd; j++) {

					Integer state = states.get(j);

					ConditionIntraInBoundTransitionsIdentifierVertical obj = new ConditionIntraInBoundTransitionsIdentifierVertical(
							states, 0, states.size(), state);

					tasks.add(mainPool.submit(obj));
				}

				int ind = 0;
				for (int j = indexStart; j < indexEnd; j++) {

					try {
						List<Integer> res = tasks.get(ind).get();

						if (res != null && !res.isEmpty()) {
							result.put(states.get(j), res);
						}

						ind++;
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				return result;
			}

		}

		protected List<ConditionIntraInBoundTransitionsIdentifier> createSubTasks() {

			List<ConditionIntraInBoundTransitionsIdentifier> dividedTasks = new LinkedList<ConditionIntraInBoundTransitionsIdentifier>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new ConditionIntraInBoundTransitionsIdentifier(states, indexStart, mid));
			dividedTasks.add(new ConditionIntraInBoundTransitionsIdentifier(states, mid, indexEnd));

			return dividedTasks;
		}

	}

	class ConditionIntraInBoundTransitionsIdentifierVertical extends RecursiveTask<List<Integer>> {

		private static final long serialVersionUID = 1L;
		List<Integer> states;
		int indexStart;
		int indexEnd;
		public static final int THRESHOLD = 100;
		private List<Integer> result;
//		private int endState;
		private Digraph<Integer> transitionDigraph = transitionSystem.getDigraph();
		private boolean hasTransition = false;
//		private Integer srcState;
		private Integer desState;
		
		public ConditionIntraInBoundTransitionsIdentifierVertical(List<Integer> states, int startIndex, int endIndex,
				Integer desState) {
			this.states = states;
			indexStart = startIndex;
			indexEnd = endIndex;
			result = new LinkedList<Integer>();
			this.desState = desState;
		}

		@Override
		protected List<Integer> compute() {
			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<ConditionIntraInBoundTransitionsIdentifierVertical, List<Integer>>() {

							@Override
							public List<Integer> apply(ConditionIntraInBoundTransitionsIdentifierVertical arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<List<Integer>>() {

							@Override
							public List<Integer> apply(List<Integer> arg0, List<Integer> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				for (int i = indexStart; i < indexEnd; i++) {

					Integer srcState = states.get(i);

					if (desState == srcState) {
						continue;
					}

					// check if the srcState already had identified the
					// desState
					if (result.contains(srcState)) {
						continue;
					}

					// tries to find a transition from src to des
					hasATransition(srcState, desState);

				}

				return result;
			}

		}

		protected boolean hasATransition(Integer srcState, Integer desState) {

			hasTransition = false;

			LinkedList<Integer> v = new LinkedList<Integer>();
			// this.startState = srcState;
			 v.add(srcState);
//			this.endState = desState;

			int length = 1;
			// search for a transition
			 depthFirst(v, length);

//			v = checkForTransitionBFS(srcState, endState);

//			if (v == null) {
//				return false;
//			}

			// // add desState to srcState
			// if (hasTransition) {
			// result.add(desState);
			//
			// // remove first and last since they are the targets
			// v.removeFirst();
			// v.removeLast();
			//
			// } else {
			// // just remove the srcState
			// v.removeFirst();
			//
			// }

			// remove src state
//			v.removeFirst();

			 if(!hasTransition) {
				 return hasTransition;
			 }
			 
			 // v contains a transition to the desState, so all nodes on that transition is added to the desState
			for (Integer node : v) {

				// then add desState to srcState list of states
				int index = states.indexOf(node);

				if (index >= indexStart && index < indexEnd) {

					if (!result.contains(node)) {
						result.add(node);
					}
				}
			}

			return hasTransition;

		}

		private void depthFirst(LinkedList<Integer> visited, int length) {

			List<Integer> nodes = transitionDigraph.outboundNeighborsForTransitionGeneration(visited.getLast());

			for (Integer node : nodes) {

				if (visited.contains(node)) {
					continue;
				}

				if (node.equals(desState)) {
//					visited.addLast(node);
					hasTransition = true;
					// visited.removeLast();
					return;
				}
				
				//check if max level reached
				if(length  >= maxLevel) {
					continue;
				}

				visited.addLast(node);
//				length++;

				depthFirst(visited, (length+1));

				if (hasTransition) {
					return;
				}
				
				visited.removeLast();
			}
		}

		private LinkedList<Integer> checkForTransitionBFS(Integer srcState, Integer endState) {

			LinkedList<Integer> queue = new LinkedList<Integer>();
			LinkedList<Integer> visited = new LinkedList<Integer>();

			queue.add(srcState);
			visited.add(srcState);

			int cnt = 0;
			while (!queue.isEmpty() && cnt < maxLevel) {

				Integer state = queue.removeFirst();

				List<Integer> states = transitionDigraph.outboundNeighborsForTransitionGeneration(state);

				for (Integer neighborState : states) {

					if (!visited.contains(neighborState)) {
						queue.add(neighborState);
						visited.add(neighborState);
					}

					// check if it the endState
					if (neighborState.equals(endState)) {
						hasTransition = true;
						return visited;
					}
				}

				cnt++;
			}

			return null;
		}

		protected List<ConditionIntraInBoundTransitionsIdentifierVertical> createSubTasks() {

			List<ConditionIntraInBoundTransitionsIdentifierVertical> dividedTasks = new LinkedList<ConditionIntraInBoundTransitionsIdentifierVertical>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new ConditionIntraInBoundTransitionsIdentifierVertical(states, indexStart, mid, desState));
			dividedTasks.add(new ConditionIntraInBoundTransitionsIdentifierVertical(states, mid, indexEnd, desState));

			return dividedTasks;
		}

	}

	
	class PreconditionMatcher extends RecursiveTask<List<GraphPath>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private List<GraphPath> result;
		private List<Activity> activities;

		// number of states in the precondition on which the division into sub
		// threads should take place
		private int preThreshold = 100;

		public PreconditionMatcher(int indexStart, int indexEnd, List<Activity> acts) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			result = new LinkedList<GraphPath>();

			activities = acts;
		}

		@Override
		protected List<GraphPath> compute() {
			if ((indexEnd - indexStart) > preThreshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<PreconditionMatcher, List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(PreconditionMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(List<GraphPath> arg0, List<GraphPath> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				// do the matching by slicing Assets to match to into different
				// pieces
				List<ForkJoinTask<List<GraphPath>>> postCons = new LinkedList<ForkJoinTask<List<GraphPath>>>();

				for (int i = indexStart; i < indexEnd; i++) {
					int preconditionState = preconditionStates.get(i);
					// List<GraphPath> threadResult = mainPool
					// .invoke(new PostconditionMatcher(0,
					// postconditionStates.size(), preconditionState));
					// PostconditionMatcher tmp = new PostconditionMatcher(0,
					// postconditionStates.size(), preconditionState);
					// postCons.add(tmp);
					postCons.add(mainPool.submit(
							new PostconditionMatcher(0, postconditionStates.size(), preconditionState, activities)));
				}

				for (ForkJoinTask<List<GraphPath>> task : postCons) {

					try {
						result.addAll(task.get());
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				// result.addAll(threadResult);
				return result;
			}
		}

		protected List<PreconditionMatcher> createSubTasks() {

			List<PreconditionMatcher> dividedTasks = new LinkedList<PredicateHandler.PreconditionMatcher>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new PreconditionMatcher(indexStart, mid, activities));
			dividedTasks.add(new PreconditionMatcher(mid, indexEnd, activities));

			return dividedTasks;
		}

	}

	class PostconditionMatcher extends RecursiveTask<List<GraphPath>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private int preState;
		private List<GraphPath> result;
		private List<GraphPath> localResult;
		// private Integer startState;
		private Integer endState;
		private Digraph<Integer> transitionDigraph = transitionSystem.getDigraph();
		// number of states in the precondition on which the division into sub
		// threads should take place
		private int postThreshold = 100;

		// computed during idnetification (currently not used)
		private List<Integer> preIntraState;
		private boolean hasTransition = false;
		private Integer endIntraState;

		private List<Activity> activities;
		// private int conditionIndex = 0;
		// private LinkedList<Integer> nodeHistory;

		// computed before
		List<Integer> preIntraStates;

		public PostconditionMatcher(int indexStart, int indexEnd, int preState, List<Activity> acts) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.preState = preState;
			result = new LinkedList<GraphPath>();

			// preIntraState = new LinkedList<Integer>();
			activities = acts;
			// nodeHistory = new LinkedList<Integer>();
			preIntraStates = getIntraOutboundStates(preState);
		}

		@Override
		protected List<GraphPath> compute() {
			if ((indexEnd - indexStart) > postThreshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<PostconditionMatcher, List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(PostconditionMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(List<GraphPath> arg0, List<GraphPath> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				// do the matching by slicing Assets to match to into different
				// pieces
				List<GraphPath> stateResult;
				for (int i = indexStart; i < indexEnd; i++) {
					int postconditionState = postconditionStates.get(i);
					stateResult = getPaths(postconditionState);
					result.addAll(stateResult);
				}

				return result;
			}
		}

		protected List<PostconditionMatcher> createSubTasks() {

			List<PostconditionMatcher> dividedTasks = new LinkedList<PredicateHandler.PostconditionMatcher>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new PostconditionMatcher(indexStart, mid, preState, activities));
			dividedTasks.add(new PostconditionMatcher(mid, indexEnd, preState, activities));

			return dividedTasks;
		}

		public List<GraphPath> getPaths(Integer desState) {
			LinkedList<Integer> v = new LinkedList<Integer>();
			localResult = new LinkedList<GraphPath>();

			v.add(preState);
			// this.endState = desState;

			List<Integer> allVisited = new LinkedList<Integer>();

			allVisited.add(preState);

			// find all possible transitions
			// depthFirst(desState, v, allVisited);
			breadthFirst(desState);

			return localResult;
		}

		private void depthFirst(Integer endState, LinkedList<Integer> transition, List<Integer> allVisited) {

			List<Integer> nodes = transitionDigraph.outboundNeighborsForTransitionGeneration(transition.getLast());

			boolean isAdded = false;

			// examine adjacent nodes
			// for (Integer node : nodes) {
			// if (visited.contains(node)) {
			// continue;
			// }
			// if (node.equals(endState)) {
			// visited.add(node);
			// addTransitiontoList(visited);
			// visited.removeLast();
			// break;
			// }
			// }

			for (Integer node : nodes) {

				// if (visited.contains(node) || node.equals(endState)) {
				// continue;
				// }

				// if (visited.contains(node)) {
				// continue;
				// }

				if (allVisited.contains(node)) {
					continue;
				}

				/**
				 * Check if current node has a transition to the start node
				 * 
				 */
				// if the node is a state that the srcState has a transition to,
				// then skip
				// if (isNodeIntraTransition(node)) {
				// continue;
				// }

				if (preIntraStates != null && preIntraStates.contains(node)) {
					continue;
				}

				// check if node satisfies current condition
				// if(conditionIndex < orderedConditions.size()) {
				// Predicate tmp = orderedConditions.get(conditionIndex);
				// if(tmp == null) {
				// //there's no condition to check
				// conditionIndex++;
				// } else {
				// if(tmp.getBigraphStates().contains(node)) {
				// conditionIndex++;
				// nodeHistory.add(node);
				// }
				// }
				// }
				/*****************************/

				if (node.equals(endState)) {
					transition.add(node);

					// identified transition satisfies other activities
					// conditions
					// if(conditionIndex >= orderedConditions.size()) {
					addTransitiontoList(transition);

					// }

					transition.removeLast();

					continue;
				}

				transition.addLast(node);

				// if(!isAdded) {
				allVisited.addAll(nodes);
				// isAdded = true;
				// }

				depthFirst(endState, transition, allVisited);

				transition.removeLast();

				// if(isAdded) {
				allVisited.removeAll(nodes);
				// isAdded = false;
				// }

				// if(!nodeHistory.isEmpty() &&
				// nodeHistory.getLast().equals(tmpState)) {
				// conditionIndex--;
				// nodeHistory.removeLast();
				// }

			}

		}

		private void breadthFirst(Integer endState) {

			LinkedList<List<Integer>> queue = new LinkedList<List<Integer>>();
			LinkedList<Integer> visited = new LinkedList<Integer>();

			List<Integer> transition = new LinkedList<Integer>();

			transition.add(preState);
			queue.add(transition);
			visited.add(preState);
			
			//holds the list of states that have transition to the end state
			List<Integer> intraInboundStates = getIntraInboundStates(endState);
			
			// add all precondition states that the preState has a transition to
			// visited.addAll(preIntraStates);

			while (!queue.isEmpty()) {

				List<Integer> trans = queue.poll();

				Integer state = trans.get(trans.size() - 1);

				// check if its the endState
				// if (state.equals(endState)) {
				//
				// // add found transitions to the list
				// addTransitiontoListBFS(trans);
				//
				// //not interested in any neighbours if final reached
				// continue;
				// }

				List<Integer> states = transitionDigraph.outboundNeighborsForTransitionGeneration(state);

				if (states != null && !states.isEmpty()) {

					for (Integer neighborState : states) {

						// if the neighbour state has a transition to the pre
						// state
						if (preIntraStates != null && preIntraStates.contains(neighborState)) {
							continue;
						}

						//if the neighbour state has a transition to the end state then skip this neigbour state
						if(intraInboundStates!= null && intraInboundStates.contains(neighborState)) {
							continue;
						}
						
						if (!visited.contains(neighborState)) {
							List<Integer> newTrans = new LinkedList<Integer>(trans);
							newTrans.add(neighborState);

							if (neighborState.equals(endState)) {
								if (newTrans.size() > minimumNumberOfActions) {
									addTransitiontoListBFS(newTrans);
								} else {
//									logger.putMessage(instanceName
//											+ "transition is less than minimum number of actions. " + newTrans);
									newTrans = null;
								}

							} else {
								visited.add(neighborState);
								queue.add(newTrans);
							}

						}
					}
				}
			}

		}

		private void addTransitiontoList(List<Integer> transition) {

			// if the transition does not contain states from the other
			// activities, then it is ignored
			if (!analyseTransition(transition)) {
				return;
			}

			// logger.putMessage(transition.toString());

			LinkedList<Integer> newList = new LinkedList<Integer>(transition);
			GraphPath path = new GraphPath(transitionSystem);

			// newList.addAll(transition);

			// path.setPredicateSrc(null);
			// path.setPredicateDes(null);
			path.setStateTransitions(newList);
			localResult.add(path);

		}

		private void addTransitiontoListBFS(List<Integer> transition) {

			// if the transition does not contain states from the other
			// activities, then it is ignored
			if (!analyseTransition(transition)) {
				return;
			}

			// logger.putMessage(transition.toString());

			// LinkedList<Integer> newList = new
			// LinkedList<Integer>(transition);
			GraphPath path = new GraphPath(transitionSystem);

			// newList.addAll(transition);

			// path.setPredicateSrc(null);
			// path.setPredicateDes(null);
			path.setStateTransitions((LinkedList<Integer>) transition);
			localResult.add(path);

		}

		private boolean isNodeIntraTransition(Integer node) {

			int index = preconditionStates.indexOf(node);

			// if node is not in the precondition states then it is not checked
			// i.e. a node should be a precondition state
			if (index == -1) {
				return false;
			}

			// check if node is in the intra state list. if yes return true. no,
			// look for it
			if (preIntraState.contains(node)) {
				return true;

			}

			return hasATransition(node);

		}

		protected boolean hasATransition(Integer desState) {

			hasTransition = false;

			LinkedList<Integer> v = new LinkedList<Integer>();
			// this.startState = srcState;
			v.add(preState);
			this.endIntraState = desState;

			// search for a transition
			checkForTransition(v); // DFS
			// v = checkForTransitionBFS(srcState, desState); //BFS

			// add desState to srcState
			if (hasTransition) {
				preIntraState.add(desState);

				// remove first and last since they are the targets
				v.removeFirst();
				v.removeLast();

			} else {
				// just remove the srcState
				v.removeFirst();

			}

			for (Integer node : v) {

				if (!preIntraState.contains(node)) {
					preIntraState.add(node);
				}
			}

			return hasTransition;

		}

		private void checkForTransition(LinkedList<Integer> visited) {

			List<Integer> nodes = transitionDigraph.outboundNeighborsForTransitionGeneration(visited.getLast());

			for (Integer node : nodes) {

				if (visited.contains(node)) {
					continue;
				}

				if (node.equals(endIntraState)) {
					visited.addLast(node);
					hasTransition = true;
					return;
				}

				visited.addLast(node);
				checkForTransition(visited);

				if (hasTransition) {
					return;
				}
			}
		}

		private LinkedList<Integer> checkForTransitionBFS(Integer srcState, Integer endState) {

			LinkedList<Integer> queue = new LinkedList<Integer>();
			LinkedList<Integer> visited = new LinkedList<Integer>();

			queue.add(srcState);
			visited.add(srcState);

			while (!queue.isEmpty()) {

				Integer state = queue.removeFirst();

				List<Integer> states = transitionDigraph.outboundNeighborsForTransitionGeneration(state);

				for (Integer neighborState : states) {

					if (!visited.contains(neighborState)) {
						queue.add(neighborState);
						visited.add(neighborState);
					}

					// check if it the endState
					if (neighborState.equals(endState)) {
						hasTransition = true;
						return visited;
					}
				}
			}

			return null;
		}

		protected boolean analyseTransition(List<Integer> states) {

			boolean isCheckingPrecondition = true;

			ListIterator<Activity> activitiesIterator = activities.listIterator();

			if (activities != null) {

				int j = 0;

				isCheckingPrecondition = true;

				activitiesIterator = activities.listIterator();

				while (activitiesIterator.hasNext()) {
					IncidentActivity activity = (IncidentActivity) activitiesIterator.next();

					// get precondition of the activity (assumption: there
					// is
					// only one precondition)
					List<Predicate> preCons = activity.getPredicates(PredicateType.Precondition);
					List<Predicate> postCons = activity.getPredicates(PredicateType.Postcondition);

					Predicate pre = (preCons != null && !preCons.isEmpty()) ? preCons.get(0) : null;
					LinkedList<Integer> preStates = pre != null ? pre.getBigraphStates() : null;

					// get precondition of the activity (assumption: there
					// is only one postcondition)
					Predicate post = (postCons != null && !postCons.isEmpty()) ? postCons.get(0) : null;
					LinkedList<Integer> postStates = post != null ? post.getBigraphStates() : null;

					// the activity has no pre or post conditions, then move
					// to the next activity
					if (pre == null && post == null) {
						continue;
					}

					// assumption: each predicate should satisfy different
					// state
					// in the transition
					for (; j < states.size(); j++) { // last state is for
														// the
														// src and des
														// activities
						int state = states.get(j);

						// if it is the last element and either it is still
						// checking the precondition or the postcondition
						// does
						// not contain the state then remove the path and
						// break
						// to outerloop if there are still activities to
						// iterate
						if (j == states.size() - 1 && (activitiesIterator.hasNext() ||
						// or if it is the last activity but it is still
						// checking precondition // over
								(preStates != null && isCheckingPrecondition) ||
								// or if it is last activity and and the
								// postcondition does not have the state as
								// one of its own
								(postStates != null && !postStates.contains(state)))) {
							return false;
							// break outerLoop;
						}

						// find a match for the precondition
						if (isCheckingPrecondition && preStates != null) {
							// if no match is found then move to next state
							// in transition
							if (!preStates.contains(state)) {
								continue;
								// else, if a state matches a precondition
								// state, then move to the postcondition, if
								// there's one, if not then break to next
								// activity
							} else {
								if (postStates != null) {
									isCheckingPrecondition = false;
								} else {
									isCheckingPrecondition = true;
									break;
								}
							}

							// find a match for the postcondition
						} else {
							if (!postStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = true;
								break;
							}
						}
					}
				}
			}

			return true;
		}

	}

//	class DepthFirstSearcher implements Callable<GraphPath> {
//
//		public DepthFirstSearcher(Integer start, Integer endState) {
//
//		}
//
//		@Override
//		public GraphPath call() throws Exception {
//			// TODO Auto-generated method stub
//
//			return null;
//		}
//
//	}

	class TransitionAnalyser extends RecursiveTask<List<GraphPath>> {

		private static final long serialVersionUID = 1L;
		private int threshold = 100;
		private int indexStart;
		private int indexEnd;

		// list of activities in sequence
		private List<Activity> activities;

		// indices of transitions that should be removed from the list of
		// transitions
		private List<GraphPath> transitionsToRemove;

		public TransitionAnalyser(int startIndex, int endIndex, List<Activity> activities) {
			this.indexStart = startIndex;
			this.indexEnd = endIndex;
			this.activities = activities;
			transitionsToRemove = new LinkedList<GraphPath>();
		}

		@Override
		protected List<GraphPath> compute() {

			if ((indexEnd - indexStart) > threshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<TransitionAnalyser, List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(TransitionAnalyser arg0) {
								// TODO Auto-generated method stub
								return arg0.transitionsToRemove;
							}

						}).reduce(transitionsToRemove, new BinaryOperator<List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(List<GraphPath> arg0, List<GraphPath> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				analyseTransitions();
			}

			return transitionsToRemove;
		}

		protected List<TransitionAnalyser> createSubTasks() {

			List<TransitionAnalyser> dividedTasks = new LinkedList<PredicateHandler.TransitionAnalyser>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new TransitionAnalyser(indexStart, mid, activities));
			dividedTasks.add(new TransitionAnalyser(mid, indexEnd, activities));

			return dividedTasks;
		}

		protected void analyseTransitions() {

			// LinkedList<Activity> activities =
			// getMiddleActivities(sourceActivity, destinationActivity);

			// add the first and the last activities
			// activities.addFirst(sourceActivity);
			// activities.addLast(destinationActivity);

			boolean isCheckingPrecondition = true;
			// boolean isCheckingPostcondition = false;

			// check if each path contains at least one of the satisfied states
			// for
			// each activity
			// can be parallelised
			// ListIterator<GraphPath> pathsIterator =
			// transitions.listIterator();
			ListIterator<Activity> activitiesIterator = activities.listIterator();

			if (activities != null) {
				for (int i = indexStart; i < indexEnd; i++) {

					GraphPath path = transitions.get(i);
					LinkedList<Integer> states = path.getStateTransitions();

					int j = 0;

					isCheckingPrecondition = true;

					activitiesIterator = activities.listIterator();

					outerLoop: while (activitiesIterator.hasNext()) {
						IncidentActivity activity = (IncidentActivity) activitiesIterator.next();

						// get precondition of the activity (assumption: there
						// is
						// only one precondition)
						List<Predicate> preCons = activity.getPredicates(PredicateType.Precondition);
						List<Predicate> postCons = activity.getPredicates(PredicateType.Postcondition);

						Predicate pre = (preCons != null && !preCons.isEmpty()) ? preCons.get(0) : null;
						LinkedList<Integer> preStates = pre != null ? pre.getBigraphStates() : null;

						// get precondition of the activity (assumption: there
						// is
						// only one postcondition)
						Predicate post = (postCons != null && !postCons.isEmpty()) ? postCons.get(0) : null;
						LinkedList<Integer> postStates = post != null ? post.getBigraphStates() : null;

						// the activity has no pre or post conditions, then move
						// to the next activity
						if (pre == null && post == null) {
							continue;
						}

						// assumption: each predicate should satisfy different
						// state
						// in the transition
						for (; j < states.size(); j++) { // last state is for
															// the
															// src and des
															// activities
							int state = states.get(j);

							// if it is the last element and either it is still
							// checking the precondition or the postcondition
							// does
							// not contain the state then remove the path and
							// break
							// to outerloop if there are still activities to
							// iterate
							if (j == states.size() - 1 && (activitiesIterator.hasNext() ||
							// or if it is the last activity but it is still
							// checking precondition // over
									(preStates != null && isCheckingPrecondition) ||
									// or if it is last activity and and the
									// postcondition does not have the state as
									// one of its own
									(postStates != null && !postStates.contains(state)))) {
								// pathsIterator.remove();
								transitionsToRemove.add(path);
								break outerLoop;
							}

							// find a match for the precondition
							if (isCheckingPrecondition && preStates != null) {
								// if no match is found then move to next state
								// in transition
								if (!preStates.contains(state)) {
									continue;
									// else, if a state matches a precondition
									// state, then move to the postcondition, if
									// there's one, if not then break to next
									// activity
								} else {
									if (postStates != null) {
										isCheckingPrecondition = false;
									} else {
										isCheckingPrecondition = true;
										break;
									}
								}

								// find a match for the postcondition
							} else {
								if (!postStates.contains(state)) {
									continue;
								} else {
									isCheckingPrecondition = true;
									break;
								}
							}
						}
					}
				}
			}
		}

	}

}
