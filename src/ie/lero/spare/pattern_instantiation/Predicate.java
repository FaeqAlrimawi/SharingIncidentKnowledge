package ie.lero.spare.pattern_instantiation;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.xquery.XQException;

import org.json.JSONArray;
import org.json.JSONObject;

import cyberPhysical_Incident.Activity;
import ie.lero.spare.franalyser.utility.BigraphNode;
import ie.lero.spare.franalyser.utility.JSONTerms;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Site;

public class Predicate {
	
	private Bigraph bigraphPredicate;
	private PredicateType predicateType; //precondition, postcondition
	private String name;
	private LinkedList<Integer> bigraphStates; //what states from the execution of a bigrapher the pred satisfies
	private Predicate[] associatedPredicates; //to be implemented, those are linked predicates
	private LinkedList<GraphPath> paths;
	private Activity incidentActivity;
	private LinkedList<Integer> statesIntraSatisfied;
	private LinkedList<Integer> statesInterSatisfied;
	private boolean isDebugging = true;
	private int numOfRoots;
	private SystemInstanceHandler systemHandler;
	private String incidentDocument;
	
	public Predicate(){
		predicateType = PredicateType.Precondition;
		name="";
		bigraphStates = new LinkedList<Integer>();
		statesIntraSatisfied = new LinkedList<Integer>();
		statesInterSatisfied = new LinkedList<Integer>();
		paths = new LinkedList<GraphPath>();
		systemHandler = SystemsHandler.getCurrentSystemHandler();
		}
	
	public Predicate(SystemInstanceHandler sysHandler, String incidentDoc){
		this();
		systemHandler = sysHandler;
		incidentDocument = incidentDoc;
		}

	public PredicateType getPredicateType() {
		return predicateType;
	}

