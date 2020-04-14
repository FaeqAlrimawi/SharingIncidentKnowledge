package ie.lero.spare.utility;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.LabelExtractor;
import ie.lero.spare.pattern_instantiation.Predicate;

public class TransitionSystem {

	// private TransitionSystem transitionSystem = null;
	private Digraph<Integer> transitionGraph;
	private Integer startState;
	private Integer endState;
	private Predicate predicateDes;
	private Predicate predicateSrc;
	private LinkedList<GraphPath> paths;
	private String fileName;
	private int numberOfStates;

	// used to label transition system
	private String[] actionNames;

	public TransitionSystem() {
		transitionGraph = new Digraph<Integer>();
		numberOfStates = -1;

		// createDigraphFromJSON();
	}

	public TransitionSystem(String fileName) {
		transitionGraph = new Digraph<Integer>();
		numberOfStates = -1;
		this.fileName = fileName;
		createDigraphFromJSON();
	}

	// public TransitionSystem(Digraph<Integer> digraph) {
	// transitionGraph = new Digraph<Integer>(digraph);
	// numberOfStates = -1;
	//
	// // fileName = BigraphAnalyser.getBigrapherExecutionOutputFolder() +
	// // "/transitions";
	//// createDigraph();
	// createDigraphFromJSON();
	// }

	// public TransitionSystem getTransitionSystemInstance() {
	//
	// if (transitionSystem != null) {
	// return transitionSystem;
	// }
	//
	// transitionSystem = new TransitionSystem();
	//
	// return transitionSystem;
	//
	// }

	private void createDigraph() {

		String[] transitionsFileLines = null;
		Integer st1;
		Integer st2;
		float probability = -1;
		String label = null;
		String[] tmp;

		transitionsFileLines = FileManipulator.readFileNewLine(fileName);

		numberOfStates = new Integer(transitionsFileLines[0].split(" ")[0]); // gets
																				// the
																				// number
																				// of
																				// states

		for (int i = 1; i < transitionsFileLines.length; i++) {
			probability = -1;
			label = null;
			tmp = transitionsFileLines[i].split(" ");
			st1 = new Integer(Integer.parseInt(tmp[0]));
			st2 = new Integer(Integer.parseInt(tmp[1]));
			if (tmp.length >= 3) { // if bigraph is probabilistic
				if (tmp[2].matches("^(?:(?:\\-{1})?\\d+(?:\\.{1}\\d+)?)$")) { // if
																				// the
																				// 3rd
																				// element
																				// is
																				// a
																				// probability
					probability = Float.parseFloat(tmp[2]);
					if (tmp.length == 4) { // if it has labels
						label = tmp[3];
					}

				} else { // if there is no probab
					label = tmp[2];
				}
			}
			transitionGraph.add(st1, st2, probability, label);
		}

	}

	private void createDigraphFromJSON() {

		// to be done************

		String[] transitionsFileLines = null;
		Integer st1;
		Integer st2;
		double probability = -1;
		String label = null;
		String[] tmp;

		// transitionsFileLines = FileManipulator.readFileNewLine(fileName);

		JSONParser parser = new JSONParser();
		JSONObject obj;

		try {
			JSONArray ary;

			if (fileName.endsWith(".txt")) {
				createDigraph();
				return;
			}

			obj = (JSONObject) parser.parse(new FileReader(fileName));

			// if the transitions come from a brs file
			ary = (JSONArray) obj.get(JSONTerms.TRANSITIONS_BRS);

			// if the transitions come from pbrs
			if (ary == null) {
				ary = (JSONArray) obj.get(JSONTerms.TRANSITIONS__PROP_BRS);
			} 

			if(ary == null){
				ary = (JSONArray) obj.get(JSONTerms.TRANSITIONS__STOCHASTIC_BRS);
			}

			// numberOfStates = new Integer(transitionsFileLines[0].split("
			// ")[0]);
			// //gets the number of states

			Iterator<JSONObject> iter = ary.iterator();
			JSONObject tmpObj = null;
			Object objGeneral = null;
			
			while (iter.hasNext()) {

				tmpObj = iter.next();	

				// source state
				String srcState = tmpObj.get(JSONTerms.TRANSITIONS__SOURCE).toString();
				st1 = srcState != null ? Integer.valueOf(srcState) : -1;

				// destination state
				String desState = tmpObj.get(JSONTerms.TRANSITIONS__TARGET).toString();
				st2 = desState != null ? Integer.valueOf(desState) : -1;

				// if one of the states is not set to a proper state ( between 0
				// & Max-States-1)
				if (st1 == -1 || st2 == -1) {
					continue;
				}

				// probability. If there's no probability then its set to -1
				objGeneral = tmpObj.get(JSONTerms.TRANSITIONS__PROBABILITY);
				probability = objGeneral != null ? Double.parseDouble(objGeneral.toString()) : -1;

				// label for action
				objGeneral = tmpObj.get(JSONTerms.TRANSITIONS__LABEL);
				label = objGeneral != null ? objGeneral.toString() : null;

				// System.out.println(st1+" "+ st2+" "+label);
				transitionGraph.add(st1, st2, probability, label);
			}

			numberOfStates = transitionGraph.getNumberOfNodes();

		} catch (Exception ie) {
			ie.printStackTrace();
		}

	}

