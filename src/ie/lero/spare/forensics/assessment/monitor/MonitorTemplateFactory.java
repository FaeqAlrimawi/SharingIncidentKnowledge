package ie.lero.spare.forensics.assessment.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cyberPhysical_Incident.BigraphExpression;

public class MonitorTemplateFactory {

	public static final MonitorTemplateFactory eInstance = new MonitorTemplateFactory();

	protected Map<String, MonitorTemplate> templates;

	protected MonitorTemplateFactory() {
		createTemplates();
	}

	protected void createTemplates() {

		templates = new HashMap<String, MonitorTemplate>();

		// create templates
		String monitorType = null;
		String targetType = null;
		String action = null;
		String stateToMonitor = null;

		// === CCTV template
		monitorType = "CCTV";
		targetType = "Room";
		List<String> actions = new LinkedList<String>();
		actions.add("VisitorEnterRoom");
//		actions.add("TurnOnSmartTV");
		stateToMonitor = "Hallway{hallway}.(id | CCTV{ipNet}) | Room{hallway}.(Visitor.id)";

		createTemplate(monitorType, actions, targetType, stateToMonitor);

		// monitor data sent to a bus network.
		// Monitor type is DigitalProcess
		// the monitor can monitor the busnetwork if it can get a copy of the data
		// received by the busnetwork, then analyse it
		monitorType = "DigitalProcess";
		targetType = "BusNetwork";
		action = "SendData";
		stateToMonitor = "BusNetwork{bus}.Data | DigitalProcess{bus}.Data";

		createTemplate(monitorType, action, targetType, stateToMonitor);

		// Packet sniffer
		monitorType = "NetworkPacketSniffer";
		targetType = "BusNetwork";
		action = "SendData";
		stateToMonitor = "BusNetwork{net}.DataPacket | NetworkPacketSniffer{net}.DataPacket";

		createTemplate(monitorType, action, targetType, stateToMonitor);

		// motion sensor
		monitorType = "MotionSensor";
		targetType = "BusRoom";
		action = "VisitorEnterRoom";
		stateToMonitor = "Hallway{hallway}.(id | MotionSensor) | Room{hallway}.(Visitor.id)";

		createTemplate(monitorType, action, targetType, stateToMonitor);

		// logger
		// Packet sniffer
		monitorType = "SoftwareLogger";
		targetType = "Software";
		action = "AccessSoftware";
		stateToMonitor = "Software{soft}.Data | SoftwareLogger{soft}.Data";

		createTemplate(monitorType, action, targetType, stateToMonitor);

	}

	/**
	 * Creates a new monitor template with the given parameters.
	 * 
	 * @param monitorType     The type of the monitor e.g., CCTV
	 * @param actionMonitored the action name the monitor template can monitor
	 * @param stateToMonitor  and the state to monitor expressed as a BigraphER
	 *                        expression
	 * @return {@value TemplateID} if the new template is created. {@value Null} if
	 *         the new template could not be created.
	 */
	public String createTemplate(String monitorType, String actionMonitored, String stateToMonitor) {

		return createTemplate(monitorType, actionMonitored, null, stateToMonitor, 0);
	}

	/**
	 * Creates a new monitor template with the given parameters.
	 * 
	 * @param monitorType     The type of the monitor e.g., CCTV
	 * @param actionMonitored the action name the monitor template can monitor
	 * @param targetType      the target type to monitor e.g., Room
	 * @param stateToMonitor  and the state to monitor expressed as a BigraphER
	 *                        expression
	 * @return {@value TemplateID} if the new template is created. {@value Null} if
	 *         the new template could not be created.
	 */
	public String createTemplate(String monitorType, String actionMonitored, String targetType, String stateToMonitor) {

		List<String> monitorableActions = new LinkedList<String>();
		monitorableActions.add(actionMonitored);

		return createTemplate(monitorType, monitorableActions, targetType, stateToMonitor, 0);
	}

	/**
	 * Creates a new monitor template with the given parameters.
	 * 
	 * @param monitorType        The type of the monitor e.g., CCTV
	 * @param monitorableActions A list of actions that the monitor can monitor
	 * @param targetType         the target type to monitor e.g., Room
	 * @param stateToMonitor     and the state to monitor expressed as a BigraphER
	 *                           expression
	 * @return {@value TemplateID} if the new template is created. {@value Null} if
	 *         the new template could not be created.
	 */
	public String createTemplate(String monitorType, List<String> monitorableActions, String targetType,
			String stateToMonitor) {

		return createTemplate(monitorType, monitorableActions, targetType, stateToMonitor, 0);
	}

	/**
	 * Creates a new monitor template with the given parameters.
	 * 
	 * @param monitorType     The type of the monitor e.g., CCTV
	 * @param actionMonitored the action name the monitor template can monitor
	 * @param targetType      the target type to monitor e.g., Room
	 * @param stateToMonitor  and the state to monitor expressed as a BigraphER
	 *                        expression
	 * @param cost            The cost of monitoring
	 * @return {@value TemplateID} if the new template is created. {@value Null} if
	 *         the new template could not be created.
	 */
	public String createTemplate(String monitorType, String actionMonitored, String targetType, String stateToMonitor,
			double cost) {

		List<String> monitorableActions = new LinkedList<String>();
		monitorableActions.add(actionMonitored);

		return createTemplate(monitorType, monitorableActions, targetType, stateToMonitor, cost);
	}

