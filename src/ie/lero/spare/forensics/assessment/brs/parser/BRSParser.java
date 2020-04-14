package ie.lero.spare.forensics.assessment.brs.parser;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import core.brs.parser.Tokenizer.Token;
//import core.brs.parser.utilities.BigraphNode;
//import core.brs.parser.utilities.JSONTerms;
//import core.instantiation.analysis.FileManipulator;
import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.Connectivity;
import cyberPhysical_Incident.CyberPhysicalIncidentFactory;
import cyberPhysical_Incident.Entity;
import ie.lero.spare.forensics.assessment.brs.parser.Tokenizer.Token;
import ie.lero.spare.forensics.assessment.brs.parser.utilities.BigraphNode;
import ie.lero.spare.forensics.assessment.brs.parser.utilities.JSONTerms;
import ie.lero.spare.pattern_instantiation.analysis.FileManipulator;
//import ie.lero.spare.franalyser.utility.BigraphNode;
//import ie.lero.spare.franalyser.utility.JSONTerms;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;
import it.uniud.mads.jlibbig.core.std.Site;

public class BRSParser {

	private Tokenizer brsTokenizer;
	private BigraphWrapper bigWrapper;

	private static final int ACTION_NAME_INDEX = 0;
	private static final int ACTION_PRE_INDEX = 1;
	private static final int ACTION_POST_INDEX = 2;
	private static final int ACTION_REACT_NAME_INDEX = 3;

	private int controlNum = 1;

	// key is the react name, value is the action name taken from KeywordRules
	private Map<String, String> mapReactToAction;

	public BRSParser() {
		// bigWrapper = new BigraphWrapper();
		controlNum = 1;
		mapReactToAction = new HashMap<String, String>();
	}

	/**
	 * Parses a given action string written using BigraphER syntax.
	 * 
	 * @param action
	 *            String of the action
	 * @return ActionWrapper object which contains information about the action
	 *         (e.g., name, pre, and post)
	 */
	public ActionWrapper parseBigraphERAction(String action) {

		List<String> actionComps = preProcessAction(action);

		return parseBigraphERAction(actionComps);

	}

	/**
	 * Parses the actions (aka reaction rules) in the given BigraphER file
	 * (.big)
	 * 
	 * @param bigraphERFilePath
	 *            the BigraphER file (.big)
	 * @return A map of the actions in which the key is the action name and the
	 *         value is an ActionWrapper containing action info (pre, post)
	 */
	public BRSWrapper parseBigraphERFile(String bigraphERFilePath) {

		Map<String, ActionWrapper> actions = new HashMap<String, ActionWrapper>();
		Map<String, String> bigStmts = new HashMap<String, String>();
		BRSWrapper brsWrapper = new BRSWrapper();

		// get statements separated by ;
		boolean ignoreComments = true;
		String[] lines = FileManipulator.readBigraphERFile(bigraphERFilePath, ignoreComments);

		// == file path
		brsWrapper.setFilePath(bigraphERFilePath);

		// String initBig = null;
		// process big if any
		for (String line : lines) {

			line = line.trim();

			// remove comment (#)
			if (line.contains(JSONTerms.BIG_COMMENT)) {
				line = line.substring(0, line.indexOf(JSONTerms.BIG_COMMENT));
			}

			// ===control
			if (line.startsWith(JSONTerms.BIG_CTRL) || line.startsWith(JSONTerms.BIG_ATOMIC)
					|| line.startsWith(JSONTerms.BIG_FUN)) {
				// analyse control
				// e.g., "ctrl CONTROL_NAME = 1;"

				// remove atomic
				if (line.contains(JSONTerms.BIG_ATOMIC)) {
					line = line.replace(JSONTerms.BIG_ATOMIC, "");
				}

				// remove ctrl
				if (line.contains(JSONTerms.BIG_CTRL)) {
					line = line.replace(JSONTerms.BIG_CTRL, "");
				}

				// remove fun (controls in states are numbered so they are
				// defferent from the .big file definition)
				if (line.contains(JSONTerms.BIG_FUN)) {
					line = line.replace(JSONTerms.BIG_FUN, "");
				}

				line = line.trim();

				// get control name
				String[] parts = line.split("=");
				if (parts.length > 1) {

					String ctrlName = parts[0].trim();
					String arityStr = parts[1].trim();

					int arity = Integer.parseInt(arityStr);

					// add control
					brsWrapper.addControl(ctrlName, arity);
				}

			}

			// === big, then preserve to check that other actions don't contain
			// any
			if (line.startsWith(JSONTerms.BIG_BIG)) {

				// big format: "big name = stmt"
				// add to the map
				String[] parts = line.split("=");

				if (parts.length > 1) {

					// get title
					String title = parts[0];
					title = title.replace(JSONTerms.BIG_BIG, "");
					title = title.trim();

					// if (initBig == null) {
					// initBig = title;
					// }

					// get rest of statement
					String bigSt = parts[1];

					bigSt = bigSt.trim();

					if (bigSt.contains(";")) {
						bigSt = bigSt.replace(";", "");
					}

					bigStmts.put(title, bigSt);
				}
			}

			// ===type (brs, pbrs, sbrs)
			if (line.startsWith(JSONTerms.BIG_BEGIN)) {
				String[] parts = line.split(" ");

				if (parts.length > 1) {

					String type = parts[1].trim();

					switch (type) {
					case JSONTerms.BIG_BRS:
						brsWrapper.setType(BigraphType.BRS);
						break;
					case JSONTerms.BIG_PROP_BRS:
						brsWrapper.setType(BigraphType.PBRS);
						break;
					case JSONTerms.BIG_STOCHASTIC_BRS:
						brsWrapper.setType(BigraphType.SBRS);
						break;
					default:
						brsWrapper.setType(BigraphType.UNKNOWN);
					}
				}
			}
		}

		// check if any big contains others
		for (String bigTitle : bigStmts.keySet()) {
			processBigStatment(bigTitle, bigStmts);
		}

		// clear data
		mapReactToAction.clear();

		// === reactions
		for (String line : lines) {
			line = line.trim();
			// if action
			if (line.startsWith(JSONTerms.BIG_REACT)) {

				// check if the actions refers to any bigs, if so then replace
				// them
				List<String> parts = processReactStatment(line, bigStmts);

				ActionWrapper act = parseBigraphERAction(parts);

				actions.put(act.getActionName(), act);
			}

		}

		brsWrapper.setActions(actions);

		return brsWrapper;
	}

