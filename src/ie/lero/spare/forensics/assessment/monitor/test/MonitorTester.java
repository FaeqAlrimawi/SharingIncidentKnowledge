package ie.lero.spare.forensics.assessment.monitor.test;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ie.lero.spare.forensics.assessment.monitor.MonitorManager;
import ie.lero.spare.forensics.assessment.monitor.MonitorTemplateFactory;
//import core.instantiation.analysis.TraceMiner;
//import core.monitor.MonitorManager;
//import core.monitor.MonitorSolution;
//import core.monitor.MonitorTemplateFactory;
import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.analysis.TraceMiner;

public class MonitorTester {

	TraceMiner miner;

	public void setTraceMiner(TraceMiner traceMiner) {
		miner = traceMiner;
	}

	/**
	 * A functionality to test how to identify which part of a system is being
	 * affected by an action One way to do this is by instantiating the action with
	 * IDs. Add IDs to the controls/classes of the action redex
	 * 
	 * @param args
	 */
	protected void testIdentificationOfMatchID() {

		if (miner == null) {
			System.err.println("TraceMiner object is null");
			return;
		}

		// === Add monitor templates to Factory (optional)
		String monitorTempType = "MotionSensor";
		String actionToMonitor = "Move";
		String monitorExpression = "Room{con}.Visitor | Room.{con}.MotionSensor";

		MonitorTemplateFactory instance = MonitorTemplateFactory.eInstance;

		String id = instance.createTemplate(monitorTempType, actionToMonitor, monitorExpression);

		// === create a monitor manager which can be used to find monitors
		MonitorManager mngr = new MonitorManager();

		// one can add monitors
//		mngr.addMonitor(mon);

		// or one can just load monitors defined by the factory
		mngr.loadFactoryMonitors();

		mngr.setTraceMiner(miner);

		mngr.printMonitors();

		// ===test if the monitor can monitor the specified action pre and post states
		// in a trace
		String actionMonitored = "VisitorEnterRoom";
		String targetAssetID = "Office_T24";

		int preState = 1;
		int postState = 237;

		/*
		 * === This checks whether the monitor with the given ID (monitorID) can monitor
		 * the target with the given ID (targetAssetID) when the action takes place
		 * through the pre and post states. can monitor is evaluated by checking whether
		 * the monitor's partial-state is satisfied in the post-state more than in the
		 * pre-state.
		 */
//		boolean isMonitorable = mon.canMonitor(monitorID, targetAssetID, preState, postState);

		/*
		 * === This checks whether the monitor with the given ID (monitorID) can monitor
		 * an asset with the given target type, when the action takes place.
		 */
//		boolean isMonitorable = mon.canMonitor(monitorID, null, preState, postState);

		/*
		 * === This checks if the monitor can monitor the target with the given ID
		 * (targetAssetID), when the action takes place.
		 */
//		boolean isMonitorable = mon.canMonitor(targetAssetID, preState, postState);

		/*
		 * === This checks if the monitor can monitor the set target, when the action
		 * takes place.
		 */
//		boolean isMonitorable = mon.canMonitor(targetAssetID, preState, postState);

		int monitorResult = 11;

		// == checking if a trace can be monitored
		GraphPath trace = miner.getTrace(1);

		if (trace != null) {
			System.out.println("Can monitor trace[" + trace.getInstanceID() + "] (" + trace.toSimpleString() + ")?");

			List<String> unmonitoredActions = new LinkedList<String>();

			monitorResult = mngr.canMonitor(trace, unmonitoredActions);

//			MonitorSolution sol = mngr.findOptimalMonitorsForTrace(trace);
////			
//			System.out.println("Sol: " + sol);

			System.out.println("Unmonitored Actions in the trace: " + unmonitoredActions);
		}

		// == checking if a single action can be monitored
//		System.out.println("Can monitor action [" + actionMonitored + "] with change: pre[" + preState + "] post["
//				+ postState + "]?");
//		monitorResult = mngr.canMonitor(actionMonitored, preState, postState);

		switch (monitorResult) {
		case MonitorManager.CAN_MONITOR:
			System.out.println("Yes! can monitor");
			break;

		case MonitorManager.CANNOT_MONITOR:
			System.out.println("NO! cannot monitor");
			break;

		case MonitorManager.NO_MONITORS_AVAILABLE:
			System.out.println("NO monitors available to monitor the given action");
			break;

		case MonitorManager.UNDETERMINED:
			System.out.println("Cannot determine");
			break;

		case MonitorManager.ERROR:
			System.out.println("Error occurred");
			break;

		default:
			break;
		}

	}

	public static void main(String[] args) {

		String basicPath = "ie/lero/spare/forensics/assessment/monitor/example/";

		String ltsLocationStr = basicPath + "states";
		String ltsLocationExternalStr = basicPath + "states5K_reduced";

		String bigFileStr = basicPath + "systemBigraphER.big";
		String bigFileExternalStr = basicPath + "lero_uniqueAssetID.big";

		String tracesFilePath = basicPath + "traces_reduced_5k.json";

		URL ltsLocation = MonitorTester.class.getClassLoader().getResource(ltsLocationExternalStr);
		URL bigFileLocation = MonitorTester.class.getClassLoader().getResource(bigFileExternalStr);
		URL tracesURL = MonitorTester.class.getClassLoader().getResource(tracesFilePath);

		String LTS = ltsLocationExternalStr;
		String bigFile = bigFileExternalStr;
		String traces = tracesFilePath;

		// LTS
		if (ltsLocation != null) {
			LTS = ltsLocation.getPath();
		} else {
			System.err.println("LTS location is not found");
			return;
		}

		// bigrapher file
		if (bigFileLocation != null) {
			bigFile = bigFileLocation.getPath();
		} else {
			System.err.println("Bigapher file is not found");
			return;
		}

		// traces
		if (tracesURL != null) {
			traces = tracesURL.getPath();
		} else {
			System.err.println("Traces file is not found");
			return;
		}

		TraceMiner miner = new TraceMiner();

		miner.setBigraphERFile(bigFile);
		miner.setStatesFolder(LTS);
		miner.loadTracesFromFile(traces);

		MonitorTester tester = new MonitorTester();

		tester.setTraceMiner(miner);

//		tester.testReactionRuleMatching();

		tester.testIdentificationOfMatchID();
	}

}
