package ie.lero.spare.pattern_instantiation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import cyberPhysical_Incident.Activity;

public class IncidentPath {

	LinkedList<HashMap<String, LinkedList<GraphPath>>> distinctPaths;
	PredicateHandler predicateHandler;
	LinkedList<GraphPath> incidentPath;

	public IncidentPath() {
		distinctPaths = new LinkedList<HashMap<String, LinkedList<GraphPath>>>();
		predicateHandler = null;
		incidentPath = new LinkedList<GraphPath>();

	}

	public IncidentPath(PredicateHandler pred) {
		distinctPaths = new LinkedList<HashMap<String, LinkedList<GraphPath>>>();
		predicateHandler = pred;
		incidentPath = new LinkedList<GraphPath>();
	}

	public void generateDistinctPaths() {
		LinkedList<Activity> activities = new LinkedList<Activity>();
		LinkedList<Activity> visitedActivities = new LinkedList<Activity>();
		IncidentActivity tmp;
		// LinkedList<HashMap<String, LinkedList<GraphPath>>> paths = new
		// LinkedList<HashMap<String,LinkedList<GraphPath>>>();

		IncidentActivity initialActivity = (IncidentActivity)predicateHandler.getInitialActivity();
		activities.add(initialActivity);

		while (!activities.isEmpty()) {
			tmp = (IncidentActivity)activities.pop();

			if (tmp != null) {

				// check if visited before
				if (visitedActivities.contains(tmp)) {
					continue;
				}
				distinctPaths.add(tmp.getFullPathsToNextActivities());
				activities.addAll(tmp.getNextActivities());
				
				visitedActivities.add(tmp);
			}

		}

	}

	public void combineNeighbourActivities(IncidentActivity activitySrc, IncidentActivity activityDes) {

		// not done
		//
		//
		HashMap<String, LinkedList<GraphPath>> pathSrc = activitySrc.getPathsToNextActivities();
		HashMap<String, LinkedList<GraphPath>> pathDes = activitySrc.getPathsToNextActivities();
		HashMap<String, LinkedList<GraphPath>> pathRes = new HashMap<String, LinkedList<GraphPath>>();
		IncidentActivity actTmp;

		for (String actName : pathSrc.keySet()) {
			for (GraphPath pa : pathSrc.get(actName)) {
				for (Activity act : activitySrc.getNextActivities()) {
					IncidentActivity incAct = (IncidentActivity)act;
					if (act.getName().contentEquals(actName)) {
						pathDes = incAct.getPathsToNextActivities();
						actTmp = incAct;
						break;
					}
				}
				// pathRes.put(actName+","+actTmp.getName(), value)
			}
		}

	}

	public PredicateHandler getPredicateHandler() {
		return predicateHandler;
	}

	public void setPredicateHandler(PredicateHandler predicateHandler) {
		this.predicateHandler = predicateHandler;
	}

	public LinkedList<HashMap<String, LinkedList<GraphPath>>> getDistinctPaths() {
		return distinctPaths;
	}

	public void setDistinctPaths(LinkedList<HashMap<String, LinkedList<GraphPath>>> distinctPaths) {
		this.distinctPaths = distinctPaths;
	}