	protected String processBigStatment(String bigTitle, Map<String, String> bigStmts) {
		// replaces the given stmt in the map
		// processes all internal stmts in the given stmt if any found

		// if (brsTokenizer == null) {
		// createBRSTokenizer();
		// }

		String stmt = bigStmts.get(bigTitle);
		StringBuilder newStmt = new StringBuilder();

		// System.out.println(bigTitle + ":" + stmt);

		Tokenizer brsTok = createBRSTokenizer();
		brsTok.tokenize(stmt);

		boolean isConnection = false;

		for (Token t : brsTok.getTokens()) {

			if (t.token == BigraphERTokens.WORD && bigStmts.containsKey(t.sequence) && !isConnection) {
				// if the token refers to another big then replace it
				// System.out.println("callinnnnng:: " + t.sequence + ":::" +
				// bigStmts.get(t.sequence));
				newStmt.append(processBigStatment(t.sequence, bigStmts));

			} else {

//				 if (t.token == BigraphERTokens.SMALL_SPACE) {
//				 System.out.println("t"+t.sequence+"d");
//					 
//				 }

				if (t.token == BigraphERTokens.OPEN_BRACKET_CONNECTIVITY) {
					isConnection = true;
				} else if (t.token == BigraphERTokens.CLOSED_BRACKET_CONNECTIVITY) {
					isConnection = false;
				}

				newStmt.append(t.sequence);
			}
		}

		stmt = newStmt.toString();

		
		// System.out.println("Newwww:::" + bigTitle + ":" + stmt);
		bigStmts.put(bigTitle, stmt);

		return stmt;

	}

	protected List<String> processReactStatment(String react, Map<String, String> bigStmts) {
		// replaces the given stmt in the map
		// processes all internal stmts in the given stmt if any found

		// if (brsTokenizer == null) {
		// createBRSTokenizer();
		// }

		// String stmt = bigStmts.get(reactTitle);
		List<String> reactParts = preProcessAction(react);

		StringBuilder newStmt = new StringBuilder();

		// check precondition
		String pre = reactParts.get(ACTION_PRE_INDEX);

		Tokenizer brsTok = createBRSTokenizer();
		brsTok.tokenize(pre);

		boolean isConnection = false;

		String actionName = null;

		boolean isKeyword = false;

		for (Token t : brsTok.getTokens()) {

			// if it is a not a Control ( and not a connection) and contained in
			// the big stmts, then replace
			if (t.token == BigraphERTokens.WORD && bigStmts.containsKey(t.sequence) && !isConnection) {
				// if the token refers to another big then replace it
				newStmt.append(bigStmts.get(t.sequence));

			} else {

				if (t.token == BigraphERTokens.WORD && t.sequence.equalsIgnoreCase(JSONTerms.CONTROL_RULES_KEYWORDS)) {
					isKeyword = true;
				} else if (t.token == BigraphERTokens.WORD && isKeyword) {
					actionName = t.sequence;
					// System.out.println(actionName);
					isKeyword = false;
				} else if (t.token == BigraphERTokens.OPEN_BRACKET_CONNECTIVITY) {
					isConnection = true;
				} else if (t.token == BigraphERTokens.CLOSED_BRACKET_CONNECTIVITY) {
					isConnection = false;
				}

				newStmt.append(t.sequence);
			}
		}

		pre = newStmt.toString();

		// check precondition
		String post = reactParts.get(ACTION_POST_INDEX);

		newStmt.setLength(0);

		brsTok.tokenize(post);

		for (Token t : brsTok.getTokens()) {

			// if it is a not a Control ( and not a connection) and contained in
			// the big stmts, then replace
			if (t.token == BigraphERTokens.WORD && bigStmts.containsKey(t.sequence) && !isConnection) {
				// if the token refers to another big then replace it
				newStmt.append(bigStmts.get(t.sequence));

			} else {

				if (t.token == BigraphERTokens.WORD && t.sequence.equalsIgnoreCase(JSONTerms.CONTROL_RULES_KEYWORDS)) {
					isKeyword = true;
				} else if (t.token == BigraphERTokens.WORD && isKeyword) {
					actionName = t.sequence;
					// System.out.println(actionName);
					isKeyword = false;
				} else if (t.token == BigraphERTokens.OPEN_BRACKET_CONNECTIVITY) {
					isConnection = true;
				} else if (t.token == BigraphERTokens.CLOSED_BRACKET_CONNECTIVITY) {
					isConnection = false;
				}

				newStmt.append(t.sequence);
			}
		}

		post = newStmt.toString();

		// update the action
		String title = reactParts.get(ACTION_NAME_INDEX);

		// clear
		reactParts.clear();

		// add
		if (actionName != null) {
			reactParts.add(ACTION_NAME_INDEX, actionName);
		}

		// update map of action name
		mapReactToAction.put(title, actionName);

		reactParts.add(ACTION_PRE_INDEX, pre);
		reactParts.add(ACTION_POST_INDEX, post);

		// add react name to the action parts
		reactParts.add(ACTION_REACT_NAME_INDEX, title);

		return reactParts;

	}

