package ie.lero.spare.forensics.assessment.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

//import core.brs.parser.BigraphWrapper;
//import core.brs.parser.utilities.JSONTerms;
//import core.instantiation.analysis.TraceMiner;
import cyberPhysical_Incident.Entity;
import environment.EnvironmentDiagram;
import ie.lero.spare.forensics.assessment.brs.parser.BigraphWrapper;
import ie.lero.spare.forensics.assessment.brs.parser.utilities.JSONTerms;
import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.analysis.TraceMiner;

/**
 * A class that manages available monitors. One can add monitors to the manager,
 * or create monitors from available templates. Monitor templates can be created
 * using {@link MonitorTemplateFactory}. Monitor templates can be created using
 * {@link MonitorTemplateDepricated}
 * 
 * @author Faeq
 *
 */
public class MonitorManager {

	// list of monitors in the system
	// the string represents the ID of the monitor in the system model
//	protected static List<String> monitorsList = new LinkedList<String>();

	// holds all available monitors
	// key is monitor ID (unique name), and value is a Monitor object
	protected Map<String, Monitor> monitors;

	// system model
	// can be used to fill the monitors list
	EnvironmentDiagram systemModel;

	// trace miner containing info about the location of states and signature
	TraceMiner miner;

	// values for canMonitor method
	public static final int CAN_MONITOR = 1;
	public static final int UNDETERMINED = 0;
	public static final int CANNOT_MONITOR = -1;
	public static final int NO_MONITORS_AVAILABLE = -2;
	public static final int ERROR = -3;

	public MonitorManager() {

		monitors = new HashMap<String, Monitor>();
	}

	public void setSystemModel(EnvironmentDiagram sysModel) {

		systemModel = sysModel;
	}

	public EnvironmentDiagram getSystemModel() {
		return systemModel;
	}

	public boolean addMonitor(Monitor monitor) {

		if (monitor == null) {
			return false;
		}

		String monitorID = monitor.getMonitorID();

		// create new id
		if (monitorID == null) {
			Random rand = new Random();

			int tries = 1000;

			while (tries > 0) {
				monitorID = "monitor-" + rand.nextInt(10000);

				if (!monitors.containsKey(monitorID)) {
					break;
				}

				tries--;
			}

			if (monitorID == null) {
				return false;
			}

			monitor.setMonitorID(monitorID);
		}

		if (!monitors.containsKey(monitorID)) {
			monitors.put(monitorID, monitor);
		}

		return true;
	}

	public boolean removeMonitor(String monitorID) {

		if (monitorID == null) {
			return false;
		}

		if (monitors.containsKey(monitorID)) {
			monitors.remove(monitorID);
			return true;
		}

		return false;
	}

	public boolean loadFactoryMonitors() {

		// loads monitors defined by templates in the factory

		MonitorTemplateFactory instace = MonitorTemplateFactory.eInstance;

		Map<String, Monitor> factoryMonitors = instace.createAllMonitors();

		if (factoryMonitors != null) {
			monitors.putAll(factoryMonitors);

			return true;
		}

		return false;
	}

	public void setTraceMiner(TraceMiner miner) {

		this.miner = miner;
	}

	public boolean loadFactoryMonitors(TraceMiner miner) {

		// loads monitors defined by templates in the factory

		MonitorTemplateFactory instace = MonitorTemplateFactory.eInstance;

		Map<String, Monitor> factoryMonitors = instace.createAllMonitors();

		if (factoryMonitors != null) {

			for (Entry<String, Monitor> entry : factoryMonitors.entrySet()) {
				Monitor mon = entry.getValue();

				mon.setTraceMiner(miner);

				monitors.put(entry.getKey(), mon);
			}
		}

		this.miner = miner;
		
		return true;
	}

	public Monitor getMonitor(String action) {

		return monitors.get(action);
	}

	public boolean hasMonitors() {

		return !monitors.isEmpty();
	}

	public boolean hasMonitor(String monitorID) {

		return monitors.containsKey(monitorID);
	}

	/**
	 * Returns a copy of the list of the monitors IDs available
	 * 
	 * @return A copy of the list of monitor IDs
	 */
	public List<String> getMonitorsIDs() {

		if (monitors != null) {
			return new LinkedList<String>(monitors.keySet());
		}

		return null;
	}

	public void extractMonitorsFromSystemModel(EnvironmentDiagram sysModel) {

		if (sysModel == null) {
			return;
		}

		systemModel = sysModel;

		// TBI...
		// == this method should look for monitors in the system model and add them to
		// the list of current models

	}