	/**
	 * From each activity a set of state transitions are combined with the next
	 * set of the next activity. An assumption made which is that all activities
	 * are sequentionally ordered (e.g., act1 -> act2 -> act3)
	 * 
	 * @return GraphPath object that contains the state transitions from initial
	 *         activity to the final
	 */
//	public GraphPath getRandomPath() {
//
//		if (distinctPaths == null || distinctPaths.size() == 0) {
//			return null;
//		}
//
//		GraphPath p = new GraphPath();
//		GraphPath p2 = new GraphPath();
//		Random rand = new Random();
//		int tries = 0;
//		boolean isPathSelected = false;
//
//		LinkedList<GraphPath> tmpPath = distinctPaths.get(0)
//				.get(predicateHandler.getInitialActivity().getNextActivities()
//						.get(rand.nextInt(predicateHandler.getInitialActivity().getNextActivities().size())).getName());
//
//		p2 = tmpPath.get(rand.nextInt(tmpPath.size()));
//
//		for (int i = 1; i < distinctPaths.size(); i++) {
//			for (LinkedList<GraphPath> pa : distinctPaths.get(i).values()) {
//				while (!isPathSelected && tries < 10000) {
//					p = pa.get(rand.nextInt(pa.size()));
//					if (p2.getStateTransitions().size() > 0
//							&& p2.getStateTransitions().getLast().compareTo(p.getStateTransitions().getFirst()) == 0) {
//						p2 = p2.combine(p);
//						isPathSelected = true;
//					} else if (p2.getStateTransitions().size() == 0) {
//						p2 = p2.combine(p);
//					} else {
//						tries++;
//					}
//				}
//				isPathSelected = false;
//				tries = 0;
//				break;
//			}
//		}
//
//		return p2;
//	}

	/**
	 * From each activity a set of state transitions are combined with the next
	 * set of the next activity. An assumption made which is that all activities
	 * are sequentionally ordered (e.g., act1 -> act2 -> act3)
	 * 
	 * @return GraphPath object that contains the state transitions from initial
	 *         activity to the final
	 */
	public LinkedList<GraphPath> getAllPaths() {

		LinkedList<Activity> activities = new LinkedList<Activity>();
		LinkedList<IncidentActivity> visitedActivities = new LinkedList<IncidentActivity>();
		IncidentActivity tmp;
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
		
		LinkedList<GraphPath> paths2 = new LinkedList<GraphPath>();
		IncidentActivity initialActivity = (IncidentActivity)predicateHandler.getInitialActivity();

		for (GraphPath pa : initialActivity.getPathsToNextActivities()
				.get(initialActivity.getNextActivities().get(0).getName())) {
			paths.add(pa);
		}

		activities.add(initialActivity.getNextActivities().get(0));

		while (!activities.isEmpty()) {
			tmp = (IncidentActivity)activities.pop();

			if (tmp != null) {

				// check if visited before
				if (visitedActivities.contains(tmp)) {
					continue;
				}

				if (tmp.getNextActivities() != null && tmp.getNextActivities().size() > 0) {
					for (GraphPath pNxt : tmp.getPathsToNextActivities()
							.get(tmp.getNextActivities().get(0).getName())) {
						for (GraphPath pCurrent : paths) {
							// checks whether the end state of the current transition is
							// the same for the next transition
							if (pCurrent.getStateTransitions().getLast() 
									.compareTo(pNxt.getStateTransitions().getFirst()) == 0)
								paths2.add(pCurrent.combine(pNxt));
	
						}
					}
					if (!paths2.isEmpty()) {
						paths.clear();
						paths.addAll(paths2);
						paths2.clear();
					} else { // it means no match between current and next
								// activities transitions happened
						// i.e. there are no paths to next activity
						System.out.println(tmp.getName() + " has no transitions to next activity(ies)");
					}
				}

				if (tmp.getNextActivities() != null && tmp.getNextActivities().size() > 0) {
					activities.add(tmp.getNextActivities().get(0));
				}

				/*
				 * for(IncidentActivity act : tmp.getNextActivities()) {
				 * activities.add(act); }
				 */

				visitedActivities.add(tmp);
			}

		}

		// for the last activity predicates
		if (predicateHandler.getFinalActivity() != null) {
			for (GraphPath pNxt : ((IncidentActivity)predicateHandler.getFinalActivity()).getPathsBetweenPredicates()) {
				for (GraphPath pCurrent : paths) {
					// checks whether the end state of the current transition is
					// the same for the next transition
					if (pCurrent.getStateTransitions().getLast()
							.compareTo(pNxt.getStateTransitions().getFirst()) == 0) {
						paths2.add(pCurrent.combine(pNxt));
					}
				}
			}
			if (!paths2.isEmpty()) {
				paths.clear();
				paths.addAll(paths2);
				paths2.clear();
			}
		}

		if (hasDuplicates(paths)) {
			return removeDuplicates(paths);
		}

		return paths;
	}