	/**
	 * Parses a given action parts (name, pre, post) written using BigraphER
	 * syntax.
	 * 
	 * @param actionParts
	 *            a List containing the name, pre, and post, respectively
	 * 
	 * @return ActionWrapper object which contains information about the action
	 *         (e.g., name, pre, and post)
	 */
	public ActionWrapper parseBigraphERAction(List<String> actionParts) {

		ActionWrapper actionWrapper = new ActionWrapper();

		BigraphWrapper preWrapper = null;
		BigraphWrapper postWrapper = null;

		if (actionParts == null) {
			return null;
		}

		String actionName = actionParts.get(ACTION_NAME_INDEX);
		String pre = actionParts.get(ACTION_PRE_INDEX);
		String post = actionParts.get(ACTION_POST_INDEX);
		String reactName = null;

		if (actionParts.size() > 3) {
			reactName = actionParts.get(ACTION_REACT_NAME_INDEX);
		}

		if (pre != null) {
			preWrapper = parseBigraphERCondition(pre);

			if (preWrapper != null) {
				preWrapper.setCondition(true);
			}
		}

		if (post != null) {
			postWrapper = parseBigraphERCondition(post);

			if (postWrapper != null) {
				postWrapper.setCondition(true);
			}
		}

		actionWrapper.setActionName(actionName);
		actionWrapper.setPrecondition(preWrapper);
		actionWrapper.setPostcondition(postWrapper);
		actionWrapper.setReactName(reactName);

		return actionWrapper;

	}

	protected List<String> preProcessAction(String action) {

		// returns in the list
		// 0: action name
		// 1: pre
		// 2: post

		if (action == null || action.isEmpty()) {
			return null;
		}

		List<String> components = new LinkedList<String>();
		// parse a BigraphER action with format "react action_name = redex ->
		// reactum;"
		String[] parts = action.split("=");

		String actionName = null;
		String pre = null;
		String post = null;

		// parts[0] is the action name
		if (parts.length > 1) {

			// ===get action name
			actionName = parts[0];

			if (actionName.contains(JSONTerms.BIG_REACT)) {
				actionName = actionName.replace(JSONTerms.BIG_REACT, "");
			}

			actionName = actionName.trim();

			// ===get conditions
			String[] conditions = null;

			if (parts[1].contains(JSONTerms.BIG_IMPLY)) {
				conditions = parts[1].split(JSONTerms.BIG_IMPLY);
			} else if (parts[1].contains(JSONTerms.BIG_IMPLY_2)) {
				conditions = parts[1].split(JSONTerms.BIG_IMPLY_2);
			}

			if (conditions != null && conditions.length > 1) {
				pre = conditions[0];
				post = conditions[1];

				// remove any ; and [] from post
				if (post != null) {
					if (post.contains(JSONTerms.BIG_AT)) {
						post = post.substring(0, post.lastIndexOf(JSONTerms.BIG_AT));
					}

					if (post.contains(JSONTerms.BIG_SEMICOLON)) {
						post = post.replace(JSONTerms.BIG_SEMICOLON, "");
					}

				}

			}
		}

//		System.out.println(actionName);
//		System.out.println(pre);
//		System.out.println(post);
//		System.out.println();
		components.add(ACTION_NAME_INDEX, actionName);
		components.add(ACTION_PRE_INDEX, pre);
		components.add(ACTION_POST_INDEX, post);

		return components;

	}

	/**
	 * Parses the given condition in BRS format to identify entities and
	 * connectivity then creates a new condition based on that
	 * 
	 * @param BRSexp
	 *            Bigraph expressed as a BigraphExpression object
	 * @return BigraphWrapper object that contains various information about the
	 *         given BRS
	 */
	public BigraphWrapper parseBigraph(BigraphExpression BRSexp) {

		BigraphWrapper bigWrpr = new BigraphWrapper();
		bigWrapper = bigWrpr;

		// clear data if any
		clear();

		bigWrapper.setBigraphExpression(BRSexp);

		// int numOfRoots = 0;

		for (Entity ent : BRSexp.getEntity()) {

			// ===add control
			String entityName = addControl(ent);

			// ===add root
			addEntityRoot(entityName);

			// ===add site
			if (ent.isHasSite()) {
				addSite(entityName);
			}

			// ===add connectivity
			for (Connectivity con : ent.getConnectivity()) {
				updateConnectivity(con.getName(), entityName);
			}

			addChildren(entityName, ent.getEntity());
		}

		if (bigWrapper != null) {
			bigWrapper.setCondition(true);
		}

		return bigWrapper;

	}

	protected void addChildren(String parentEntityName, EList<Entity> entities) {

		// BigraphNode node;

		for (Entity entity : entities) {

			// ===add control
			String entityName = addControl(entity);

			// ===add parent
			updateEntityContainer(entityName, parentEntityName);

			// ===add site
			if (entity.isHasSite()) {
				addSite(entityName);
			}

			// add connectivity
			for (Connectivity con : entity.getConnectivity()) {
				updateConnectivity(con.getName(), entityName);
			}

			addChildren(entityName, entity.getEntity());
		}
	}

