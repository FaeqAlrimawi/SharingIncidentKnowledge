package ie.lero.spare.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ie.lero.spare.pattern_instantiation.BigraphAnalyser;

public class LogFileAnalyser {

	String statesString = "Number of States=";

	String noTransitionsGenerated = "NO potential incident instances";
	String transitionsIdentified = "transitions removed.";
	String removeTransitionsStmt = "Removing from identified transitions";
	String removeTransitionsNone = "None Removed";

	String analysisFile = "analysisResult.txt";

	List<String> preConditionStatements = new LinkedList<String>();
	List<String> postConditionStatements = new LinkedList<String>();

	protected void createConditionStatements(int numOfActivities) {

		String preStmt = "";
		String postStmt = "";

		for (int i = 1; i <= numOfActivities; i++) {
			preStmt = BigraphAnalyser.BIGRAPH_ANALYSER_NAME + Logger.SEPARATOR_BTW_INSTANCES + "Condition [activity" + i
					+ "_Precondition] matching time:";
			postStmt = BigraphAnalyser.BIGRAPH_ANALYSER_NAME + Logger.SEPARATOR_BTW_INSTANCES + "Condition [activity"
					+ i + "_Postcondition] matching time:";

			preConditionStatements.add(preStmt);
			postConditionStatements.add(postStmt);
		}
	}

