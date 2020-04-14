package ie.lero.spare.pattern_instantiation.analysis.utilities;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQException;

import org.json.JSONArray;
import org.json.JSONObject;

import ie.lero.spare.forensics.assessment.brs.parser.utilities.BigraphNode;
import ie.lero.spare.forensics.assessment.utilities.JSONTerms;
//import ie.lero.spare.franalyser.utility.BigraphNode;
//import ie.lero.spare.franalyser.utility.JSONTerms;
//import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.Site;

public class Predicate {

	//key is entity name, value is asset name
	private Map<String, String> entityAssetMap;
	
	//key is asset name, value is control
	private Map<String, String> assetControlMap;
	
	private int numOfRoots;
	private String incidentDocument;
	
	public Predicate(){

		}


	public String getIncidentDocument() {
		return incidentDocument;
	}

	public void setIncidentDocument(String incidentDocument) {
		this.incidentDocument = incidentDocument;
	}

	public Map<String, String> getEntityAssetMap() {
		return entityAssetMap;
	}

	public void setEntityAssetMap(Map<String, String> entityAssetMap) {
		this.entityAssetMap = entityAssetMap;
	}

	public Map<String, String> getAssetControlMap() {
		return assetControlMap;
	}

	public void setAssetControlMap(Map<String, String> assetControlMap) {
		this.assetControlMap = assetControlMap;
	}
	
	public boolean convertToMatchedAssets(JSONObject obj, String conditionName) {

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

				String incidentEntityName = tmpObj.get(JSONTerms.NAME).toString();
				
				String assetName = entityAssetMap.get(incidentEntityName);
				
				if(assetName!=null) {
					
					String control = assetControlMap.get(assetName);	
					
//					System.out.println("inc: " + incidentEntityName+" ast: "+assetName+" control: "+control);
					
					tmpObj.put(JSONTerms.NAME, assetName);
					tmpObj.put(JSONTerms.CONTROL, control);
					tmpObj.put(JSONTerms.INCIDENT_ASSET_NAME, incidentEntityName);
					isAdded = true;
				}
				
				
//				for (Entry<String,String> entry : entityAssetMap.entrySet()) {
//					String incidentEntityName = entry.getKey();
//					String assetName = entry.getValue();
//					String control = assetControlMap.get(assetName)!=null?assetControlMap.get(assetName).get(0):null;
//					
//					if (incidentEntityName.equals(name)) {
//						tmpObj.put(JSONTerms.NAME, assetName);
//						tmpObj.put(JSONTerms.CONTROL, control);
//						tmpObj.put(JSONTerms.INCIDENT_ASSET_NAME, incidentEntityName);
//						isAdded = true;
//						break;
//					}
//				}

				if (!isAdded) {
					if (!unMatchedEntityNames.contains(incidentEntityName)) {
						unMatchedEntityNames.add(incidentEntityName);
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
			System.err.println("PredicateGenerator>>Some entities in condition [" + conditionName
					+ "] have no map to incident entities:" + Arrays.toString(unMatchedEntityNames.toArray()));
			return false;
		}

		return true;
	}

	
	public 	Bigraph convertJSONtoBigraph(JSONObject redex, Signature sig){

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
		BigraphBuilder biBuilder = new BigraphBuilder(sig);
		
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
			arity = sig.getByName(n.getControl()).getArity();
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
			createNode(nd, biBuilder, libBigRoots, libBigOuterNames, libBigNodes, sig);	
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
			HashMap<String, OuterName> outerNames, HashMap<String, Node> nodes, Signature sig) {
		
		LinkedList<Handle> names = new LinkedList<Handle>();
		OuterName tmp; 
		// find the difference between the outernames (i.e. connections) of the
		// node and the outernames defined for that node in the signature
		int difference = node.getOuterNames().size()
				- sig.getByName(node.getControl()).getArity();

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
		Node n = biBuilder.addNode(node.getControl(), createNode(node.getParent(), biBuilder, libBigRoots, outerNames, nodes, sig), names);

		nodes.put(node.getId(), n);
		return n;
			
	}
}

