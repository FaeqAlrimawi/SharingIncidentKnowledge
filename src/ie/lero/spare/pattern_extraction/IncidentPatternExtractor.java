package ie.lero.spare.pattern_extraction;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.eclipse.emf.common.util.EList;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.ActivityInitiator;
import cyberPhysical_Incident.ActivityPattern;
import cyberPhysical_Incident.ActivityType;
import cyberPhysical_Incident.Actor;
import cyberPhysical_Incident.ActorLevel;
import cyberPhysical_Incident.ActorRole;
import cyberPhysical_Incident.Asset;
import cyberPhysical_Incident.Behaviour;
import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.Connection;
import cyberPhysical_Incident.CrimeScript;
import cyberPhysical_Incident.CyberPhysicalIncidentFactory;
import cyberPhysical_Incident.Entity;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.IncidentEntity;
import cyberPhysical_Incident.Knowledge;
import cyberPhysical_Incident.Level;
import cyberPhysical_Incident.Location;
import cyberPhysical_Incident.Mobility;
import cyberPhysical_Incident.Postcondition;
import cyberPhysical_Incident.Precondition;
import cyberPhysical_Incident.Resource;
import cyberPhysical_Incident.Scene;
import cyberPhysical_Incident.ScriptCategory;
import cyberPhysical_Incident.Type;
import cyberPhysical_Incident.Vulnerability;
import environment.CyberPhysicalSystemPackage;
import environment.EnvironmentDiagram;
import ie.lero.spare.utility.Logger;
import ie.lero.spare.utility.ModelsHandler;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;


public class IncidentPatternExtractor {

	protected String originalIncidentFilePath;
	protected String systemFilePath;

	protected IncidentDiagram abstractIncidentModel;
	protected IncidentDiagram originalIncidentModel;
	protected EnvironmentDiagram systemModel;

	protected Map<String, String> entityMap = new HashMap<String, String>();
	protected List<ActivityPattern> activityPatterns;

	// used to restrict the # of actions/activities to search when matching a
	// pattern postconditions
	// Max effective value would equal to the number of actions in a scene
	protected static int MaxNumberOfActions = 10;

	protected CyberPhysicalIncidentFactory instance = CyberPhysicalIncidentFactory.eINSTANCE;

	// used to create a random number (b/w 0-10000) and attach it with
	// ACTIVITY_NAME
	// to create a probabl unique name for an activity
	protected static final int MAX_LENGTH = 10000;

	protected static final String ACTIVITY_NAME = "abstractActivity";

	// the key is an integer that represents an activity pattern id taken from
	// the list of activity patterns
	// and the value is a list where each entry in the list represents a map to
	// the sequence of actions (or activities),
	// A map is the sequence of activities (or actions) in the incident instance
	// e.g. entry 0,1 refers to the sequence of actions/activities in an
	// incident model that corresponds to pattern ID [0] and Map ID [1]
	protected Map<Integer, List<int[]>> allPatternsMaps = new HashMap<Integer, List<int[]>>();

	// holds the level of severity for each map (sequence of actions that map to
	// a pattern)
	protected int[] patternSeverityLevels;

	// the key is an integer which represents an activity pattern id
	// the value is a list where each index (position in the list) refers to a Map id and the activity
	// represents an abstraction for that entry
	// e.g., entry 0,1 has an abstract activity that corresponds to the sequence
	// of actions of the same entry (0,1) in the variable allPatternsMaps
	protected Map<Integer, List<Activity>> potentialAbstractActivities = new HashMap<Integer, List<Activity>>();

	// the key is an abstract activity (taken from the
	// potentialAbstractActivities variable)
	// the value is a list of the original activities that were abstracted
	protected Map<Activity, List<Activity>> abstractedActivities = new HashMap<Activity, List<Activity>>();

	// List of incident entities that were removed from the original model
	// when the abstract model was created
	protected List<IncidentEntity> removedEntities = new LinkedList<IncidentEntity>();

	// List of entities in the incident instance that were not abstracted
	protected List<IncidentEntity> unAbstractedEntities = new LinkedList<IncidentEntity>();

	// Map of incident entities (concrete entity name, abstract entity name)
	protected Map<String, String> entitiesConcreteAbstractMap = new HashMap<String, String>();

	// Map of incident connections (concrete connection name, abstract
	// connectionname)
	protected Map<String, String> connectionsConcreteAbstractMap = new HashMap<String, String>();

	// list of patterns that had no maps in the incident instance sequence
	protected List<ActivityPattern> unmappedPatterns = new LinkedList<ActivityPattern>();

	/** Entity abstraction parameters **/
	// key is class name and the list of strings are the names of its
	// superclasses
	protected Map<String, List<String>> classMap;

	// Map of abstraction level for each class name in the classMap
	// key is string class (should be one of the names in the classMap variable)
	// and value is the index of class name available in the list of class names
	// on the classMap variable
	protected Map<String, Integer> classAbstractionLevelMap;

	// maximum abstraction level
	protected final int MAX_ABSTRACTION_LEVEL = 10;

	// default abstraction level for all entities
	protected final int DEFAULT_ABSTRACTION_LEVEL = 0;

	// Logging
	protected Logger logger;
	protected boolean isPrintToScreen = true;
	protected boolean isSaveLog = false;
	protected String outputFolder;

	protected static final String STARTER_MARKER = "$";
	protected static final String SEPARATOR = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
	
	private void runLogger() {

		if (originalIncidentFilePath != null && !originalIncidentFilePath.isEmpty()) {
			outputFolder = originalIncidentFilePath.substring(0, originalIncidentFilePath.lastIndexOf("/"));
		} else {
			outputFolder = ".";
		}

		logger = new Logger();

		logger.setPrintToScreen(isPrintToScreen);
		logger.setSaveLog(isSaveLog);
		logger.setLogFolder(outputFolder + "/log");

		logger.createLogFile();

		logger.start();

	}

	public IncidentPatternExtractor() {

	}

	public IncidentPatternExtractor(IncidentDiagram incidentModel, EnvironmentDiagram systemModel) {

		originalIncidentModel = incidentModel;
		this.systemModel = systemModel;
	}

	public String getOriginalIncidentFilePath() {
		return originalIncidentFilePath;
	}

	public void setOriginalIncidentFilePath(String incidentFilePath) {
		this.originalIncidentFilePath = incidentFilePath;
	}

	public String getSystemFilePath() {
		return systemFilePath;
	}

	public void setSystemFilePath(String systemFilePath) {
		this.systemFilePath = systemFilePath;
	}

	public IncidentDiagram extract(IncidentDiagram incidentModel, EnvironmentDiagram systemModel) {

		originalIncidentModel = incidentModel;
		this.systemModel = systemModel;

		return extract();
	}

	public IncidentDiagram extract(String incidentFilepath, String systemFilePath) {

		originalIncidentFilePath = incidentFilepath;
		this.systemFilePath = systemFilePath;
//		originalIncidentModel = incidentModel;
		
		int tries = 1000;
		//update slashes
		while(originalIncidentFilePath.contains("\\") && tries >0) {
			originalIncidentFilePath = originalIncidentFilePath.replace("\\", "/");
			tries--;
		}
		
		tries = 1000;
		while(systemFilePath.contains("\\") && tries >0) {
			systemFilePath = systemFilePath.replace("\\", "/");
			tries--;
		}
		
		IncidentDiagram incidentModel = ModelsHandler.addIncidentModel(originalIncidentFilePath);

		EnvironmentDiagram systemModel = ModelsHandler.addSystemModel(this.systemFilePath);

		this.systemModel = systemModel;
		originalIncidentModel = incidentModel;
		
		return extract();
	}

