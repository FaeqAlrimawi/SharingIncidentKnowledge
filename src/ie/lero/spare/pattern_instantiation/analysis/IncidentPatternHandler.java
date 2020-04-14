package ie.lero.spare.pattern_instantiation.analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xquery.XQException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import core.instantiation.analysis.utilities.IncidentModelHandler;
//import core.instantiation.analysis.utilities.Predicate;
//import core.instantiation.analysis.utilities.PredicateType;
//import core.instantiation.analysis.utilities.SystemModelHandler;
//import core.instantiation.analysis.utilities.XqueryExecuter;
import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.Condition;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.Postcondition;
import cyberPhysical_Incident.Precondition;
import environment.Asset;
import environment.EnvironmentDiagram;
//import ie.lero.spare.franalyser.utility.FileNames;
import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator;
import ie.lero.spare.pattern_instantiation.analysis.utilities.IncidentModelHandler;
import ie.lero.spare.pattern_instantiation.analysis.utilities.Predicate;
import ie.lero.spare.pattern_instantiation.analysis.utilities.PredicateType;
import ie.lero.spare.pattern_instantiation.analysis.utilities.SystemModelHandler;
import ie.lero.spare.pattern_instantiation.analysis.utilities.XqueryExecuter;
import ie.lero.spare.utility.FileNames;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;

public class IncidentPatternHandler {

	private IncidentDiagram incidentPattern;
	private EnvironmentDiagram systemModel;

	// private IncidentDiagram incidentPatternConcrete;
	private String incidentPatternFilePath;
	private String systemModelFilePath;
	private String tracesFilePath;

	// key is entity name, value is asset name
	private Map<String, String> entityAssetMap;

	// key is asset name, value is the Asset object from system model
	// private Map<String, Asset> nameToAssetMap;

	// key is asset name, value is control
	private Map<String, String> assetNameToControlMap;

	// key is asset class name, value is a list of controls in the bigrapher
	// file with the first as the main
	private Map<String, List<String>> assetControlMap;

	private final static String ENTITY_NAME = "incident_entity_name";
	private final static String ASSET_NAME = "system_asset_name";
	private final static String MAPS = "maps";

	private TraceMiner miner;

	private boolean isConcreteCreated = false;

	public IncidentPatternHandler(TraceMiner traceMiner) {
		entityAssetMap = new HashMap<String, String>();
		// nameToAssetMap = new HashMap<String, Asset>();
		assetNameToControlMap = new HashMap<String, String>();

		miner = traceMiner;

	}

	public void setBigraphFilePath(String bigFilePath) {

		if (miner != null) {
			miner.setBigraphERFile(bigFilePath);
		}
	}

	public String getTracesFilePath() {
		return tracesFilePath;
	}

	public void setTracesFilePath(String tracesFilePath) {
		this.tracesFilePath = tracesFilePath;
	}

	public TraceMiner getMiner() {
		return miner;
	}

	public void setMiner(TraceMiner miner) {
		this.miner = miner;
	}

	public String getIncidentPatternFilePath() {
		return incidentPatternFilePath;
	}

	public void setIncidentPatternFilePath(String incidentPatternFilePath) {
		this.incidentPatternFilePath = incidentPatternFilePath;
	}

	public String getSystemModelFilePath() {
		return systemModelFilePath;
	}

	public void setSystemModelFilePath(String systemModelFilePath) {
		this.systemModelFilePath = systemModelFilePath;
	}

	public Map<String, String> getEntityAssetMap() {
		return entityAssetMap;
	}

	public void setEntityAssetMap(Map<String, String> entityAssetMap) {
		this.entityAssetMap = entityAssetMap;
	}

	public IncidentDiagram loadIncidentPattern(String filePath) {

		if (filePath == null) {
			return null;
		}

		incidentPattern = IncidentModelHandler.loadIncidentFromFile(filePath);

		if (incidentPattern != null) {
			incidentPatternFilePath = filePath;
		}

		return incidentPattern;
	}

	public EnvironmentDiagram loadSystemModel(String systemFilePath) {

		if (systemFilePath == null) {
			return null;
		}

		systemModel = SystemModelHandler.loadSystemFromFile(systemFilePath);

		if (systemModel != null) {
			systemModelFilePath = systemFilePath;
		}

		return systemModel;
	}

