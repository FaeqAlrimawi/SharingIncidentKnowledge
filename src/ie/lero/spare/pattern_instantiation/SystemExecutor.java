package ie.lero.spare.pattern_instantiation;

import java.util.HashMap;

import ie.lero.spare.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Signature;

public interface SystemExecutor {
	
	public String execute();
	public TransitionSystem getTransitionSystem();
	public Signature getBigraphSignature();
	public HashMap<Integer, Bigraph> getStates();
	public String[] getActionNames();

}
