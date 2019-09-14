package ie.lero.spare.franalyser.utility;

public class JSONTerms {
	
	
	public static final String ENTITY = "entity";
	public static final String OUTERNAME = "connectivity";
	public static final String INNERNAME = "innername";
	public static final String NAME = "name";
	public static final String CONTROL = "control";
	public static final String ISLINK = "isLink";
	public static final String ISCLOSED = "isClosed";
	public static final String SITE = "site";
	public static final String INCIDENT_ASSET_NAME = "incidentAssetName";
	public static final String TRUE_VALUE = "true";

	//potential instance generated strings
	public static final String INSTANCE_POTENTIAL = "potential_incident_instances";
	public static final String INSTANCE_POTENTIAL_COUNT = "instances_count";
	public static final String INSTANCE_POTENTIAL_INSTANCES = "instances";
	public static final String INSTANCE_POTENTIAL_INSTANCES_ID = "instance_id";
	public static final String INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS = "transitions";
	public static final String INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTION = "action";
	public static final String INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_SOURCE = "source";
	public static final String INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_TARGET = "target";
	public static final String INSTANCE_MAP = "map";
	public static final String INSTANCE_MAP_SYSTEM_ASSET_NAME = "system_asset_name";
	public static final String INSTANCE_MAP_INCIDENT_ENTITY_NAME= "incident_entity_name";	
	//compact format
	public static final String INSTANCE_POTENTIAL_INSTANCES_TRANSITIONS_ACTIONS = "actions";
	
	//bigrapher state
	public static final String BIGRAPHER_PLACE_GRAPH = "place_graph";
	public static final String BIGRAPHER_DAG = "dag";
	public static final String BIGRAPHER_ROOT_NODE = "rn"; //rn: root contianing nodes
	public static final String BIGRAPHER_ROOT_SITE = "rs"; //rs: root contianing site
	public static final String BIGRAPHER_NODE_NODE = "nn"; //nn: node contining node
	public static final String BIGRAPHER_NODE_SITE = "ns"; //nn: node contining site
	public static final String BIGRAPHER_SOURCE = "source";
	public static final String BIGRAPHER_TARGET = "target";
	public static final String BIGRAPHER_LINK_GRAPH = "link_graph";
	public static final String BIGRAPHER_NUM_REGIONS = "num_regions";
	public static final String BIGRAPHER_REGIONS = "regions";
	public static final String BIGRAPHER_NUM_SITES = "num_sites";
	public static final String BIGRAPHER_SITES = "sites";
	public static final String BIGRAPHER_NODES = "nodes";
	public static final String BIGRAPHER_NUM_NODES = "num_nodes";
	public static final String BIGRAPHER_NODE_ID = "node_id";
	public static final String BIGRAPHER_OUTER = "outer";
	public static final String BIGRAPHER_INNER = "inner";
	public static final String BIGRAPHER_PORTS = "ports";
	public static final String BIGRAPHER_CONTROL = "control";
	public static final String BIGRAPHER_CNTRL_NAME = "ctrl_name";
//	public static final String BIGRAPHER_CONTROL_ID = "control_id";
	public static final String BIGRAPHER_CNTRL_ARITY = "ctrl_arity";
	public static final String BIGRAPHER_CNTRL_PARAMS = "ctrl_params";
//	public static final String BIGRAPHER_CONTROL_ARITY = "control_arity";
	public static final String BIGRAPHER_NAME = "name";
	
	//bigrapher transition file
	public static final String TRANSITIONS_BRS = "brs";
	public static final String TRANSITIONS__PROP_BRS = "pbrs";
	public static final String TRANSITIONS__STOCHASTIC_BRS = "sbrs";
	public static final String TRANSITIONS__TARGET = "target";
	public static final String TRANSITIONS__SOURCE = "source";
	public static final String TRANSITIONS__PROBABILITY = "probability";
	public static final String TRANSITIONS__LABEL = "action";
	
	
	
}