	// gets timing of activities in the log file. The activity timing is
	// determined to be the longer timing of between the precondition and
	// postcondition of an activity (parallelism is used for the conditions).
	// currently it looks for three activities
	protected void extractActivityInformation(String logFileName, int numOfActivities,  boolean identifyInstancesNames, boolean getTransitions) {

		String[] fileNames = null;
		String outputFolder = "";

		File file = new File(logFileName);

		boolean isDirectory = false;

		if (file.isDirectory()) {
			fileNames = file.list();
			isDirectory = true;
		} else if (file.isFile()) {
			fileNames = new String[1];
			fileNames[0] = file.getName();
		} else {
			System.err.println("[" + logFileName + "] is NOT recognised as folder or file. Execution terminated");
			return;
		}

		if (isDirectory) {
			outputFolder = logFileName;
		} else {
			outputFolder = logFileName.substring(0, logFileName.lastIndexOf("/"));
		}

		// create precondition and postcondition statements for the given number
		// of activities
		createConditionStatements(numOfActivities);

		List<Long> actTiming = new LinkedList<Long>();
		List<String> instance = new LinkedList<String>();
		List<Long> instanceTransitions = new LinkedList<Long>();

		String states = "unknown";

		StringBuilder str = new StringBuilder();
		str.append("analysed: ["+outputFolder+"]").append("\n\n");
		str.append("[Log file analysed]\nstates\nThread[i]:activity1-timing;activity2-timing;activity3-timing;;[numberOfTransitions]\n\n");

		for (String fileName : fileNames) {

			// clear lists
			actTiming.clear();
			instance.clear();
			instanceTransitions.clear();

			System.out.println( "analysing [" + fileName +"]");
			
			// read only text files
			if (!fileName.startsWith("log") || !fileName.endsWith(".txt")) {
				System.out.println("["+fileName + "] NOT a log file. Skipping...");
//				str.append("["+fileName + "] NOT a log file\n\n");
				continue;
			}

			str.append("[" + fileName +"]").append("\n");
			
			fileName = outputFolder + "/" + fileName;

			String[] lines = FileManipulator.readFileNewLine(fileName);

			states = getActivityTiming(lines, actTiming, instance, instanceTransitions);

			int index = 0;
			str.append(states).append("\n");
			for (int i = 0; i < actTiming.size(); i = i + numOfActivities) {
				// name
				if(identifyInstancesNames) {
				str.append(instance.get(i)).append(":");
				}
				
				for (int j = 0; j < numOfActivities; j++) {
					str.append(actTiming.get(i + j)).append(";");
				}

				if(getTransitions) {
				// transitions
				if (index < instanceTransitions.size()) {
					str.append(";[").append(instanceTransitions.get(index)).append("]");
				}
				}
				index++;
				str.append("\n");
			}

			str.append("\n\n");
		}

		try {

			FileWriter anaFile = new FileWriter(outputFolder + "/"+analysisFile);

			BufferedWriter writer = new BufferedWriter(anaFile);

			writer.write(str.toString());

			writer.close();

			System.out.println("################# Analysis Complete #################\n");
			System.out.println("## "+analysisFile + " content:");
			System.out.println(str.toString());
			System.out.println("\n## Output file saved to: " + outputFolder + "/"+analysisFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getActivityTiming(String[] logLines, List<Long> actTiming, List<String> instance,
			List<Long> instanceTransitions) {

		String states = "";
		long preTiming = 0;
		long postTiming = 0;
		long numOfTransitions = 0;

		for (String line : logLines) {

			// get states numbers
			if (line.contains(statesString)) {
				states = line.split("=")[1].trim();
				continue;
			}

			// get transitions number if any
			if (line.contains(noTransitionsGenerated)) {
				instanceTransitions.add((long) 0);
				continue;
			}

			if (line.contains(removeTransitionsStmt)) {
				String tmp = line.split("\\(")[1].split("\\)")[0];
				numOfTransitions = Long.parseLong(tmp);

			}

			if (line.contains(removeTransitionsNone)) {
				instanceTransitions.add(numOfTransitions);
			}

			// get transitions number if any
			if (line.contains(transitionsIdentified)) {
				String tmp = line.split(Logger.SEPARATOR_BTW_INSTANCES)[2].split(" ")[1];
				numOfTransitions -= Long.parseLong(tmp);
				instanceTransitions.add(numOfTransitions);
				continue;
			}

			// get postcondition activity
			for (String preStmt : preConditionStatements) {
				if (line.contains(preStmt)) {
					String tmp = line.split(":")[5].trim().split(" ")[0].replace("ms", "");
					preTiming = Long.parseLong(tmp);
					break;
				}
			}

			// get postcondition activity
			for (String postStmt : postConditionStatements) {
				if (line.contains(postStmt)) {
					String tmp = line.split(":")[5].trim().split(" ")[0].replace("ms", "");
					postTiming = Long.parseLong(tmp);
					break;
				}
			}

			// set activity time after getting pre and post timings.
			// Activity time is the condition with higher timing
			if (preTiming != 0 && postTiming != 0) {
				if (preTiming > postTiming) {
					actTiming.add(preTiming);
				} else {
					actTiming.add(postTiming);
				}

				// get instance (or thread) name
				String tmp = line.split(":")[4].trim().split(Logger.SEPARATOR_BTW_INSTANCES)[0].trim();
				instance.add(tmp);

				preTiming = 0;
				postTiming = 0;
			}

		}

		return states;
	}

	public static void main(String[] args) {

		LogFileAnalyser analyser = new LogFileAnalyser();

		String outputFolderVM32 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-32/log";
		String outputFolderVM16 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-16/log";
		String outputFolderVM8 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-8/log";
		String outputFolderVM4 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-4/log";
		String outputFolderVM2 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-2/log";
		String outputFolderVMNoThreads = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/No threads/log";

		String outputFolderUbuntu8 = "D:/Bigrapher data/lero/instantiation data/ubuntu data/CPU-8/log";
		String outputFolderUbuntu4 = "D:/Bigrapher data/lero/instantiation data/ubuntu data/CPU-4/log";
		String outputFolderUbuntu2 = "D:/Bigrapher data/lero/instantiation data/ubuntu data/CPU-2/log";
		String outputFolderUbuntuNoThreads = "D:/Bigrapher data/lero/instantiation data/ubuntu data/No threads/log";

		String outputFolderVM32_100k = "D:/Bigrapher data/lero/lero100K/log";

		int numOfActivities = 3;

		// extracts number of states, activity timing, and number of transitions
		// generated (if any)
		// output is saved into a txt file named "analysisResult.txt"
		
		boolean isDefineInstanceName = false;
		boolean isDefineTransitions = false;
		analyser.extractActivityInformation(outputFolderVM32, numOfActivities, isDefineInstanceName, isDefineTransitions);
	}
}
