package ie.lero.spare.forensics.assessment.monitor;

import java.util.LinkedList;
import java.util.List;

import ie.lero.spare.forensics.assessment.brs.parser.utilities.JSONTerms;

//import core.brs.parser.utilities.JSONTerms;

public interface MonitorTerms {
	
	
	//indicates the target control in a given bigraph e.g., Server.Target
	public static final String TAG_MONITOR_TARGET = "MonitorTarget_Tag";
	
	//indicates the monitor in a given bigraph
	public static final String TAG_MONITOR = "Monitor_Tag";
	
	//used for different purposes such as signature update
	public static final List<String> MONITOR_TERMS = new LinkedList<String>(){{
		add(TAG_MONITOR);
		add(TAG_MONITOR_TARGET);
		add(JSONTerms.CONTROL_ASSET_ID);}
	};
	
	public static final List<String> MONITOR_TERMS_TO_IGNORE = new LinkedList<String>(){{
		add(TAG_MONITOR);
		add(TAG_MONITOR_TARGET);
		
		}
	};

}
