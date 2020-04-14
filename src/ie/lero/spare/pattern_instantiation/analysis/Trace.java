package ie.lero.spare.pattern_instantiation.analysis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ie.lero.spare.forensics.assessment.brs.parser.ActionWrapper;
import ie.lero.spare.forensics.assessment.brs.parser.BigraphWrapper;

//import core.brs.parser.ActionWrapper;
//import core.brs.parser.BigraphWrapper;

public class Trace implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -29617119168807943L;

	// key is action name, value is the object rep. the action
	private Map<String, ActionWrapper> actions;

	// id
	private String traceID;

	public Trace() {
		actions = new HashMap<String, ActionWrapper>();
	}

	public String getTraceID() {
		return traceID;
	}

	public void setTraceID(String traceID) {
		this.traceID = traceID;
	}

	public Map<String, ActionWrapper> getActions() {
		return actions;
	}

	public void setActions(Map<String, ActionWrapper> actions) {
		this.actions = actions;
	}

	public void addAction(ActionWrapper newAction) {

		if (newAction == null) {
			return;
		}

		String actName = newAction.getActionName();

		actions.put(actName, newAction);
	}

	public void addAction(ActionWrapper newAction, int originalPreState, int originalPostState) {

		if (newAction == null) {
			return;
		}

		String actName = newAction.getActionName();

		BigraphWrapper pre = newAction.getPrecondition();

		if (pre != null) {
			pre.setOriginalState(originalPreState);
		}

		BigraphWrapper post = newAction.getPrecondition();

		if (post != null) {
			post.setOriginalState(originalPreState);
		}

		actions.put(actName, newAction);
	}

	public boolean save(String filePath) {

		FileOutputStream fileOut;
		try {
			//remove any Bigaph objects from the actions
			removeBigraphObjsFromActions();
			fileOut = new FileOutputStream(filePath);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();

			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

	protected void removeBigraphObjsFromActions(){
		
		if(actions == null) {
			return;
		}
		
		for(ActionWrapper act : actions.values()) {
			
			//precondition
			BigraphWrapper pre = act.getPrecondition();
			
			if(pre!=null ) {
				//set bigraph object to null
//				if(pre.getBigraphObject()!=null){
//					pre.setBigraphObject(null);	
//				}
				
				pre.setBigraphObject(null);
				pre.setSignature(null);
				//set signature to null
//				if(pre.getSignature()!=null) {
//					pre.setSignature(null);
//				}
			}
			
			
			
			//postcondition
			BigraphWrapper post = act.getPostcondition();
			
			if(post!=null) {
				post.setBigraphObject(null);
				post.setSignature(null);
			}
			
			
		}
	}
//	/**
//	 * Finds the causal dependency chain between actions in the given incident
//	 * trace
//	 * 
//	 * @param incidentTrace
//	 *            a GraphPath object representing the potential incident
//	 * @return A map of the causal dependency between actions in the given
//	 *         potential incident. Key is the action name, value is a boolean
//	 *         whether the action depends on the previous action or not
//	 */
//	public Map<String, Boolean> findCausalDependency(GraphPath incidentTrace) {
//
//		if (incidentTrace == null) {
//			return null;
//		}
//
//		Map<String, Boolean> causalDependencyMap = new HashMap<String, Boolean>();
//
//		return causalDependencyMap;
//	}
//
//	/**
//	 * Determines if the action is causally dependent on the preAction Causally
//	 * dependence means that the action would not have happened if the preAction
//	 * had not happened.
//	 * 
//	 * @param preAction
//	 *            the previous action
//	 * @param action
//	 *            the action that we want to determine if it is causally
//	 *            dependent on preAction
//	 * @return True if action is causally dependent on the preAction. False
//	 *         otherwise
//	 */
//	public boolean areActionsCausallyDependent(ActionWrapper action, ActionWrapper preAction, TraceMiner traceMiner) {
//
//		/**
//		 * Causally dependence is implemented by checking if the pre-condition
//		 * of the given action is matches to the state that the preAction's
//		 * pre-condition matches to. If it matches then the action is NOT
//		 * causally dependent. Otherwise, it is causally dependent
//		 **/
//
//		if(action == null || preAction == null || traceMiner == null) {
//			return false;
//		}
//		
//		
//		//get Bigraph representation of the precondition of action
////		Bigraph actionPre = action.getPrecondition()!=null?action.getPrecondition().ge;
//		
//		
//		return false;
//	}
}