	public IncidentDiagram extract() {

		runLogger();
		
		if (originalIncidentModel == null) {
			logger.putError("Incident model is null");
			return null;
		}

		if (systemModel == null) {
			logger.putError("System model is null");
			return null;
		}

	
		// start timing
		long startTime = Calendar.getInstance().getTimeInMillis();

		logger.putMessage("#####################Incident Pattern Extraction#####################");

		// print incident instance file name, if available
		if (originalIncidentFilePath != null && !originalIncidentFilePath.isEmpty()) {
			logger.putMessage("*Incident instance file: [" + originalIncidentFilePath + "]");
		} else {

		}

		// print system file name, if available
		if (originalIncidentFilePath != null && !originalIncidentFilePath.isEmpty()) {
			logger.putMessage("*System file: [" + systemFilePath + "]");
		}

		// create a copy
		abstractIncidentModel = ModelsHandler.cloneIncidentModel(originalIncidentModel);

		// =======Load patterns====================
		logger.putMessage("Load activity patterns...");
	
		activityPatterns = new LinkedList<ActivityPattern>();

		Map<String, ActivityPattern> ptrs = ModelsHandler.getActivityPatterns();

		for (ActivityPattern ptr : ptrs.values()) {
//			System.out.println(ptr.getName());
			activityPatterns.add(ptr);
		}

//		logger.putMessage("Activity patterns loaded: " + ptrs.keySet());

		/**
		 * =======Create Abstract Incident Model=========================== The
		 * process for creating an abstract incident model: 1-Abstract data of
		 * crime script (e.g., script category) 2-abstract activities 2.1-Match
		 * patterns to the incident instance 2.2-Find a set of matched patterns
		 * that could be mapped to the incident (using constraint solver)
		 * 2.3-Replace incident instance activities with pattern activities
		 * 2.4-Perform a [general] abstraction of instance activities that are
		 * not matched to patterns (currently not implemented) 3-Abstract
		 * entities. For each entity in the incident instance find an asset with
		 * the same name in the system model. If found, then return an abstract
		 * asset of it and update the incident entity with information of the
		 * returned abstract asset
		 **/

		// =======Abstract CrimeScript===================
		logger.putMessage("Abstracting CrimeScript data...");

//		logger.putMessage("Create abstract CrimeScript entity from original");
		updateCrimeScriptData();

		// =======Abstract Activities====================
		logger.putMessage("Abstracting Activities...");

		/** 1-Find map matches for all patterns in the incident model **/
		logger.putMessage("Mapping activity patterns to the sequence of actions...");

		mapPatterns();

		/**
		 * 2-Take best solution found and apply it (i.e. add new abstract
		 * activities to the incident model while removing the corresponding
		 * ones based on the patterns used)
		 **/
		logger.putMessage("Creating abstract activity sequence...");

		updateMatchedPatternsInModel();

		/**
		 * 3-For the rest of the activities (i.e. not matched to a pattern) a
		 * general abstraction is done, mainly, abstracting name and assets
		 **/
		abstractUnmatchedActivities(); // *maybe not needed, currently no effect

		// =======Abstract entities====================
		logger.putMessage("Abstracting Incident Entities...");

		// remove unused entities after abstracting activities, then use the
		// system model
		// to create
		logger.putMessage("Generating abstract assets for assets in the incident instance...");

		abstractEntities();

		/** ================================================================ **/

		// =======Print results========================
		logger.putSeparator();

		logger.putMessage("Printing results...");

		// prints details of each activity if true
		boolean printActivityDetails = true;

		// prints separator
		boolean isDecorated = true;


		// print identified map between activity patterns and actions
		logger.putMessage(getInputPatternMap());
		
		// print original incident model
		logger.putMessage(getOriginalIncidentModel(printActivityDetails));

		// print abstracted incident model
		logger.putMessage(getAbstractIncidentModel(printActivityDetails));

		// print removed entities
		logger.putMessage(getRemovedEntities(isDecorated));

		// print abstracted activities
		logger.putMessage(getAbstractActivities(isDecorated));

		// print un-abstracted entities
		logger.putMessage(getUnAbstractedEntities(isDecorated));

		// print concrete-to-abstract entity map
		logger.putMessage(getConcreteAbstractEntityMap(isDecorated));

		// print concrete-to=abstract connection map
		logger.putMessage(getConcreteAbstractConnectionMap(isDecorated));

		// =======Save abstract model==================
		logger.putSeparator();

		String abstractModelFilePath = null;

		if (originalIncidentFilePath != null) {
			abstractModelFilePath = originalIncidentFilePath.replace(".cpi", "_abstract.cpi");
		} else {
			Random rand = new Random();
			abstractModelFilePath = "abstractIncident_" + rand.nextInt(1000) + ".cpi";
		}

		ModelsHandler.saveIncidentModel(abstractIncidentModel, abstractModelFilePath);
		logger.putMessage("Extracted incident model is saved to:" + abstractModelFilePath);

		long endTime = Calendar.getInstance().getTimeInMillis();
		
		long timePassed = endTime - startTime;

		int secMils = (int) timePassed % 1000;
		int hours = (int) (timePassed / 3600000) % 60;
		int mins = (int) (timePassed / 60000) % 60;
		int secs = (int) (timePassed / 1000) % 60;

		// execution time
		logger.putMessage("##################### Execution Completed #####################");
		logger.putMessage(
				"Execution time: " + timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]");

		logger.terminateLogging();

		return abstractIncidentModel;
	}

	
	/**
	 * Find an [optimal] solution to the pattern maps generated, then add
	 * potential abstract activities, which correspond to the solution found, to
	 * the incident model
	 */
	public void updateMatchedPatternsInModel() {

		if (allPatternsMaps == null || allPatternsMaps.isEmpty()) {
			return;
		}

		// calculates the severity level of each maps
		calculateMapsSeveriy();

		PatternMappingSolver solver = new PatternMappingSolver();

		List<int[]> bestSolution = solver.findOptimalSolution2(allPatternsMaps, patternSeverityLevels);
		solver.printOptimalSolution();

		// solver.findSolutions(allPatternsMaps, patternSeverityLevels);
		// solver.printAllSolutions();

		int[] bestSolutionPatternIDs = solver.getOptimalSolutionPatternsID();
		int[] bestSolutionMapIDs = solver.getOptimalSolutionMapsID();

		// System.out.println(bestSolution.size());
		for (int i = 0; i < bestSolution.size(); i++) {

			// get activity sequence of the solution
			int[] activitiesIDs = bestSolution.get(i);

			List<Activity> activitySequence = abstractIncidentModel.getActivitySequence(activitiesIDs);

			int patternID = bestSolutionPatternIDs[i];
			int MapID = bestSolutionMapIDs[i];

			// get potential abstract activity generated from matching the
			// pattern given by pattern id
			Activity abstractActivity = potentialAbstractActivities.get(patternID).get(MapID);

			// update next and previous activities of the potential activity
			replaceNextActivities(activitySequence.get(activitySequence.size() - 1), abstractActivity);
			replacePreviousActivities(activitySequence.get(0), abstractActivity);

			// add the potential activity to the sequence
			addNewActivityToSequence(abstractActivity, activitySequence.get(0));

			// remove the mapped activity sequence from the incident sequence
			removeMappedActivitiesFromSequence(activitySequence);

			// save the mapping of the abstrac to the sequence
			abstractedActivities.put(abstractActivity, activitySequence);
		}

		// update incident model information
		abstractIncidentModel.setActivity(null);
		abstractIncidentModel.setActivitySequence(null);
		abstractIncidentModel.getActivitySequence();

	}

	protected void calculateMapsSeveriy() {

		int numOfPatternMapped = allPatternsMaps.size();

		patternSeverityLevels = new int[numOfPatternMapped];

		int i = 0;
		for (Integer patternIndex : allPatternsMaps.keySet()) {
			patternSeverityLevels[i] = activityPatterns.get(patternIndex).getSeverity().getValue();
			i++;
		}

	}

	/**
	 * For all activities in the incident instance that were not abstracted by
	 * the matched patterns a general abstraction is performed over each
	 */
	public void abstractUnmatchedActivities() {

		// TBI
	}

	/**
	 * Generate how patterns can be mapped to incident instance
	 * 
	 */
	protected Map<Integer, List<int[]>> mapPatterns() {

		// create a local signature of the incident model
		abstractIncidentModel.createBigraphSignature();

		// create map variable to hold results of mapping a pattern to the
		// incident
		Map<String, List<String>> patternMaps;

		// create a map variable to find solutions to how differnet patterns can
		// be applied to the incident
		// Map<Integer, List<int[]>> allPatternsMaps = new HashMap<Integer,
		// List<int[]>>();

		// match patterns to incident to obtain possible maps
		for (int i = 0; i < activityPatterns.size(); i++) {

			patternMaps = matchActivityPattern(activityPatterns.get(i), i);

			List<int[]> result = convertPatternResult(patternMaps);

			if (result != null && !result.isEmpty()) {
				allPatternsMaps.put(i, result);
			} else {
				unmappedPatterns.add(activityPatterns.get(i));
			}

		}

		return allPatternsMaps;
	}

	protected List<int[]> convertPatternResult(Map<String, List<String>> patternMaps) {

		// converts result into another format obtianed from matching a pattern
		// to an incident

		if (patternMaps == null || patternMaps.isEmpty()) {
			return null;
		}

		List<int[]> result = new LinkedList<int[]>();

		for (Entry<String, List<String>> entry : patternMaps.entrySet()) {
			for (String act : entry.getValue()) {
				int[] seq = abstractIncidentModel.getActivityNumberSequence(entry.getKey(), act);
				result.add(seq);
			}
		}
		return result;
	}

	public Map<String, List<String>> matchActivityPattern(ActivityPattern activityPattern, int index) {

		// find all possible matches of the given pattern in the
		// abstractIncidentModel
		// sequence
		// assume activity pattern has one activity
		// match precondition to one (initial) activity and then the post to the
		// same or the next

		if (activityPattern == null) {
			return null;
		}

		Activity ptrActivity = !activityPattern.getAbstractActivity().isEmpty()
				? activityPattern.getAbstractActivity().get(0) : null;

		if (ptrActivity == null) {
			return null;
		}

		Activity initialActivity = null;
		Activity finalActivity = null;
		Activity currentActivity = null;
		List<String> tmpPreMatchedActivities = new LinkedList<String>();
		List<HashMap<String, String>> entityMaps = new LinkedList<HashMap<String, String>>();

		// final result. Key is activity name that satisfies the pattern
		// precondition
		// and List<String> are the activities that satisfy the postcondition of
		// the pattern
		HashMap<String, List<String>> prePostMappingActivities = new HashMap<String, List<String>>();

		boolean isptrPreMatched = false;
		boolean isptrPostMatched = false;

		for (Scene scene : abstractIncidentModel.getScene()) {

			isptrPreMatched = false;
			isptrPostMatched = false;

			initialActivity = scene.getInitialActivity();
			finalActivity = scene.getFinalActivity();
			currentActivity = initialActivity;

			// try to find a match of the pattern precondition (currently finds
			// all matches)
			while (true) {
				entityMap.clear();
				isptrPreMatched = false;

				// compare precondition (true(pre), false(post)) of the first
				// activity
				isptrPreMatched = comparePatternIncidentActivities(ptrActivity, currentActivity, true, false);

				// if match found
				if (isptrPreMatched) {

					entityMaps.add(new HashMap<String, String>(entityMap));

					// compare precondition (false (pre), true (post)) of the
					// first activity
					isptrPostMatched = comparePatternIncidentActivities(ptrActivity, currentActivity, false, true);

					if (isptrPostMatched) {

						// add to the result map
						if (!prePostMappingActivities.containsKey(currentActivity.getName())) {
							List<String> matchedActs = new LinkedList<String>();
							matchedActs.add(currentActivity.getName());
							prePostMappingActivities.put(currentActivity.getName(), matchedActs);
						} else {
							prePostMappingActivities.get(currentActivity.getName()).add(currentActivity.getName());
						}

						// =======create a potential abstract activity for the
						// matched activities
						createPotentialAbstractActivity(currentActivity, currentActivity, index);

						entityMaps.remove(entityMaps.size() - 1);
					} else {
						// added to the temp to search in next activities
						tmpPreMatchedActivities.add(currentActivity.getName());
					}
				}

				Activity next = !currentActivity.getNextActivities().isEmpty()
						? currentActivity.getNextActivities().get(0) : null;

				// if reached the last activity of the scene
				if (currentActivity.equals(finalActivity)) {
					break;
				}
				// move to check next activity
				currentActivity = next;
			}

			// try to find a match for the pattern postcondition in next
			// activities of the scene
			for (int i = 0; i < tmpPreMatchedActivities.size(); i++) {

				entityMap = entityMaps.get(i);
				isptrPostMatched = false;
				Activity preActivity = scene.getActivity(tmpPreMatchedActivities.get(i));

				currentActivity = preActivity;

				if (currentActivity == null || currentActivity.equals(finalActivity)) {
					continue;
				}

				int cnt = 0;

				// compare the pattern postcondition with the postcondition of
				// next activity
				// until the max number of activities (or actions) is reached or
				// the final activity is reached
				while (cnt < MaxNumberOfActions && !currentActivity.equals(finalActivity)) {

					Activity next = !currentActivity.getNextActivities().isEmpty()
							? currentActivity.getNextActivities().get(0) : null;

					if (next == null) {
						break;
					}

					currentActivity = next;

					// compare postconditions
					isptrPostMatched = comparePatternIncidentActivities(ptrActivity, currentActivity, false, true);

					// if there is a match from one of the activities
					if (isptrPostMatched) {

						// add to the result map
						if (!prePostMappingActivities.containsKey(preActivity.getName())) {
							List<String> matchedActs = new LinkedList<String>();
							matchedActs.add(currentActivity.getName());
							prePostMappingActivities.put(preActivity.getName(), matchedActs);
						} else {
							prePostMappingActivities.get(preActivity.getName()).add(currentActivity.getName());
						}

						// =======create a potential abstract activity for the
						// matched activities

						createPotentialAbstractActivity(preActivity, currentActivity, index);

						// one match is only taken (i.e. first match)
						break;
					}

					cnt++;
				}
			}
		}

		return prePostMappingActivities;

	}

