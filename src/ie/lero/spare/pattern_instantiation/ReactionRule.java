package ie.lero.spare.pattern_instantiation;

import java.util.ArrayList;

public class ReactionRule {
	
	private String redex;
	private String reactum;
	private String name;
	ArrayList<Integer> redexStates;
	ArrayList<Integer> reactumStates;
	
	public ReactionRule() {
		redexStates = new ArrayList<Integer>();
		reactumStates = new ArrayList<Integer>();
	}

	public String getRedex() {
		return redex;
	}

	public void setRedex(String redex) {
		this.redex = redex;
	}

	public String getReactum() {
		return reactum;
	}

	public void setReactum(String reactum) {
		this.reactum = reactum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Integer> getRedexStates() {
		return redexStates;
	}

	public void setRedexStates(ArrayList<Integer> redexStates) {
		this.redexStates = redexStates;
	}

	public ArrayList<Integer> getReactumStates() {
		return reactumStates;
	}

	public void setReactumStates(ArrayList<Integer> reactumStates) {
		this.reactumStates = reactumStates;
	}
	
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!ReactionRule.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final ReactionRule other = (ReactionRule) obj;
	    
	    if(this.name.contentEquals(other.getName())) {
	    	return true;
	    }
	    
	    return false;
	}

	public String toString() {
		StringBuilder res = new StringBuilder();
		
		res.append(name)
		.append("\nRedex states = ").append(redexStates)
		.append("\nReactum states = ").append(reactumStates);
		
		return res.toString();
	}
	
}
