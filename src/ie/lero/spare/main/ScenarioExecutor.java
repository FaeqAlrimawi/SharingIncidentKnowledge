package ie.lero.spare.main;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import ie.lero.spare.franalyser.utility.ModelsHandler;
import ie.lero.spare.pattern_extraction.IncidentPatternExtractor;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator;

/**
 * This class is used for testing the extraction and instantation techniques
 *
 */
public class ScenarioExecutor {
	
	protected static void executeFromPrompt() {

		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(System.in));
		Scanner scan = new Scanner(System.in);

		boolean isDone = false;
		int scenID = -1;
		List<Integer> scenarioIDs = new LinkedList<Integer>() {
			{
				add(1);
				add(2);
			}
		};

		int tries = 10;

		System.out.println("Enter 1 or 2 to execute incident pattern extraction (1) or incident pattern instantiation (2)");
		System.out.println("** Data for executing the techniques should be contained under \"resources\" folder **");

		loop: while (!isDone && tries > 0) {
			try {

				String input = scan.next();
				input = input.trim();
				scenID = Integer.parseInt(input);

				if (scenarioIDs.contains(scenID)) {
					isDone = true;
				} else {
					System.out.println("Please enter 1 for pattern extraction, or 2 for pattern instantiation");
				}

			} catch (NumberFormatException e) {
				System.out.println("Please enter 1 for pattern extraction, or 2 for pattern instantiation");
				continue loop;
			} finally {
				tries--;
			}
		}

		switch (scenID) {
		case 1:
			// extraction
			executeExtractionInRC1Scenario();
			break;
		case 2:
			// instantiation
			executeInstantionInRC2Scenario();
			break;
		default:
			break;
		}

	}

	/**
	 * execute incident pattern instantiation scenario (scenario 2 at RC2) as described in the TSE paper
	 */
	protected static void executeInstantionInRC2Scenario() {

		String internalTechsFolder = "../../../../executable_jar/DemoData/scenario2_instantiation/";
		String extrnalTechsFolder = "./DemoData/scenario2_instantiation/";
		
		String incidentPatternName = "incidentPattern.cpi";
		String systemModelName = "RC2.cps";
		File incidentPatternFile = null;
		File sysModelFile = null;
		String incidentPatternFilePath = null;
		String sysModelFilePath = null;

		URL incidentPattern = IncidentPatternInstantiator.class
				.getResource(internalTechsFolder + incidentPatternName);
		URL sysModel = IncidentPatternInstantiator.class
				.getResource(internalTechsFolder + systemModelName);

		if (incidentPattern == null) {
			// System.err.println("Incident pattern [" + incidentPatternName +
			// "] is not found.");
			incidentPatternFile = new File(extrnalTechsFolder + incidentPatternName);

			if (incidentPatternFile.exists()) {
				incidentPatternFilePath = incidentPatternFile.getAbsolutePath();
			} else {
				System.err.println("Incident instance [" + incidentPatternName + "] is not found.");
				return;
			}

			// return;
		} else {
			incidentPatternFilePath = incidentPattern.getPath();
		}

		if (sysModel == null) {
//			System.err.println("System model [" + systemModelName + "] is not found.");
			sysModelFile = new File(extrnalTechsFolder + systemModelName);

			if (sysModelFile.exists()) {
				sysModelFilePath = sysModelFile.getAbsolutePath();
			} else {
				System.err.println("System model [" + systemModelName + "] is not found.");
				return;
			}
			// return;
		} else {
			sysModelFilePath = sysModel.getPath();
		}

		IncidentPatternInstantiator instantiator = new IncidentPatternInstantiator();

		instantiator.execute(incidentPatternFilePath, sysModelFilePath);

	}

	/**
	 * Execute pattern extraction scenario (scenario 1 in RC1) as described in the TSE paper
	 */
	protected static void executeExtractionInRC1Scenario() {

		String internalTechsFolder = "../../../../executable_jar/DemoData/scenario1_extraction/";
		String extrnalTechsFolder = "./DemoData/scenario1_extraction/";
		
		String incidentInstanceName = "incidentInstance.cpi";
		String systemModelName = "RC1.cps";
		String activityPatternFolder = "activityPatterns/";
		File incidentInstanceFile = null;
		File sysModelFile = null;
		String incidentInstanceFilePath = null;
		String sysModelFilePath = null;

		// String[] activityPatterns = new String[] { "collectDataPattern.cpi",
		// "connectToNetworkPattern.cpi",
		// "movePhysicallyPattern.cpi", "rogueLocationSetup.cpi",
		// "usingMaliciousFiles.cpi" };

		String[] activityPatterns = new String[] { "collectDataPattern.cpi", "connectToNetworkPattern.cpi",
				"movePhysicallyPattern.cpi", "rogueLocationSetup.cpi", "usingMaliciousFiles.cpi" };

		URL incidentInstance = IncidentPatternInstantiator.class
				.getResource(internalTechsFolder + incidentInstanceName);
		URL sysModel = IncidentPatternInstantiator.class
				.getResource(internalTechsFolder + systemModelName);

		if (incidentInstance == null) {

			incidentInstanceFile = new File(extrnalTechsFolder + incidentInstanceName);

			if (incidentInstanceFile.exists()) {
				incidentInstanceFilePath = incidentInstanceFile.getAbsolutePath();
			} else {
				System.err.println("Incident instance [" + incidentInstanceName + "] is not found.");
				return;
			}
		} else {
			incidentInstanceFilePath = incidentInstance.getPath();
		}

		if (sysModel == null) {
			// System.err.println("System model [" + systemModelName + "] is not
			// found.");
			sysModelFile = new File(extrnalTechsFolder + systemModelName);

			if (sysModelFile.exists()) {
				sysModelFilePath = sysModelFile.getAbsolutePath();
			} else {
				System.err.println("System model [" + systemModelName + "] is not found.");
				return;
			}

		} else {
			sysModelFilePath = sysModel.getPath();
		}

		// get activity patterns
		for (String actPtrFile : activityPatterns) {
			URL actPtrn = IncidentPatternInstantiator.class
					.getResource(internalTechsFolder + activityPatternFolder + actPtrFile);

			if (actPtrn != null) {
				ModelsHandler.addActivityPattern(actPtrn.getPath());
			} else {

				File file = new File(extrnalTechsFolder + activityPatternFolder + actPtrFile);

				if (file.exists()) {
					ModelsHandler.addActivityPattern(file.getAbsolutePath());
				} else {
					System.err.println("Activity pattern [" + actPtrFile + "] is not found");
				}

			}
		}

		IncidentPatternExtractor extractor = new IncidentPatternExtractor();

		extractor.extract(incidentInstanceFilePath, sysModelFilePath);
	}

	public static void main(String[] args) {
		
		executeFromPrompt();
	}

}