	/**
	 * Determines whether the given trace can be monitored or not. A trace can be
	 * monitored if all its actions can be monitored by at least one monitor
	 * 
	 * @param trace                   A GraphPath object representing the trace
	 * @param actionCannotBeMonitored if given, then it will be filled at the end of
	 *                                the method's execution with action names, in
	 *                                the trace, that can not be monitored, if any
	 * @return an integer indicating the result. In general, a positive integer
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public int canMonitor(GraphPath trace, List<String> actionsCannotBeMonitored) {

		if (trace == null) {
			return ERROR;
		}

		if (actionsCannotBeMonitored == null) {
			actionsCannotBeMonitored = new LinkedList<String>();
		}

		List<String> actions = trace.getTraceActions();
		List<Integer> states = trace.getStateTransitions();
		int canMon = -1;

		for (int i = 0; i < actions.size(); i++) {

			String action = actions.get(i);
			int preState = states.get(i);
			int postState = states.get(i + 1);

//			System.out.println("Can monitor action [" + action + "] with change: pre[" + preState + "] post["
//					+ postState + "]?");

			canMon = canMonitor(action, preState, postState);

			// if there's an issue, then return the issue
			if (canMon != CAN_MONITOR) {
				actionsCannotBeMonitored.add(action);
//				System.out.println("Cannot Monitor");
//				return canMon;
			}

//			System.out.println("Can monitor");
		}

		if (actionsCannotBeMonitored != null && !actionsCannotBeMonitored.isEmpty()) {
			return CANNOT_MONITOR;
		}

		return CAN_MONITOR;
	}

	/**
	 * Determines whether the given trace can be monitored or not. A trace can be
	 * monitored if all its actions can be monitored by at least one monitor
	 * 
	 * @param trace A GraphPath object representing the trace
	 * @return an integer indicating the result. In general, a positive integer
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public int canMonitor(GraphPath trace) {

		if (trace == null) {
			return ERROR;
		}

		List<String> actions = trace.getTraceActions();
		List<Integer> states = trace.getStateTransitions();

		for (int i = 0; i < actions.size(); i++) {

			String action = actions.get(i);
			int preState = states.get(i);
			int postState = states.get(i + 1);

//			System.out.println("Can monitor action [" + action + "] with change: pre[" + preState + "] post["
//					+ postState + "]?");

			int canMon = canMonitor(action, preState, postState);

			// if there's an issue, then return the issue
			if (canMon != CAN_MONITOR) {
//				System.out.println("Cannot Monitor");
				return canMon;
			}

//			System.out.println("Can monitor");
		}

		return CAN_MONITOR;
	}

	/**
	 * Determines whether the monitor with the given ID can monitor the given action
	 * and assetID in the change (pre and post states)
	 * 
	 * @param monitorId Monitor ID that is required to monitor the given action and
	 *                  asset in the change
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @param miner     TraceMiner object that contain info about the folder that
	 *                  contains the states
	 * @return an integer indicating the result. In general, a positive integer
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public int canMonitor(String monitorID, String action, String assetID, int preState, int postState,
			TraceMiner miner) {

		// if there are no monitors then return no monitors available
		if (monitors == null || monitors.isEmpty()) {
			return NO_MONITORS_AVAILABLE;
		}

		// if the monitor id is null then check all monitors
		if (monitorID == null) {
			for (Monitor monitor : monitors.values()) {

				// if the monitor can monitor the given action then, check the states
//				if (action.equals(monitor.getActionMonitored())) {
				boolean canMon = monitor.canMonitor(action, assetID, preState, postState, miner);

				if (canMon) {
					return CAN_MONITOR;
				}
//				}

			}
		}
		// else, check if the monitor that has the given ID can monitor the given
		// action, asset, and change
		else {
			Monitor mon = monitors.get(monitorID);

			// if monitor is not found
			if (mon == null) {
				return UNDETERMINED;
			}

			// check if the monitor can monitor
//			if (action.equals(mon.getActionMonitored())) {
			boolean canMon = mon.canMonitor(action, assetID, preState, postState);

			if (canMon) {
				return CAN_MONITOR;
			}
//			}
		}

		return CANNOT_MONITOR;
	}

	/**
	 * Determines whether the monitor with the given ID can monitor the given action
	 * and assetID in the change (pre and post states)
	 * 
	 * @param monitorId Monitor ID that is required to monitor the given action and
	 *                  asset in the change
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @param miner     TraceMiner object that contain info about the folder that
	 *                  contains the states
	 * @return an integer indicating the result. In general, a positive integer
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public int canMonitor(String monitorID, String action, String assetID, int preState, int postState) {

		return canMonitor(monitorID, action, assetID, preState, postState, miner);

	}

	/**
	 * Determines whether the given action and assetID in the change (pre and post
	 * states) can be monitored by at least 1 monitor
	 * 
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @param miner     TraceMiner object that contain info about the folder that
	 *                  contains the states
	 * @return an integer indicating the result. In general, a positive integer
	 * 
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public int canMonitor(String action, String assetID, int preState, int postState, TraceMiner miner) {

		// if there are no monitors then return no monitors available
		return canMonitor(null, action, assetID, preState, postState, miner);
	}

	/**
	 * Determines whether the given action and assetID in the change (pre and post
	 * states) can be monitored by at least 1 monitor
	 * 
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @return an integer indicating the result. In general, a positive integer
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public int canMonitor(String action, String assetID, int preState, int postState) {

		// if there are no monitors then return no monitors available
		return canMonitor(null, action, assetID, preState, postState, miner);
	}

	/**
	 * Determines whether the given action in the change (pre and post states) can
	 * be monitored by at least 1 monitor
	 * 
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @param miner     TraceMiner object that contain info about the folder that
	 *                  contains the states
	 * @return an integer indicating the result. In general, a positive integer
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public int canMonitor(String action, int preState, int postState, TraceMiner miner) {

		return canMonitor(null, action, null, preState, postState, miner);

	}

	/**
	 * Determines whether the given action in the change (pre and post states) can
	 * be monitored by at least 1 monitor
	 * 
	 * @param action    Action to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @return an integer indicating the result. In general, a positive integer
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public int canMonitor(String action, int preState, int postState) {

		return canMonitor(null, action, null, preState, postState, miner);

	}

	/**
	 * Returns all Monitors that can monitor the given action and assetID in the
	 * change (pre and post states)
	 * 
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @return The list of monitors that are capable of monitoring the given action,
	 *         asset, and change
	 */
	public List<Monitor> getCapableMonitors(String action, String assetID, int preState, int postState) {

		List<Monitor> capableMonitors = new LinkedList<Monitor>();

		for (Monitor monitor : monitors.values()) {

			// if the monitor can monitor the given action then, check the states
			boolean canMon = monitor.canMonitor(action, assetID, preState, postState, miner);

			if (canMon) {
				capableMonitors.add(monitor);
			}

		}

		return capableMonitors;

	}