	/**
	 * Parses the given condition in BRS format to identify entities and
	 * connectivity then creates a new condition based on that
	 * 
	 * @param BRScondition
	 *            as a string
	 * @return BigraphWrapper object that contains various information about the
	 *         given BRS
	 */
	public BigraphWrapper parseBigraphERCondition(String BigrapherState) {

		if (brsTokenizer == null) {
			brsTokenizer = createBRSTokenizer();
		}

		// brsExpression = BRScondition;
		BigraphWrapper bigWrpr = new BigraphWrapper();
		bigWrapper = bigWrpr;

		// clear data if any
		clear();

		bigWrapper.setBigraphERString(BigrapherState);

		CyberPhysicalIncidentFactory instance = CyberPhysicalIncidentFactory.eINSTANCE;

		int rootNum = 0;

		LinkedList<Entity> rootEntities = new LinkedList<Entity>();
		LinkedList<Entity> allEntities = new LinkedList<Entity>();
		LinkedList<Entity> containers = new LinkedList<Entity>();
		LinkedList<String> closedConnectivities = new LinkedList<String>();

		// boolean isBracketContainment = false;
		boolean isContainment = false;
		boolean isFirstEntity = true;
		boolean isBigraphJuxta = false;
		boolean isEntityJuxta = false;
		boolean hasSite = false;
		boolean isConnectivity = false;
		boolean isClosedConnectivity = false;

		// ===tokenize
		brsTokenizer.tokenize(BigrapherState);
		for (Tokenizer.Token tok : brsTokenizer.getTokens()) {
			switch (tok.token) {

			case BigraphERTokens.CONTAINMENT: // .

				// add to the container the last entity in all entities
				containers.addFirst(allEntities.getLast());
				isContainment = true;

				break;

			case BigraphERTokens.OPEN_BRACKET: // (

				if (!containers.isEmpty()) {
					// isBracketContainment = true;
					isContainment = false;
				}

				break;

			case BigraphERTokens.CLOSED_BRACKET: // )

				// remove a container from the list of containers
				if (!containers.isEmpty()) {

					// check if it has site
					if (!hasSite) {
						containers.getFirst().setSite(null);
						containers.getFirst().setHasSite(false);
					} else { // reset
						hasSite = false;
					}

					containers.pop();
				}

				if (containers.isEmpty()) {
					// isBracketContainment = false;
				}

				break;

			case BigraphERTokens.ENTITY_JUXTAPOSITION: // |
				// next element should be contained in the same entity as the
				// previous
				isEntityJuxta = true;

				break;
			case BigraphERTokens.BIGRAPH_JUXTAPOSITION: // ||

				// next element should be a root element
				isBigraphJuxta = true;

				break;

			case BigraphERTokens.SITE:// id

				// by default a site is created with each entity

				// if the token is site then if it is containment add site to
				// last add to all entities
				// done by default

				// else if container is not empty then add to the head of the
				// container list
				// done by default

				// just look for cases where site needs to be removed

				// maybe you should cover when site is in bigraph juxtaposition
				if (isBigraphJuxta) {
					// get last added root
					// to be done
					incrementRootSites();
					isBigraphJuxta = false;
				} else if (isEntityJuxta) {

					// get current container
					if (containers.isEmpty()) {
						// by default a root has a site
						// if containers is empty then
						// Entity lastRoot = rootEntities.removeLast();
						// Entity newRoot = instance.createEntity();
						//
						// newRoot.setName("Root-" + rootNum);
						// newRoot.getEntity().add(lastRoot);
						// newRoot.getSite().
						//
						// // ===update entities and containers
						// addExtraRoot(newRoot.getName());
						//
						// updateEntityContainer(entityName, newRoot.getName());
						//
						// if (bigWrapper.getControlMap().containsKey(lastRoot))
						// {
						// removeRoot(bigWrapper.getControlMap().get(lastRoot));
						// updateEntityContainer(bigWrapper.getControlMap().get(lastRoot),
						// newRoot.getName());
						// }
						//
						// // for now root is not added to all entities
						// rootEntities.add(newRoot);
						//
						isEntityJuxta = false;
						//
						// rootNum++;
					} else {
						Entity ent = containers.getFirst();
						String entityName = bigWrapper.getControlMap().get(ent);
						addSite(entityName);
					}

				} else {
					// add site to last added entity
					Entity ent = allEntities.getLast();
					//
					String entityName = bigWrapper.getControlMap().get(ent);

					// System.out.println("adding site to " + entityName);

					addSite(entityName);

					// need to remove a container
					if (isContainment) {
						containers.removeFirst();
					}

				}

				// hasSite = true;

				break;

			case BigraphERTokens.OPEN_BRACKET_CONNECTIVITY:// {

				isConnectivity = true;
				// name or words are recognised as connectivity for last added
				// entity in all entities until closing the bracket
				break;

			case BigraphERTokens.CLOSED_BRACKET_CONNECTIVITY: // }

				// connectivity names ended
				isConnectivity = false;
				break;

			case BigraphERTokens.CLOSED_CONNECTIVITY: // e.g., /con

				// next token should be a name that relates to a connectivity
				// but it is closed
				isClosedConnectivity = true;

				break;
			case BigraphERTokens.COMMA: // ,
				// defines different names
				// nothing to be done
				break;

			case BigraphERTokens.WORD: // entity or connectivity

				// if closed connectivity token appeared, then the word is a
				// connectivity name that
				if (isClosedConnectivity) {
//					System.out.println(tok.sequence);
					closedConnectivities.add(tok.sequence);
					isClosedConnectivity = false;
				}
				// if it is connectivity
				else if (isConnectivity) {
					// create connectivity for last added entity in all entities
					Entity lastAdded = allEntities.getLast();

					Connectivity tmpCon = instance.createConnectivity();
					tmpCon.setName(tok.sequence);

					lastAdded.getConnectivity().add(tmpCon);

					// ===update connectivity value
					updateConnectivity(tok.sequence, bigWrapper.getControlMap().get(lastAdded));

					// close connectivity
					if (!closedConnectivities.isEmpty()) {
						if (tok.sequence.equalsIgnoreCase(closedConnectivities.getLast())) {
							tmpCon.setIsClosed(true);
							isClosedConnectivity = false;
							closedConnectivities.removeLast(); // remove last
						}
					}

					// if it is entity
				} else {
					// create an entity
					Entity tmp = instance.createEntity();
					tmp.setName(tok.sequence);
					allEntities.add(tmp);

					// ===add to contro map
					String entityName = addControl(tmp);

					// check if containers are not empty, if so, then get the
					// head (first element as the current container)
					if (!containers.isEmpty()) {

						Entity currentContainer = containers.getFirst();

						currentContainer.getEntity().add(tmp);

						// ===update entities and containers
						updateEntityContainer(entityName, bigWrapper.getControlMap().get(currentContainer));

						// System.out.println("entity " + tok.sequence + " is
						// contained in " + currentContainer.getName());

						// if containment is not within brackets ()
						if (isContainment) {
							// System.out.println("removing container: " +
							// currentContainer.getName());
							// it has no site then! so remove it
							currentContainer.setSite(null);
							currentContainer.setHasSite(false);
							containers.removeFirst();
							isContainment = false;
						}

						// consume entity juxta if it is true
						if (isEntityJuxta) {
							isEntityJuxta = false;
						}

					} else if (isBigraphJuxta) { // if entity after ||
						rootEntities.add(tmp);

						// ===update entity roots
						addEntityRoot(entityName);

						isBigraphJuxta = false;

					} else if (isEntityJuxta) { // if entity after |
						// then the last added entity should be remove from the
						// root and a new entity created that combines both
//						Entity lastRoot = rootEntities.removeLast();
//						Entity newRoot = instance.createEntity();
//
//						newRoot.setName("Root-" + rootNum);
//						newRoot.getEntity().add(lastRoot);
//						newRoot.getEntity().add(tmp);
//
//						// ===update entities and containers
//						addExtraRoot(newRoot.getName());
//
//						updateEntityContainer(entityName, newRoot.getName());
//
//						if (bigWrapper.getControlMap().containsKey(lastRoot)) {
//							removeRoot(bigWrapper.getControlMap().get(lastRoot));
//							updateEntityContainer(bigWrapper.getControlMap().get(lastRoot), newRoot.getName());
//						}
//
//						// for now root is not added to all entities
//						rootEntities.add(newRoot);
//
//						isEntityJuxta = false;
//
//						rootNum++;
						Entity lastRoot = rootEntities.getLast();

						// if the last root is an added root, then just add the entity to it
						if (lastRoot.getName().startsWith("Root")) {
							lastRoot.getEntity().add(tmp);
							updateEntityContainer(entityName, lastRoot.getName());
							
						} // else if the last root is an entity, then create a new root and add both to it
						else {
							Entity newRoot = instance.createEntity();

							newRoot.setName("Root-" + rootNum);
							newRoot.getEntity().add(lastRoot);
							newRoot.getEntity().add(tmp);
							
							// ===update entities and containers
							addExtraRoot(newRoot.getName());

							updateEntityContainer(entityName, newRoot.getName());

							if (bigWrapper.getControlMap().containsKey(lastRoot)) {
								removeRoot(bigWrapper.getControlMap().get(lastRoot));
								updateEntityContainer(bigWrapper.getControlMap().get(lastRoot), newRoot.getName());
							}

							// for now root is not added to all entities
							rootEntities.removeLast();
							rootEntities.add(newRoot);

							rootNum++;
						}	
						
						isEntityJuxta = false;
					}

					else { // if entity is not contained anywhere

						// if entity is the first one
						if (isFirstEntity) {
							rootEntities.add(tmp);

							// ===update entity roots
							addEntityRoot(entityName);

							isFirstEntity = false;
						}

					}
				}
				break;
			default:
				// nothing
				// System.out.println("ignoring " + tok.sequence);

			}
		}

		// printAll();
		// ===create bigraph expression
		BigraphExpression newBRS = instance.createBigraphExpression();

		newBRS.getEntity().addAll(rootEntities);

		bigWrapper.setBigraphExpression(newBRS);

		if (bigWrapper != null) {
			bigWrapper.setCondition(true);
		}

		return bigWrapper;
	}