	public int loadNumberOfStates() {

		// JSONParser parser = new JSONParser();
		// JSONObject obj;
		//
		// try {
		// obj = (JSONObject)parser.parse(new FileReader(fileName));
		// JSONArray ary = (JSONArray)obj.get("transition_system");
		// numberOfStates = ary.size();
		//
		// } catch(Exception ie) {
		// ie.printStackTrace();
		// }

		return numberOfStates;
	}

	public LinkedList<GraphPath> getPaths(Predicate predSrc, Predicate predDes) {
		LinkedList<Integer> v = new LinkedList<Integer>();
		predicateSrc = predSrc;
		predicateDes = predDes;
		GraphPath tmpG;
		int size = 0;
		LinkedList<Integer> tmp;
		paths = new LinkedList<GraphPath>();
		for (Integer startState : predSrc.getBigraphStates()) {
			v.clear();

			this.startState = startState;
			v.add(this.startState);
			for (Integer endState : predDes.getBigraphStates()) {
				this.endState = endState;

				if (startState.compareTo(endState) == 0) {
					tmpG = new GraphPath(this);
					tmpG.setPredicateSrc(predicateSrc);
					predicateSrc.addIntraSatisfiedState(startState);
					tmpG.setPredicateDes(predicateDes);
					predicateDes.addIntraSatisfiedState(startState);
					tmp = new LinkedList<Integer>();
					tmp.add(startState);
					tmp.add(startState);
					tmpG.setStateTransitions(tmp);
					paths.add(tmpG);

				} else {
					depthFirst(transitionGraph, v);
					if (paths.size() > size) {
						predicateSrc.addIntraSatisfiedState(startState);
						predicateDes.addIntraSatisfiedState(endState);
						size = paths.size();
					}
				}

			}
		}

		return paths;
	}

	public synchronized LinkedList<GraphPath> getPaths(Predicate predSrc, Predicate predDes,
			boolean useSatisfiedStates) {
		LinkedList<Integer> v = new LinkedList<Integer>();
		predicateSrc = predSrc;
		predicateDes = predDes;
		GraphPath tmpG;
		int size = 0;
		LinkedList<Integer> tmp;
		paths = new LinkedList<GraphPath>();
		for (Integer startState : predSrc.getStatesIntraSatisfied()) {
			v.clear();

			this.startState = startState;
			v.add(this.startState);
			for (Integer endState : predDes.getStatesIntraSatisfied()) {
				this.endState = endState;

				if (startState.compareTo(endState) == 0) {
					tmpG = new GraphPath(this);
					tmpG.setPredicateSrc(predicateSrc);
					predicateSrc.addInterSatisfiedState(startState);
					tmpG.setPredicateDes(predicateDes);
					predicateDes.addInterSatisfiedState(startState);
					tmp = new LinkedList<Integer>();
					tmp.add(startState);
					tmp.add(startState);
					tmpG.setStateTransitions(tmp);
					paths.add(tmpG);

				} else {
					depthFirst(transitionGraph, v);
					if (paths.size() > size) { // can be changed to check
												// whether a state matches all
												// other states
						predicateSrc.addInterSatisfiedState(startState);
						predicateDes.addInterSatisfiedState(endState);
						size = paths.size();
					}
				}

			}
		}

		return paths;
	}

