package ie.lero.spare.pattern_instantiation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.impl.ActivityImpl;
import ie.lero.spare.utility.PredicateType;
import ie.lero.spare.utility.TransitionSystem;

public class IncidentActivity extends ActivityImpl {

	//private String name;
//	private ArrayList<IncidentActivity> previousActivities;
//	private ArrayList<IncidentActivity> nextActivities;
	private ArrayList<Predicate> predicates;
	private HashMap<String, LinkedList<GraphPath>> pathsToNextActivities;
	// other attributes (e.g., initiator) can be added later
	private SystemInstanceHandler systemHandler;
	private TransitionSystem transitionSystem;
	
	public IncidentActivity() {
		predicates = new ArrayList<Predicate>();
		pathsToNextActivities = new HashMap<String, LinkedList<GraphPath>>();
		systemHandler = SystemsHandler.getCurrentSystemHandler();
		transitionSystem = systemHandler.getTransitionSystem();
	}

	public IncidentActivity(String name) {
		this();
		this.name = name;
	}
	
	public IncidentActivity(Activity activity) {
		super(activity);
		predicates = new ArrayList<Predicate>();
		pathsToNextActivities = new HashMap<String, LinkedList<GraphPath>>();
		systemHandler = SystemsHandler.getCurrentSystemHandler();
		transitionSystem = systemHandler.getTransitionSystem();
		
	}
	
	public IncidentActivity(Activity activity, SystemInstanceHandler sysHandler) {
		super(activity);
		predicates = new ArrayList<Predicate>();
		pathsToNextActivities = new HashMap<String, LinkedList<GraphPath>>();
		systemHandler = sysHandler;
		transitionSystem = systemHandler.getTransitionSystem();
		
	}

