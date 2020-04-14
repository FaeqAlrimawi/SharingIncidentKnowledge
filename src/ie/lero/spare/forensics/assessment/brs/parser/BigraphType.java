package ie.lero.spare.forensics.assessment.brs.parser;

public enum BigraphType {
	
	BRS ("brs"), 
	PBRS ("pbrs"), // propabilistic
	SBRS("sbrs"),
	UNKNOWN("Unknown");// stochastic
	
		BigraphType(String type) {
		
	}

}
