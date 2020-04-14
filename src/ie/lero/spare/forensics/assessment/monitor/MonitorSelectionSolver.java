package ie.lero.spare.forensics.assessment.monitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

/**
 * A class that finds a solution(s) to a given set of actions and their
 * potential monitors. It returns solutions as {@link MonitorSolution} objects.
 * 
 * @author Faeq
 *
 */
public class MonitorSelectionSolver {

//	protected List<int[]> optimalSolution;
//	protected int[] optimalSolutionPatternsID;
//	protected int[] optimalSolutionMapsID;
//	protected int optimalSolutionSeverity;
//	protected int[] patternSeverityLevel;

	// input
	Map<String, List<Monitor>> monitors;

	// convert the given map into a map that can be used by the solver
	// key is an integer indicating the position (index) referring to action
	// the value is a list containing monitor indices referring to all monitor that
	// can monitor the given action index
	Map<Integer, List<Integer>> convertedMap;

	// the first index indicates an action and the second is the monitor. The value
	// held is a monitor ID
	int[][] actionMonitorMatrix;
	int[][] actionMonitorCostMatrix;

	// key is a monitor and value is its id
	Map<Monitor, Integer> monitorToIDMap;

	// result containing integer which indicates solution id and list of
	// integers that
	// are the pattern maps
	protected Map<Integer, List<Integer>> allSolutions;

	// list that contains the sum of cost for each solution
	// index is a solution id and the integer value is the cost sum
	protected List<Integer> allSolutionsCost;

	// all solutions. Same as allSolutions map but converted into MonitorSolution
	// list
	List<MonitorSolution> solutionsFound;

	// key is an action and the value is its id
	Map<Integer, String> actionToIDMap;

	// action sequence that correspond to the sequence of monitors in a solution
	List<String> actionsSequence;

	// key is monitor id and value is cost
	Map<Integer, Integer> monitorsCosts;

	// isOptimal
	boolean isOptimal = true;

	// if true then it minimises the cost
	boolean isMinimal = true;

	// if true then it finds different monitors for different actions
	boolean isAllDifferent = true;

	// variables used for finding a solution
	// sum of cost of a solution
	IntVar costSum = null;

	// monitors variables
	IntVar[] monitorsVars = null;

	public MonitorSelectionSolver() {
		convertedMap = new HashMap<Integer, List<Integer>>();
		monitorToIDMap = new HashMap<Monitor, Integer>();
		allSolutions = new HashMap<Integer, List<Integer>>();
		solutionsFound = new LinkedList<MonitorSolution>();
		actionToIDMap = new HashMap<Integer, String>();
		actionsSequence = new LinkedList<String>();
		monitorsCosts = new HashMap<Integer, Integer>();
		allSolutionsCost = new LinkedList<Integer>();

	}

	/**
	 * Finds ALL solutions for the given actions and their monitors
	 * 
	 * @param actionsMonitorsMap A map in which the key is action name and the value
	 *                           is a list of monitors that can monitor the action
	 * @param isOptimal          If true, then an optimal solution is found. If
	 *                           false then it finds all solutions
	 * @param allDifferent       if true, then a solution should contain unique
	 *                           monitors for actions. If false, then a solution can
	 *                           use a monitor for more than one action
	 * @param isMinimum          if true, then a minimum cost for a solution is
	 *                           searched. If false, then cost will be ignored
	 * @return A List of MonitorSolution objects, in which each object contains
	 *         information about the solution (e.g., id, a monitor for each action,
	 *         and cost for the solution)
	 */
	public List<MonitorSolution> solve(Map<String, List<Monitor>> actionsMonitorsMap, boolean isOptimal,
			boolean allDifferent, boolean isMinimum) {

		if (actionsMonitorsMap == null || actionsMonitorsMap.isEmpty()) {
			return null;
		}

		// reset variables
		reset();

		this.isOptimal = isOptimal;
		this.isAllDifferent = allDifferent;
		this.isMinimal = isMinimum;

		this.monitors = actionsMonitorsMap;

		// ====create ids for actions and monitors
		int actionID = 0;
		int monitorID = 0;

		for (Entry<String, List<Monitor>> entry : monitors.entrySet()) {

			String action = entry.getKey();
			List<Monitor> actionMonitors = entry.getValue();

			List<Integer> actionMonitorIDs = new LinkedList<Integer>();

			actionToIDMap.put(actionID, action);

			convertedMap.put(actionID, actionMonitorIDs);

			actionID++;

			// monitor id and cost
			for (Monitor mon : actionMonitors) {
				// if the monitor has no id, then create one and then add the id to the list of
				// monitor ids for the current action
				if (!monitorToIDMap.containsKey(mon)) {
					monitorToIDMap.put(mon, monitorID);
					actionMonitorIDs.add(monitorID);

					// cost
					monitorsCosts.put(monitorID, (int) mon.getCost());

					monitorID++;
					// if the monitor id already exists, then just add it to the list
				} else {
					actionMonitorIDs.add(monitorToIDMap.get(mon));

					// cost
					monitorsCosts.put(monitorID, (int) mon.getCost());
				}

			}

		}

		// create action sequence corresponding to the sequence of monitors in a solution
		for (Integer actID : convertedMap.keySet()) {
			actionsSequence.add(actionToIDMap.get(actID));
		}

		// get action-monitor matrix
		generateActionMonitorMatrix();

		// find solutions
		// key is solution id, value is the id of the monitor

		if (isOptimal) {
			// optimal solution
			findOptimalSolution();
		} else {
			// all possible solutions
			findSolutions();
		}

		getFoundSolutions();

		return solutionsFound;
	}

