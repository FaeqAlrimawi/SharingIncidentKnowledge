package ie.lero.spare.pattern_instantiation;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQException;

import org.json.JSONArray;
import org.json.JSONObject;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.IncidentDiagram;
import environment.EnvironmentDiagram;
import ie.lero.spare.franalyser.utility.FileManipulator;
import ie.lero.spare.franalyser.utility.JSONTerms;
import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.ModelsHandler;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.Signature;

public class PredicateGenerator {

	// private AssetMap assetMap;
	private PredicateHandler predHandler;
	private String[] spaceAssetSet;
	private String[] incidentAssetNames;
	// private boolean isDebugging = true;
	private String[] systemAssetControls;
	private Logger logger;
	private SystemInstanceHandler systemHandler;
	private String incidentDocument;
	
	// used to find a map of an asset to a control
	private Map<String, List<String>> assetControlMap;

//	public PredicateGenerator() {
//		
//		this(null, null);
//	}
//	
//	public PredicateGenerator(Map<String, String> assetControlMap) {
//
//		this(assetControlMap, null);
//	}
//	
//	public PredicateGenerator(Map<String, String> assetControlMap, Logger logger) {
//		predHandler = new PredicateHandler();
//
//		this.assetControlMap = assetControlMap;
////		loadAssetControlMap();
//		
//		this.logger = logger;
//
//	}