	/**
	 * Core function for matching activity pattern condition to an incident instance model activity condition
	 * @param patternActivity
	 * @param incidentActivity
	 * @param comparePrecondition
	 * @param comparePostCondition
	 * @return
	 */
	protected boolean comparePatternIncidentActivities(Activity patternActivity, Activity incidentActivity,
			boolean comparePrecondition, boolean comparePostCondition) {

		// compare attributes and references of both activities

		// basic activity attributes:
		// 1-Behaviour Type (e.g., normal, malicious)
		// 2-System action
		// 3-Type (e.g., physical, digital)
		// 4-Duration (to be implemented)
		// 5-Timing (to be implemented)

		// 1- Behaviour Type (e.g., normal, malicious)
		Behaviour incActBehaviour = incidentActivity.getBehaviourType();
		Behaviour ptrActBehaviour = patternActivity.getBehaviourType();

		// if both activities don't have the same behaviour then return false
		if (!ptrActBehaviour.equals(incActBehaviour)) {
			return false;
		}

		// 2-system action
		String incActAction = incidentActivity.getSystemAction();
		String ptrActAction = patternActivity.getSystemAction();

		// if the pattern activity has an action that is not equal to the action
		// in the incident activity then return false
		if (ptrActAction != null && !ptrActAction.isEmpty() && !ptrActAction.equals(incActAction)) {
			return false;
		}

		// 3-Type (e.g., Physical, digital)
		ActivityType incActType = incidentActivity.getType();
		ActivityType ptrActType = patternActivity.getType();

		// if the pattern activity has a type that is different from the
		// incident activity then return false
		if (ptrActType != null && !ptrActType.equals(incActType)) {
			return false;
		}

		// activity references:
		// 1-Initiator
		// 2-Target assets
		// 3-Resources
		// 4-Exploited assets
		// 5-Location
		// 6-Vicitms
		// 7-Condition (Pre or Post)
		// 8-Accomplices (to be implemented)

		// the rule by which certain aspects of an activity should be matched to
		// a pattern is that
		// if it exists in the pattern precondition then it should be matched to
		// an entity of the activity
		// that its precondition is being test otherwise it should be matched to
		// the postcondition

		BigraphExpression ptrBigExp = null;
		BigraphExpression incBigExp = null;

		if (comparePrecondition) {
			Precondition ptrPre = patternActivity.getPrecondition();
			ptrBigExp = ptrPre != null ? (BigraphExpression) ptrPre.getExpression() : null;

			Precondition incPre = incidentActivity.getPrecondition();
			incBigExp = incPre != null ? (BigraphExpression) incPre.getExpression() : null;

		} else if (comparePostCondition) {
			Postcondition ptrPost = patternActivity.getPostcondition();
			ptrBigExp = ptrPost != null ? (BigraphExpression) ptrPost.getExpression() : null;

			Postcondition incPost = incidentActivity.getPostcondition();
			incBigExp = incPost != null ? (BigraphExpression) incPost.getExpression() : null;

		}

		if (ptrBigExp == null) {
			return false;
		}

		boolean canBeApplied = false;

		// 1-Initiator: compare initiator attributes found in the pattern
		// activity to that in the incident activity
		ActivityInitiator ptrInitiator = patternActivity.getInitiator();
		String ptrInitiatorName = ptrInitiator != null ? ((IncidentEntity) ptrInitiator).getName() : null;

		if (ptrBigExp.hasEntity(ptrInitiatorName)) {
			ActivityInitiator incInitiator = incidentActivity.getInitiator();
			canBeApplied = compareInitiators(ptrInitiator, incInitiator);

			if (!canBeApplied) {
				return false;
			}

			if (ptrInitiator != null) {
				if (!entityMap.containsKey(ptrInitiatorName)) {
					entityMap.put(ptrInitiatorName, ((IncidentEntity) incInitiator).getName());
				}
			}
		}

		// 2-Target assets
		Asset ptrTargetAsset = !patternActivity.getTargetedAssets().isEmpty()
				? patternActivity.getTargetedAssets().get(0) : null;
		String ptrTargetAssetName = ptrTargetAsset != null ? ptrTargetAsset.getName() : null;

		if (ptrBigExp.hasEntity(ptrTargetAssetName)) {

			Asset incTargetAsset = !incidentActivity.getTargetedAssets().isEmpty()
					? incidentActivity.getTargetedAssets().get(0) : null;
			canBeApplied = compareAssets(ptrTargetAsset, incTargetAsset);

			if (!canBeApplied) {
				return false;
			}

			if (ptrTargetAsset != null) {
				if (!entityMap.containsKey(ptrTargetAssetName)) {
					entityMap.put(ptrTargetAssetName, incTargetAsset.getName());
				}

			}
		}

		// 3-Resources
		Resource ptrResource = !patternActivity.getResources().isEmpty() ? patternActivity.getResources().get(0) : null;
		String ptrResourceName = ptrResource != null ? ptrResource.getName() : null;

		if (ptrBigExp.hasEntity(ptrResourceName)) {

			Resource incResource = !incidentActivity.getResources().isEmpty() ? incidentActivity.getResources().get(0)
					: null;

			canBeApplied = compareResources(ptrResource, incResource);

			if (!canBeApplied) {
				return false;
			}

			if (ptrResource != null) {
				if (!entityMap.containsKey(ptrResourceName)) {
					entityMap.put(ptrResourceName, incResource.getName());
				}
			}
		}

		// 4-Exploited assets
		Asset ptrExploitedAsset = !patternActivity.getExploitedAssets().isEmpty()
				? patternActivity.getExploitedAssets().get(0) : null;
		String ptrExploitedAssetName = ptrExploitedAsset != null ? ptrExploitedAsset.getName() : null;

		if (ptrBigExp.hasEntity(ptrExploitedAssetName)) {

			Asset incExploitedAsset = !incidentActivity.getExploitedAssets().isEmpty()
					? incidentActivity.getExploitedAssets().get(0) : null;

			canBeApplied = compareAssets(ptrExploitedAsset, incExploitedAsset);

			if (!canBeApplied) {

				// maybe pattern exploited asset could be the target of an
				// activity
				if (comparePrecondition) {
					Asset incTargetAsset = !incidentActivity.getTargetedAssets().isEmpty()
							? incidentActivity.getTargetedAssets().get(0) : null;
					canBeApplied = compareAssets(ptrExploitedAsset, incTargetAsset);

					if (!canBeApplied) {
						return false;
					}

					if (ptrExploitedAsset != null) {
						if (!entityMap.containsKey(ptrExploitedAssetName)) {
							entityMap.put(ptrExploitedAssetName, incTargetAsset.getName());
						}

					}

				} 
			} else {
				if (!entityMap.containsKey(ptrExploitedAssetName)) {
					entityMap.put(ptrExploitedAssetName, incExploitedAsset.getName());
				}
			}

		}

		// 5-Locations
		Location ptrLocation = patternActivity.getLocation();
		String ptrLocationName = ptrLocation != null ? ((IncidentEntity) ptrLocation).getName() : null;

		if (ptrBigExp.hasEntity(ptrLocationName)) {

			Location incLocation = incidentActivity.getLocation();

			canBeApplied = compareLocations(ptrLocation, incLocation);

			if (!canBeApplied) {
				return false;
			}

			if (ptrLocation != null) {
				if (!entityMap.containsKey(ptrLocationName)) {
					entityMap.put(ptrLocationName, ((IncidentEntity) incLocation).getName());
				}
			}
		}

		// 6-Vicitms
		Actor ptrVicitm = !patternActivity.getVictims().isEmpty() ? patternActivity.getVictims().get(0) : null;
		String ptrVicitmName = ptrVicitm != null ? ptrVicitm.getName() : null;

		if (ptrBigExp.hasEntity(ptrVicitmName)) {

			Actor incVicitm = !incidentActivity.getVictims().isEmpty() ? incidentActivity.getVictims().get(0) : null;

			canBeApplied = compareActors(ptrVicitm, incVicitm);

			if (!canBeApplied) {
				return false;
			}

			if (ptrVicitm != null) {
				if (!entityMap.containsKey(ptrVicitmName)) {
					entityMap.put(ptrVicitmName, incVicitm.getName());
				}
			}
		}

		// 7-Conditions (pre or post)
		canBeApplied = compareConditions(ptrBigExp, incBigExp);

		if (!canBeApplied) {
			return false;
		}

		return true;
	}

	protected boolean compareInitiators(ActivityInitiator patternActivityInitiator,
			ActivityInitiator incidentActivityInitiator) {

		if (patternActivityInitiator == null) {
			return true;
		}

		// if the pattern activity has an initiator but the incident activity
		// does not then return false
		if (patternActivityInitiator != null && incidentActivityInitiator == null) {
			return false;
		}

		// attributes to compare between initiators
		// 1-Initiator Type (i.e. are they of the same class such as actor or
		// asset)

		// if the class of the pattern activity initiator is the same (or
		// superclass) of the incident activity initiator then return false
		if (!patternActivityInitiator.getClass().isInstance(incidentActivityInitiator)) {
			return false;
		}

		boolean canBeApplied = false;

		if (Actor.class.isInstance(patternActivityInitiator)) {
			canBeApplied = compareActors((Actor) patternActivityInitiator, (Actor) incidentActivityInitiator);
		} else if (Asset.class.isInstance(patternActivityInitiator)) {
			canBeApplied = compareAssets((Asset) patternActivityInitiator, (Asset) incidentActivityInitiator);
		} else if (Resource.class.isInstance(patternActivityInitiator)) {
			canBeApplied = compareResources((Resource) patternActivityInitiator, (Resource) incidentActivityInitiator);
		} else {
			canBeApplied = compareIncidentEntities((IncidentEntity) patternActivityInitiator,
					(IncidentEntity) incidentActivityInitiator);
		}

		if (!canBeApplied) {
			return false;
		}

		return true;
	}

