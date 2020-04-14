package ie.lero.spare.forensics.assessment.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A class that represents a solution found by the
 * {@link MonitorSelectionSolver}. A solution contains informations such as
 * solution ID; a map in which the key is action name and value is the monitor
 * that can monitor the action; and cost
 * 
 * @author Faeq
 *
 */
public class MonitorSolution {

	// solution id
	protected int solutionID;

	// map in which the key is the action name, and value is the monitor that can
	// monitor that action
	protected Map<String, Monitor> actionMonitors;

	// solution cost
	int cost;

	public MonitorSolution() {
		actionMonitors = new HashMap<String, Monitor>();
	}

	public int getSolutionID() {
		return solutionID;
	}

	public void setSolutionID(int solutionID) {
		this.solutionID = solutionID;
	}

	public Map<String, Monitor> getActionMonitors() {
		return actionMonitors;
	}

	public void setActionMonitors(Map<String, Monitor> actionMonitors) {
		this.actionMonitors = actionMonitors;
	}

	public int getCost() {

		if (cost == 0) {
			cost = getCalculatedCost();
		}

		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void addActionMonitor(String actionName, Monitor monitor) {

		if (actionName == null || actionName.isEmpty()) {
			return;
		}

		actionMonitors.put(actionName, monitor);
	}

	public boolean removeActionMonitor(String actionName) {

		if (actionName == null || actionName.isEmpty()) {
			return false;
		}

		if (actionMonitors.containsKey(actionName)) {
			actionMonitors.remove(actionName);
			return true;
		}

		return false;
	}

	protected int getCalculatedCost() {

		int cost = 0;

		if (actionMonitors != null) {
			for (Monitor mon : actionMonitors.values()) {
				cost += mon.getCost();
			}
		}

		return cost;
	}

	public Monitor getMonitor(String action) {

		if (action == null || action.isEmpty()) {
			return null;
		}

		return actionMonitors.get(action);
	}

	/**
	 * Returns all action names that the given monitor can monitor
	 * 
	 * @param monitor Monitor object
	 * @return List of action names that the given monitor can monitor in this
	 *         solution
	 */
	public List<String> getActions(String monitorID) {

		if (monitorID == null) {
			return null;
		}

		List<String> actions = new LinkedList<String>();

		for (Entry<String, Monitor> entry : actionMonitors.entrySet()) {
			Monitor mon = entry.getValue();
			String action = entry.getKey();

			if (monitorID.equals(mon.getMonitorID())) {
				actions.add(action);
			}

		}

		return actions;
	}

	/**
	 * Returns all action names that the given monitor can monitor
	 * 
	 * @param monitor Monitor object
	 * @return List of action names that the given monitor can monitor in this
	 *         solution
	 */
	public List<String> getActions(Monitor monitor) {

		if (monitor == null) {
			return null;
		}

		List<String> actions = new LinkedList<String>();

		String monID = monitor.getMonitorID();

		// if the monitor has an id then search using id. Otherwise, search using the
		// object itself
		if (monID != null) {

			actions = getActions(monID);

		} else {
			for (Entry<String, Monitor> entry : actionMonitors.entrySet()) {
				Monitor mon = entry.getValue();
				String action = entry.getKey();

				if (monitor == mon) {
					actions.add(action);
				}

			}
		}

		return actions;
	}

	public String toString() {

		StringBuilder bldr = new StringBuilder();

		String newLine = System.getProperty("line.separator");

		// id and cost
		bldr.append("Solution ID: ").append(solutionID).append(" Total-Cost: ").append(getCost()).append(newLine)
				.append(newLine);

		// solution (e.g., *Action: action1 ^Monitor: monitor1 (cost: 43)
		bldr.append("{ActionName ==> MonitorID (cost)}").append(newLine);

		for (Entry<String, Monitor> entry : actionMonitors.entrySet()) {
			String actionName = entry.getKey();
			Monitor monitor = entry.getValue();

			bldr.append(actionName).append(" ==> ").append(monitor.getMonitorID()).append(" (")
					.append(monitor.getCost()).append(")").append(newLine);
		}

		return bldr.toString();
	}

	public void print() {

		String solStr = toString();

		System.out.println("=====================================================");

		System.out.println(solStr);

		System.out.println("=====================================================");
	}

}
