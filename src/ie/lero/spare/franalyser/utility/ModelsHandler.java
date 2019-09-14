package ie.lero.spare.franalyser.utility;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.ActivityPattern;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.IncidentEntity;
import environment.EnvironmentDiagram;
import externalUtility.IncidentModelHandler;

public class ModelsHandler {

	// the string is the filePath to the model
	private static Map<String, IncidentDiagram> incidentModels = new HashMap<String, IncidentDiagram>();
	private static Map<String, EnvironmentDiagram> systemModels = new HashMap<String, EnvironmentDiagram>();
	private static IncidentDiagram currentIncidentModel;
	private static EnvironmentDiagram currentSystemModel;
	private static Map<String, ActivityPattern> activityPatterns = new HashMap<String, ActivityPattern>();
	private static ActivityPattern currentActivityPattern;
	private static String encryption = "SHA-256";

	protected static String getEncryption(String name) {

		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(encryption);
			messageDigest.update(name.getBytes());
			String encryptedString = new String(messageDigest.digest());

			return encryptedString;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	public static Map<String, IncidentDiagram> getIncidentModels() {
		return incidentModels;
	}

	public synchronized static IncidentDiagram loadIncidentModel(String filePath) {

		return IncidentModelHandler.loadIncidentFromFile(filePath);

	}

	public synchronized static IncidentDiagram getIncidentModel(String filePath) {

		if (filePath == null) {
			return null;
		}

		IncidentDiagram model = null;

		model = incidentModels.get(getEncryption(filePath));

		// if model is not loaded then load it and save it in the map
		if (model == null) {
			model = addIncidentModel(filePath);
		}

		return model;
	}

	public synchronized static IncidentDiagram addIncidentModel(String filePath) {

		if (filePath == null) {
			return null;
		}

		IncidentDiagram model = null;

		model = loadIncidentModel(filePath);

		incidentModels.put(getEncryption(filePath), model);

		if (incidentModels.size() == 1) {
			currentIncidentModel = model;
		}

		return model;

	}

	public synchronized static IncidentDiagram removeIncidentModel(String filePath) {

		if (filePath == null) {
			return null;
		}

		return incidentModels.remove(getEncryption(filePath));

	}

	public synchronized static void setIncidentModels(Map<String, IncidentDiagram> newIncidentModels) {
		incidentModels = newIncidentModels;
	}

	public synchronized static String saveIncidentModel(IncidentDiagram incidentModel) {

		String incidentModelName;

		String crimScriptName = incidentModel.getCrimeScript() != null ? incidentModel.getCrimeScript().getName()
				: null;

		if (crimScriptName == null || crimScriptName.isEmpty()) {
			Random rand = new Random();
			incidentModelName = "incidentModel_" + rand.nextInt(1000) + ".cpi";
		} else {
			// convert spaces to underscores
			crimScriptName = crimScriptName.replaceAll(" ", "_");

			incidentModelName = "./" + crimScriptName + ".cpi";
		}

		return saveIncidentModel(incidentModel, incidentModelName);
	}

	public synchronized static String saveIncidentModel(IncidentDiagram incidentModel, String filePath) {

		try {
			boolean isSaved = IncidentModelHandler.SaveIncidentToFile(incidentModel, filePath);

			if (isSaved) {
				return filePath;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return null;
	}

	public synchronized static IncidentDiagram cloneIncidentModel(IncidentDiagram incidentModel) {

		try {

			String filePath = saveIncidentModel(incidentModel);

			if (filePath != null) {

				IncidentDiagram inc = loadIncidentModel(filePath);

				// remove tmp (i.e. the copy) file File tmpFile = new
				File tmpFile = new File(filePath);
				if (tmpFile.exists()) {
					tmpFile.delete();
				}

				return inc;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return null;

	}

	public synchronized static boolean isValidIncidentModel(IncidentDiagram incidentModel) {

		try {

			String filePath = saveIncidentModel(incidentModel);

			if (filePath != null) {

				IncidentDiagram inc = loadIncidentModel(filePath);

				// remove tmp (i.e. the copy) file File tmpFile = new
				File tmpFile = new File(filePath);
				if (tmpFile.exists()) {
					tmpFile.delete();
				}

				if (inc != null) {
					return true;
				}

			}

		} catch (Exception e) {
			return false;
		}

		return false;

	}

	public synchronized static boolean isValidIncidentModel(String incidentfilePath) {

		try {

			int tries = 1000;
			String pathCopy = incidentfilePath;

			while (pathCopy.contains("\\") && tries > 0) {
				pathCopy = pathCopy.replace("\\", "/");
				tries--;
			}

			IncidentDiagram inc = IncidentModelHandler.loadIncidentFromFile(pathCopy);

			if (inc != null) {
				return true;
			}

		} catch (Exception e) {
			return false;
		}

		return false;

	}

	public synchronized static Map<String, EnvironmentDiagram> getSystemModels() {
		return systemModels;
	}

	public synchronized static EnvironmentDiagram getSystemModel(String filePath) {

		if (filePath == null) {
			return null;
		}

		EnvironmentDiagram model = null;

		model = systemModels.get(getEncryption(filePath));

		// if model is not loaded then load it and save it in the map
		if (model == null) {
			model = addSystemModel(filePath);
		}

		return model;
	}

	public synchronized static EnvironmentDiagram addSystemModel(String filePath) {

		if (filePath == null) {
			return null;
		}

		EnvironmentDiagram model = null;

		model = SystemModelHandler.loadSystemFromFile(filePath);

		systemModels.put(getEncryption(filePath), model);

		if (systemModels.size() == 1) {
			currentSystemModel = model;
		}

		return model;

	}

	public synchronized static EnvironmentDiagram removeSystemModel(String filePath) {

		if (filePath == null) {
			return null;
		}

		return systemModels.remove(getEncryption(filePath));

	}

	public synchronized static boolean saveSystemModel(EnvironmentDiagram systemModel) {

		try {

			return SystemModelHandler.SaveSystemToFile(systemModel, ".");

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized static boolean saveSystemModel(EnvironmentDiagram systemModel, String filePath) {

		try {

			return SystemModelHandler.SaveSystemToFile(systemModel, filePath);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// public synchronized static IncidentDiagram
	// cloneSystemModel(EnvironmentDiagram systemModel) {
	//
	// try {
	//
	// boolean isSaved = saveSystemModel(systemModel);
	//
	// if (isSaved) {
	//
	// EnvironmentDiagram clone = loadSystemModel(filePath);
	//
	// // remove tmp (i.e. the copy) file File tmpFile = new
	// File tmpFile = new File(filePath);
	// if (tmpFile.exists()) {
	// tmpFile.delete();
	// }
	//
	// return inc;
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// return null;
	// }
	//
	// return null;
	//
	// }

	public synchronized static Map<String, ActivityPattern> getActivityPatterns() {
		return activityPatterns;
	}

	public synchronized static ActivityPattern getActivityPattern(String filePath) {

		if (filePath == null) {
			return null;
		}

		ActivityPattern pattern = null;

		pattern = activityPatterns.get(getEncryption(filePath));

		// if model is not loaded then load it and save it in the map
		if (pattern == null) {
			pattern = addActivityPattern(filePath);
		}

		return pattern;
	}

	public synchronized static ActivityPattern addActivityPattern(String filePath) {

		if (filePath == null) {
			return null;
		}

		ActivityPattern pattern = null;

		pattern = ActivityPatternModelHandler.loadActivityPatternFromFile(filePath);

		activityPatterns.put(getEncryption(filePath), pattern);

		if (activityPatterns.size() == 1) {
			currentActivityPattern = pattern;
		}

		return pattern;

	}

	public synchronized static ActivityPattern removeActivityPattern(String filePath) {

		if (filePath == null) {
			return null;
		}

		return activityPatterns.remove(getEncryption(filePath));

	}

	public synchronized static void setActivityPatterns(Map<String, ActivityPattern> activityPatterns) {
		ModelsHandler.activityPatterns = activityPatterns;
	}

	public static void setSystemModels(Map<String, EnvironmentDiagram> systemModels) {
		ModelsHandler.systemModels = systemModels;
	}

	public synchronized static IncidentDiagram getCurrentIncidentModel() {
		return currentIncidentModel;
	}

	public synchronized static List<Activity> getCurrentIncidentModelActivities() {

		if (currentIncidentModel == null) {
			return null;
		}

		return currentIncidentModel.getActivity();
	}

	public synchronized static List<Activity> getIncidentModelActivities(IncidentDiagram incidentModel) {

		if (incidentModel == null) {
			return null;
		}

		return incidentModel.getActivity();
	}

	public synchronized static IncidentEntity getCurrentIncidentModelEntity(String entityName) {

		if (currentIncidentModel == null) {
			return null;
		}

		return currentIncidentModel.getEntity(entityName);
	}

	public synchronized static IncidentEntity getIncidentModelEntity(IncidentDiagram incidentModel, String entityName) {

		if (incidentModel == null) {
			return null;
		}

		return incidentModel.getEntity(entityName);
	}

	public synchronized static void setCurrentIncidentModel(IncidentDiagram currentIncidentModel) {
		ModelsHandler.currentIncidentModel = currentIncidentModel;
	}

	public synchronized static EnvironmentDiagram getCurrentSystemModel() {
		return currentSystemModel;
	}

	public synchronized static ActivityPattern getCurrentActivityPattern() {
		return currentActivityPattern;
	}

	public synchronized static void setCurrentSystemModel(EnvironmentDiagram currentSystemModel) {
		ModelsHandler.currentSystemModel = currentSystemModel;
	}

	public synchronized static void setCurrentActivityPattern(ActivityPattern newCurrentActivityPattern) {
		ModelsHandler.currentActivityPattern = newCurrentActivityPattern;
	}

	public synchronized static void clearAll() {
		currentActivityPattern = null;
		currentIncidentModel = null;
		currentSystemModel = null;

		incidentModels.clear();
		systemModels.clear();
		activityPatterns.clear();
	}

}