	/**
	 * Parses the given state file into a BigraphWrapper
	 * 
	 * @param bigraphERStateFile
	 *            the file path for the state. Should be json
	 * @return BigraphWrapper object that holds info about the state
	 */
	public BigraphWrapper parseBigraphERState(String bigraphERStateFile) {

		// brsExpression = BRScondition;
		BigraphWrapper bigWrpr = new BigraphWrapper();
		bigWrapper = bigWrpr;

		// clear data if any
		clear();

		// bigWrapper.setBigraphERString(bigraphERStateFile);

		// implement a conversion of a BigraphER state to a wrapper
		//
		//
		JSONParser parser = new JSONParser();
		try {

			JSONObject state = (JSONObject) parser.parse(new FileReader(bigraphERStateFile));

			Bigraph big = convertJSONtoBigraph(state);

			bigWrapper.setCondition(false);
			bigWrapper.setBigraphObject(big);

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bigWrapper;
	}

	/**
	 * converts a given bigraph in JSON format to a Bigraph object from the
	 * LibBig library. A signature should be created first using the
	 * buildSignature method.
	 * 
	 * @param state
	 *            the JSON object containing the bigtaph
	 * @return Bigraph object
	 */
	// updated implementation of the convertJSONtoBigraph method that correspond
	// to Bigrapher v1.7.0
	// need to update CONTROL_ID & CONTROL_ARITY in JSONTerms class before
	// execution
	public Bigraph convertJSONtoBigraph(JSONObject state) {

		String tmp;
		String tmpArity = null;
		JSONObject tmpObj;
		JSONObject tmpCtrl;
		HashMap<String, BigraphNode> nodes = new HashMap<String, BigraphNode>();
		BigraphNode node;
		JSONArray ary;
		JSONArray innerAry;
		JSONArray outerAry;
		JSONArray portAry;
		Iterator<JSONObject> it;
		Iterator<JSONObject> itInner;
		Iterator<JSONObject> itOuter;
		Iterator<JSONObject> itPort;
		int src, target;
		LinkedList<String> outerNames = new LinkedList<String>();
		LinkedList<String> innerNames = new LinkedList<String>();
		LinkedList<String> outerNamesFull = new LinkedList<String>();
		LinkedList<String> innerNamesFull = new LinkedList<String>();

		HashMap<String, OuterName> libBigOuterNames = new HashMap<String, OuterName>();
		HashMap<String, InnerName> libBigInnerNames = new HashMap<String, InnerName>();
		HashMap<String, Node> libBigNodes = new HashMap<String, Node>();
		LinkedList<Root> libBigRoots = new LinkedList<Root>();
		LinkedList<Site> libBigSites = new LinkedList<Site>();

		SignatureBuilder sigBuilder = new SignatureBuilder();

		// number of roots, sites, and nodes respectively
		int numOfRoots = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH))
				.get(JSONTerms.BIGRAPHER_NUM_REGIONS).toString());
		int numOfSites = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH))
				.get(JSONTerms.BIGRAPHER_NUM_SITES).toString());
		int numOfNodes = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH))
				.get(JSONTerms.BIGRAPHER_NUM_NODES).toString());
		int edgeNumber = 0;

		// get controls & their arity [defines signature]. Controls are assumed
		// to be active (i.e. true)
		ary = (JSONArray) state.get(JSONTerms.BIGRAPHER_NODES);
		it = ary.iterator();
		while (it.hasNext()) {
			node = new BigraphNode();
			tmpObj = (JSONObject) it.next(); // gets hold of node info

			tmpCtrl = (JSONObject) tmpObj.get(JSONTerms.BIGRAPHER_CONTROL);
			tmp = tmpCtrl.get(JSONTerms.BIGRAPHER_CNTRL_NAME).toString();
			tmpArity = tmpCtrl.get(JSONTerms.BIGRAPHER_CNTRL_ARITY).toString();

			// set node id
			node.setId(tmpObj.get(JSONTerms.BIGRAPHER_NODE_ID).toString());
			// set node control
			node.setControl(tmp);
			nodes.put(node.getId(), node);

			// create a sig for the state
			sigBuilder.add(tmp, true, Integer.parseInt(tmpArity));
		}

		// get parents for nodes from the place_graph=>
		// roots and sites numbers
		ary = (JSONArray) ((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_ROOT_NODE);
		it = ary.iterator();

		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			src = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_SOURCE).toString());
			target = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_TARGET).toString());
			nodes.get(Integer.toString(target)).setParentRoot(src);
		}

		ary = (JSONArray) ((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_ROOT_SITE);
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			src = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_SOURCE).toString());
			target = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_TARGET).toString());
			nodes.get(Integer.toString(target)).setParentRoot(src);
		}

		ary = (JSONArray) ((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_NODE_NODE);
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			src = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_SOURCE).toString());
			target = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_TARGET).toString());

			// set parent node in the target node
			nodes.get(Integer.toString(target)).setParent(nodes.get(Integer.toString(src)));
			// add child node to source node
			nodes.get(Integer.toString(src)).addChildNode(nodes.get(Integer.toString(target)));

		}

		// get outer names and inner names for the nodes. Currently, focus on
		// outer names
		// while inner names are extracted they are not updated in the nodes
		ary = (JSONArray) (state.get(JSONTerms.BIGRAPHER_LINK_GRAPH));
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			outerNames.clear();
			innerNames.clear();

			// get outer names
			outerAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_OUTER));
			innerAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_INNER));
			portAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_PORTS));

			// get outernames
			for (int i = 0; i < outerAry.size(); i++) {
				JSONObject tmpOuter = (JSONObject) outerAry.get(i);

				outerNames.add(tmpOuter.get(JSONTerms.BIGRAPHER_NAME).toString());
			}

			// get inner names
			for (int i = 0; i < innerAry.size(); i++) {
				JSONObject tmpInner = (JSONObject) innerAry.get(i);

				innerNames.add(tmpInner.get(JSONTerms.BIGRAPHER_NAME).toString());
			}

			// get nodes connected to outer names. Inner names should be
			// considered
			if (outerNames.size() > 0) {
				for (int i = 0; i < portAry.size(); i++) {
					JSONObject tmpPort = (JSONObject) portAry.get(i);
					node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());

					node.addOuterNames(outerNames);
					node.addInnerNames(innerNames);
				}
			} else { // if there are no outer names, then create edges by
						// creating outernames, adding them to the nodes, then
						// closing the outername

				for (int i = 0; i < portAry.size(); i++) {
					JSONObject tmpPort = (JSONObject) portAry.get(i);
					node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());

					node.addOuterName("edge" + edgeNumber, true);
				}
				edgeNumber++;
			}

			// add inner names to nodes
			if (innerNames.size() > 0) {
				for (int i = 0; i < portAry.size(); i++) {
					JSONObject tmpPort = (JSONObject) portAry.get(i);
					node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());
					;
					node.addInnerNames(innerNames);
				}
			}
		}

		outerNamesFull.addAll(outerNames);
		innerNamesFull.addAll(innerNames);

		//// Create Bigraph Object \\\\\

		Signature tmpSig = sigBuilder.makeSignature();

		// if (tmpSig == null) {
		// return null;
		// }

		BigraphBuilder biBuilder = new BigraphBuilder(tmpSig);

		// create roots for the bigraph
		for (int i = 0; i < numOfRoots; i++) {
			libBigRoots.add(biBuilder.addRoot(i));
		}

		// create outer names
		OuterName tmpNm;
		HashMap<String, Boolean> isClosedMap = new HashMap<String, Boolean>();

		for (BigraphNode nd : nodes.values()) {
			for (BigraphNode.OuterName nm : nd.getOuterNamesObjects()) {
				if (libBigOuterNames.get(nm.getName()) == null) {
					tmpNm = biBuilder.addOuterName(nm.getName());
					libBigOuterNames.put(nm.getName(), tmpNm);
					isClosedMap.put(nm.getName(), nm.isClosed());
				}
			}
		}

		// create inner names
		// consider closing iner names also (future work)
		for (String inner : innerNamesFull) {
			libBigInnerNames.put(inner, biBuilder.addInnerName(inner));
		}

		// initial creation of nodes
		for (BigraphNode nd : nodes.values()) {
			if (libBigNodes.containsKey(nd.getId())) {
				continue;
			}
			createNode(nd, biBuilder, libBigRoots, libBigOuterNames, libBigNodes);
		}

		// close outernames
		for (OuterName nm : libBigOuterNames.values()) {
			if (isClosedMap.get(nm.getName())) {
				biBuilder.closeOuterName(nm);
			}
		}

		// add sites to bigraph (probably for states they don't have sites)
		for (BigraphNode n : nodes.values()) {
			if (n.hasSite()) {
				biBuilder.addSite(libBigNodes.get(n.getId()));
			}
		}

		return biBuilder.makeBigraph();
	}

	private static Node createNode(BigraphNode node, BigraphBuilder biBuilder, LinkedList<Root> libBigRoots,
			HashMap<String, OuterName> outerNames, HashMap<String, Node> nodes) {

		LinkedList<Handle> names = new LinkedList<Handle>();

		for (String n : node.getOuterNames()) {
			names.add(outerNames.get(n));
		}

		// if the parent is a root
		if (node.isParentRoot()) { // if the parent is a root

			Node n = biBuilder.addNode(node.getControl(), libBigRoots.get(node.getParentRoot()), names);
			nodes.put(node.getId(), n);
			return n;
		}

		// if the parent is already created as a node in the bigraph
		if (nodes.containsKey(node.getParent().getId())) {

			Node n = biBuilder.addNode(node.getControl(), nodes.get(node.getParent().getId()), names);
			nodes.put(node.getId(), n);
			return n;
		}

		Node n = biBuilder.addNode(node.getControl(),
				createNode(node.getParent(), biBuilder, libBigRoots, outerNames, nodes), names);
		nodes.put(node.getId(), n);
		return n;

	}

	/**
	 * Create a signature from a given state
	 * 
	 * @return
	 */
	public Signature createSignatureFromStates(JSONObject state) {
		SignatureBuilder sigBuilder = new SignatureBuilder();
		JSONArray ary;
		Iterator<?> it;
		JSONObject tmpObj, tmpCtrl;
		String tmp, tmpArity;
		LinkedList<String> controls = new LinkedList<String>();
		// int numOfStates = createTransitionSystem().getNumberOfStates();
		// JSONParser parser = new JSONParser();

		// read state from file
		// state = (JSONObject) parser.parse(new FileReader(outputFolder + "/" +
		// i + ".json"));
		ary = (JSONArray) state.get(JSONTerms.BIGRAPHER_NODES);
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info

			tmpCtrl = (JSONObject) tmpObj.get(JSONTerms.BIGRAPHER_CONTROL);
			tmp = tmpCtrl.get(JSONTerms.BIGRAPHER_CNTRL_NAME).toString();
			tmpArity = tmpCtrl.get(JSONTerms.BIGRAPHER_CNTRL_ARITY).toString();

			if (!controls.contains(tmp)) {
				// to avoid duplicates
				controls.add(tmp);
				sigBuilder.add(tmp, true, Integer.parseInt(tmpArity));
			}

		}

		return sigBuilder.makeSignature();

	}

	protected void addSite(String entityName) {

		if (entityName == null) {
			return;
		}

		Map<String, Boolean> sites = bigWrapper.getEntitySiteMap();

		sites.put(entityName, true);
	}

	protected void incrementRootSites() {

		bigWrapper.incrementRootSites();
	}

	protected String addControl(Entity entity) {

		if (entity == null) {
			return null;
		}

		String uniqName = "";

		uniqName = entity.getName() + controlNum;

		controlNum++;

		bigWrapper.getControlMap().put(entity, uniqName);

		return uniqName;
	}

	protected void addEntityRoot(String entityRoot) {

		updateEntityContainer(entityRoot, null);

		// add to root
		if (!bigWrapper.getRoots().contains(entityRoot)) {
			bigWrapper.getRoots().add(entityRoot);
		}

	}

	protected void addExtraRoot(String root) {

		// add to root
		if (!bigWrapper.getRoots().contains(root)) {
			bigWrapper.getRoots().add(root);
		}

	}

	protected void removeRoot(String root) {

		if (bigWrapper.getRoots() == null) {
			return;
		}

		bigWrapper.getRoots().remove(root);

	}

	protected void updateConnectivity(String conName, String entityName) {

		// add connection to the list of connections
		if (!bigWrapper.getConnections().contains(conName)) {
			bigWrapper.getConnections().add(conName);
		}

		// add to map of connections
		if (bigWrapper.getConnectivityMap().containsKey(conName)) {
			List<String> cons = bigWrapper.getConnectivityMap().get(conName);
			if (!cons.contains(entityName)) {
				cons.add(entityName);
			}
		} else {
			// new connection
			List<String> cons = new LinkedList<String>();
			cons.add(entityName);
			bigWrapper.getConnectivityMap().put(conName, cons);
		}

		// add to entity connection map
		if (bigWrapper.getEntityConnectivityMap().containsKey(entityName)) {
			List<String> cons = bigWrapper.getEntityConnectivityMap().get(entityName);
			if (!cons.contains(conName)) {
				cons.add(conName);
			}
		} else {
			// new connection for the entity
			List<String> cons = new LinkedList<String>();
			cons.add(conName);
			bigWrapper.getEntityConnectivityMap().put(entityName, cons);
		}
	}

	protected void updateEntityContainer(String entityName, String entityContainer) {

		// add to all entities
		if (!bigWrapper.getEntities().contains(entityName)) {
			bigWrapper.getEntities().add(entityName);
		}

		bigWrapper.getContainerEntitiesMap().put(entityName, entityContainer);

		// add to the control map
		// controlMap.put(uniqName, entityName);

		// update parent entity
		if (entityContainer == null) {
			return;
		}

		// update contained entities for parent
		if (bigWrapper.getContainedEntitiesMap().containsKey(entityContainer)) {
			List<String> children = bigWrapper.getContainedEntitiesMap().get(entityContainer);
			if (!children.contains(entityName)) {
				children.add(entityName);
			}
		} else {
			// new entity
			List<String> children = new LinkedList<String>();
			children.add(entityName);
			bigWrapper.getContainedEntitiesMap().put(entityContainer, children);
		}

	}

	protected Tokenizer createBRSTokenizer() {

		Tokenizer brsTokenizer = new Tokenizer();

		// order has importance
		
		brsTokenizer.add(BigraphERTokens.TOKEN_CONTAINMENT, BigraphERTokens.CONTAINMENT);
		brsTokenizer.add(BigraphERTokens.TOKEN_COMPOSITION, BigraphERTokens.COMPOSITION);
		brsTokenizer.add(BigraphERTokens.TOKEN_BIGRAPH_JUXTAPOSITION, BigraphERTokens.BIGRAPH_JUXTAPOSITION);
		brsTokenizer.add(BigraphERTokens.TOKEN_ENTITY_JUXTAPOSITION, BigraphERTokens.ENTITY_JUXTAPOSITION);
		brsTokenizer.add(BigraphERTokens.TOKEN_SITE, BigraphERTokens.SITE);
		brsTokenizer.add(BigraphERTokens.TOKEN_1, BigraphERTokens.ONE_1);
		brsTokenizer.add(BigraphERTokens.TOKEN_OPEN_BRACKET, BigraphERTokens.OPEN_BRACKET);
		brsTokenizer.add(BigraphERTokens.TOKEN_CLOSED_BRACKET, BigraphERTokens.CLOSED_BRACKET);
		brsTokenizer.add(BigraphERTokens.TOKEN_OPEN_BRACKET_CONNECTIVITY, BigraphERTokens.OPEN_BRACKET_CONNECTIVITY);
		brsTokenizer.add(BigraphERTokens.TOKEN_CLOSED_BRACKET_CONNECTIVITY,
				BigraphERTokens.CLOSED_BRACKET_CONNECTIVITY);
		brsTokenizer.add(BigraphERTokens.TOKEN_CLOSED_CONNECTIVITY, BigraphERTokens.CLOSED_CONNECTIVITY);
		brsTokenizer.add(BigraphERTokens.TOKEN_COMMA, BigraphERTokens.COMMA);
		brsTokenizer.add(BigraphERTokens.TOKEN_SMALL_SPACE, BigraphERTokens.SMALL_SPACE);
		brsTokenizer.add(BigraphERTokens.TOKEN_WORD, BigraphERTokens.WORD);

		return brsTokenizer;
	}

	public void clear() {

		if (bigWrapper != null) {
			bigWrapper.clear();
		}

		controlNum = 1;
	}

	// public String getBrsExpression() {
	// return brsExpression;
	// }

	// public void modifyBrsExpression() {
	//
	// //replace controls with equivlent entity names used
	//// List<Token> tokens = brsTokenizer.getTokens();
	// int fromIndex = 0;
	//
	//// String entityName = entities.get(index);
	//// S
	// modifiedBrsExpression = brsExpression;
	// StringBuffer temp = new StringBuffer(brsExpression);
	// int start = 0;
	// int end = 0;
	//
	// for(String entityName : entities) {
	// String ctrl = getControl(entityName);
	// start = temp.indexOf(ctrl, fromIndex);
	// end = start+ctrl.length();
	// fromIndex = end;
	//// System.out.println("entity: " + entityName + " ctrl: "+ctrl);
	// temp.delete(start, end);
	// temp.insert(start, entityName);
	//
	//// modifiedBrsExpression = modifiedBrsExpression.replace
	// }
	//
	// modifiedBrsExpression = temp.toString();
	//// for(Token t : tokens) {
	////
	//// if(t)
	//// }
	// }

	// protected String getControl(String entityName) {
	//
	// for(Entry<Entity, String> entry : controlMap.entrySet()) {
	// if(entry.getValue().equals(entityName)){
	// return entry.getKey().getName();
	// }
	// }
	// return null;
	// }

}