	public IncidentActivity(String name, ArrayList<Predicate> preds) {
		this();
		this.name = name;
		predicates = preds;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(ArrayList<Predicate> predicates) {
		this.predicates = predicates;
	}

	public void addPredicate(Predicate pred) {
		predicates.add(pred);
	}

	/*public EList<IncidentActivity> getPreviousActivities() {
		
		return previousActivities;

	}*/

	public void setPreviousActivities(ArrayList<Activity> previousActivities) {
		getPreviousActivities().clear();
		getPreviousActivities().addAll(previousActivities);
	}

	/*public ArrayList<IncidentActivity> getNxtActivities() {
		return nextActivities;
	}*/

	public void setNextActivities(ArrayList<Activity> nextActivities) {
		getNextActivities().clear();
		getNextActivities().addAll(nextActivities);
	}

	public void addNextActivity(Activity activity) {
		getNextActivities().add(activity);
	}

	public void addPreviousActivity(Activity activity) {
		getPreviousActivities().add(activity);
	}

	
	public HashMap<String, LinkedList<GraphPath>> getPathsToNextActivities() {
		if(pathsToNextActivities == null || pathsToNextActivities.size() ==0) {
			return getFullPathsToNextActivities();
		}
		return pathsToNextActivities;
	}

	public LinkedList<GraphPath> getPathsToNextActivity(IncidentActivity nextActivity) {
		if(pathsToNextActivities == null || pathsToNextActivities.size() ==0) {
			return getFullPathsToNextActivities(nextActivity);
		}
		return pathsToNextActivities.get(nextActivity.getName());
	}

	
	public void setPathsToNextActivities(HashMap<String, LinkedList<GraphPath>> distincPaths) {
		this.pathsToNextActivities = distincPaths;
	}

	/**
	 * Checks whether an activity is satisfied or not
	 * 
	 * @return true if all predicates (preconditions and postcondtions) have at
	 *         least one state that satisfies them and there is at least one
	 *         path from each precondition to each postcondition, otherwise it
	 *         returns false. if an activity has no pre or post conditions (no
	 *         predicates at all) then it returns default as true
	 */
	public boolean isActivitySatisfied() {
		//Check the satisfaction criteria which can be changed to be more strict
		//for example each precondition should have at least one path to each postcondition
		//or if association (i.e. which pre lead to which post) between conditions is defined then it should match it
		
		ArrayList<Predicate> preconditions = new ArrayList<Predicate>();
		ArrayList<Predicate> postconditions = new ArrayList<Predicate>();

		preconditions = getPredicates(PredicateType.Precondition);
		postconditions = getPredicates(PredicateType.Postcondition);
		
		// could be changed to array that holds the indices or names of
		// predicates satisfying the one examined
		boolean[] preSatisfied = new boolean[preconditions.size()];
		boolean[] postSatisfied = new boolean[postconditions.size()];

		for (boolean t : preSatisfied) {
			t = false;
		}
		for (boolean m : postSatisfied) {
			m = false;
		}

		for (int i = 0; i < preconditions.size(); i++) { // checking if all pres
															// have paths to
															// posts and all
															// posts have paths
															// to pres
			for (int j = 0; j < postconditions.size(); j++)
				if (preconditions.get(i).hasPathsTo(postconditions.get(j))) {
					preSatisfied[i] = true;
					postSatisfied[j] = true;
				}
		}

		for (boolean pre : preSatisfied) {
			if (!pre) {
				return false;
			}
		}
		for (boolean post : postSatisfied) {
			if (!post) {
				return false;
			}
		}

		//all preconditions should have a common state to start from (how about postconditions? my opinion is that it is not necessary for postconditions), even preconditions are not necessarily to be the same state
		return true;
	}

	public ArrayList<Predicate> getPredicates(PredicateType predicateType) {
		ArrayList<Predicate> preds = new ArrayList<Predicate>();

		if (predicates == null) {
			return null;
		}

		for (Predicate p : predicates) {
			if (p.getPredicateType() == predicateType) {
				preds.add(p);
			}
		}

		return preds;
	}

	public ArrayList<Predicate> getPredicates(String predicateType) {
		ArrayList<Predicate> preds = new ArrayList<Predicate>();

		if (predicates == null) {
			return null;
		}

		for (Predicate p : predicates) {
			if (p.getPredicateType().toString().contentEquals(predicateType)) {
				preds.add(p);
			}
		}

		return preds;
	}

	public LinkedList<GraphPath> getPathsBetweenPredicates() {
		
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();

		for (Predicate p : getPredicates(PredicateType.Precondition)) { //while pre and post conditions refer to the same paths, when they added (the paths) they are not duplicated
			paths.addAll(p.getPaths());
		}

		return paths;
	}

	/**
	 * Finds all possible paths from each postcondition in the current activity to the preconditions
	 * of next activities. Currently, it finds whatever it can i.e. no restrictions
	 * @return LinkedList of GraphPath objects that contain possible paths. Empty List if there are none
	 */
	public LinkedList<GraphPath> findPathsToNextActivities() {
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
		LinkedList<GraphPath> tmp;
		
		//for all postconditions of current activity find all paths to preconditions of next activity
		for (Predicate predCurrent : getPredicates(PredicateType.Postcondition)) {
			for (Activity act : getNextActivities()) {
				IncidentActivity incAct = (IncidentActivity)act;
				for (Predicate predNext : incAct.getPredicates(PredicateType.Precondition)) {
					tmp = transitionSystem.getPaths(predCurrent, predNext, true);
					paths.addAll(tmp);
				}

			}
			
		}
		
		return paths;
	}
	
	/**
	 * Finds all possible paths from each postcondition in the current activity to the preconditions
	 * of next activity specified by the parameter. Currently, it finds whatever it can i.e. no restrictions
	 * @param nextActivity The next activity as an IncidentActivity object
	 * @return LinkedList of GraphPAth objects containing the paths
	 */
	public LinkedList<GraphPath> findPathsToNextActivity(IncidentActivity nextActivity) {
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
		LinkedList<GraphPath> tmp;
		
		//for all postconditions of current activity find all paths to preconditions of next activity
		for (Predicate predCurrent : getPredicates(PredicateType.Postcondition)) {
				for (Predicate predNext : nextActivity.getPredicates(PredicateType.Precondition)) {
					tmp = transitionSystem.getPaths(predCurrent, predNext, true);
					paths.addAll(tmp);
				}

		}
		
		return paths;
	}
	
	/**
	 * Finds all possible paths from each postcondition in previous activities to the preconditions
	 * of current activity specified by the parameter. Currently, it finds whatever it can i.e. no restrictions
	 * @return LinkedList of GraphPath objects containing the paths
	 */
	public LinkedList<GraphPath> findPathsFromPreviousActivities() {
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
		
		for(Activity act : getPreviousActivities()) {
			IncidentActivity incAct = (IncidentActivity)act;
			paths.addAll(findPathsFromPreviousActivity(incAct));
		}
		
		return paths;
	}
	
	/**
	 * Finds all possible paths from each postcondition in previous activities to the preconditions
	 * of current activity specified by the parameter. Currently, it finds whatever it can i.e. no restrictions
	 * @param preActivity the previous activity as IncidentActivity object
	 * @return LinkedList of GraphPath objects containing the paths
	 */
	public LinkedList<GraphPath> findPathsFromPreviousActivity(IncidentActivity preActivity) {
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
		LinkedList<GraphPath> tmp;
		
		//for all postconditions of previous activity find all paths to preconditions of current activity
		for (Predicate predPrevious : preActivity.getPredicates(PredicateType.Postcondition)) {
				for (Predicate predCurrent : getPredicates(PredicateType.Precondition)) {
					tmp = transitionSystem.getPaths(predPrevious, predCurrent, true);
					paths.addAll(tmp);
				}

		}
		
		return paths;
	}
	
	/**
	 * Checks whether for each postcondition in the current activity there exists at least 
	 * one path to a precondition in each next activity
	 * @return true if all postconditions has at least one path to preconditions of all next activities,
	 * false otherwise
	 */
	public boolean hasPathsToNextActivities() {
		
		LinkedList<GraphPath> tmp;
		
		//for all postconditions of current activity find all paths to preconditions of next activity
		for (Predicate predCurrent : getPredicates(PredicateType.Postcondition)) {
			for (Activity act : getNextActivities()) {
				IncidentActivity incAct = (IncidentActivity)act;
				for (Predicate predNext : incAct.getPredicates(PredicateType.Precondition)) {
					tmp = transitionSystem.getPaths(predCurrent, predNext);
					if (tmp.isEmpty()) {
						return false;
					}
				}

			}
		}
		
		return true;
	}
	
	/**
	 * Checks whether the current activity has at least one path from each postcondition
	 * to any precondition in the nextActivity parameter
	 * @param nextActivity the next activity object
	 * @return true if all postconditions of current activity have at least one path to a precondition 
	 * of the next activity. False otherwise
	 */
	public boolean hasPathsToNextActivity(IncidentActivity nextActivity) {
		
		LinkedList<GraphPath> tmp;
		
		//for all postconditions of current activity find all paths to preconditions of next activity
		for (Predicate predCurrent : getPredicates(PredicateType.Postcondition)) {
				for (Predicate predNext : nextActivity.getPredicates(PredicateType.Precondition)) {
					tmp = transitionSystem.getPaths(predCurrent, predNext);
					if (tmp.isEmpty()) {
						return false;
					}
				}
		}
		
		return true;
	}

	/**
	 * Finds all possible paths from the preconditions of the current activity to the preconditions of 
	 * all next activities
	 * @return a HashMap containing all possible paths to next activities as next activity names are used
	 * as keys to store the LinkedList containing the paths
	 */
	public HashMap<String, LinkedList<GraphPath>> getFullPathsToNextActivities() {
		HashMap<String, LinkedList<GraphPath>> paths = new HashMap<String, LinkedList<GraphPath>>();
		
		for(Activity act : getNextActivities()) {
			IncidentActivity incAct = (IncidentActivity)act;
			if(incAct != null) {
				paths.put(act.getName(), getFullPathsToNextActivities(incAct));
			}
			
		}
		
		pathsToNextActivities = paths;
		return paths;
	}
	
	/**
	 * Finds all possible paths from the preconditions of the current activity to the preconditions of 
	 * nextActivity parameter
	 * @param nextActivity the next activity
	 * @return a LinkedList of GraphPath objects that contain all possible paths specified before
	 */
	public LinkedList<GraphPath> getFullPathsToNextActivities(IncidentActivity nextActivity) {
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
		LinkedList<GraphPath> tmpPaths = new LinkedList<GraphPath>();
		LinkedList<GraphPath> intraPaths;
		LinkedList<GraphPath> interNextPaths;
		
		intraPaths = getPathsBetweenPredicates();
		interNextPaths = findPathsToNextActivity(nextActivity);

		//if initial state
		if(previousActivities == null || previousActivities.isEmpty()) {
			for(GraphPath p : intraPaths) {
				if(p.getPredicateDes().getStatesInterSatisfied().contains(p.getEndState())) {
					tmpPaths.add(p);
				}
			}
		} else { //reducing the number of intra paths to ones that match the inter states satisfied
			for(GraphPath p : intraPaths) {
				if(p.getPredicateDes().getStatesInterSatisfied().contains(p.getEndState())
						&& p.getPredicateSrc().getStatesInterSatisfied().contains(p.getStartState())) {
					tmpPaths.add(p);
				}
			}
		}
		
		for(GraphPath pIntra : tmpPaths) {
			for(GraphPath pInter : interNextPaths) {
				if (pIntra.getEndState().compareTo(pInter.getStartState()) == 0) {
					paths.add(pIntra.combine(pInter));
					
				}
			}
		}
		
		return paths;
	}
	
	/*@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!IncidentActivity.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final IncidentActivity other = (IncidentActivity) obj;
	    
	    if(this.name.contentEquals(other.getName())) {
	    	return true;
	    }
	    
	    return false;
	}*/
	
/*	public static IncidentActivity convertActivityToIncidentActivityObject(Activity activity) {
	
		if(activity == null) {
			return null;
		}
		
		return new IncidentActivity(activity);
		
		
		
	}*/
	
	public String toString() {
		
		return name;
	}
}
