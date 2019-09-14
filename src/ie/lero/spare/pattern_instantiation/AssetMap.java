package ie.lero.spare.pattern_instantiation;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.xml.xquery.XQException;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.Connection;
import cyberPhysical_Incident.ConnectionState;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.IncidentEntity;
import cyberPhysical_Incident.Mobility;
import environment.EnvironmentDiagram;
import ie.lero.spare.franalyser.utility.CartesianIterator;
import ie.lero.spare.franalyser.utility.ModelsHandler;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

public class AssetMap {
	private String[] incidentEntityNames;
	// private String[][] systemAssetMatches;
	private HashMap<String, List<String>> matchedSystemAssets;
	private LinkedList<String[]> uniqueCombinations;
	public int numberOfSets;
	// private static DateTimeFormatter dtf =
	// DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	private int numberOfSegments = 2;
	private int sizeofSegment = 3;
	// private LinkedList<String> systemAssets;

	// for generating sequences

	// number of rules
	// rules for each asset are 3 currently: hasConnectivity? 0 or Type,
	// isParent? 0 or 1, isChild? 0 or 1
	public static final int rulesNum = 3;
	// rules for each general asset in relation to other general assets in the
	// generalEntities array
	public static List<int[]> rulesList;

	public AssetMap() {
		numberOfSets = 0;
		incidentEntityNames = null;
		// systemAssetMatches = null;
		matchedSystemAssets = new HashMap<String, List<String>>();
	}

	/*
	 * public AssetMap(String [] incidentEntityNames, String[][]
	 * systemAssetMatches) { this(); this.incidentEntityNames =
	 * incidentEntityNames; // this.systemAssetMatches = systemAssetMatches; }
	 */
	public HashMap<String, List<String>> getMatchedSystemAssets() {
		return matchedSystemAssets;
	}

	public void setMatchedSystemAssets(HashMap<String, List<String>> matchedSystemAssets) {

		this.matchedSystemAssets = matchedSystemAssets;

		if (matchedSystemAssets != null) {
			Object[] tmp = matchedSystemAssets.keySet().toArray();
			incidentEntityNames = Arrays.copyOf(tmp, tmp.length, String[].class);
			tmp = null;
		}

	}

	public String[] getIncidentEntityNames() {
		return incidentEntityNames;
	}

	/*
	 * public void setIncidentEntityNames(String[] incidentEntityNames) {
	 * this.incidentEntityNames = incidentEntityNames; }
	 */

	/*
	 * public String[][] getSystemAssetMatches() { return systemAssetMatches; }
	 */

	/*
	 * public void setSystemAssetMatches(String[][] systemAssetMatches) {
	 * this.systemAssetMatches = systemAssetMatches; }
	 */

	public LinkedList<String[]> getUniqueCombinations() {

		if (uniqueCombinations == null) {
			uniqueCombinations = generateUniqueCombinations(CartesianIterator.isStrict);
		}

		return uniqueCombinations;
	}

	/*
	 * public void setUniqueCombinations(LinkedList<String[]>
	 * uniqueCombinations) { this.uniqueCombinations = uniqueCombinations; }
	 */

	/*
	 * public String[] getSystemAssetMatches(int incidentAssetIndex) { String []
	 * matches=null;
	 * 
	 * if(incidentAssetIndex > 0 &&
	 * incidentAssetIndex<incidentEntityNames.length) { matches =
	 * systemAssetMatches[incidentAssetIndex]; }
	 * 
	 * return matches; }
	 */

	public int getNumberOfSegments() {
		return numberOfSegments;
	}

	public void setNumberOfSegments(int numberOfSegments) {
		this.numberOfSegments = numberOfSegments;
	}

	public int getSizeofSegment() {
		return sizeofSegment;
	}

	public void setSizeofSegment(int sizeofSegment) {
		this.sizeofSegment = sizeofSegment;
	}

