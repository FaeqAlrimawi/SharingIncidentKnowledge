package ie.lero.spare.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class InstanceChecker {

	protected LinkedList<String> essentialActions;

	private InstanceChecker() {

		// essentialActions = new LinkedList<String>();
		// essentialActions.add("EnterRoom");
		// essentialActions.add("ConnectBusDevice");
		// essentialActions.add("CollectData");
	}

	public InstanceChecker(List<String> actions) {

		essentialActions = new LinkedList<String>(actions);

	}

	public List<Integer> hasAllEssentials(JSONObject instance) {

		int cnt = 0;
		int maxNumber = essentialActions.size();
		boolean hasAll = false;

		List<Integer> instancesIDWithMissingAction = new LinkedList<Integer>();
		List<String> matchedActions = new LinkedList<String>();

		JSONObject potential = (JSONObject)instance.get(JSONTerms.INSTANCE_POTENTIAL);

		if (potential == null) {
			return null;
		}

		JSONArray potential_instances = (JSONArray)potential.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES);

		// loop over the generated instances
		for (int i = 0; i < potential_instances.size(); i++) {
			JSONObject tmp = (JSONObject)potential_instances.get(i);

			JSONArray transitions = (JSONArray)tmp.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS);
			int instanceID = Integer.parseInt(tmp.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_ID).toString());
			// reset vars
			cnt = 0;
			hasAll = false;
			matchedActions.clear();
			
			// loop over transitions for each instance
			for (int j = 0; j < transitions.size(); j++) {
				JSONObject tmpIns = (JSONObject)transitions.get(j);

				String action = tmpIns.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTION).toString();

				if (!matchedActions.contains(action) && essentialActions.contains(action)) {
					cnt++;
					matchedActions.add(action);
				}

				if (cnt == maxNumber) {
					hasAll = true;
					break;
				}
			}

			if (!hasAll) {
				instancesIDWithMissingAction.add(instanceID);
			}
		}

		return instancesIDWithMissingAction;
	}

	public void checkIfCorrect(String folderName) {

		System.out.println("Essential actions: " + Arrays.toString(essentialActions.toArray())+"\n");
		/* Function to get File Name */
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (!listOfFiles[i].isFile()) {
				continue;
			}

			String fileName = listOfFiles[i].getAbsolutePath();
			String extension = fileName.substring(fileName.lastIndexOf("."));

			if (!extension.contains("json")) {
				continue;
			}
			
			System.out.println("Checking file: " + fileName);
			 JSONParser parser = new JSONParser();
			 
			 try {
				 
				FileReader reader = new FileReader(fileName);
				JSONObject instance = (JSONObject) parser.parse(reader);
				
				List<Integer> unMatched = hasAllEssentials(instance);
				
				if(unMatched == null) {
					System.err.println("Not a formatted potential incident instance JSON");
				} else if (!unMatched.isEmpty()) {
					System.err.println("####Instance IDs that don't contain all essential actions:" + Arrays.toString(unMatched.toArray()));
				} else {
					System.out.println("OK");
				}
				
				
			} catch ( IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[]args) {
		
		List<String> essentialActions = new LinkedList<String>();
		
		essentialActions.add("EnterRoom");
		essentialActions.add("ConnectBusDevice");
		essentialActions.add("CollectData");
		
		InstanceChecker ins = new InstanceChecker(essentialActions);
		
		String ubuntuCPU8 = "D:/Bigrapher data/lero/instantiation data/ubunut data/CPU-8/output";
		String ubuntuCPU4 = "D:/Bigrapher data/lero/instantiation data/ubunut data/CPU-4/output";
		String ubuntuCPU2 = "D:/Bigrapher data/lero/instantiation data/ubunut data/CPU-2/output";
		String ubuntuNoThreads = "D:/Bigrapher data/lero/instantiation data/ubunut data/No threads/output";
		
		String VMCPU16 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-16/output";
		String VMCPU8 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-8/output";
		String VMCPU4 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-4/output";
		String VMCPU2 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-2/output";
		String VMnoThreads = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/No threads/output";
		
		String VMCPU32_100K = "D:/Bigrapher data/lero/lero100K/output";
		//ubuntu
//		ins.checkIfCorrect(ubuntuCPU8);//checked
//		ins.checkIfCorrect(ubuntuCPU4);//checked
//		ins.checkIfCorrect(ubuntuCPU2);//checked
//		ins.checkIfCorrect(ubuntuNoThreads);//checked
		
		//VM ubuntu
//		ins.checkIfCorrect(VMCPU16);//checked
//		ins.checkIfCorrect(VMCPU8);//checked
//		ins.checkIfCorrect(VMCPU4);//checked
//		ins.checkIfCorrect(VMnoThreads);//checked
		
		ins.checkIfCorrect(VMCPU32_100K);
		
	}

}