	/**
	 * Returns all Monitors that can monitor the given action in the change (pre and
	 * post states)
	 * 
	 * @param action    Action to monitor
	 * @param assetID   target asset ID to monitor
	 * @param preState  the state of the system BEFORE the action takes place
	 * @param postState the state of the system AFTER the action takes place
	 * @return The list of monitors that are capable of monitoring the given action
	 *         and change
	 */
	public List<Monitor> getCapableMonitors(String action, int preState, int postState) {

		return getCapableMonitors(action, null, preState, postState);

	}

	/**
	 * Determines whether the given trace can be monitored or not. A trace can be
	 * monitored if all its actions can be monitored by at least one monitor
	 * 
	 * @param trace A GraphPath object representing the trace
	 * @return an integer indicating the result. In general, a positive integer
	 *         indicates a success, a negative indicates a problem occurred.
	 *         Integers range from CAN_MONITOR to CANNOT_MONITOR, with states
	 *         in-between for indicating states where, for example, it is
	 *         UNDETERMINED or NO_MONITORS_AVAILABLE
	 */
	public Map<String, List<Monitor>> getCapableMonitors(GraphPath trace) {

		if (trace == null) {
			return null;
		}

		List<String> actions = trace.getTraceActions();
		List<Integer> states = trace.getStateTransitions();

		Map<String, List<Monitor>> actionsMonitorsMap = new HashMap<String, List<Monitor>>();

		for (int i = 0; i < actions.size(); i++) {

			String action = actions.get(i);
			int preState = states.get(i);
			int postState = states.get(i + 1);

//			System.out.println("Can monitor action [" + action + "] with change: pre[" + preState + "] post["
//					+ postState + "]?");

			List<Monitor> mons = getCapableMonitors(action, preState, postState);

			actionsMonitorsMap.put(action, mons);

		}

		return actionsMonitorsMap;
	}