	public LinkedList<GraphPath> getPaths(Integer srcState, Integer desState) {
		LinkedList<Integer> v = new LinkedList<Integer>();
		paths = new LinkedList<GraphPath>();
		predicateSrc = null;
		predicateDes = null;
		GraphPath tmpG;
		LinkedList<Integer> tmp;

		// adds the state itself if both the source and the destination states
		// are the same
		if (srcState.equals(desState)) {
			tmpG = new GraphPath(this);
			tmpG.setPredicateSrc(null);
			tmpG.setPredicateDes(null);
			tmp = new LinkedList<Integer>();
			tmp.add(srcState);
			tmp.add(srcState);
			tmpG.setStateTransitions(tmp);
			paths.add(tmpG);

			return paths;
		}

		this.startState = srcState;
		v.add(this.startState);
		this.endState = desState;
		depthFirst(transitionGraph, v);

		return paths;
	}

	private void depthFirst(Digraph<Integer> graph, LinkedList<Integer> visited) {

		List<Integer> nodes = graph.outboundNeighbors(visited.getLast());

		// examine adjacent nodes
		for (Integer node : nodes) {
			if (visited.contains(node)) {
				continue;
			}
			if (node.equals(endState)) {
				visited.add(node);
				addTransitiontoList(visited);
				visited.removeLast();
				break;
			}
		}
		for (Integer node : nodes) {
			if (visited.contains(node) || node.equals(endState)) {
				continue;
			}
			visited.addLast(node);
			depthFirst(graph, visited);
			visited.removeLast();
		}
	}

	private void addTransitiontoList(List<Integer> transition) {
		LinkedList<Integer> newList = new LinkedList<Integer>();
		GraphPath path = new GraphPath(this);

		newList.addAll(transition);

		path.setPredicateSrc(predicateSrc);
		path.setPredicateDes(predicateDes);
		path.setStateTransitions(newList);
		paths.add(path);

	}

	public void setFileName(String fileName) {
		this.fileName = fileName;

	}

	public String getFileName() {
		return fileName;
	}

	public Digraph<Integer> getDigraph() {
		return transitionGraph;
	}

	public String getLabel(Integer srcState, Integer desState) {

		return transitionGraph.getLabel(srcState, desState);
	}

	public double getProbability(Integer srcState, Integer desState) {

		return transitionGraph.getProbability(srcState, desState);
	}

	public int getNumberOfStates() {
		return numberOfStates;
	}

	public void setNumberOfStates(int numberOfStates) {
		this.numberOfStates = numberOfStates;
	}

	public String toString() {
		return transitionGraph.toString();
	}

	// public static void main(String[]args) {
	//
	// TransitionSystem.setFileName("D:/Bigrapher
	// data/scenario1/states_100/transitions.json");
	// TransitionSystem tra = TransitionSystem.getTransitionSystemInstance();
	//
	// System.out.println(tra.getDigraph().toString());
	//
	// }

	// public static void setInstanceNull() {
	// transitionSystem = null;
	// }
	//
	// public void updateDigraphLabels(String[] actionNames) {
	//
	// LabelExtractor ext = new LabelExtractor();
	// ext.updateDigraphLabels(actionNames);
	// }

	// public String createNewLabelledTransitionFile(String [] actionNames) {
	//
	// LabelExtractor ext = new LabelExtractor();
	// ext.updateDigraphLabels(actionNames);
	// return ext.createNewLabelledTransitionFile();
	// }

	public void setActionNames(String[] actionNames) {
		this.actionNames = Arrays.copyOf(actionNames, actionNames.length);
	}
}