	public String[] getSystemAssetsMatched(String incidentEntityName) {

		String[] matches = null;
		/*
		 * int index=-1; incidentAssetName = incidentAssetName.toLowerCase();
		 * for(int i=0;i<incidentAssetName.length();i++) {
		 * if(incidentEntityNames[i].toLowerCase().contentEquals(
		 * incidentAssetName)){ index = i; break; } } if(index != -1) { matches
		 * = systemAssetMatches[index]; }
		 */

		matches = (String[]) matchedSystemAssets.get(incidentEntityName).toArray();

		return matches;
	}

	public String[][] getSpaceAssetsMatched(String[] incidentAssets) {
		String[][] result = new String[incidentAssets.length][];

		for (int i = 0; i < incidentAssets.length; i++) {
			result[i] = getSystemAssetsMatched(incidentAssets[i]);
		}

		return result;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		for (Entry<String, List<String>> entity : matchedSystemAssets.entrySet()) {
			result.append(entity.getKey()).append(" => ").append(entity.getValue()).append("\n");
		}

		return result.toString();
	}

	public String toStringCompact() {
		StringBuilder result = new StringBuilder();

		for (Entry<String, List<String>> entity : matchedSystemAssets.entrySet()) {
			result.append(entity.getKey()).append(":").append(entity.getValue()).append(";");
		}

		return result.toString();
	}

	/**
	 * Checks if there are any duplicate names in the given array
	 * 
	 * @param strs
	 *            String array containing the system assets
	 * @return true if two strings in the array are equal
	 */
	private boolean containsDuplicate(String[] strs) {

		LinkedList<String> list = new LinkedList<String>();

		for (String key : strs) {
			if (list.contains(key)) {
				return true;

			}
			list.add(key);
		}
		return false;
	}

	private boolean containsDuplicateUsingThreads(String[] strs) {

		LinkedList<String> list = new LinkedList<String>();
		LinkedList<String> strs2 = new LinkedList<String>();
		String[] tmp;

		// int cnt = 0;

		String rest = Arrays.toString(strs);
		tmp = rest.split("\\[|\\]|,");
		for (String a : tmp) {
			a.trim();
			if (!a.contains("[a-z]")) {
				strs2.add(a);
			}
		}

		System.out.println(strs2.size());
		for (String a : strs2) {
			System.out.println(a);
		}

		for (String key : strs2) {
			if (list.contains(key)) {
				return true;

			}
			list.add(key);
		}
		return false;
	}

