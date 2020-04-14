package ie.lero.spare.pattern_instantiation;

import java.util.HashMap;

import ie.lero.spare.utility.Logger;
import ie.lero.spare.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Signature;

public class SystemInstanceHandler {

	private String outputFolder;
	private SystemExecutor executor;
	private TransitionSystem transitionSystem;
	private HashMap<Integer, Bigraph> states;
	private Signature globalBigraphSignature;
//	private boolean isDebugging = true;
	private String errorSign = "## ";
	private Logger logger;
	private long sysID;//set by the SystemsHandler
	public static final String SYSTEM_INSTANCE_HANDLER = "System-Instance-Handler";
	private String instanceName;
	
	
	public SystemInstanceHandler() {
	
			instanceName = SYSTEM_INSTANCE_HANDLER+Logger.SEPARATOR_BTW_INSTANCES;
	}
	
	public boolean analyseBRS() {

		boolean isDone = false;
		// BlockingQueue<String> msgQ = Logger.getInstance().getMsgQ();
		// Logger logger = Logger.getInstance();

		if (executor == null) {
			logger.putError(instanceName+"Bigraph System Executor is not set");
			isDone = false;
		} else {
			logger.putMessage(instanceName+"Executing the Bigraphical Reactive System (BRS)...");
			outputFolder = executor.execute();

			if (outputFolder != null) {
				logger.putMessage(instanceName+"Creating Bigraph Signature...");

				// get the signature
				globalBigraphSignature = executor.getBigraphSignature();

				if (globalBigraphSignature != null) {
				} else {
					logger.putError(instanceName+ errorSign
							+ "Something went wrong creating the Bigraph signature");
					isDone = false;
				}

				logger.putMessage(instanceName+"Creating Bigraph transition system...");
				// get the transition system
				transitionSystem = executor.getTransitionSystem();

				if (transitionSystem != null) {
				} else {
					logger.putError(instanceName+errorSign
							+ "something went wrong while creating the Bigraph transition system");
					isDone = false;
				}

				logger.putMessage(instanceName+"Loading states...");
				// gete states as Bigraph objects
				states = executor.getStates();

				if (states != null) {
				} else {
					logger.putError(instanceName+errorSign
							+ "something went wrong while loading the Bigraph system states");
					isDone = false;
				}

				isDone = true;
			} else {
				logger.putError(instanceName+
					errorSign + "something went wrong while executing the BRS");
				isDone = false;
			}
		}

		return isDone;
	}

	public void setSysID(long id) {
		sysID = id;
	}

	public long getSysID() {
		
		return sysID;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public SystemExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(SystemExecutor executor) {
		this.executor = executor;
	}

	public TransitionSystem getTransitionSystem() {
		return transitionSystem;
	}

	public HashMap<Integer, Bigraph> getStates() {
		return states;
	}

	public Signature getGlobalBigraphSignature() {
		return globalBigraphSignature;
	}

	public void setBigraphSignature(Signature bigraphSignature) {
		this.globalBigraphSignature = bigraphSignature;
	}

}