	public void setPredicateType(PredicateType predType) {
		this.predicateType = predType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Activity getIncidentActivity() {
		return incidentActivity;
	}

	public void setIncidentActivity(Activity incidentActivity) {
		this.incidentActivity = incidentActivity;
	}

	public LinkedList<Integer> getBigraphStates() {
		return bigraphStates;
	}

	public void setBigraphStates(LinkedList<Integer> bigraphStates) {
		this.bigraphStates = bigraphStates;
	}

	public boolean addBigraphState(Integer state) {
		
		boolean isAdded= false;
		
		bigraphStates.add(state);
		
		return isAdded;
	}

	public LinkedList<Integer> getStatesIntraSatisfied() {
		return statesIntraSatisfied;
	}

	public Bigraph getBigraphPredicate() {
		return bigraphPredicate;
	}

	public void setBigraphPredicate(Bigraph bigraphPredicate) {
		this.bigraphPredicate = bigraphPredicate;
	}
	
	public void setBigraphPredicate(JSONObject JSONPredicate) {
		this.bigraphPredicate = convertJSONtoBigraph(JSONPredicate);
	}

	public void setStatesIntraSatisfied(LinkedList<Integer> statesIntraSatisfied) {
		this.statesIntraSatisfied = statesIntraSatisfied;
	}
	
	public void addIntraSatisfiedState(Integer state) {
		if(!statesIntraSatisfied.contains(state)) {
			statesIntraSatisfied.add(state);
		}
	}

	
	public LinkedList<Integer> getStatesInterSatisfied() {
		return statesInterSatisfied;
	}

	public void setStatesInterSatisfied(LinkedList<Integer> statesInterSatisfied) {
		this.statesInterSatisfied = statesInterSatisfied;
	}

	public void addInterSatisfiedState(Integer state) {
		if(!statesInterSatisfied.contains(state)) {
			statesInterSatisfied.add(state);
		}
	}
	
	public void removeAllBigraphStates() {
		bigraphStates.clear();
	}
	public boolean validatePredicate() {
		boolean isValid = true;
		//to be done...how to validate them? could be using the validate command in bigraph and output the errors
		return isValid;
	}

	
	public Predicate[] getAssociatedPredicates() {
		return associatedPredicates;
	}

	public void setAssociatedPredicates(Predicate[] associatedPredicates) {
		this.associatedPredicates = associatedPredicates;
	}

	
	public LinkedList<GraphPath> getPaths() {
		return paths;
	}
	
/*	public LinkedList<GraphPath> getPaths(Predicate pred) {
		LinkedList<GraphPath> list = new LinkedList<GraphPath>();
		
		for (GraphPath p : paths) {
			if (pred.getPredicateType() == PredicateType.Precondition){
				if(pred.getBigraphPredicateName()  //if pred is a precondition
						.contentEquals(p.getPredicateSrc().getBigraphPredicateName())) {
					list.add(p);
				}
			} else { //if pred is a postcondition
				if(pred.getBigraphPredicateName()
						.contentEquals(p.getPredicateDes().getBigraphPredicateName())) {
					list.add(p);
				}
			}
		}
		return list;
	}*/

	public void removeAllPaths() {
		paths.clear();
	}
	public void setPaths(LinkedList<GraphPath> paths) {
		this.paths = paths;
	}

	public void addPaths(LinkedList<GraphPath> paths) {
		this.paths.addAll(paths);
	}
	
	public String toString(){
		StringBuilder res = new StringBuilder();
		
		res.append("{Name:").append(getName()).append(", Type:").append(getPredicateType().toString()).
		append(", ActivityName:").append(incidentActivity.getName()).append(", Predicate:");
		
	return res.toString();
	}
	
	public String toPrettyString(){
		StringBuilder res = new StringBuilder();
		
		res.append("\nName: ").append(getName()).
		append("\nType: ").append(getPredicateType().toString()).
		append("\nActivityName: ").append(incidentActivity.getName()).
		append("\nStates Satisfying: ");
		for(Integer state : bigraphStates) {
			res.append(state).append(",");
		}
		res.deleteCharAt(res.length()-1); //delete ","
		
		res.append("\nPaths Satisfying: ");
		for(GraphPath path : paths) {
			if(predicateType == PredicateType.Precondition) {
				res.append(path.getPredicateDes().getBigraphPredicateName()).append(":").append(path.toPrettyString(systemHandler.getTransitionSystem())).append("\n");
			} else {
				res.append(path.getPredicateSrc().getBigraphPredicateName()).append(":").append(path.toPrettyString(systemHandler.getTransitionSystem())).append("\n");
			}
			
		}
		
	return res.toString();
	}
	
	public String getBigraphPredicateStatement() {
		StringBuilder res= new StringBuilder();
		
		res.append("big ").append(getName()).append("_").append(getPredicateType()).append("_")
		.append(incidentActivity.getName()).append(" = ").append(";\r\n");
		
		return res.toString();
		
	}
	
	public String getBigraphPredicateName() {
		StringBuilder res = new StringBuilder();
		
		res.append(getName()).append("_").append(getPredicateType()).append("_").append(incidentActivity.getName());
		
		return res.toString();
	}
	
/*	public boolean isSatisfied() {
		
		if(predicateType == PredicateType.Precondition) {
			if(paths.size() > 0) { //this indicates that a predicate has at least one state and one path to a postcondition state
				return true;
			}
		}
		
		if(predicateType == PredicateType.Postcondition) {
			
		}
		
		return false;
	}*/

	public boolean hasStates() {
		
		if(bigraphStates != null && bigraphStates.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasPaths() {
		
		if(paths != null && paths.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasPathsTo(Predicate pred) {
		
		for(GraphPath path : paths) {
			if(pred.getPredicateType() == PredicateType.Postcondition) {
				if(path.getPredicateDes().getName().contentEquals(pred.getName())) {
					return true;
				}
			} else if(pred.getPredicateType() == PredicateType.Precondition) {
				if(path.getPredicateSrc().getName().contentEquals(pred.getName())) {
					return true;
				}
			}
			
		}
		
		return false;
	}
	
	public LinkedList<GraphPath> getPathsTo(Predicate pred) {
		LinkedList<GraphPath> ps = new LinkedList<GraphPath>();
		
		for(GraphPath path : paths) {
			if(pred.getPredicateType() == PredicateType.Postcondition) {
				if(path.getPredicateDes().getName().contentEquals(pred.getName())) {
					ps.add(path);
				}
			} else if(pred.getPredicateType() == PredicateType.Precondition) {
				if(path.getPredicateSrc().getName().contentEquals(pred.getName())) {
					ps.add(path);
				}
			}
			
		}
		
		return ps;
	}
/*	
	private Bigraph convertPredicateToBigraph() {
		
		
		//convert predicate to bigraph
		
		BigraphBuilder bigraphBuilder = new BigraphBuilder(SystemInstanceHandler.getGlobalBigraphSignature());
		
		return bigraphBuilder.makeBigraph();
	}*/
	
	public 	Bigraph convertJSONtoBigraph(JSONObject redex){

		HashMap<String,BigraphNode> nodes = new HashMap<String, BigraphNode>();
		LinkedList<BigraphNode.OuterName> outerNames = new LinkedList<BigraphNode.OuterName>();
		LinkedList<BigraphNode.InnerName> innerNames = new LinkedList<BigraphNode.InnerName>();
		HashMap<String, OuterName> libBigOuterNames = new HashMap<String, OuterName>();
		HashMap<String, InnerName> libBigInnerNames = new HashMap<String, InnerName>();
		HashMap<String, Node> libBigNodes = new HashMap<String, Node>();
		LinkedList<Root> libBigRoots = new LinkedList<Root>();
		LinkedList<Site> libBigSites = new LinkedList<Site>();
		
		numOfRoots = 0;
		//get entities (or nodes) information from the json object of the condition
		//if the json object is null, then nothing will be done and null will be returned
		if(!unpackPredicateJSON(redex, nodes)) {
			return null;
		}

		/////build Bigraph object
		BigraphBuilder biBuilder = new BigraphBuilder(systemHandler.getGlobalBigraphSignature());
		
		//create roots for the bigraph
		for(int i=0;i<numOfRoots;i++) {
			libBigRoots.add(biBuilder.addRoot(i));
		}
		
		
		int difference;
		int arity;
		int newSize = 0;
		LinkedList<BigraphNode.OuterName> names;
		
		/////To avoid the issue of matching using outernames, I don't create outernames
		////but if there are outernames for a node then I add a special node called "connected" which donates that this node is connected to the installation bus
		/////this solution should be temporary and we should find a way to use the outernames (links) to match connectivity based on it
		
		
		for(BigraphNode n : nodes.values()) {
			
			//create bigraph outernames
			arity = systemHandler.getGlobalBigraphSignature().getByName(n.getControl()).getArity();
			names = n.getOuterNamesObjects();
			difference = names.size() - arity;
			//if the node has more outernames than that in the signature and knowledge is partial, then only add outernames equal to the arity
			//other option is to leave it, then the other extra outernames will be defined as empty i.e. XX:o<-{}
			if (difference > 0 && n.isKnowledgePartial()) {
				newSize = arity;
			} else {
				newSize = names.size();
			}
			for(int i = 0;i<newSize;i++) {
				if(!outerNames.contains(names.get(i))) {
					libBigOuterNames.put(names.get(i).getName(), biBuilder.addOuterName(names.get(i).getName()));
					//biBuilder.closeOuterName(names.get(i).getName());
					outerNames.add(names.get(i));
				}	
				
			}
			
			//create bigraph inner names
			for(BigraphNode.InnerName in : n.getInnerNamesObjects()) {
				if(!innerNames.contains(in)) {
					libBigInnerNames.put(in.getName(), biBuilder.addInnerName(in.getName()));
					innerNames.add(in);
				}	
			}
		}
	
		//initial creation of bigraph nodes
		for(BigraphNode nd : nodes.values()) {
			if(libBigNodes.containsKey(nd.getId())) {
				continue;
			}
			createNode(nd, biBuilder, libBigRoots, libBigOuterNames, libBigNodes);	
		}
		
		/*//if there are outernames
		for(BigraphNode n : nodes.values()) {
		if(n.getOuterNamesObjects() != null && n.getOuterNamesObjects().size() >0) {
			//add a "connected" node to the bigraph with the father being this node
			biBuilder.addNode("Connected", libBigNodes.get(n.getId()));
		}
		}*/
		
		//close outernames after creating nodes of the Bigraph
		//this turns them into edges (or links) in the Bigraph object
		for(BigraphNode.OuterName out : outerNames) {
			if(out.isClosed()) {
				biBuilder.closeOuterName(out.getName());
			}
		}
		
/*		LinkedList<String> visited = new LinkedList<String>();
		for(BigraphNode nd : nodes.values()) {
			for(BigraphNode.OuterName ot : nd.getOuterNamesObjects()) {
				if(ot.isClosed() && libBigOuterNames.containsKey(ot.getName()) && !visited.contains(ot.getName())) {
					biBuilder.closeOuterName(ot.getName());
					visited.add(ot.getName());
				}
			}
		}*/
	
		//close every outername....should be removed...it is just for testing
	/*	for(OuterName ot : libBigOuterNames.values()) {
			biBuilder.closeOuterName(ot);
		}*/
		
		//close innernames after creating nodes of the Bigraph
		for(BigraphNode.InnerName in : innerNames) {
			if(in.isClosed()) {
				biBuilder.closeInnerName(in.getName());
			}
		}		
		
		//add sites to bigraph
		for(BigraphNode n : nodes.values()) {
			if(n.hasSite()) {
				biBuilder.addSite(libBigNodes.get(n.getId()));
			}
		}
		
		//System.out.println("a "+biBuilder.makeBigraph());
		return biBuilder.makeBigraph();
	}
	
	/**
	 * loops the given json object to return internal tags (children) info
	 * @param obj JSONObject
	 * @param nodes BigraphNode objects holding the inner tags info
	 */
	private boolean unpackPredicateJSON(JSONObject obj, HashMap<String,BigraphNode> nodes) {
		
		JSONArray ary;
		BigraphNode node;
		JSONObject tmpObj;
		JSONObject tmpObject;
		LinkedList<JSONObject> objs = new LinkedList<JSONObject>();
		
		if(obj.isNull(JSONTerms.ENTITY)) {
			return false;
		}
		
		objs.add(obj);
		
		while(!objs.isEmpty()) {
			tmpObject = objs.pop();
			
			if (JSONArray.class.isAssignableFrom(tmpObject.get(JSONTerms.ENTITY).getClass())){	
				ary = (JSONArray) tmpObject.get(JSONTerms.ENTITY);
			} else {
				ary = new JSONArray();
				ary.put((JSONObject)tmpObject.get(JSONTerms.ENTITY));
			}
			//get all entities (they are divided by || as Bigraph)
			/*if (JSONArray.class.isAssignableFrom(redex.get("entity").getClass())){	
			ary = (JSONArray) redex.get("entity");
			
			*/
			for(int i=0;i<ary.length();i++) {
				node = new BigraphNode();
				tmpObj = ary.getJSONObject(i);
				node.setId(tmpObj.get(JSONTerms.NAME).toString());
				node.setControl(tmpObj.get(JSONTerms.CONTROL).toString());
				node.setIncidentAssetName(tmpObj.get(JSONTerms.INCIDENT_ASSET_NAME).toString());
				//if the current entity has no entity parent i.e. has a root as a parent
				if(tmpObject.isNull(JSONTerms.NAME)) {
					node.setParentRoot(numOfRoots);
					numOfRoots++;
				} else {
					node.setParent(nodes.get(tmpObject.get(JSONTerms.NAME)));
				}		
				//update knowledge about the connections for that node
				try {
					node.setKnowledgePartial(XqueryExecuter.isKnowledgePartial(node.getIncidentAssetName(), incidentDocument));
				} catch (FileNotFoundException | XQException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//if the node already exists in the predicate, then it is assumed it is being copied
				//so a new node with the same properties are created with different IDs
				if(nodes.containsKey(node.getId())) {
					node.setId(node.getId()+"_copy");
				} 
				
				nodes.put(node.getId(), node);
		
				//get outer names
				JSONArray tmpAry;

				if (!tmpObj.isNull(JSONTerms.OUTERNAME)) {
					// if there are more than one outername
					if (JSONArray.class.isAssignableFrom(tmpObj.get(JSONTerms.OUTERNAME).getClass())) {
						tmpAry = tmpObj.getJSONArray(JSONTerms.OUTERNAME);
					} else { // if there is only one outername
						tmpAry = new JSONArray();
						tmpAry.put((JSONObject) tmpObj.get(JSONTerms.OUTERNAME));
					}

					for (int j = 0; j < tmpAry.length(); j++) {
						String name = ((JSONObject) tmpAry.get(j)).get(JSONTerms.NAME).toString();
						boolean isClosed = false;
						if (!((JSONObject) tmpAry.get(j)).isNull(JSONTerms.ISCLOSED)) {
							isClosed = ((JSONObject) tmpAry.get(j)).get(JSONTerms.ISCLOSED).toString().equals(JSONTerms.TRUE_VALUE);
						}
						node.addOuterName(name, isClosed);
					}
				}
				
				// get inner names
				if (!tmpObj.isNull(JSONTerms.INNERNAME)) {
					
					//if there are more than one innername
					if (JSONArray.class.isAssignableFrom(tmpObj.get(JSONTerms.INNERNAME).getClass())) {
						tmpAry = tmpObj.getJSONArray(JSONTerms.INNERNAME);
					} else { //if there is only one innername
						tmpAry = new JSONArray();
						tmpAry.put((JSONObject) tmpObj.get(JSONTerms.INNERNAME));
					}
					
					for (int j = 0; j < tmpAry.length(); j++) {
						String name = ((JSONObject) tmpAry.get(j)).get(JSONTerms.NAME).toString();
						boolean isClosed = false;
						if (!((JSONObject) tmpAry.get(j)).isNull(JSONTerms.ISCLOSED)) {
							isClosed = ((JSONObject) tmpAry.get(j)).get(JSONTerms.ISCLOSED).toString().equals(JSONTerms.TRUE_VALUE);
						}
						node.addInnerName(name, isClosed);
					}
				}
				
				//get sites
				if(!tmpObj.isNull(JSONTerms.SITE)) {	
						node.setSite(true);
				}
				
				//get childern
				if(!tmpObj.isNull(JSONTerms.ENTITY)) {
					objs.add(tmpObj);
				}	
			}
		}
		return true;
	}

	private	 Node createNode(BigraphNode node, BigraphBuilder biBuilder, LinkedList<Root> libBigRoots, 
			HashMap<String, OuterName> outerNames, HashMap<String, Node> nodes) {
		
		LinkedList<Handle> names = new LinkedList<Handle>();
		OuterName tmp; 
		// find the difference between the outernames (i.e. connections) of the
		// node and the outernames defined for that node in the signature
		int difference = node.getOuterNames().size()
				- systemHandler.getGlobalBigraphSignature().getByName(node.getControl()).getArity();

		// if knowledge is partial for the node,
		if (node.isKnowledgePartial()) {
			// then if number of outernames less than that in the signature,
			while (difference < 0) {
				// then the rest are either:
				// 1-created, added for that node.
				tmp = biBuilder.addOuterName();
				outerNames.put(tmp.getName(), tmp);
				node.addOuterName(tmp.getName());
				difference++;
				// 2-create, added, then closed for that node (they become links
				// or edges i.e. XX:e)
			}
			// if it is more than that in the signature, then

		} else {
			// if knowledge is exact and number of outernames are different,
			while (difference < 0) {
				// then create and close for that node.
				tmp = biBuilder.addOuterName();
				// close outernames
				biBuilder.closeOuterName(tmp);
				outerNames.put(tmp.getName(), tmp);
				node.addOuterName(tmp.getName());
				difference++;
			}
		}

		for(String n : node.getOuterNames()) {
			names.add(outerNames.get(n));
		}
		
		//if the parent is a root
		if(node.isParentRoot()) { //if the parent is a root	
			Node  n = biBuilder.addNode(node.getControl(), libBigRoots.get(node.getParentRoot()), names);
			
			nodes.put(node.getId(), n);
			return n;
		}
		
		//if the parent is already created as a node in the bigraph
		if(nodes.containsKey(node.getParent().getId())) {
			Node  n = biBuilder.addNode(node.getControl(), nodes.get(node.getParent().getId()), names);
			
			nodes.put(node.getId(), n);
			return n;
		}
		
		//a node will take as outernames only the number specified in the bigraph signature
		//for example, if a node has arity 2, then it will take only two outernames (the first two) and ignore any other that might exist in the names variable
		//if the number of outernames defined are less than in the signature, then the rest of outernames will be defined as links (i.e. XX:e)
		Node n = biBuilder.addNode(node.getControl(), createNode(node.getParent(), biBuilder, libBigRoots, outerNames, nodes), names);

		nodes.put(node.getId(), n);
		return n;
			
	}
	
	/*public static void main(String[] args){
		Predicate p = new Predicate();
		Matcher matcher = new Matcher();
		
		try {
			JSONObject o = XqueryExecuter.getBigraphConditions("activity2", PredicateType.Precondition);
			//SystemInstanceHandler.setFileName("actors.big");
			SystemInstanceHandler.setOutputFolder("output");
			//SystemInstanceHandler.createSignatureFromBRS();
		//	SystemInstanceHandler.loadStates();
			Bigraph redex = p.convertJSONtoBigraph(o);
			p.print(redex.toString());
			for (int i = 0; i < SystemInstanceHandler.getStates().size(); i++) {
				if (matcher.match(SystemInstanceHandler.getStates().get(i), redex).iterator().hasNext()) {
					System.out.println("state " + i + " matched");
				}
			}
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void print(String msg) {
		if(isDebugging) {
			System.out.println("Predicate: "+msg);
		}
	}*/
}