	protected boolean compareIncidentEntities(IncidentEntity patternEntity, IncidentEntity incidentEntity) {

		if (patternEntity != null && incidentEntity == null) {
			return false;
		}

		if (patternEntity == null) {
			return true;
		}

		// Attributes:
		// 1-Type
		// 2-Parent Entity
		// 3-Contained Entities
		// 4-Connections

		// 1-type: compare the types of both entities
		String ptrActType = patternEntity.getType() != null ? patternEntity.getType().getName() : null;
		String incActType = incidentEntity.getType() != null ? incidentEntity.getType().getName() : null;

		if (ptrActType != null && incActType == null) {
			return false;
		}

		if (ptrActType != null && incActType != null) {
			if (!isSameClassOrSuperClass(ptrActType, incActType)) {
				return false;
			}
		}

		// 2-parent entity: find out if the type of the parent entity is of the
		// same type or super type to that of the incident activity
		IncidentEntity ptrActParent = (IncidentEntity) patternEntity.getParentEntity();
		IncidentEntity incActParent = (IncidentEntity) incidentEntity.getParentEntity();

		if (ptrActParent != null && incActParent == null) {
			return false;
		}

		// check parent types (pattern parent type should be same class or super
		// class of incident parent type)
		if (ptrActParent != null && incActParent != null) {

			String ptrParentType = ptrActParent.getType() != null ? ptrActParent.getType().getName() : null;
			String incParentType = incActParent.getType() != null ? incActParent.getType().getName() : null;

			if (!isSameClassOrSuperClass(ptrParentType, incParentType)) {
				return false;
			}
		}

		// 3-Contained Entities: check number & types
		// if knowledge is complete in pattern entity then both should have the
		// same number,
		// and type of pattern contained entities should be same class or
		// superclass
		Knowledge ptrCotnainedEntitiesKnowledge = incidentEntity.getContainedAssetsKnowledge();
		// Knowledge incContainedEntitiesKnowledge =
		// patternEntity.getContainedAssetsKnowledge();

		EList<Location> ptrContainedEntities = patternEntity.getContainedEntities();
		EList<Location> incContainedEntities = incidentEntity.getContainedEntities();

		// check if the knowledge is exact then both should have the same number
		// of entities
		if (ptrCotnainedEntitiesKnowledge.equals(Knowledge.EXACT)) {
			if (ptrContainedEntities.size() != incContainedEntities.size()) {
				return false;
			}
		}

		// check for the case when the pattern has more cotnained entities
		// what should be done!? Allow it? return false?

		// check the types of both (pattern types should be same class or
		// superclass of the incident ones)
		if (!checkContainedEntities(ptrContainedEntities, incContainedEntities)) {
			return false;
		}

		// 4-Connections: check number & types
		// if knowledge is complete in pattern entity then both should have the
		// same number,
		// and type of pattern contained entities should be same class or
		// superclass
		Knowledge ptrConnectionsKnowledge = patternEntity.getConnectionsKnowledge();
		// Knowledge incContainedEntitiesKnowledge =
		// patternEntity.getContainedAssetsKnowledge();

		EList<Connection> ptrConnections = patternEntity.getConnections();
		EList<Connection> incConnections = incidentEntity.getConnections();

		// check if the knowledge is exact then both should have the same number
		// of entities
		if (ptrConnectionsKnowledge.equals(Knowledge.EXACT)) {
			if (ptrConnections.size() != incConnections.size()) {
				return false;
			}
		}

		// check for the case when the pattern has more cotnained entities
		// what should be done!? Allow it? return false?

		// check the types of both (pattern types should be same class or
		// superclass of the incident ones)
		if (!checkConnections(ptrConnections, incConnections)) {
			return false;
		}

		return true;
	}

	protected boolean compareAssets(Asset patternAsset, Asset incidentAsset) {

		if (patternAsset != null && incidentAsset == null) {
			return false;
		}

		if (patternAsset == null) {
			return true;
		}

		boolean isApplicable = compareIncidentEntities(patternAsset, incidentAsset);

		if (!isApplicable) {
			return false;
		}

		EList<Vulnerability> ptrVuls = patternAsset.getVulnerability();
		EList<Vulnerability> incVuls = incidentAsset.getVulnerability();

		// compare vulnerabilities
		List<Integer> matchedConnections = new LinkedList<Integer>();

		for (Vulnerability ptrVul : ptrVuls) {

			if (ptrVul.getName() == null || ptrVul.getName().isEmpty()) {
				continue;// ignored
			}

			boolean isVulMatched = false;

			Vulnerability incVul = null;

			for (int i = 0; i < incVuls.size(); i++) {

				if (matchedConnections.contains(i)) {
					continue;
				}

				incVul = incVuls.get(i);

				if (ptrVul.equals(incVul)) {
					matchedConnections.add(i);
					isVulMatched = true;
					break;
				}
			}

			// if none of the incident vulnerabilities match the pattern
			// vulnerabilities then it is a mismatch
			if (!isVulMatched) {
				return false;
			}

			isVulMatched = false;

		}

		return true;
	}

	protected boolean compareActors(Actor patternActor, Actor incidentActor) {

		if (patternActor != null && incidentActor == null) {
			return false;
		}

		if (patternActor == null) {
			return true;
		}

		boolean canBeApplied = compareIncidentEntities(patternActor, incidentActor);

		if (!canBeApplied) {
			return false;
		}

		// Attributes to compare:
		// 1-Role (e.g., offender, vicitm)
		// 2-Level (e.g., individual, group)

		// 1-Role
		ActorRole ptrRole = patternActor.getRole();
		ActorRole incRole = incidentActor.getRole();

		if (!ptrRole.equals(incRole)) {
			return false;
		}

		// 2-Level
		ActorLevel ptrLevel = patternActor.getLevel();
		ActorLevel incLevel = incidentActor.getLevel();

		if (!ptrLevel.equals(incLevel)) {
			return false;
		}

		return true;
	}

	protected boolean compareResources(Resource patternResource, Resource incidentResource) {

		if (patternResource != null && incidentResource == null) {
			return false;
		}

		if (patternResource == null) {
			return true;
		}

		boolean canBeApplied = compareIncidentEntities(patternResource, incidentResource);

		if (!canBeApplied) {
			return false;
		}

		// specific comparison criteria for resources can be defined here

		return true;
	}

	protected boolean compareLocations(Location patternLocation, Location incidentLocation) {

		if (patternLocation != null && incidentLocation == null) {
			return false;
		}

		if (patternLocation == null) {
			return true;
		}

		boolean canBeApplied = compareIncidentEntities((IncidentEntity) patternLocation,
				(IncidentEntity) incidentLocation);

		if (!canBeApplied) {
			return false;
		}

		// more specifc criteria to locations can be defined here
		// for example, connection ends can be further explored here

		return true;

	}