	/**
	 * Finds an OPTIMAL solution for the given actions and their monitors map. The
	 * solution that has Unique monitors for actions, and minimum cost
	 * 
	 * @param actionsMonitorsMap A map in which the key is action name and the value
	 *                           is a list of monitors that can monitor the action
	 * @return An object of MonitorSolution containing information about the
	 *         solution (e.g., id, a monitor for each action, and cost for the
	 *         solution)
	 */
	public MonitorSolution solve(Map<String, List<Monitor>> actionsMonitorsMap) {

		isOptimal = true;
		isAllDifferent = true;
		isMinimal = true;

		List<MonitorSolution> solutions = solve(actionsMonitorsMap, isOptimal, isAllDifferent, isMinimal);

		if (solutions != null && solutions.size() > 0) {
			return solutions.get(0);
		}

		return null;
	}

	protected void reset() {
		allSolutions.clear();
		allSolutionsCost.clear();
		solutionsFound.clear();
		convertedMap.clear();
		monitorToIDMap.clear();
		actionToIDMap.clear();
		actionsSequence.clear();
		monitorsCosts.clear();
		actionMonitorMatrix = null;

	}

	/**
	 * Generates a two-dimensional array that the first index indicates an actionID
	 * (or its position in the sequence of actions, while the second index indicates
	 * the monitor ID. The value given for a particular position is a monitor ID
	 */
	protected void generateActionMonitorMatrix() {

		int numOfActions = convertedMap.size();

		// monitors
		actionMonitorMatrix = new int[numOfActions][];

		// monitors cost
		actionMonitorCostMatrix = new int[numOfActions][];

		int indexAction = 0;

		for (Entry<Integer, List<Integer>> entry : convertedMap.entrySet()) {

			List<Integer> list = entry.getValue();

			// monitor
			actionMonitorMatrix[indexAction] = new int[list.size()];

			// cost
			actionMonitorCostMatrix[indexAction] = new int[list.size()];

			for (int indexMonitor = 0; indexMonitor < list.size(); indexMonitor++) {

				int monID = list.get(indexMonitor);

				actionMonitorMatrix[indexAction][indexMonitor] = monID;
				actionMonitorCostMatrix[indexAction][indexMonitor] = monitorsCosts.get(monID);
			}

			indexAction++;
		}

	}

	/**
	 * returns all solutions found
	 * 
	 * @return A map in which the key is a solution number (or ID) and the value is
	 *         a list of the monitors
	 */
	public List<MonitorSolution> getFoundSolutions() {

		if (!solutionsFound.isEmpty()) {
			return solutionsFound;
		}

		for (Entry<Integer, List<Integer>> solution : allSolutions.entrySet()) {

			int solID = solution.getKey();

			MonitorSolution monSol = getSolution(solID);

			solutionsFound.add(monSol);
		}

		return solutionsFound;
	}

	/**
	 * Creates a MonitorSolution object using the given solution ID
	 * 
	 * @param solutionID a solution id to use
	 * @return A MonitorSolution object containing information about the solution
	 *         (id, monitors and their actions, and cost)
	 */
	protected MonitorSolution getSolution(int solutionID) {

		List<Integer> monitorIDs = allSolutions.get(solutionID);

		if (monitorIDs == null) {
			return null;
		}

		int actionIndex = 0;

		// new solution
		MonitorSolution monSol = new MonitorSolution();

		// set id
		monSol.setSolutionID(solutionID);

		// get action name and monitor for the given monitor ID
		for (Integer monID : monitorIDs) {
			// **sequence of monitors indicate sequence of actions

			String actionName = actionsSequence.get(actionIndex);
			Monitor mon = getMonitor(monID);

			monSol.addActionMonitor(actionName, mon);

			actionIndex++;
		}

		// set cost
		if (allSolutionsCost.size() > solutionID) {
			monSol.setCost(allSolutionsCost.get(solutionID));
		}

		return monSol;
	}