	/*
	 * private boolean containsDuplicate(Integer [] strs) {
	 * 
	 * LinkedList<Integer> list = new LinkedList<Integer>();
	 * 
	 * for(Integer key: strs) { if (list.contains(key)) { return true;
	 * 
	 * } list.add(key); } return false; }
	 */
	public boolean hasEntitiesWithNoMatch() {

		/*
		 * for(int i=0;i<systemAssetMatches.length;i++) {
		 * if(systemAssetMatches[i][0] == null) { return true; } }
		 */

		for (List<String> matches : matchedSystemAssets.values()) {

			if (matches.isEmpty()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns all incident entities that have no matches
	 * 
	 * @return array of strings that hold the names of the incident entities
	 */
	public List<String> getIncidentAssetsWithNoMatch() {

		// String [] assetNames;
		LinkedList<String> names = new LinkedList<String>();

//		if (!hasEntitiesWithNoMatch()) {
//			return null;
//		}

		for (Entry<String, List<String>> entity : matchedSystemAssets.entrySet()) {

			if (entity.getValue().isEmpty()) {
				names.add(entity.getKey());
			}
		}

		return names;
	}

//	public String getIncidentAssetInfo(String assetName) {
//		String info = "";
//		String query = XqueryExecuter.NS_DECELERATION + "doc(\"" + XqueryExecuter.INCIDENT_DOC + "\")//"
//				+ XqueryExecuter.INCIDENT_ROOT_ELEMENT + "/asset[@name=\"" + assetName + "\"]";
//
//		try {
//			info = XqueryExecuter.executeQuery(query);
//		} catch (FileNotFoundException | XQException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return info;
//	}

//	public String getIncidentAssetInfo(String[] assetNames) {
//		String info = "";
//		StringBuilder names = new StringBuilder("(");
//
//		for (String e : assetNames) {
//			names.append("\"").append(e).append("\",");
//		}
//		names.deleteCharAt(names.length() - 1);
//		names.append(")");
//
//		System.out.println(names.toString());
//		String query = XqueryExecuter.NS_DECELERATION + "doc(\"" + XqueryExecuter.INCIDENT_DOC + "\")//"
//				+ XqueryExecuter.INCIDENT_ROOT_ELEMENT + "/asset[@name=" + names.toString() + "]";
//
//		try {
//			info = XqueryExecuter.executeQuery(query);
//		} catch (FileNotFoundException | XQException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return info;
//	}

	/**
	 * Returns a random unique sequence of system assets
	 * 
	 * @return array of strings holding the names of the system assets
	 */
	public String[] getRandomSystemAssetMatches() {

		if (uniqueCombinations == null) {
			generateUniqueCombinations(true);
		}
		Random rand = new Random();
		int index = rand.nextInt(uniqueCombinations.size());
		return uniqueCombinations.get(index);

	}

	/**
	 * Generates all unique combinations of system assets that correspond to the
	 * set of incident assets
	 * 
	 * @return LinkedList<String[]> containing all unique combinations
	 */
	/*
	 * public LinkedList<Integer[]> generateUniqueCombinationsUsingIntegers() {
	 * 
	 * Integer [][] matches = new Integer [systemAssetMatches.length][];
	 * systemAssets = new LinkedList<String>();
	 * 
	 * //convert strings to integers to generate combinations for(int
	 * i=0;i<matches.length;i++) { matches[i] = new Integer
	 * [systemAssetMatches[i].length]; for(int j=0;j<matches[i].length;j++) {
	 * if(!systemAssets.contains(systemAssetMatches[i][j])) {
	 * systemAssets.add(systemAssetMatches[i][j]); } matches[i][j] =
	 * systemAssets.indexOf(systemAssetMatches[i][j]); } }
	 * 
	 * 
	 * for(int i=0;i<matches.length;i++) { for(int j=0;j<matches[i].length;j++)
	 * { System.out.println(matches[i][j]+"==="+ systemAssetMatches[i][j]); } }
	 * 
	 * Iterable<Integer[]> it = () -> new CartesianIterator<>(matches,
	 * Integer[]::new); LinkedList<Integer[]> tmp = new LinkedList<Integer[]>();
	 * 
	 * for (Integer[] s : it) { if(!containsDuplicate(s)) { tmp.add(s); } }
	 * 
	 * System.out.println("size "+tmp.size()); return tmp; }
	 */

	private void createIncidentEntitiesRules() {
	
		IncidentDiagram incidentModel = ModelsHandler.getCurrentIncidentModel();
			createIncidentEntitiesRules(incidentModel);
	}
	
	private void createIncidentEntitiesRules(IncidentDiagram incidentModel) {

//		IncidentDiagram incidentModel = ModelsHandler.getCurrentIncidentModel();

		List<IncidentEntity> generalEntities = new LinkedList<IncidentEntity>();

		for (String entityName : incidentEntityNames) {
			IncidentEntity ent = incidentModel.getEntity(entityName);
			generalEntities.add(ent);
		}

		// rules for each asset are 4 currently: isSame? 0 or 1,
		// hasConnectivity? 0 or 1, isParent? 0 or 1, isChild? 0 or 1
		// the size of the rules array = rulesNum*neighbourhood*size of
		// generalAsset array (i.e. number of general assets)
		int numOfEntities = generalEntities.size();

		IncidentEntity src, des;

		rulesList = new LinkedList<int[]>();

		for (int i = 0; i < numOfEntities - 1; i++) {
			int size = (numOfEntities - 1 - i) * rulesNum;
			rulesList.add(new int[size]);
		}

		int index = 0;

		// determine properties
		for (int i = 0; i < rulesList.size(); i++) {
			src = generalEntities.get(i);

			index = i + 1;
			int[] tmpAry = rulesList.get(i);
			for (int j = 0; j < tmpAry.length;) {
				des = generalEntities.get(index);

//				System.out.println("check..." + src.getName() + " & " + des.getName());

				// [1] isConnected
				for (Connection con : src.getConnections()) {

					if(con.getState() == ConnectionState.TEMPORARY
							|| con.getState() == ConnectionState.UNKNOWN) {
						continue;
					}
					
					IncidentEntity ent1 = (cyberPhysical_Incident.IncidentEntity) con.getEntity1();
					IncidentEntity ent2 = (cyberPhysical_Incident.IncidentEntity) con.getEntity2();

					if ((ent1 != null && ent1.getName().equals(des.getName()))
							|| (ent2 != null && ent2.getName().equals(des.getName()))) {
						tmpAry[j] = 1;// con.getType().ordinal();
						break;
					}
				}

				if (tmpAry[j] != 1) {
					tmpAry[j] = 0;
				}

				//next property to check
				j++;
				
				// [2] is the destination parent of source
				IncidentEntity srcParent = (IncidentEntity) src.getParentEntity();
				if (src.getMobility() == Mobility.FIXED && 
						srcParent != null && srcParent.getName().equals(des.getName())) {
					tmpAry[j] = 1;
				} else {
					tmpAry[j] = 0;
				}

				//next property to check
				j++;
				
				// [3] is destination a child in source
				if (des.getMobility() == Mobility.FIXED &&
						src.getContainedEntities().contains(des)) {
					tmpAry[j] = 1;
				} else {
					tmpAry[j] = 0;
				}

				index++;
				
				//next property to check
				j++;
			}
		}
	}

	public void setCriteriaStrict(boolean isStrict) {
		CartesianIterator.isStrict = isStrict;
	}
	
	public LinkedList<String[]> generateUniqueCombinations(boolean isStrict) {

		String[][] systemAssetMatches = getDoubleArrayOfMatches();
		createIncidentEntitiesRules();
		
		setCriteriaStrict(isStrict);
		
		CartesianIterator<String> it = new CartesianIterator<String>(systemAssetMatches, String[]::new);

		
		uniqueCombinations = new LinkedList<String[]>();

		LinkedList<LinkedList<String>> res = it.iterateElements();

		for (LinkedList<String> lst : res) {
			// if(!containsDuplicate(s)) {
			uniqueCombinations.add(lst.toArray(new String[0]));
			// }
		}

		return uniqueCombinations;
	}

	public LinkedList<String[]> generateUniqueCombinations(boolean isStrict, IncidentDiagram incidentModel, EnvironmentDiagram sysModel) {

		String[][] systemAssetMatches = getDoubleArrayOfMatches();
		
		createIncidentEntitiesRules(incidentModel);
		
		setCriteriaStrict(isStrict);
		
		CartesianIterator<String> it = new CartesianIterator<String>(systemAssetMatches, String[]::new, sysModel);

		
		uniqueCombinations = new LinkedList<String[]>();

		LinkedList<LinkedList<String>> res = it.iterateElements();

		for (LinkedList<String> lst : res) {
			// if(!containsDuplicate(s)) {
			uniqueCombinations.add(lst.toArray(new String[0]));
			// }
		}

		return uniqueCombinations;
	}
	
	private String[][] getDoubleArrayOfMatches() {

		String[][] systemAssetMatches = new String[matchedSystemAssets.keySet().size()][];

		int index = 0;

		for (List<String> matches : matchedSystemAssets.values()) {
			Object[] tmp = matches.toArray();
			systemAssetMatches[index] = Arrays.copyOf(tmp, tmp.length, String[].class);
			index++;
		}

		return systemAssetMatches;

	}

	public LinkedList<String[]> generateUniqueCombinationsUsingThreads() {

		// multi-threading is requried to speed up the process of finding all
		// possible combinations
		// LinkedList<String> [] arys = new LinkedList<String>()[5];
		// number of segments required
		// int num = 3;
		// number of space assets each segment should take
		// int size = 2;

		String[][] systemAssetMatches = getDoubleArrayOfMatches();

		// segments
		String[][][] segments = new String[numberOfSegments][sizeofSegment][];
		LinkedList<String>[] results = new LinkedList[numberOfSegments];

		// segments the spaceAssets (or system assets array) into several 2d
		// arrays
		// where each 2d aray points to a segment in system array
		for (int i = 0; i < numberOfSegments; i++) {
			for (int j = 0; j < sizeofSegment; j++) {
				segments[i][j] = new String[systemAssetMatches[j + (i * sizeofSegment)].length];
				segments[i][j] = systemAssetMatches[j + (i * sizeofSegment)];
			}

			// create lists to hold results from each segment
			results[i] = new LinkedList<String>();
		}

		// used to wait for the threads to end before proceeding with result
		CountDownLatch latch = new CountDownLatch(numberOfSegments);

		// create threads
		SetsGeneratorThread[] setsGenerators = new SetsGeneratorThread[numberOfSegments];

		for (int i = 0; i < numberOfSegments; i++) {
			setsGenerators[i] = new SetsGeneratorThread(segments[i], results[i], latch, i);
			setsGenerators[i].start();
		}

		// wait for threads to finish execution
		try {
			latch.await();
			LinkedList<String> finalResult = new LinkedList<String>();
			if (systemAssetMatches.length % 2 == 0) {
				String[][] res = new String[2][];
				res[0] = results[0].toArray(new String[0]);
				res[1] = results[1].toArray(new String[0]);
				Iterable<String[]> it = () -> new CartesianIterator<>(res, String[]::new);
				for (String[] s : it) {
					if (!containsDuplicate(s)) {
						finalResult.add(Arrays.toString(s));
						// uniqueCombinations.add(s);
					}
				}
			} else {
				String[][] res = new String[3][];
				res[0] = results[0].toArray(new String[0]);
				res[1] = results[1].toArray(new String[0]);
				res[2] = systemAssetMatches[systemAssetMatches.length - 1];
				Iterable<String[]> it = () -> new CartesianIterator<>(res, String[]::new);
				for (String[] s : it) {
					if (!containsDuplicate(s)) {
						finalResult.add(Arrays.toString(s));
						// uniqueCombinations.add(s);
					}
				}
			}
			/*
			 * if (numberOfSegments ==3){ //returns how many higher levels are
			 * there // int num2 = numberOfSegments/2; // for(int
			 * i=0;i<num2;i++) { // // } LinkedList<String> tmp = new
			 * LinkedList<String>(); String [][][] res = new String [2][2][];
			 * res[0][0] = results[0].toArray(new String[0]); res[0][1] =
			 * results[1].toArray(new String[0]); CountDownLatch latch2 = new
			 * CountDownLatch(1); SetsGeneratorThread thred = new
			 * SetsGeneratorThread(res[0], tmp, latch2, 3); thred.start();
			 * latch2.await(); res[1][0] = tmp.toArray(new String[0]);;
			 * res[1][1] = results[2].toArray(new String[0]); Iterable<String[]>
			 * it = () -> new CartesianIterator<>(res[1], String[]::new); for
			 * (String[] s : it) { if(!containsDuplicate(s)) { finalResult.add(
			 * Arrays.toString(s)); //uniqueCombinations.add(s); } } }
			 */

			/*
			 * int cnt = 0; String[] tmp;
			 * 
			 * for(String s : finalResult) { tmp = s.split(","); for(String a :
			 * tmp) { if(!a.equals("") || !a.equals(" ") | !a.isEmpty()) {
			 * cnt++; } } }
			 */
			containsDuplicateUsingThreads(finalResult.toArray(new String[0]));

			System.out.println("size: " + finalResult.size());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * for(int i=0;i<num;i++) {
		 * System.out.println("size: "+results[i].size()+"\n"+results[i]); }
		 */
		return uniqueCombinations;
	}

	/*
	 * public static void main(String [] args){
	 * 
	 * AssetMap m = new AssetMap(); AssetMap m2 = new AssetMap(); String []
	 * dummy = {"a","b","c","d","e","f","g","h","i","j"}; Random rand = new
	 * Random();
	 * 
	 * //represents number of system assets that match each incident asset
	 * assuming int rows = 8; //represents number of incident assets int columns
	 * = 10; // String [] a = {"a", "b", "c"}; //
	 * System.out.println(Arrays.toString(a)); String [][] tst = new
	 * String[rows][columns]; int cnt = 0; //generate dummy array assuming they
	 * are all unique for(int i = 0;i<rows;i++) { for(int j=0;j<columns;j++) {
	 * tst[i][j] = ""+j;//cnt;//dummy[rand.nextInt(dummy.length)]; cnt++; } }
	 * 
	 * //set the number of segments and the size of each depending on the number
	 * of rows //dividing could depend on the number of segments or the size of
	 * a segment int number = 2; int size = 2;
	 * 
	 * //division depending on number of segments //if(rows % number == 0){
	 * m.setNumberOfSegments(number); m.setSizeofSegment(rows/number); // }
	 * //division depending on size // if(rows % size == 0) { //
	 * m.setSizeofSegment(size); // m.setNumberOfSegments(rows/size); // }
	 * 
	 * m.setsystemAssetMatches(tst); m2.setsystemAssetMatches(tst);
	 * 
	 * System.out.
	 * println("Testing [The generation of unqiue sequences USING 3 threads] using a "
	 * +rows+"" + "*"+columns+ "\nstatring time [" +
	 * dtf.format(LocalDateTime.now())+"]"); LinkedList<String[]> seq =
	 * m.generateUniqueCombinationsUsingThreads();
	 * //System.out.println(seq.size());
	 * 
	 * System.out.
	 * println("Testing [The generation of unqiue sequences WITHOUT threads] using a "
	 * +rows+"" + "*"+columns+ "\nstatring time [" +
	 * dtf.format(LocalDateTime.now())+"]"); LinkedList<String[]> seq2 =
	 * m2.generateUniqueCombinations(); System.out.println(seq2.size());
	 * for(String [] s: seq2) { System.out.println(Arrays.toString(s)); }
	 * 
	 * System.out.println("Finished [" + dtf.format(LocalDateTime.now())+"]");
	 * 
	 * //size (if all unique) = columns^rows //System.out.println(seq.size());
	 * 
	 * }
	 */
}

class SetsGeneratorThread implements Runnable {

	private int threadID;
	private Thread t;
	private String[][] array;
	private LinkedList<String> resultArray;
	private CountDownLatch latch;

	public SetsGeneratorThread(String[][] ary, LinkedList<String> result, CountDownLatch latch, int id) {
		// TODO Auto-generated constructor stub
		array = ary;
		resultArray = result;
		this.latch = latch;
		threadID = id;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		Iterable<String[]> it = () -> new CartesianIterator<>(array, String[]::new);
		for (String[] s : it) {
			if (!containsDuplicate(s)) {
				resultArray.add(Arrays.toString(s));
				// uniqueCombinations.add(s);
			}
		}

		latch.countDown();
		/*
		 * if(resultArray != null)
		 * System.out.println(threadID+" size:"+resultArray.size());
		 * System.out.println(resultArray.toString());
		 */
	}

	public void start() {
		System.out.println("Starting " + threadID);
		if (t == null) {
			t = new Thread(this, "" + threadID);
			t.start();
		}
	}

	private boolean containsDuplicate(String[] strs) {

		LinkedList<String> list = new LinkedList<String>();

		for (String key : strs) {
			if (list.contains(key)) {
				return true;

			}
			list.add(key);
		}
		return false;
	}

}