	/**
	 * Finds if the given patternType parameter is of the same class or super
	 * class of the given incidentType parameter
	 * 
	 * @param patternType
	 * @param incidentType
	 * @return
	 */
	protected boolean isSameClassOrSuperClass(String patternType, String incidentType) {

		// depends on the classes of the cyber physical system

		if (patternType != null && incidentType == null) {
			return false;
		}

		if (patternType == null && incidentType == null) {
			return true;
		}

		try {
			String potentialPatternClassName = "environment.impl." + patternType;

			if (!patternType.endsWith("Impl")) {
				potentialPatternClassName += "Impl";
			}

			Class<?> patternClass = Class.forName(potentialPatternClassName);

			String potentialIncidentClassName = "environment.impl." + incidentType;

			if (!incidentType.endsWith("Impl")) {
				potentialIncidentClassName += "Impl";
			}

			Class<?> incidentClass = Class.forName(potentialIncidentClassName);

			if (!patternClass.isAssignableFrom(incidentClass)) {
				return false;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		}

		return true;
	}

	protected boolean checkContainedEntities(EList<Location> patternContainedEntities,
			EList<Location> incidentContainedEntities) {

		// compare type
		LinkedList<Integer> matchedcontainedAssets = new LinkedList<Integer>();

		for (Location ent : patternContainedEntities) {

			IncidentEntity ptrcontainedEntity = (IncidentEntity) ent;

			if (ptrcontainedEntity.getType() == null) {
				continue;// ignored
			}

			String ptrTypeName = ptrcontainedEntity.getType().getName();

			boolean iscontainedEntityMatched = false;

			IncidentEntity incContainedEntity = null;

			for (int i = 0; i < incidentContainedEntities.size(); i++) {

				if (matchedcontainedAssets.contains(i)) {
					continue;
				}

				incContainedEntity = (IncidentEntity) incidentContainedEntities.get(i);

				String incTypeName = incContainedEntity.getType() != null ? incContainedEntity.getType().getName()
						: null;

				if (isSameClassOrSuperClass(ptrTypeName, incTypeName)) {
					matchedcontainedAssets.add(i);
					iscontainedEntityMatched = true;
					break;
				}
			}

			// if none of the incident contained entities match the pattern
			// contained entities then it is a mismatch
			if (!iscontainedEntityMatched) {
				return false;
			}

			iscontainedEntityMatched = false;

		}

		return true;
	}

	protected boolean checkConnections(EList<Connection> patternConnections, EList<Connection> incidentConnections) {

		// compare type
		LinkedList<Integer> matchedConnections = new LinkedList<Integer>();

		for (Connection ptrConnection : patternConnections) {

			if (ptrConnection.getType() == null) {
				continue;// ignored
			}

			String ptrTypeName = ptrConnection.getType().getName();

			boolean isConnectionMatched = false;

			Connection incConnection = null;

			for (int i = 0; i < incidentConnections.size(); i++) {

				if (matchedConnections.contains(i)) {
					continue;
				}

				incConnection = incidentConnections.get(i);

				String incTypeName = incConnection.getType() != null ? incConnection.getType().getName() : null;

				if (isSameClassOrSuperClass(ptrTypeName, incTypeName)) {
					matchedConnections.add(i);
					isConnectionMatched = true;
					break;
				}
			}

			// if none of the incident contained entities match the pattern
			// contained entities then it is a mismatch
			if (!isConnectionMatched) {
				return false;
			}

			isConnectionMatched = false;

		}

		return true;
	}

	protected boolean compareConditions(BigraphExpression patternCondition, BigraphExpression incidentCondition) {

		if (patternCondition == null) {
			return true;
		}

		if (patternCondition != null && (incidentCondition == null || incidentCondition.isEmpty())) {
			return false;
		}

		boolean isGround = true;
		Bigraph incBigraph = incidentCondition.createBigraph(isGround);
		Matcher matcher = new Matcher();

		if (incBigraph != null) {
			// update entities names in the pattern precondition by mapping
			// names to the incident conditions
			BigraphExpression ptrcondCopy = patternCondition.clone();

			boolean isAllMapped = updateEntityNames(ptrcondCopy);

			if (isAllMapped) {
				// create a bigraph of the pattern precondition
				Bigraph ptrBigraph = ptrcondCopy.createBigraph(!isGround);

				if (matcher.match(incBigraph, ptrBigraph).iterator().hasNext()) {
					return true;
				}

			}
		}

		return false;
	}

	protected boolean updateEntityNames(BigraphExpression patternCondition) {

		// System.out.println(entityMap);
		List<String> notFoundNames = new LinkedList<String>();
		LinkedList<Entity> visitedEntities = new LinkedList<Entity>();

		visitedEntities.addAll(patternCondition.getEntity());

		while (!visitedEntities.isEmpty()) {

			Entity entity = visitedEntities.pop();
			if (entityMap.containsKey(entity.getName())) {
				entity.setName(entityMap.get(entity.getName()));
			} else {

				notFoundNames.add(entity.getName());
			}

			visitedEntities.addAll(entity.getEntity());
		}

		// if some entities cannot be mapped then try to find a [similar] entity
		// in the incident activity
		// condition that has not yet been mapped, otherwise the conditions do
		// NOT match
		// currently if there are unmapped entities in the pattern condition
		// then it is a NO match
		if (notFoundNames.size() != 0) {
			// System.out.println(notFoundNames);
			return false;
		}

		// System.out.println("True");
		return true;
	}

	/**
	 * Creates a new activity out of the given activity sequence and activity
	 * pattern
	 * 
	 * @param activitySequence
	 *            The sequence from which a new activity is created
	 * @param activityPattern
	 *            The pattern that is used to create pre and post conditions
	 *            from
	 * @return New <em>Activity</em> that replaces the sequence in the
	 *         <em>Incident Diagram</em>
	 */
	public Activity createPotentialAbstractActivity(List<Activity> activitySequence, int activityPatternIndex) {

		if (activitySequence == null || activitySequence.size() == 0) {
			return null;
		}

		ActivityPattern activityPattern = activityPatterns.get(activityPatternIndex);

		if (activityPattern == null) {
			return null;
		}

		Activity initialActivity = activitySequence.get(0);
		Activity finalActivity = activitySequence.get(activitySequence.size() - 1);
		Activity patternActivity = !activityPattern.getAbstractActivity().isEmpty()
				? activityPattern.getAbstractActivity().get(0) : null;

		Activity abstractActivity = instance.createActivity();

		// set the new activity name
		int tries = 500;
		boolean isUnique = false;
		Random rand = new Random();
		String actName = null;
		int randNum = 0;

		while (tries > 0 && !isUnique) {

			randNum = rand.nextInt(IncidentPatternExtractor.MAX_LENGTH);
			actName = IncidentPatternExtractor.ACTIVITY_NAME + "_" + randNum;
			isUnique = !abstractIncidentModel.activityNameExists(actName);
			tries--;
		}

		// set name
		abstractActivity.setName(actName);

		// set type (e.g., physical, digital)
		abstractActivity.setType(initialActivity.getType());

		// set behaviour (e.g., normal, malicious)
		abstractActivity.setBehaviourType(initialActivity.getBehaviourType());

		// set system action name
		abstractActivity.setSystemAction(initialActivity.getSystemAction());

		// set initiator
		if (patternActivity.getInitiator() != null) {

			String patternInitiatorName = ((IncidentEntity) patternActivity.getInitiator()).getName();

			String firstInitiatorName = initialActivity.getInitiator() != null
					? ((IncidentEntity) initialActivity.getInitiator()).getName() : null;

			String finalInitiatorName = finalActivity.getInitiator() != null
					? ((IncidentEntity) finalActivity.getInitiator()).getName() : null;
			ActivityInitiator mergedActivityInitiator = null;

			if (firstInitiatorName != null && entityMap.containsKey(patternInitiatorName)
					&& entityMap.containsValue(firstInitiatorName)) {
				mergedActivityInitiator = initialActivity.getInitiator();
			} else if (finalInitiatorName != null && entityMap.containsKey(patternInitiatorName)
					&& entityMap.containsValue(finalInitiatorName)) {
				mergedActivityInitiator = finalActivity.getInitiator();
			}

			abstractActivity.setInitiator(mergedActivityInitiator);
		}

		// set target asset
		if (!patternActivity.getTargetedAssets().isEmpty()) {

			String patternTargetAssetName = patternActivity.getTargetedAssets().get(0).getName();

			String firstTargetName = !initialActivity.getTargetedAssets().isEmpty()
					? initialActivity.getTargetedAssets().get(0).getName() : null;

			String finalTargetName = !finalActivity.getTargetedAssets().isEmpty()
					? finalActivity.getTargetedAssets().get(0).getName() : null;
			Asset mergedActivityTargetAsset = null;

			if (finalTargetName != null && entityMap.containsKey(patternTargetAssetName)
					&& entityMap.containsValue(finalTargetName)) {
				mergedActivityTargetAsset = finalActivity.getTargetedAssets().get(0);
			} else if (firstTargetName != null && entityMap.containsKey(patternTargetAssetName)
					&& entityMap.containsValue(firstTargetName)) {
				mergedActivityTargetAsset = initialActivity.getTargetedAssets().get(0);
			}

			if (mergedActivityTargetAsset != null) {
				abstractActivity.getTargetedAssets().add(mergedActivityTargetAsset);
			}

		}

		// set resources
		if (!patternActivity.getResources().isEmpty()) {
			String patternResourceName = patternActivity.getResources().get(0).getName();

			String firstResourceName = !initialActivity.getResources().isEmpty()
					? initialActivity.getResources().get(0).getName() : null;

			String finalResourceName = !finalActivity.getResources().isEmpty()
					? finalActivity.getResources().get(0).getName() : null;
			Resource mergedActivityResource = null;

			if (finalResourceName != null && entityMap.containsKey(patternResourceName)
					&& entityMap.containsValue(finalResourceName)) {
				mergedActivityResource = finalActivity.getResources().get(0);
			} else if (firstResourceName != null && entityMap.containsKey(patternResourceName)
					&& entityMap.containsValue(firstResourceName)) {
				mergedActivityResource = initialActivity.getResources().get(0);
			}

			abstractActivity.getResources().add(mergedActivityResource);
		}

		// set location
		if (patternActivity.getLocation() != null) {
			String patternLocationName = ((IncidentEntity) patternActivity.getLocation()).getName();

			String firstLocationName = initialActivity.getLocation() != null
					? ((IncidentEntity) initialActivity.getLocation()).getName() : null;

			String finalLocationName = finalActivity.getLocation() != null
					? ((IncidentEntity) finalActivity.getLocation()).getName() : null;

			Location mergedActivityLocation = null;

			if (finalLocationName != null && entityMap.containsKey(patternLocationName)
					&& entityMap.containsValue(finalLocationName)) {
				mergedActivityLocation = finalActivity.getLocation();
			} else if (firstLocationName != null && entityMap.containsKey(patternLocationName)
					&& entityMap.containsValue(firstLocationName)) {
				mergedActivityLocation = initialActivity.getLocation();
			}

			abstractActivity.setLocation(mergedActivityLocation);
		}

		// set exploited asset
		if (!patternActivity.getExploitedAssets().isEmpty()) {
			String patternExploitedAssetName = patternActivity.getExploitedAssets().get(0).getName();

			String firstExploitedAssetName = !initialActivity.getExploitedAssets().isEmpty()
					? initialActivity.getExploitedAssets().get(0).getName() : null;

			String finalExploitedAssetName = !finalActivity.getExploitedAssets().isEmpty()
					? finalActivity.getExploitedAssets().get(0).getName() : null;

			Asset mergedActivityExploitedAsset = null;

			if (firstExploitedAssetName != null && entityMap.containsKey(patternExploitedAssetName)
					&& entityMap.containsValue(firstExploitedAssetName)) {
				mergedActivityExploitedAsset = initialActivity.getExploitedAssets().get(0);
			} else if (finalExploitedAssetName != null && entityMap.containsKey(patternExploitedAssetName)
					&& entityMap.containsValue(finalExploitedAssetName)) {
				mergedActivityExploitedAsset = finalActivity.getExploitedAssets().get(0);
			}

			abstractActivity.getExploitedAssets().add(mergedActivityExploitedAsset);
		}

		// set conditions (pre & post)
		// set preconditions
		BigraphExpression bigExpPre = ((BigraphExpression) patternActivity.getPrecondition().getExpression()).clone();
		updateEntityNames(bigExpPre);

		abstractActivity.getPrecondition().setExpression(bigExpPre);

		// set postcondition
		BigraphExpression bigExpPost = ((BigraphExpression) patternActivity.getPostcondition().getExpression()).clone();
		updateEntityNames(bigExpPost);

		abstractActivity.getPostcondition().setExpression(bigExpPost);

		// add new activity to potential abstract activities of the given
		// pattern
		if (potentialAbstractActivities.containsKey(activityPatternIndex)) {
			potentialAbstractActivities.get(activityPatternIndex).add(abstractActivity);
		} else {
			List<Activity> abstractActs = new LinkedList<Activity>();
			abstractActs.add(abstractActivity);
			potentialAbstractActivities.put(activityPatternIndex, abstractActs);
		}

		return abstractActivity;
	}

	public Activity createPotentialAbstractActivity(Activity startingActivity, Activity endActivity,
			int activityPatternIndex) {

		if (startingActivity == null || endActivity == null) {
			return null;
		}

		List<Activity> activitySequence = new LinkedList<Activity>();

		activitySequence.add(startingActivity);

		if (startingActivity.equals(endActivity)) {
			return createPotentialAbstractActivity(activitySequence, activityPatternIndex);
		}

		// assuming there's only one next activity. In future, an algorithm for
		// finding all possible sequences between two activities can be
		// implemented
		Activity nextActivity = null;

		if (startingActivity.getNextActivities().size() > 0) {
			nextActivity = startingActivity.getNextActivities().get(0);
		}

		while (nextActivity != null) {

			activitySequence.add(nextActivity);

			if (nextActivity.equals(endActivity)) {
				break;
			}

			if (nextActivity.getNextActivities().size() > 0) {
				nextActivity = nextActivity.getNextActivities().get(0);
			} else {
				nextActivity = null;
			}

		}

		return createPotentialAbstractActivity(activitySequence, activityPatternIndex);
	}

	/**
	 * Adds the new merged activity to the original sequence of activities. It
	 * is added in place of the given second argument
	 * 
	 * @param mergedActivity
	 *            new merged activity
	 * @param originalActivity
	 *            the activity in aequence where the new activity will be placed
	 *            before
	 */
	protected void addNewActivityToSequence(Activity newActivity, Activity originalActivity) {

		for (Scene scene : abstractIncidentModel.getScene()) {
			EList<Activity> sceneActivities = scene.getActivity();
			for (int i = 0; i < sceneActivities.size(); i++) {
				if (sceneActivities.get(i).equals(originalActivity)) {
					sceneActivities.add(i, newActivity);
					break;
				}
			}
		}

	}

	protected void removeMappedActivitiesFromSequence(List<Activity> activitySequence) {

		Scene scene = null;

		Scenes_Loop: for (Scene sc : abstractIncidentModel.getScene()) {
			scene = sc;
			EList<Activity> sceneActivities = scene.getActivity();
			for (int i = 0; i < sceneActivities.size(); i++) {
				if (sceneActivities.get(i).equals(activitySequence.get(0))) {
					break Scenes_Loop;
				}
			}
		}

		if (scene != null) {
			scene.getActivity().removeAll(activitySequence);
		}
	}

	protected void replaceNextActivities(Activity sourceActivity, Activity targetActivity) {

		// set next activities, which is the next activity in the source
		// activity
		if (sourceActivity.getNextActivities().size() > 0) {
			for (Activity nextAct : sourceActivity.getNextActivities()) {
				targetActivity.getNextActivities().add(nextAct);

				// set previous activity of the next activity to be the target
				// activity
				nextAct.getPreviousActivities().remove(sourceActivity);
				nextAct.getPreviousActivities().add(targetActivity);
			}
		}
	}

	protected void replacePreviousActivities(Activity sourceActivity, Activity targetActivity) {

		if (sourceActivity.getPreviousActivities().size() > 0) {
			for (Activity prevActivity : sourceActivity.getPreviousActivities()) {
				targetActivity.getPreviousActivities().add(prevActivity);

				// set the next activity of the previous activity to be the
				// target activity
				prevActivity.getNextActivities().remove(sourceActivity);
				prevActivity.getNextActivities().add(targetActivity);
			}
		}
	}

	/**
	 * Create an abstraction of each entity in the incident model by looking for
	 * it in the system model then if found an abstract of that asset is
	 * returned and the incident entity is updated with information in the
	 * matched asset. Otherwise (i.e. if no asset in the system model is found)
	 * the incident entity remains as is
	 */
	public void abstractEntities() {

		// 1-look for assets that exist in the system model
		// 2-if one found, then use the system model to find an abstraction of
		// this entity based on the system meta-model
		// 3-replace the original asset in the incident model with its
		// abstraction from the system model
		// In case an asset is not found in the system model then it remains as
		// is

		List<IncidentEntity> entities = abstractIncidentModel.getEntity();

		environment.Asset systemAsset = null;
		String entityName = null;

		// remove unused entities in activities. Unused means an incident entity
		// (asset, actor, or resource) that it has not been
		// used in any of the activities conditions and neither its parent,
		// any of its contained assets, or any of its connections
		removedEntities = abstractIncidentModel.removeUnusedEntities();

		entities = abstractIncidentModel.getEntity();

		// create an abstract entity for each instance entity using system
		// assets
		for (IncidentEntity entity : entities) {
			entityName = entity.getName();
			systemAsset = systemModel.getAsset(entityName);

			if (systemAsset != null) {
				IncidentEntity newEntity = abstractEntity(entity, systemAsset);
				entitiesConcreteAbstractMap.put(entityName, newEntity.getName());
			} else {
				unAbstractedEntities.add(entity);
			}
		}
	}

	protected IncidentEntity abstractEntity(IncidentEntity entity, environment.Asset systemAsset) {

		if (entity == null || systemAsset == null) {
			return null;
		}

		environment.Asset abstractedSystemAsset = systemAsset.abstractAsset();

		if (abstractedSystemAsset == null) {
			return null;
		}

		String oldEntityName = entity.getName();

		String incidentEntityTypeName = "";

		IncidentEntity abstractedEntity = entity;

		abstractedEntity.setName(abstractedSystemAsset.getName());

		String newEntityName = abstractedEntity.getName();

		//// update type to be of the same type as the abstracted asset
		Type type = abstractedEntity.getType();

		if (type == null) {
			type = instance.createType();
		}

		// set type name. Can be done either using the type generate by the
		// abstract asset, or the type used from the classMap

		// setting the name using generated type from abstract asset
		// type.setName(abstractedSystemAsset.getClass().getSimpleName().replace("Impl",
		// ""));

		// setting type name using the classMap
		// takes the first option (i.e. 0) other options are more abstract

		// for (Entry<String, List<String>> entry : classMap.entrySet()) {
		// System.out.println(entry.getKey() + " " + entry.getValue());
		// }

		incidentEntityTypeName = getAbstractType(systemAsset);

		type.setName(incidentEntityTypeName);
		// set type
		abstractedEntity.setType(type);

		// set mobility

		if (abstractedSystemAsset.getMobility() == environment.Mobility.MOVABLE) {
			abstractedEntity.setMobility(Mobility.MOVABLE);
		} else if (abstractedSystemAsset.getMobility() == environment.Mobility.FIXED) {
			abstractedEntity.setMobility(Mobility.FIXED);
		}

		// properties
		for (environment.Property astProp : abstractedSystemAsset.getProperty()) {
			cyberPhysical_Incident.Property prop = instance.createProperty();

			prop.setName(astProp.getName());
			prop.setValue(astProp.getValue());

			abstractedEntity.getProperties().add(prop);
		}

		// Vulnerabilities for assets
		if (Asset.class.isInstance(abstractedEntity)) {
			for (environment.Vulnerability astVul : abstractedSystemAsset.getVulnerabilities()) {
				Vulnerability vul = instance.createVulnerability();

				vul.setName(astVul.getName());
				vul.setDescription(astVul.getDescription());
				vul.setURL(astVul.getURL());

				// severity level
				switch (astVul.getSeverity().getValue()) {

				case environment.Level.HIGH_VALUE:
					vul.setSeverity(Level.HIGH);
					break;
				case environment.Level.MEDIUM_VALUE:
					vul.setSeverity(Level.MEDIUM);
					break;
				case environment.Level.LOW_VALUE:
					vul.setSeverity(Level.HIGH);
					break;
				case environment.Level.UNKNOWN_VALUE:
					vul.setSeverity(Level.UNKNOWN);
					break;
				default:
					vul.setSeverity(Level.UNKNOWN);
				}

				((Asset) abstractedEntity).getVulnerability().add(vul);
			}
		}

		// abstract entity connections
		for (Connection con : entity.getConnections()) {

			String oldConnName = con.getName();
			environment.Connection astCon = systemModel.getConnection(oldConnName);

			if (astCon == null) {
				continue;
			}

			environment.Connection astConAbstract = astCon.abstractConnection();

			con.setName(astConAbstract.getName());

			String newConnName = con.getName();

			connectionsConcreteAbstractMap.put(oldConnName, newConnName);

			con.setBidirectional(astConAbstract.isBidirectional());

			// set vulnerabilities
			for (environment.Vulnerability astVul : astConAbstract.getVulnerabilities()) {
				Vulnerability vul = instance.createVulnerability();

				vul.setName(astVul.getName());
				vul.setDescription(astVul.getDescription());
				vul.setURL(astVul.getURL());

				// severity level
				switch (astVul.getSeverity().getValue()) {

				case environment.Level.HIGH_VALUE:
					vul.setSeverity(Level.HIGH);
					break;
				case environment.Level.MEDIUM_VALUE:
					vul.setSeverity(Level.MEDIUM);
					break;
				case environment.Level.LOW_VALUE:
					vul.setSeverity(Level.HIGH);
					break;
				case environment.Level.UNKNOWN_VALUE:
					vul.setSeverity(Level.UNKNOWN);
					break;
				default:
					vul.setSeverity(Level.UNKNOWN);
				}

				con.getVulnerabilities().add(vul);
			}

			// set properties
			for (environment.Property astProp : astConAbstract.getProperties()) {
				cyberPhysical_Incident.Property prop = instance.createProperty();

				prop.setName(astProp.getName());
				prop.setValue(astProp.getValue());

				con.getProperties().add(prop);
			}
		}

		//// update all the entity name in all the conditions of the incident
		//// activities
		for (Activity act : abstractIncidentModel.getActivity()) {
			act.replaceEntityName(oldEntityName, newEntityName);
		}

		return abstractedEntity;
	}

	protected String getAbstractType(environment.Asset systemAsset) {

		String type = "";

		if (classMap == null || classMap.isEmpty()) {
			classMap = generateAssetClassAbstractionLevels();
		}

		// get asset class name
		String assetClassName = systemAsset.getClass().getSimpleName().replace("Impl", "");

		// get abstraction classes for the asset class (e.g., smart light,
		// computing device)
		List<String> abstractionLevels = classMap.get(assetClassName);

		// determine which level of abstraction to choose from the list of
		// levels
		if (abstractionLevels != null && !abstractionLevels.isEmpty()) {

			// use the level of abstractions map to determine required level
			Integer levelIndex = classAbstractionLevelMap.get(assetClassName);

			if (levelIndex != null && levelIndex > 0 && levelIndex < abstractionLevels.size()) {

				type = abstractionLevels.get(levelIndex);

				// if there's no level specified for the asset class then get
				// the default
			} else {
				type = abstractionLevels.get(DEFAULT_ABSTRACTION_LEVEL);
			}

			// if the asset class name is not in the map, then the type of the
			// entity will be the same as the class
		} else {
			type = assetClassName;
		}

		return type;
	}

	protected void updateCrimeScriptData() {

		/**
		 * Update some metadata for the abstract Currently includes: 1-Category
		 * (e.g., track, script, meta-script): For example form INSTANCE to
		 * TRACK/SCRIPT 2- (future) Higher level script name: which links the
		 * abstract to other scripts
		 */

		//// update incident category
		CrimeScript originalCrimeScript = originalIncidentModel.getCrimeScript();
		int currentCategoryValue = originalCrimeScript != null ? originalCrimeScript.getCategory().getValue() : null;

		CrimeScript absCrimeScript = abstractIncidentModel.getCrimeScript();

		if (absCrimeScript == null) {
			return;
		}

		switch (currentCategoryValue) {

		case ScriptCategory.INSTANCE_VALUE: // incident instance
			absCrimeScript.setCategory(ScriptCategory.PATTERN); // least
																// abstract
			break;
//		case ScriptCategory.PATTERN_VALUE:
//			absCrimeScript.setCategory(ScriptCategory.PROTOPATTERN);
//			break;
//		case ScriptCategory.PROTOPATTERN_VALUE:
//			absCrimeScript.setCategory(ScriptCategory.METAPATTERN); // most
//																	// abstract
//			break;
		default:
			absCrimeScript.setCategory(ScriptCategory.PATTERN); // default state
		}
	}

	public Map<String, List<String>> generateAssetClassAbstractionLevels() {

		Method[] packageMethods = CyberPhysicalSystemPackage.class.getDeclaredMethods();

		Map<String, List<String>> classMap = new HashMap<String, List<String>>();
		Map<String, Integer> defaultAbstractionLevel = new HashMap<String, Integer>();

		String className = null;

		for (Method mthd : packageMethods) {

			className = mthd.getName();
			Class cls = mthd.getReturnType();

			// only consider EClass as the classes
			if (!cls.getSimpleName().equals("EClass")) {
				continue;
			}

			// remove [get] at the beginning
			// if it contains __ then it is not a class its an attribut
			if (className.startsWith("get")) {
				className = className.replace("get", "");
			}

			// create a class from the name
			String fullClassName = "environment.impl." + className + "Impl";

			int numOfLevels = 10;

			try {

				Class potentialClass = Class.forName(fullClassName);

				List<String> classHierarchy = new LinkedList<String>();
				int cnt = 0;

				do {
					classHierarchy.add(potentialClass.getSimpleName().replace("Impl", ""));
					potentialClass = potentialClass.getSuperclass();
					cnt++;
				} while (potentialClass != null && !potentialClass.getSimpleName().equals("Container")
						&& cnt < numOfLevels);

				// add new entry to the map
				classMap.put(className, classHierarchy);

				// default class abstaction is the same class
				defaultAbstractionLevel.put(className, DEFAULT_ABSTRACTION_LEVEL);

			} catch (ClassNotFoundException e) {

				// if there's no such class then continue
				continue;
			}
		}

		// set default abstraction level

		this.classMap = classMap;
		this.classAbstractionLevelMap = defaultAbstractionLevel;

		return classMap;
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param allSolutions
	 * @param patternIDs
	 * @param mapIDs
	 * @param allSolutionsSeverity
	 */
	public String getAllSolutions(Map<Integer, List<int[]>> allSolutions, List<int[]> patternIDs, List<int[]> mapIDs,
			List<Integer> allSolutionsSeverity) {

		StringBuilder str = new StringBuilder();

		int numOfPatterns = -1;
		int maxPatternsSolutionIndex = -1;
		int numOfActions = -1;
		int maxActionsSolutionIndex = -1;

		List<Integer> ptrsNum = new LinkedList<Integer>();

		// count the maximum number of patterns used in any solution
		if (patternIDs != null) {
			for (int j = 0; j < patternIDs.size(); j++) {
				int[] ary = patternIDs.get(j);
				ptrsNum.clear();
				for (int i = 1; i < ary.length; i++) {
					if (!ptrsNum.contains(ary[i])) {
						ptrsNum.add(ary[i]);
					}
				}

				if (numOfPatterns < ptrsNum.size()) {
					numOfPatterns = ptrsNum.size();
					maxPatternsSolutionIndex = j;
				}
			}
		}

		// count the maximum number of actions used in any solution
		if (allSolutions != null) {
			for (int i = 0; i < allSolutions.size(); i++) {
				int tmp = 0;
				List<int[]> solution = allSolutions.get(i);
				// get number of actions in this solution
				for (int[] actions : solution) {
					tmp += actions.length;
				}
				if (tmp > numOfActions) {
					numOfActions = tmp;
					maxActionsSolutionIndex = i;
				}
			}
		}

		str.append(STARTER_MARKER).append("Solutions Summary:\n");

		// =======some statistics
		str.append("-Number of Solutions found:").append(allSolutions.size()).append("\n")
				.append("-Max number of Patterns used:").append(numOfPatterns)
				.append(" (Solution [" + maxPatternsSolutionIndex + "])").append("\n")
				.append("-Max number of Actions mapped:").append(numOfActions)
				.append(" (Solution [" + maxActionsSolutionIndex + "])").append("\n\n");

		// print solutions
		for (int i = 0; i < allSolutions.size(); i++) {
			str.append("-Solution [").append(i).append("]:\n");
			str.append(solutionToString(allSolutions.get(i), patternIDs.get(i), mapIDs.get(i),
					allSolutionsSeverity.get(i)));
			str.append("\n");
		}

		str.append(SEPARATOR);

		return str.toString();
	}

	protected String solutionToString(List<int[]> solution, int[] patternIDs, int[] mapIDs, int allSolutionsSeverity) {

		StringBuilder str = new StringBuilder();

		// print maps
		str.append("--Maps:");
		for (int j = 0; j < solution.size(); j++) {
			str.append("[");
			for (int activityID : solution.get(j)) {
				str.append(abstractIncidentModel.getActivityName(activityID)).append(", ");
			}
			str.deleteCharAt(str.lastIndexOf(" "));
			str.deleteCharAt(str.lastIndexOf(","));
			str.append("], ");
		}
		str.deleteCharAt(str.lastIndexOf(","));
		str.append("\n");

		// print patterns ids used
		str.append("--Pattern:[");
		for (int patternID : patternIDs) {
			str.append(activityPatterns.get(patternID).getName()).append(", ");
		}
		str.deleteCharAt(str.lastIndexOf(" "));
		str.deleteCharAt(str.lastIndexOf(","));
		str.append("]\n");

		// print maps ids used
		str.append("--Map IDs:").append(Arrays.toString(mapIDs)).append("\n");

		// print each solution severity sum
		str.append("--Severity:").append(allSolutionsSeverity);

		return str.toString();
	}

	public String getOptimalSolution(List<int[]> optimalSolution, int[] optimalSolutionPatternsID,
			int[] optimalSolutionMapsID, int optimalSolutionSeverity) {

		StringBuilder str = new StringBuilder();

		str.append(STARTER_MARKER).append("Optimal Solution:\n");
		// print maps
		str.append("Maps:");
		for (int i = 0; i < optimalSolution.size(); i++) {
			str.append("[");
			for (int j = 0; j < optimalSolution.get(i).length; j++) {
				int actID = optimalSolution.get(i)[j];
				str.append(abstractIncidentModel.getActivityName(actID)).append(", ");
			}
			str.deleteCharAt(str.lastIndexOf(" "));
			str.deleteCharAt(str.lastIndexOf(","));
			str.append("], ");
		}

		str.deleteCharAt(str.lastIndexOf(" "));
		str.deleteCharAt(str.lastIndexOf(","));

		// print patterns ids used
		str.append("Pattern IDs:").append(Arrays.toString(optimalSolutionPatternsID)).append("\n");

		// print maps ids used
		str.append("Map IDs:").append(Arrays.toString(optimalSolutionMapsID)).append("\n");

		// print each solution severity sum
		str.append("severity:").append(optimalSolutionSeverity).append("\n");

		str.append(SEPARATOR);

		return str.toString();
	}

	public String getInputPatternMap() {

		StringBuilder str = new StringBuilder();

		str.append(STARTER_MARKER).append("Input Patterns Maps:\n");

		for (Entry<Integer, List<int[]>> entry : allPatternsMaps.entrySet()) {

			str.append("-Pattern[").append(activityPatterns.get(entry.getKey()).getName()).append("]: ");

			if (entry.getValue().size() > 0) {
				for (int i = 0; i < entry.getValue().size(); i++) {
					int[] ary = entry.getValue().get(i);
					str.append("[");
					for (int activityID : ary) {
						str.append(originalIncidentModel.getActivityName(activityID) + ", ");
					}

					str.deleteCharAt(str.lastIndexOf(" "));
					str.deleteCharAt(str.lastIndexOf(","));
					str.append("]");

					// print potential incident corresponding to this map
					str.append("-->(").append(potentialAbstractActivities.get(entry.getKey()).get(i).getName())
							.append("), ");

				}

				str.deleteCharAt(str.lastIndexOf(" "));
				str.deleteCharAt(str.lastIndexOf(","));
			} else {
				str.append("None");
			}
			str.append("\n");
		}

		if (unmappedPatterns.size() > 0) {
			for (ActivityPattern ptr : unmappedPatterns) {
				str.append("-Pattern[").append(ptr.getName()).append("]: None\n");
			}
		}

		str.append(SEPARATOR);

		return str.toString();
	}

	public String getActivityInfo(Activity activity, boolean isDecorated) {

		StringBuilder str = new StringBuilder();
		String nullValue = "-";

		if (activity == null) {
			str.append("Activity is Null");
		} else {

			if (isDecorated) {
				str.append(STARTER_MARKER).append(
						"[" + activity.getName() + "] Info:\n");
			} else {
				str.append("== (").append(activity.getName()).append(") \n");
			}
			str.append("Initiator: ").append(
					activity.getInitiator() != null ? ((IncidentEntity) activity.getInitiator()).getName() : nullValue)
					.append("\n");
			str.append("Target-Asset: ").append(
					!activity.getTargetedAssets().isEmpty() ? activity.getTargetedAssets().get(0).getName() : nullValue)
					.append("\n");
			str.append("Exploited-Asset: ").append(!activity.getExploitedAssets().isEmpty()
					? activity.getExploitedAssets().get(0).getName() : nullValue).append("\n");
			str.append("Resource: ")
					.append(!activity.getResources().isEmpty() ? activity.getResources().get(0).getName() : nullValue)
					.append("\n");
			str.append("Location: ").append(
					activity.getLocation() != null ? ((IncidentEntity) activity.getLocation()).getName() : nullValue)
					.append("\n");
			str.append("Next-Activity: ").append(
					!activity.getNextActivities().isEmpty() ? activity.getNextActivities().get(0).getName() : nullValue)
					.append("\n");
			str.append("Previous-Activity: ").append(!activity.getPreviousActivities().isEmpty()
					? activity.getPreviousActivities().get(0).getName() : nullValue).append("\n");
			str.append("Type: ").append(activity.getType()).append("\n");
			str.append("Behaviour: ").append(activity.getBehaviourType()).append("\n");

			if (isDecorated) {
				str.append(SEPARATOR);
			}
//			else {
//				str.append("\n");
//			}

		}

		return str.toString();
	}

	public String getIncidentModelActivitySequence(IncidentDiagram abstractIncidentModel) {

		StringBuilder str = new StringBuilder();

		Activity orgAct = abstractIncidentModel.getInitialActivity();
		Activity orgNxt = !orgAct.getNextActivities().isEmpty() ? orgAct.getNextActivities().get(0) : null;

		str.append(orgAct.getName());
		while (orgNxt != null && !orgNxt.equals(orgAct)) {
			str.append("->").append(orgNxt.getName());
			orgNxt = !orgNxt.getNextActivities().isEmpty() ? orgNxt.getNextActivities().get(0) : null;
		}

		return str.toString();
	}

	public String getOriginalIncidentModel(boolean printActivitiesInfo) {

		StringBuilder str = new StringBuilder();

		str.append(STARTER_MARKER).append("Original Model:\n");

		str.append("\n*Activity Sequence:").append(getIncidentModelActivitySequence(originalIncidentModel)).append("\n\n");

		if (printActivitiesInfo) {
			Activity orgAct = originalIncidentModel.getInitialActivity();
			Activity orgNxt = !orgAct.getNextActivities().isEmpty() ? orgAct.getNextActivities().get(0) : null;

			while (orgNxt != null && !orgNxt.equals(orgAct)) {
				str.append(getActivityInfo(orgNxt, false)).append("\n");
				orgNxt = !orgNxt.getNextActivities().isEmpty() ? orgNxt.getNextActivities().get(0) : null;
			}
		}

		str.append(SEPARATOR);

		return str.toString();
	}

	public String getAbstractIncidentModel(boolean printActivitiesInfo) {

		StringBuilder str = new StringBuilder();

		str.append(STARTER_MARKER).append("Abstract Model:\n");

		str.append("\n*Activity Sequence:").append(getIncidentModelActivitySequence(abstractIncidentModel)).append("\n\n");

		if (printActivitiesInfo) {

			Activity orgAct = abstractIncidentModel.getInitialActivity();
			Activity orgNxt = !orgAct.getNextActivities().isEmpty() ? orgAct.getNextActivities().get(0) : null;

			while (orgNxt != null && !orgNxt.equals(orgAct)) {
				str.append(getActivityInfo(orgNxt, false)).append("\n");
				orgNxt = !orgNxt.getNextActivities().isEmpty() ? orgNxt.getNextActivities().get(0) : null;
			}
		}

		str.append(SEPARATOR);

		return str.toString();
	}

	public String getRemovedEntities(boolean isDecorated) {

		StringBuilder str = new StringBuilder();

		if (isDecorated) {
			str.append(STARTER_MARKER).append("Removed Entities:\n");
		} else {
			str.append("Removed Entities: ");
		}

		if (removedEntities.size() > 0) {
			for (IncidentEntity entity : removedEntities) {
				str.append(entity.getName()).append(", ");
			}

			str.deleteCharAt(str.lastIndexOf(" "));
			str.deleteCharAt(str.lastIndexOf(","));
			str.append("\n");
		} else {
			str.append("None Removed\n");
		}

		if (isDecorated) {
			str.append(SEPARATOR);
		}

		return str.toString();

	}

	public String getAbstractActivities(boolean isDecorated) {

		StringBuilder str = new StringBuilder();

		if (isDecorated) {
			str.append(STARTER_MARKER).append("Abstracted Actions:\n");
		}

		if (abstractedActivities.isEmpty()) {
			str.append("NONE\n");
		} else {
			str.append("[Action sequence removed] ==> [Abstract activity added] [Pattern used]\n");

			for (Entry<Activity, List<Activity>> entry : abstractedActivities.entrySet()) {

				// add actions
				for (Activity act : entry.getValue()) {
					str.append(act.getName()).append("->");
				}
				str.deleteCharAt(str.lastIndexOf(">"));
				str.deleteCharAt(str.lastIndexOf("-"));

				// add abstract activity
				str.append(" ==> ").append(entry.getKey().getName());

				// add pattern used
				loop1: for (Entry<Integer, List<Activity>> lst : potentialAbstractActivities.entrySet()) {
					for (Activity act : lst.getValue()) {
						if (act.equals(entry.getKey())) {
							str.append(" (").append(activityPatterns.get(lst.getKey()).getName()).append(")\n");
							break loop1;
						}
					}
				}

			}
		}

		if (isDecorated) {
			str.append(SEPARATOR);
		}

		return str.toString();
	}

	public String getUnAbstractedEntities(boolean isDecorated) {

		StringBuilder str = new StringBuilder();

		if (isDecorated) {
			str.append(STARTER_MARKER).append("Unabstracrted Entities:\n");
		}

		if (unAbstractedEntities.size() > 0) {
			for (IncidentEntity entity : unAbstractedEntities) {
				str.append(entity.getName()).append(", ");
			}

			str.deleteCharAt(str.lastIndexOf(" "));
			str.deleteCharAt(str.lastIndexOf(","));
			str.append("\n");
		} else {
			str.append("NONE\n");
		}

		if (isDecorated) {
			str.append(SEPARATOR);
		}

		return str.toString();
	}

	public String getConcreteAbstractEntityMap(boolean isDecorated) {

		StringBuilder str = new StringBuilder();

		if (isDecorated) {
			str.append(STARTER_MARKER).append("Concrete-Abstract Entities Map:\n");
		}

		if (entitiesConcreteAbstractMap.size() > 0) {
			str.append("[Concrete Entity] ==> [Abstract Entity]\n");
			for (Entry<String, String> entity : entitiesConcreteAbstractMap.entrySet()) {
				str.append(entity.getKey()).append(" ==> ").append(entity.getValue()).append("\n");
			}

		} else {
			str.append("NONE\n");
		}

		if (isDecorated) {
			str.append(SEPARATOR);
		}

		return str.toString();

	}

	public String getConcreteAbstractConnectionMap(boolean isDecorated) {

		StringBuilder str = new StringBuilder();

		if (isDecorated) {
			str.append(STARTER_MARKER).append("Concrete-Abstract Connections Map:\n");
		}

		if (connectionsConcreteAbstractMap.size() > 0) {
			str.append("[Concrete Connection] ==> [Abstract Connection]\n");
			for (Entry<String, String> entity : connectionsConcreteAbstractMap.entrySet()) {
				str.append(entity.getKey()).append(" ==> ").append(entity.getValue()).append("\n");
			}

		} else {
			str.append("NONE\n");
		}

		if (isDecorated) {
			str.append(SEPARATOR);
		}

		return str.toString();

	}

	public String getUnmappedPatterns(boolean isDecorated) {

		StringBuilder str = new StringBuilder();

		if (isDecorated) {
			str.append(STARTER_MARKER).append("Unmapped Patterns\n");
		}

		str.append("# of unmapped patterns = ").append(unmappedPatterns.size()).append("\n");
		if (unmappedPatterns.size() > 0) {
			for (ActivityPattern ptr : unmappedPatterns) {
				str.append("(").append(ptr.getName()).append("), ");
			}
			str.deleteCharAt(str.lastIndexOf(" "));
			str.deleteCharAt(str.lastIndexOf(","));
			str.append("\n");
		} else {
			str.append("NONE\n");
		}

		if (isDecorated) {
			str.append(SEPARATOR);
		}

		return str.toString();

	}

	public IncidentDiagram getAbstractIncidentModel() {
		return abstractIncidentModel;
	}

	public IncidentDiagram getOriginalIncidentModel() {
		return originalIncidentModel;
	}

	public EnvironmentDiagram getSystemModel() {
		return systemModel;
	}

	public Map<Integer, List<int[]>> getAllPatternsMaps() {
		return allPatternsMaps;
	}

	public Map<Integer, List<Activity>> getPotentialAbstractActivities() {
		return potentialAbstractActivities;
	}

	public Map<Activity, List<Activity>> getAbstractedActivities() {
		return abstractedActivities;
	}

	public List<IncidentEntity> getRemovedEntities() {
		return removedEntities;
	}

	public List<IncidentEntity> getUnAbstractedEntities() {
		return unAbstractedEntities;
	}

	public Map<String, String> getEntitiesConcreteAbstractMap() {
		return entitiesConcreteAbstractMap;
	}

	public Map<String, String> getConnectionsConcreteAbstractMap() {
		return connectionsConcreteAbstractMap;
	}

	public List<ActivityPattern> getUnmappedPatterns() {
		return unmappedPatterns;
	}

	/**
	 * 
	 * ===================== MAIN
	 * 
	 * 
	 */
	public static void main(String[] args) {

		IncidentPatternExtractor extractor = new IncidentPatternExtractor();
		String incidentFilePath = "D:/Bigrapher data/NII/incident instances/incidentInstance.cpi";
		String systemFilePath = "D:/Bigrapher data/NII/NII_ext.cps";

		//need to add patterns
		String connectToNetworkPatternFileName2 = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/connectToNetworkPattern2.cpi";
		String movePhysicallyPatternFileName2 = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/movePhysicallyPattern2.cpi";
		String collectDataPatternFileName2 = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/collectDataPattern2.cpi";
		String rogueLocationSetupFileName = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/rogueLocationSetup.cpi";
		String contentSpoofing = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/contentSpoofing.cpi";
		String usingMaliciousFile = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/usingMaliciousFiles.cpi";
		// add patterns to the map (key is file path and value is pattern) of
		// patterns in the models handler class
//		ModelsHandler.addActivityPattern(connectToNetworkPatternFileName2);
//		ModelsHandler.addActivityPattern(movePhysicallyPatternFileName2);
		ModelsHandler.addActivityPattern(collectDataPatternFileName2);
//		ModelsHandler.addActivityPattern(rogueLocationSetupFileName);
//		ModelsHandler.addActivityPattern(usingMaliciousFile);
		// ModelsHandler.addActivityPattern(connectToNetworkPatternFileName2);
		// ModelsHandler.addActivityPattern(rogueLocationSetupFileName);
		// ModelsHandler.addActivityPattern(collectDataPatternFileName2);
		// ModelsHandler.addActivityPattern(connectToNetworkPatternFileName2);
//		 ModelsHandler.addActivityPattern(movePhysicallyPatternFileName2);
		// ModelsHandler.addActivityPattern(connectToNetworkPatternFileName2);
		// ModelsHandler.addActivityPattern(movePhysicallyPatternFileName2);

		extractor.extract(incidentFilePath, systemFilePath);

	}
}