	/**
	 * Replaces incident pattern entities in conditions with asset class names
	 * 
	 * @param tracesFilePath
	 *            traces file path which contains the map (entity name to asset
	 *            name)
	 */
	public void createConcreteConditions(String tracesFilePath) {

		// update the incident pattern with concerte entities

		// load incident pattern
		if (incidentPattern == null) {
			loadIncidentPattern(incidentPatternFilePath);

			if (incidentPattern == null) {
				return;
			}
		}

		// load system model
		if (systemModel == null) {
			loadSystemModel(systemModelFilePath);

			if (systemModel == null) {
				return;
			}
		}

		// load asset to control map
		assetControlMap = loadAssetControlMap();

		if (assetControlMap == null) {
			System.err.println("asset control map is null");
			return;
		}

		JSONParser parser = new JSONParser();

		FileReader reader;
		try {

			reader = new FileReader(tracesFilePath);
			JSONObject tracesObj = (JSONObject) parser.parse(reader);

			// ===get the entity-asset map from the traces file
			fillEntityAssetMap(tracesObj);

			if (entityAssetMap == null || entityAssetMap.isEmpty()) {
				return;
			}

			// ===get asset class from system model

			Activity act = incidentPattern.getInitialActivity();

			if (act == null) {
				System.out.println("IncidentPAtternHandler:: Initial Activity is not found");
				return;
			}

			while (act != null) {
				List<Condition> conditions = new LinkedList<Condition>();
				conditions.add(act.getPrecondition());
				conditions.add(act.getPostcondition());

				// for each condition replace each
				for (Condition cond : conditions) {

					if (cond == null) {
						continue;
					}

					for (Entry<String, String> entry : entityAssetMap.entrySet()) {

						String entityName = entry.getKey();
						String assetName = entry.getValue();

						Asset ast = systemModel.getAsset(entry.getValue());

						if (ast == null) {
							System.err.println("asset : " + assetName + " is not in the system model.");
							continue;
						}

						String className = ast.getClass().getSimpleName();
						String control = "";

						// remove Impl if exists
						if (className.contains("Impl")) {
							className = className.replace("Impl", "");
						}

						// find control
						List<String> controls = assetControlMap.get(className);

						if (controls != null && controls.size() > 0) {
							control = controls.get(0);
						} else {
							System.err.println("system class [" + className + "] has no control in the bigrapher file");
							continue;
						}

						// add to asset name to control map
						assetNameToControlMap.put(assetName, control);

						// replace the entity name with a class name
						replaceEntityNameToAssetClass(cond, entityName, control);
					}
				}

				// get next act
				act = (act.getNextActivities() != null && act.getNextActivities().size() > 0)
						? act.getNextActivities().get(0) : null;

			}

			// === replace entity name with asset class
			// for(Entry<String, String> entry : entityAssetMap)

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		isConcreteCreated = true;

	}

	public void replaceEntityNameToAssetClass(Condition condition, String entityName, String control) {

		if (condition == null) {
			return;
		}

		BigraphExpression bigExp = (BigraphExpression) condition.getExpression();

		bigExp.replaceEntityName(entityName, control);

		// System.out.println(bigExp.getEntity());
		//
		// Entity entity = bigExp.getEntity(entityName);
		//
		// if (entity != null) {
		//// entity.
		//
		// System.out.println("replaced entity name [" + entityName + "] with
		// asset class [" + assetClass + "]");
		// } else {
		// System.out.println(
		// "entity name [" + entityName + "] does not exist in the condition ["
		// + condition.getName() + "]");
		// }
	}

	public void fillEntityAssetMap(JSONObject traces) {

		// reads the entity-asset map from the traces file
		if (traces == null) {
			System.err.println("traces json object is NULL");
			return;
		}

		if (!traces.containsKey(MAPS)) {
			System.err.println("Key error: " + MAPS);
			return;
		}

		JSONArray ary = (JSONArray) traces.get(MAPS);

		if (ary == null) {
			System.err.println("json array from traces file is null");
			return;
		}

		Iterator it = ary.iterator();

		// get entity asset map
		while (it != null && it.hasNext()) {

			JSONObject entityAssetObj = (JSONObject) it.next();

			if (entityAssetObj == null) {
				continue;
			}

			String entityName = entityAssetObj.get(ENTITY_NAME).toString();
			String assetName = entityAssetObj.get(ASSET_NAME).toString();

			if (entityName != null && assetName != null) {
				entityAssetMap.put(entityName, assetName);
			}
		}

		// System.out.println("Map:\n" + entityAssetMap);
	}

	protected Map<String, List<String>> loadAssetControlMap() {

		// key is asset class name, value is the list of controls from the big
		// file
		Map<String, List<String>> assetControlMap = new HashMap<String, List<String>>();

		// List<String> unMatchedControls = new LinkedList<String>();
		// Signature signature = systemHandler.getGlobalBigraphSignature();

		InputStream systemControlMapFileName = IncidentPatternInstantiator.class.getClassLoader()
				.getResourceAsStream("resources/asset-control_map.txt");
		// .getResource("ie/lero/spare/resources/" +
		// FileNames.ASSET_CONTROL_MAP);

		if (systemControlMapFileName == null) {
			System.err.println("System to Control map file [" + FileNames.ASSET_CONTROL_MAP + "] is not found");
			return null;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(systemControlMapFileName));

		// String path = systemControlMapFileName.getPath();
		// String[] lines = FileManipulator.readFileNewLine(path);

		String assetClass = null;
		String[] controlNames = null;
		String[] tmp;
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				tmp = line.split(FileNames.ASSET_CONTROL_SEPARATOR);

				if (tmp.length < 2) {
					continue;
				}

				assetClass = tmp[0]; // system class
				controlNames = tmp[1] != null ? tmp[1].split(FileNames.CONTROLS_SEPARATOR) : null; // bigrapher
																									// controls
				// String primariyControl = controlNames.length > 0 ?
				// controlNames[0] : null;

				// if (signature != null && signature.getByName(primariyControl)
				// == null) {
				// unMatchedControls.add(primariyControl);
				// }

				assetControlMap.put(assetClass, Arrays.asList(controlNames));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// logger.putMessage(Logger.SEPARATOR_BTW_INSTANCES +
		// "SystemClass<->Control map is created.");
		// if (!unMatchedControls.isEmpty()) {
		// logger.putMessage(
		// Logger.SEPARATOR_BTW_INSTANCES + "Some Primariy Controls in the map
		// have no equivalent in ("
		// + systemFileName + "):" +
		// Arrays.toString(unMatchedControls.toArray()));
		// }

		return assetControlMap;
	}

	public Map<Integer, String> findMatchingStates(GraphPath trace) {

		// ==== finds the sequence of states from the given trace that match to
		// the conditions of the incident pattern activities
		if (trace == null || miner == null) {
			System.err.println("Miner is null");
			return null;
		}

		// ===create concrete conditions
		if (!isConcreteCreated) {
			createConcreteConditions(tracesFilePath);
		}

		if (incidentPattern == null) {
			return null;
		}

		// key is state, value is condition name that it matches
		Map<Integer, String> matchingStates = new HashMap<Integer, String>();

		int currentIndex = 0;
		List<Integer> traceStates = trace.getStateTransitions();

		Activity act = incidentPattern.getInitialActivity();
		main_loop: while (act != null) {

			List<Condition> conditions = new LinkedList<Condition>();
			conditions.add(act.getPrecondition());
			conditions.add(act.getPostcondition());

			for (Condition cond : conditions) {

				if (cond == null) {
					continue;
				}

				// if current index reached the end of the trace then exit
				if (currentIndex >= traceStates.size()) {
					System.out.println("Trace end reached.");
					break main_loop;
				}

				// get bigraph object of condition
				BigraphExpression bigExp = (BigraphExpression) cond.getExpression();

				if (bigExp == null) {
					System.err.println("bigraph expression is null of condition [" + cond.getName() + "]");
					continue;
				}

				Bigraph condBig = createConditionBigraph(cond, act);

				// BigraphWrapper wrapper = new BigraphWrapper();
				// wrapper.setBigraphExpression(bigExp);
				// wrapper.createBigraph(false, miner.gets)

				// match condition to a state
				for (; currentIndex < traceStates.size(); currentIndex++) {

					int stateID = traceStates.get(currentIndex);

					// get bigraph representation
					Bigraph stateBig = miner.loadState(stateID);

					if (stateBig == null) {
						System.err.println("System state [" + stateID + "] is null");
						continue;
					}
					// else {
					// System.out.println("\nState-"+stateID+"\n"+stateBig);
					// }

					// match state to condition
					Matcher matcher = new Matcher();

					// System.out.println("Matching state ["+stateID+"] to
					// condition [" + cond.getName()+"]");
					// if it matches then add to the result
					if (matcher.match(stateBig, condBig).iterator().hasNext()) {
						matchingStates.put(stateID, cond.getName());
						// System.out.println(
						// "Matching state [" + stateID + "] to activity
						// ["+act.getName()+"] condition [" + cond.getName() +
						// "] succeeded");
						// increment index if the condition is pre
						if (cond instanceof Precondition) {
							currentIndex++;
						}
						break;
					}
					// else {
					// System.out.println(
					// "Matching state [" + stateID + "] to activity
					// ["+act.getName()+"] condition [" + cond.getName() + "]
					// Failed");
					// }

				}
			}

			// next act
			act = (act.getNextActivities() != null && act.getNextActivities().size() > 0)
					? act.getNextActivities().get(0) : null;
		}

		return matchingStates;
	}

	protected Bigraph createConditionBigraph(Condition cond, Activity act) {

		if (cond == null || act == null) {
			return null;
		}

		Bigraph condBig = null;

		PredicateType type = PredicateType.Precondition;

		if (cond instanceof Precondition) {
			type = PredicateType.Precondition;
		} else if (cond instanceof Postcondition) {
			type = PredicateType.Postcondition;
		}
		// condBig = bigExp.createBigraph(false, miner.getSignature());

		// generate big by predicate

		try {
			// System.out.println(incidentPatternFilePath);
			org.json.JSONObject condJson = XqueryExecuter.getBigraphConditions(act.getName(), type,
					incidentPatternFilePath);

			if (condJson != null) {
				Predicate pred = new Predicate();

				// System.out.println(assetNameToControlMap);

				pred.setAssetControlMap(assetNameToControlMap);
				pred.setEntityAssetMap(entityAssetMap);
				pred.setIncidentDocument(incidentPatternFilePath);

				pred.convertToMatchedAssets(condJson, cond.getName());

				condBig = pred.convertJSONtoBigraph(condJson, miner.getSignature());

			} else {
				System.err.println("condition json object is null");
			}

		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (condBig == null) {
			System.err.println("Bigraph object of condition [" + cond.getName() + "] is null");
			return null;
		}

		return condBig;
	}

	/**
	 * Returns the activity name for the given condition
	 * 
	 * @param condition
	 *            condition name
	 * @return Activity name
	 */
	public String getActivityName(String condition) {

		if (condition == null || condition.isEmpty()) {
			return null;
		}

		if (incidentPattern == null) {
			return null;
		}

		Activity act = incidentPattern.getInitialActivity();

		while (act != null) {

			// check pre
			Precondition pre = act.getPrecondition();

			if (pre != null && pre.getName().equalsIgnoreCase(condition)) {
				return act.getName();
			}

			// check post
			Postcondition post = act.getPostcondition();

			if (post != null && post.getName().equalsIgnoreCase(condition)) {
				return act.getName();
			}

			// next act
			act = (act.getNextActivities() != null && act.getNextActivities().size() > 0)
					? act.getNextActivities().get(0) : null;
		}

		return null;
	}

	public String getLastIncidentPatternCondition() {

		if (incidentPattern == null) {
			System.err.println("IncidentPatternHandler:: incidnet pattern model is null");
			return null;
		}

		Activity act = incidentPattern.getInitialActivity();
		String lastCond = null;

		int tries = 100000;

		while (act != null && tries > 0) {

			lastCond = act.getPostcondition() != null ? act.getPostcondition().getName() : null;
			
			act = (act.getNextActivities() != null && act.getNextActivities().size() > 0)
					? act.getNextActivities().get(0) : null;

			tries--;
		}

		// if(preAct!=null) {
		// Postcondition post = preAct.getPostcondition();
		//
		// return post.getName();
		// }

		return lastCond;
	}

	public static void main(String[] args) {

		TraceMiner miner = new TraceMiner();

		IncidentPatternHandler inc = new IncidentPatternHandler(miner);

		String statesFolder = "D:/Bigrapher data/lero/big with unique action names/states10K/states";
		String traceExampleFile = "resources/example/traces_10K.json";
		String sysModelFilePath = "D:/Bigrapher data/lero/big with unique action names/lero.cps";
		String bigrapherFilePath = "D:/Bigrapher data/lero/big with unique action names/lero.big";
		String incidentPatternModelFilePath = "D:/Bigrapher data/lero/big with unique action names/incidentPattern.cpi";

		URL url = IncidentPatternHandler.class.getClassLoader().getResource(traceExampleFile);

		if (url != null) {
			String filePath = url.getPath();

			// set states folder in miner
			miner.setStatesFolder(statesFolder);

			// set incident and system model files
			inc.setIncidentPatternFilePath(incidentPatternModelFilePath);
			inc.setSystemModelFilePath(sysModelFilePath);

			// set traces file
			inc.setTracesFilePath(filePath);

			// set bigrapher file if not set by the miner
			if (miner.getBigraphERFile() == null || miner.getBigraphERFile().isEmpty()) {
				inc.setBigraphFilePath(bigrapherFilePath);
			}

			GraphPath testTrace = new GraphPath();
			List<Integer> states = new LinkedList<Integer>();
			states.add(1);
			states.add(61);
			states.add(174);
			states.add(396);
			states.add(1699);
			states.add(6689);

			testTrace.setInstanceID(100);
			testTrace.setStateTransitions(states);

			Map<Integer, String> res = inc.findMatchingStates(testTrace);

			System.out.println("result::\n" + res);
			// replaces entity names with asset class names
			// inc.createConcreteConditions(filePath);

		} else {
			System.out.println("url is null");
		}

	}
}