	protected Monitor getMonitor(int monitorID) {

		for (Entry<Monitor, Integer> entry : monitorToIDMap.entrySet()) {
			if (entry.getValue().equals(monitorID)) {
				return entry.getKey();
			}
		}

		return null;
	}

	protected Model createSolverModel() {

		Model model = null;
		monitorsVars = null;
		costSum = null;

		int numOfActions = actionsSequence.size();

		// actual severity array, assuming its embedded in the argument
		// variable
		int sumCost = 0;

		for (Integer monitorCost : monitorsCosts.values()) {
			sumCost += monitorCost;
		}

		if (sumCost == 0) {
			sumCost = 1;
		}

		// =============look for
		// solution==========================================
//		while (currentNumOfMonitors > 0) {

		model = new Model("Action-Monitor Model");

		// ============Defining Variables======================//
		monitorsVars = new IntVar[numOfActions];
		IntVar[] monitorCost = new IntVar[numOfActions];
		int[] coeffs = null;

		// monitor cost coeffs set to 1 if cost is needed
		if (isMinimal) {
			// used to update severity values
			coeffs = new int[numOfActions];
			Arrays.fill(coeffs, 1); // coeff is 1

			// defines cost for a solution
			costSum = model.intVar("cost_sum", 0, sumCost);
		}

		// create monitor variables
		for (int i = 0; i < numOfActions; i++) {
			monitorsVars[i] = model.intVar("monitor-" + i, actionMonitorMatrix[i]);

			// cost
			if (isMinimal) {
				monitorCost[i] = model.intVar("monitor_" + i + "_cost", actionMonitorCostMatrix[i]);
			}
		}

		// ============Defining Constraints======================//
		// ===1- All different (if Alldifferent is true)
		// ===2- A monitor in position X should match to a monitor that already can
		// monitor the action in position X
		// ===3- Cost is set (if minimise is true)

		// 1- all different
		if (isAllDifferent) {
			model.allDifferent(monitorsVars).post();
		}

		// 2- A monitor in position X should match to a monitor that already can monitor
		// the action in position X
		List<Constraint> consList = new LinkedList<Constraint>();
		// essential: at least 1 map for each pattern
		for (int i = 0; i < monitorsVars.length; i++) {
			for (int j = 0; j < actionMonitorMatrix[i].length; j++) {

				// pattern map should be a one of the found maps

				Constraint correctActionMonitor = model.element(monitorsVars[i], actionMonitorMatrix[i],
						model.intVar(j));

				consList.add(correctActionMonitor);

				// the severity of the pattern should equal to the pattern
				// severity specified in the argument
				if (isMinimal) {
					model.ifThen(correctActionMonitor,
							model.arithm(monitorCost[i], "=", monitorsCosts.get(actionMonitorMatrix[i][j])));
				}
			}

			Constraint[] res = consList.stream().toArray(size -> new Constraint[size]);
			model.or(res).post();
			consList.clear();
		}

		// 3- cost
		if (isMinimal) {
			model.scalar(monitorCost, coeffs, "=", costSum).post();
			model.setObjective(Model.MINIMIZE, costSum);
		}

		return model;
	}

	protected Map<Integer, List<Integer>> findSolutions() {

		// ============Finding solutions======================//
		List<Solution> solutions = null;
		Solver solver = null;
//		IntVar costSum = null;
//		IntVar[] monitors = null;

		Model model = createSolverModel();

		solver = model.getSolver();
		solutions = new LinkedList<Solution>();

		while (solver.solve()) {

			// add the current solution to the solutions list
			solutions.add(new Solution(model).record());
		}

		analyseSolutions(solutions);

		return allSolutions;
	}

	protected Map<Integer, List<Integer>> findOptimalSolution() {

		// ============Finding solutions======================//
		List<Solution> solutions = new LinkedList<Solution>();
		Solver solver = null;

		Model model = createSolverModel();

		solver = model.getSolver();
		Solution solution;

		if (costSum == null) {
			costSum = model.intVar(0);
		}

		solution = solver.findOptimalSolution(costSum, Model.MINIMIZE);

		solutions.add(solution);

		analyseSolutions(solutions);

		return this.allSolutions;
	}

	protected Map<Integer, List<Integer>> analyseSolutions(List<Solution> solutions) {

		for (int j = 0; j < solutions.size(); j++) {

			Solution sol = solutions.get(j);

			if (sol == null) {
				continue;
			}

			List<Integer> solVals = new LinkedList<Integer>();

			for (int i = 0; i < monitorsVars.length; i++) {
				solVals.add(sol.getIntVal(monitorsVars[i]));
			}

			// add to solutions
			this.allSolutions.put(j, solVals);

			// add severity
			if (costSum != null) {
				allSolutionsCost.add(sol.getIntVal(costSum));
			}

		}

		return allSolutions;
	}

}