	/**
	 * Creates a new monitor template with the given parameters.
	 * 
	 * @param monitorType        The type of the monitor e.g., CCTV
	 * @param monitorableActions A list of actions that the monitor can monitor
	 * @param targetType         the target type to monitor e.g., Room
	 * @param stateToMonitor     and the state to monitor expressed as a BigraphER
	 *                           expression
	 * @param cost               The cost of monitoring
	 * @return {@value TemplateID} if the new template is created. {@value Null} if
	 *         the new template could not be created.
	 */
	public String createTemplate(String monitorType, List<String> monitorableActions, String targetType,
			String stateToMonitor, double cost) {

		int tries = 100;
		String templateID = null;

		// ==create unique template id
		while (tries > 0) {
			templateID = createUniqueTemplateName(-1);

			if (templateID != null) {
				break;
			}

			tries--;
		}

		if (templateID == null) {
			return null;
		}

		MonitorTemplate monitorTemplate = new MonitorTemplate(templateID, monitorType, monitorableActions, targetType,
				stateToMonitor, 0);

		templates.put(templateID, monitorTemplate);

		return templateID;
	}

	protected String createUniqueTemplateName(int upperBound) {

		// create name
		Random rand = new Random();
		String name = null;
		int tries = 1000;

		int max = 100000;

		if (upperBound < 0) {
			upperBound = max;
		}

		while (tries > 0) {
			name = "MT-" + rand.nextInt(upperBound);

			if (!templates.containsKey(name)) {
				break;
			}
		}

		return name;
	}

	public List<String> getAvailableTemplateNames() {

		return new LinkedList<String>(templates.keySet());
	}

	public Map<String, Monitor> createAllMonitors() {

		Map<String, Monitor> monitors = new HashMap<String, Monitor>();

		for (String monName : templates.keySet()) {
			Monitor mon = createMonitor(monName);

			monitors.put(monName, mon);
		}

		return monitors;
	}

	/**
	 * Creates a Monitor object with the given monitor template name
	 * 
	 * @param monType The type of monitor to create
	 * @return A Monitor object
	 */
	public Monitor createMonitor(String templateName) {

		return createMonitor(templateName, null);
	}

	/**
	 * Creates a Monitor object with the given monitor template name and monitor ID
	 * 
	 * @param monType The type of monitor to create
	 * @param id      The monitor ID, which can be an asset name in a system
	 * @return A Monitor object
	 */
	public Monitor createMonitor(String templateName, String id) {

		if (templateName == null || !templates.containsKey(templateName)) {
			return null;
		}

		MonitorTemplate monitorTemplate = templates.get(templateName);

		Monitor mon = new Monitor();

		mon.setMonitorID(id);
		mon.setMonitorType(monitorTemplate.getType());
		mon.setTargetType(monitorTemplate.getTargetType());
		mon.setMonitorableActions(monitorTemplate.getMonitorableActions());
		mon.setBigraphERStatment(monitorTemplate.getBigraphERMonitoringExpression());

		return mon;
	}
}

class MonitorTemplate {

//	VISITOR_ENTER_ROOM("CCTV", "Room", "VisitorEnterRoom",
//			"Hallway{hallway}.(id | CCTV{ipNet}) | Room{hallway}.(Visitor.id)");

	// type of monitor
	String type;

	// target to monitor
	String targetType;

	// expression that indicates what it can monitor. The expression is BigraphER
	// expression
	String bigraphERmonitoringExpression;

	BigraphExpression ownMonitoringExpression;

	// the action that it can monitor
//	String actionMonitored;

	// actions that can be monitored
	List<String> monitorableActions;

	// cost
	double cost;

	// monitor ID
	String monitorTemplateID;

	protected MonitorTemplate(String monitorTemplateID, String type, List<String> monitorableActions, String targetType,
			String monitoringExpression, double cost) {
		this.type = type;
		this.targetType = targetType;
		this.bigraphERmonitoringExpression = monitoringExpression;

		this.monitorableActions = new LinkedList<String>(monitorableActions);

		this.cost = cost;
		this.monitorTemplateID = monitorTemplateID;
	}

	protected MonitorTemplate(String monitorTemplateID, String type, String actionMonitored, String targetType,
			String monitoringExpression, double cost) {

		this(monitorTemplateID, type, new LinkedList<String>() {
			{
				add(actionMonitored);
			}
		}, targetType, monitoringExpression, cost);

	}

	protected String getType() {
		return type;
	}

	protected String getTargetType() {
		return targetType;
	}

	protected String getBigraphERMonitoringExpression() {
		return bigraphERmonitoringExpression;
	}

//	protected String getActionMonitored() {
//		if (monitorableActions.size() > 0)
//			return monitorableActions.get(0);
//
//		return null;
//	}

	protected List<String> getMonitorableActions() {

		return monitorableActions;
	}

//	protected  BigraphExpression getOwnMonitoringExpression() {
//		return ownMonitoringExpression;
//	}

	protected double getCost() {
		return cost;
	}

}