	public LinkedList<GraphPath> removeDuplicates(LinkedList<GraphPath> list) {

		if (list == null || list.isEmpty()) {
			return list;
		}

		LinkedList<GraphPath> result = new LinkedList<GraphPath>();
		;

		result.addAll(list);

		for (GraphPath p : list) {
			result.remove(p);

			while (result.contains(p)) {
				result.remove(result.indexOf(p));
			}

			result.add(p);
		}

		return result;
	}

	public boolean hasDuplicates(LinkedList<GraphPath> list) {

		if (list == null || list.isEmpty()) {
			return false;
		}

		LinkedList<GraphPath> tmp = new LinkedList<GraphPath>();

		tmp.addAll(list);

		for (GraphPath p1 : list) {
			tmp.remove(p1);
			if (tmp.contains(p1)) {
				return true;
			}
			tmp.add(p1);
		}

		return false;
	}

	public void printList(LinkedList<GraphPath> list) {

		System.out.println("size: " + list.size());
		for (GraphPath pe : list) {
			System.out.println(pe);
		}
	}

//	public GraphPath getPath(GraphPath initialPath) {
//
//		if (distinctPaths == null || distinctPaths.size() == 0) {
//			return null;
//		}
//
//		GraphPath p = new GraphPath();
//		GraphPath p2 = new GraphPath();
//		Random rand = new Random();
//		int tries = 0;
//		boolean isPathSelected = false;
//
//		p2 = initialPath;
//
//		for (int i = 1; i < distinctPaths.size(); i++) {
//			for (LinkedList<GraphPath> pa : distinctPaths.get(i).values()) {
//				while (!isPathSelected && tries < 10000) {
//					p = pa.get(rand.nextInt(pa.size()));
//					if (p2.getStateTransitions().size() > 0
//							&& p2.getStateTransitions().getLast().compareTo(p.getStateTransitions().getFirst()) == 0) {
//						p2 = p2.combine(p);
//						isPathSelected = true;
//					} else if (p2.getStateTransitions().size() == 0) {
//						p2 = p2.combine(p);
//					} else {
//						tries++;
//					}
//				}
//				isPathSelected = false;
//				tries = 0;
//				break;
//			}
//		}
//
//		return p2;
//	}

//	public LinkedList<GraphPath> getRandomIncidentPath() {
//
//		if (distinctPaths == null || distinctPaths.size() == 0) {
//			return null;
//		}
//
//		GraphPath p = new GraphPath();
//		// GraphPath p2 = new GraphPath();
//		Random rand = new Random();
//		int tries = 0;
//		boolean isPathSelected = false;
//		LinkedList<GraphPath> initials = new LinkedList<GraphPath>();
//		// get random initial path
//		/*
//		 * while (!isPathSelected && tries < 10000) {
//		 * 
//		 * }
//		 */
//
//		for (LinkedList<GraphPath> ls : ((IncidentActivity)predicateHandler.getInitialActivity()).getPathsToNextActivities().values()) {
//			initials.add(getPath(ls.get(rand.nextInt(ls.size()))));
//		}
//
//		for (int j = 0; j < initials.size(); j++) {
//			for (int i = 1; i < distinctPaths.size(); i++) {
//				for (LinkedList<GraphPath> pa : distinctPaths.get(i).values()) {
//
//					while (!isPathSelected && tries < 10000) {
//						p = pa.get(rand.nextInt(pa.size()));
//						if (initials.get(j).getStateTransitions().getLast()
//								.compareTo(p.getStateTransitions().getFirst()) == 0) {
//							initials.add(initials.get(j).combine(pa.get(rand.nextInt(pa.size()))));
//							isPathSelected = true;
//						} else {
//							tries++;
//						}
//					}
//					isPathSelected = false;
//					tries = 0;
//					// break;
//				}
//			}
//
//		}
//
//		for (GraphPath pa : incidentPath) {
//			System.out.println(pa);
//		}
//		return incidentPath;
//	}
}