	public void setAssetControlMap(Map<String, List<String>> assetControlMap) {
		this.assetControlMap = assetControlMap;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
//	protected void loadAssetControlMap() {
//
//		assetControlMap = new HashMap<String, String>();
//
//		String mapFile = "./etc/data/asset-control map.txt";
//
//		List<String> unMatchedControls = new LinkedList<String>();
//		Signature signature = SystemInstanceHandler.getGlobalBigraphSignature();
//
//		String[] lines = FileManipulator.readFileNewLine(mapFile);
//
//		String assetClass = null;
//		String controlName = null;
//		String[] tmp;
//
//		for (String line : lines) {
//			tmp = line.split(" ");
//			assetClass = tmp[0];
//			controlName = tmp[1];
//
//			if (signature != null && signature.getByName(controlName) == null) {
//				unMatchedControls.add(controlName);
//			} else {
//				assetControlMap.put(assetClass, controlName);
//			}
//		}
//
//		if (!unMatchedControls.isEmpty()) {
//			Logger.getInstance().putError("Loading Asset-Control Map error. No such Controls:"
//					+ Arrays.toString(unMatchedControls.toArray()));
//		}
//	}

	/*
	 * public PredicateGenerator(AssetMap map) { this(); // assetMap = map;
	 * incidentAssetNames = map.getIncidentEntityNames(); }
	 */

	public PredicateGenerator(String[] systemAsset, String[] incidentAssetName, String[] systemAssetControl
			, Map<String, List<String>> assetControlMap, Logger logger, SystemInstanceHandler systemHandler, String incidentDoc) {
//		this();
		spaceAssetSet = systemAsset;
		this.incidentAssetNames = incidentAssetName;
		this.systemAssetControls = systemAssetControl;
		
		this.assetControlMap = assetControlMap;
		this.logger = logger;
		this.systemHandler = systemHandler;
		this.predHandler = new PredicateHandler(logger, systemHandler, incidentDoc);
		incidentDocument = incidentDoc;
	}

	private HashMap<String, Activity> createIncidentActivities(IncidentDiagram incidentModel) {

		List<Activity> activities = ModelsHandler.getIncidentModelActivities(incidentModel);

		for (Activity act : activities) {

			predHandler.addIncidentActivity(new IncidentActivity(act, systemHandler));
		}

		predHandler.updateNextPreviousActivities();

		predHandler.createActivitiesDigraph();

		return predHandler.getIncidentActivities();
	}

//	private HashMap<String, Activity> createIncidentActivitiesUsingXquery() {
//		String[] tmp;
//		String[] nextPreviousActivities;
//		IncidentActivity activity;
//
//		try {
//			nextPreviousActivities = XqueryExecuter.returnNextPreviousActivities();
//
//			if (nextPreviousActivities != null) {
//				for (String res : nextPreviousActivities) {
//					tmp = res.split("##"); // first is activity name, 2nd is
//											// next activities, 3rd previous
//											// activities
//					activity = new IncidentActivity(tmp[0]);
//					predHandler.addIncidentActivity(activity);
//				}
//			}
//
//			predHandler.updateNextPreviousActivitiesUsingXquery();
//			predHandler.createActivitiesDigraph();
//
//		} catch (FileNotFoundException | XQException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return predHandler.getIncidentActivities();
//	}

	public PredicateHandler generatePredicates(IncidentDiagram incidentModel) {

		PredicateType[] types = { PredicateType.Precondition, PredicateType.Postcondition };

		try {

			// create activties of the incident
			HashMap<String, Activity> activities = createIncidentActivities(incidentModel);

			// get controls for the asset set from the system file
			// systemAssetControls =
			// XqueryExecuter.getSystemAssetControls(spaceAssetSet);

			// populates the controls array based on the map and the asset set
//			updateAssetControls();

			// create the Bigraph representation (from LibBig library) for the
			// pre/postconditions of the activities
			// assumption: esach activity has ONE precondition and ONE
			// postcondition
			for (String activity : activities.keySet()) {
				for (PredicateType type : types) {
					JSONObject condition = XqueryExecuter.getBigraphConditions(activity, type, incidentDocument);
					
					// if there is no condition returend then skip creating a
					// predicate for it
					if (condition == null || condition.isNull(JSONTerms.ENTITY)) {
						continue;
					}

					Predicate p = new Predicate(systemHandler, incidentDocument);
					p.setIncidentActivity(activities.get(activity));
					p.setPredicateType(type);
					p.setName(activity + "_" + type.toString()); // e.g., name =
																	// activity1_pre1
					// updates entity names and controls from incident pattern
					// to that from the system model
					if (convertToMatchedAssets(condition, p.getName())) {
						p.setBigraphPredicate(condition);
						if (p.getBigraphPredicate() != null)
							predHandler.addActivityPredicate(activity, p);
					}
				}

			}

		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return predHandler;
	}

//	protected void updateAssetControls() {
//
//		EnvironmentDiagram systemModel = ModelsHandler.getCurrentSystemModel();
//
//		environment.Asset tmpAst;
//		List<String> unMatchedAssets = new LinkedList<String>();
//
//		systemAssetControls = new String[spaceAssetSet.length];
//
//		for (int i = 0; i < spaceAssetSet.length; i++) {
//
//			tmpAst = systemModel.getAsset(spaceAssetSet[i]);
//
//			if (tmpAst == null) {
//				continue;
//			}
//
//			String className = tmpAst.getClass().getSimpleName().replace("Impl", "");
//			String control = assetControlMap.get(className);
//
//			if (control == null) {
//				unMatchedAssets.add(spaceAssetSet[i] + "[" + className + "]");
//				continue;
//			}
//
//			systemAssetControls[i] = control;
//		}
//
//		if (!unMatchedAssets.isEmpty()) {
//			Logger.getInstance().putError("PredicateGenerator>>Some assets have no map to controls. These are:"
//					+ Arrays.toString(unMatchedAssets.toArray()));
//		}
//	}

	/*
	 * public PredicateHandler generatePredicatesUpdated() {
	 * 
	 * PredicateType[] types = { PredicateType.Precondition,
	 * PredicateType.Postcondition };
	 * 
	 * try {
	 * 
	 * // create activties of the incident HashMap<String, Activity> activities
	 * = createIncidentActivities();
	 * 
	 * // get controls for the asset set from the system file
	 * systemAssetControls =
	 * XqueryExecuter.getSystemAssetControls(spaceAssetSet);
	 * 
	 * // create the Bigraph representation (from LibBig library) for the //
	 * pre/postconditions of the activities // assumption: esach activity has
	 * ONE precondition and ONE // postcondition for (String activity :
	 * activities.keySet()) { for (PredicateType type : types) { JSONObject
	 * condition = XqueryExecuter.getBigraphConditions(activity, type);
	 * 
	 * // if there is no condition returend then skip creating a // predicate
	 * for it if (condition == null || condition.isNull(JSONTerms.ENTITY)) {
	 * continue; }
	 * 
	 * Predicate p = new Predicate();
	 * p.setIncidentActivity(activities.get(activity));
	 * p.setPredicateType(type); p.setName(activity + "_" + type.toString()); //
	 * e.g., name = // activity1_pre1 // updates entity names and controls from
	 * incident pattern // to that from the system model if
	 * (convertToMatchedAssets(condition)) { p.setBigraphPredicate(condition);
	 * if (p.getBigraphPredicate() != null)
	 * predHandler.addActivityPredicate(activity, p); } }
	 * 
	 * }
	 * 
	 * } catch (FileNotFoundException | XQException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } return predHandler; }
	 */

	private boolean convertToMatchedAssets(JSONObject obj, String conditionName) {

		JSONArray tmpAry;
		JSONObject tmpObject;
		LinkedList<JSONObject> objs = new LinkedList<JSONObject>();

		if (obj.isNull(JSONTerms.ENTITY)) {
			return false;
		}

		objs.add(obj);
		List<String> unMatchedEntityNames = new LinkedList<String>();
		boolean isAdded = false;

		while (!objs.isEmpty()) {
			tmpObject = objs.pop();

			if (JSONArray.class.isAssignableFrom(tmpObject.get(JSONTerms.ENTITY).getClass())) {
				tmpAry = (JSONArray) tmpObject.get(JSONTerms.ENTITY);
			} else {
				tmpAry = new JSONArray();
				tmpAry.put((JSONObject) tmpObject.get(JSONTerms.ENTITY));
			}

			for (int i = 0; i < tmpAry.length(); i++) {
				JSONObject tmpObj = tmpAry.getJSONObject(i);

				String name = tmpObj.get(JSONTerms.NAME).toString();
				for (int j = 0; j < incidentAssetNames.length; j++) {
					if (incidentAssetNames[j].equals(name)) {
						tmpObj.put(JSONTerms.NAME, spaceAssetSet[j]);
						tmpObj.put(JSONTerms.CONTROL, systemAssetControls[j]);
						tmpObj.put(JSONTerms.INCIDENT_ASSET_NAME, incidentAssetNames[j]);
						isAdded = true;
						break;
					}
				}

				if (!isAdded) {
					if (!unMatchedEntityNames.contains(name)) {
						unMatchedEntityNames.add(name);
					}
				}

				// add contained entities
				if (!tmpObj.isNull(JSONTerms.ENTITY)) {
					objs.add(tmpObj);
				}

				isAdded = false;
			}

		}

		if (!unMatchedEntityNames.isEmpty()) {
			logger.putError("PredicateGenerator>>Some entities in condition [" + conditionName
					+ "] have no map to incident entities:" + Arrays.toString(unMatchedEntityNames.toArray()));
			return false;
		}

		return true;
	}

	/*
	 * public String matchConditionAssetsToSpaceAssets(String condition) {
	 * String result = condition;
	 * 
	 * //assuming a well formatted Bigraph statement for (int
	 * i=0;i<incidentAssetNames.length;i++) { //if the condition contains the
	 * name of an incident asset then replace with
	 * if(condition.contains(incidentAssetNames[i])) {
	 * 
	 * result=result.replaceAll(incidentAssetNames[i], spaceAssetSet[i]); } }
	 * 
	 * return result; }
	 */

	/*
	 * public AssetMap getAssetMap() { return assetMap; }
	 * 
	 * public void setAssetMap(AssetMap assetMap) { this.assetMap = assetMap; }
	 */
	public PredicateHandler getPredHandler() {
		return predHandler;
	}

	public void setPredHandler(PredicateHandler predHandler) {
		this.predHandler = predHandler;
	}

	/*
	 * private void print(String msg) { if(isDebugging) {
	 * System.out.println("PredicateGenerator"+msg); } }
	 */

}
