package ie.lero.spare.pattern_instantiation.analysis;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ie.lero.spare.forensics.assessment.brs.parser.utilities.JSONTerms;
//import core.brs.parser.utilities.JSONTerms;
import ie.lero.spare.pattern_instantiation.GraphPath;

public class FileManipulator {

	public synchronized static boolean openFolder(String folderName) {
		boolean isOpened = false;

		Runtime run = Runtime.getRuntime();
		String lcOSName = System.getProperty("os.name").toLowerCase();

		File myfile = new File(folderName);
		String path = myfile.getAbsolutePath();

		boolean MAC_OS_X = lcOSName.startsWith("mac os x");
		if (MAC_OS_X) {
			try {
				run.exec("open " + path);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (lcOSName.startsWith("windows")) {
			try {
				run.exec("explorer " + path);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (lcOSName.startsWith("linux")) {
			try {
				run.exec("xdg-open " + path);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return isOpened;
	}

	public synchronized static boolean openFile(String fileName) {
		boolean isOpened = false;

		Runtime run = Runtime.getRuntime();

		String lcOSName = System.getProperty("os.name").toLowerCase();

		boolean MAC_OS_X = lcOSName.startsWith("mac os x");
		if (MAC_OS_X) {
			try {
				run.exec("open " + fileName);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (lcOSName.startsWith("windows")) {
			try {

				// run.exec("rundll32 url.dll, FileProtocolHandler " +fileName);
				Desktop.getDesktop().open(new File(fileName));
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (lcOSName.startsWith("linux")) {
			try {
				run.exec("gedit " + fileName);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return isOpened;
	}

	public synchronized static String[] readFile(String fileName) {
		StringBuilder result = new StringBuilder();

		String tmp;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			while ((tmp = reader.readLine()) != null) {
				result.append(tmp);
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result.toString().split(";");
	}

	public synchronized static String[] readBigraphERFile(String fileName, boolean ignoreComments) {
		StringBuilder result = new StringBuilder();

		String tmp;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			while ((tmp = reader.readLine()) != null) {
				
				//ignore comments
				if(ignoreComments && tmp.startsWith(JSONTerms.BIG_COMMENT)) {
					continue;
				}
				
				if(tmp.contains(JSONTerms.BIG_COMMENT)) {
					tmp = tmp.substring(0, tmp.lastIndexOf(JSONTerms.BIG_COMMENT));
				}
				
				result.append(tmp);
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result.toString().split(";");
	}
	
	public synchronized static String[] readFileNewLine(String fileName) {
		StringBuilder result = new StringBuilder();

		String tmp;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			while ((tmp = reader.readLine()) != null) {
				result.append(tmp).append("\n");
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result.toString().split("\n");
	}

	public static Map<Integer, GraphPath> readInstantiatorInstancesFile(String fileName) {
		
		return readInstantiatorInstancesFile(fileName, null, null);
	}
	
	public static Map<Integer, GraphPath> readInstantiatorInstancesFile(String fileName, List<Integer> values, List<String> tracesActions) {

		if (fileName == null || fileName.isEmpty()) {
			System.err.println("Error reading file: " + fileName + ". File name is empty.");
			return null;
		}

		if (!fileName.endsWith(".json")) {
			System.err.println("Error reading file: " + fileName + ". File should be in JSON format.");
			return null;
		}

		File instancesFile = new File(fileName);

		if (!instancesFile.isFile()) {
			System.err.println(fileName + " is not a file");
			return null;
		}

		Map<Integer, GraphPath> instances = new HashMap<Integer,GraphPath>();

		int minTraceLength = 1000000;
		int maxTraceLength = -1;
		
		FileReader reader;
		boolean isCompactFormat = true;

		try {

			reader = new FileReader(instancesFile);

			// reading the json file and converting each instance into a
			// GraphPath object
			JSONParser parser = new JSONParser();

			JSONObject obj = (JSONObject) parser.parse(reader);

			// check if there are instance generated
			if (obj.containsKey(JSONTerms.INSTANCE_POTENTIAL)) {
				JSONObject objInstances = (JSONObject) obj.get(JSONTerms.INSTANCE_POTENTIAL);

				// check the instances again. if there are instances then read
				// them
				if (objInstances.containsKey(JSONTerms.INSTANCE_POTENTIAL_INSTANCES)) {

					// get instances
					JSONArray aryInstances = (JSONArray) objInstances.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES);

					// each instance currently has an instance_id (integer),
					// transitions (array of integers of states), and actions
					// (sequence of strings that correspond to the sequence of
					// transitions)
					// e.g., {
					// "instance_id":0,
					// "transitions":[1,64,271,937],
					// "actions":["EnterRoom","ConnectBusDevice","CollectData"]
					// }
					// this is a compact format. Another format exists in which
					// transitions are in the format
					// "transitions": [{"action": "EnterRoom", "source":
					// 1,"target": 64},

					// get transitions
					ListIterator<JSONObject> instancesList = aryInstances.listIterator();

					while (instancesList.hasNext()) {
						JSONObject instance = instancesList.next();

						// get instance id
						int instanceID = Integer
								.parseInt(instance.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_ID).toString());

						// get transitions
						JSONArray transitions = (JSONArray) instance
								.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS);

						List<Integer> states = new LinkedList<Integer>();
						List<String> actions = new LinkedList<String>();

						for (Object objState : transitions) {

							try {

								if (isCompactFormat) {
									Integer state = Integer.parseInt(objState.toString());
									// compact format
									states.add(state);
								} else {
									JSONObject objTransition = (JSONObject) objState;
									// expanded format
									// transition=[{src,trg, action}]
									Integer srcState = Integer.parseInt(objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_SOURCE).toString());
									Integer tgtState = Integer.parseInt(objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_TARGET).toString());
									String actionState = objTransition
											.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTION).toString();

									if (!states.contains(srcState)) {
										states.add(srcState);
									}

									if (!states.contains(tgtState)) {
										states.add(tgtState);
									}

									// add action
									actions.add(actionState);
								}

							} catch (NumberFormatException e) {
								isCompactFormat = false;
							}
						}

						// get actions
						if (instance.containsKey(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTIONS)) {
							JSONArray actionsAry = (JSONArray) instance
									.get(JSONTerms.INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTIONS);

							for (Object objAction : actionsAry) {
								
								String tmp = objAction.toString();
								actions.add(tmp);
								
								//add to the list of all actions
								if(tracesActions != null && !tracesActions.contains(tmp)) {
									tracesActions.add(tmp);
								}
							}
						}

						// create a new path/incident
						GraphPath tmpPath = new GraphPath();
						tmpPath.setInstanceID(instanceID);
						tmpPath.setStateTransitions(states);
						tmpPath.setTransitionActions(actions);

						// add to the list
						instances.put(instanceID, tmpPath);
						
						//set min trace length
						if(values!= null ) {
							int size = actions.size();
							if(minTraceLength> size) {
								minTraceLength = size;
							}
							
							//set max
							if(maxTraceLength < size) {
								maxTraceLength = size;
							}
						}
						
					}

					reader.close();
				}
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//set min & max trace lengths
		if(values!=null) {
			values.add(minTraceLength);
			values.add(maxTraceLength);
		}
		
		return instances;

	}

}
