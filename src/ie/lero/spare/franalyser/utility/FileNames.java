package ie.lero.spare.franalyser.utility;

public class FileNames {

	/**Files required by the instantiation technique**/
	
	//state transitions file
	public static final String TRANSITIONS = "transitions.json";
	
	//generated labelled transition file. Saved same location as the original
	public static final String LABELLED_TRANSITIONS = "transitions_labelled.json";
	
	//not used at the moment
	public static final String BIGRAPH_ACTIONS = "actionNames.txt";
	
	//System Classes to Controls Map file
	//contains enteries in the format: SystemClass:Bigrapher-Control-1;Bigrapher-Control-2; ...; Bigrapher-Control-N
	//e.g., SmartLight SmartLight ComputingDevice PhysicalAsset
	public static final String ASSET_CONTROL_MAP = "asset-control_map.txt";
	public static final String ASSET_CONTROL_SEPARATOR = ":";
	public static final String CONTROLS_SEPARATOR = ";";
	
}