	/**
	 * Finds an optimal set of monitors that are can monitor the given trace. A
	 * solution can be controlled via given parameters such as isOptimal.
	 * 
	 * @param trace        The trace to find a solution for
	 * @param isOptimal    If true, then an optimal solution is found. If false then
	 *                     it finds all solutions
	 * @param allDifferent if true, then a solution should contain unique monitors
	 *                     for actions. If false, then a solution can use a monitor
	 *                     for more than one action
	 * @param isMinimum    if true, then a minimum cost for a solution is searched.
	 *                     If false, then cost will be ignored
	 * @return A List of MonitorSolution objects, in which each object contains
	 *         information about the solution (e.g., id, a monitor for each action,
	 *         and cost for the solution)
	 */
	public List<MonitorSolution> findSolutionsToMonitorTrace(GraphPath trace, boolean isOptimal, boolean allDifferent,
			boolean isMinimum) {

		if (trace == null) {
			return null;
		}

		Map<String, List<Monitor>> actionsMonitorsMap = getCapableMonitors(trace);

		List<MonitorSolution> solutions = null;

		if (actionsMonitorsMap != null && !actionsMonitorsMap.isEmpty()) {

			// check that all actions have at least 1 monitor
			if (!hasActionAMonitor(actionsMonitorsMap)) {
//				solutions = new LinkedList<MonitorSolution>();
//				MonitorSolution sol = new MonitorSolution();
//				sol.setActionMonitors(actionsMonitorsMap);
				return null;
			}

			MonitorSelectionSolver solver = new MonitorSelectionSolver();

			solutions = solver.solve(actionsMonitorsMap, isOptimal, allDifferent, isMinimum);

		}

		return solutions;

	}

	protected boolean hasActionAMonitor(Map<String, List<Monitor>> actionsMonitorsMap) {

		if (actionsMonitorsMap == null) {
			return false;
		}

		for (List<Monitor> monitors : actionsMonitorsMap.values()) {

			if (monitors == null || monitors.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Finds an optimal set of monitors that are can monitor the given trace. A
	 * solution is characterised by being OPTIMAL; monitors are NOT Unique, i.e. can
	 * be used for different actions; and has minimum cost
	 * 
	 * @param trace The trace to find a solution for
	 * @return A MonitorSolution object containing information about the solution
	 *         (e.g., action and its monitor, total cost)
	 */
	public MonitorSolution findOptimalMonitorsForTrace(GraphPath trace) {

		boolean isOptimal = true;
		boolean allDifferent = false;
		boolean isMinimum = true;

		List<MonitorSolution> solutions = findSolutionsToMonitorTrace(trace, isOptimal, allDifferent, isMinimum);

		if (solutions != null && solutions.size() > 0) {
			return solutions.get(0);
		}

		return null;

	}

	/**
	 * Finds all monitors in the given bigraph wrapper and returns a list of their
	 * IDs. All available monitors are assumed to be set in the MonitorManager class
	 * 
	 * @param bigWrapper
	 * @return A list of monitor IDs that are found in the given BigrahWrapper
	 *         object
	 */
	public List<String> findMonitors(BigraphWrapper bigWrapper) {

		if (bigWrapper == null) {
			return null;
		}

		// === looks for a monitor in the given bigraph wrapper
		// it does this by finding ids for monitors, if ids are available

		if (!hasMonitors()) {
//			System.out.println("Monitor Manager: There are no Monitors");
			return null;
		}

		List<String> monitorIDsFound = new LinkedList<String>();

		for (Entry<Entity, String> entry : bigWrapper.getControlMap().entrySet()) {
			Entity ent = entry.getKey();
			String id = entry.getValue();

			String control = ent.getName();

			// if the control is asset id, then look for child for id
			if (control.equalsIgnoreCase(JSONTerms.CONTROL_ASSET_ID)) {
				List<String> assetIDList = bigWrapper.getContainedEntitiesMap().get(id);

				if (assetIDList != null && !assetIDList.isEmpty()) {
					String assetIDUniqueName = assetIDList.get(0);
					String assetID = bigWrapper.getControl(assetIDUniqueName);
					// if monitor is found, then ad to the list
					if (monitors.containsKey(assetID)) {
						monitorIDsFound.add(assetID);
					}
				}
			}

		}

		return monitorIDsFound;
	}

	/**
	 * Finds all monitors in the given bigraphER expression and returns a list of
	 * their IDs. All available monitors are assumed to be set in the MonitorManager
	 * class
	 * 
	 * @param bigraphERExpression a BigraphER expression
	 * @return A list of monitor IDs that are found in the given BigrahWrapper
	 *         object
	 */
	public List<String> findMonitors(String bigraphERExpression) {

		if (bigraphERExpression == null) {
			return null;
		}

		BigraphWrapper bigWrapper = new BigraphWrapper();
		bigWrapper.parseBigraphERCondition(bigraphERExpression);

		return findMonitors(bigWrapper);
	}

	public void printMonitors() {

		System.out.println("===== MONITOR MANAGER =====\n");

		if (!monitors.isEmpty()) {
			for (Monitor mon : monitors.values()) {
				System.out.println(mon.toString());
			}
		} else {
			System.out.println("There are no monitors");

		}

		System.out.println("============================\n");
	}
}
