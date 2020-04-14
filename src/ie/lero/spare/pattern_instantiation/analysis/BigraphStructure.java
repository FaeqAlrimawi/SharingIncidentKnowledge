package ie.lero.spare.pattern_instantiation.analysis;

import java.util.List;

import ie.lero.spare.forensics.assessment.brs.parser.BigraphRelationType;



public class BigraphStructure {
	
	private BigraphRelationType structureType;
	
	private String mainEntityName;
	
	private List<String> entities;
	
	private List<String> connections;

	public BigraphRelationType getStructureType() {
		return structureType;
	}

	public void setStructureType(BigraphRelationType structureType) {
		this.structureType = structureType;
	}

	public String getMainEntityName() {
		return mainEntityName;
	}

	public void setMainEntityName(String mainEntityName) {
		this.mainEntityName = mainEntityName;
	}

	public List<String> getEntities() {
		return entities;
	}

	public void setEntities(List<String> entities) {
		this.entities = entities;
	}

	public List<String> getConnections() {
		return connections;
	}

	public void setConnections(List<String> connections) {
		this.connections = connections;
	}
	
	public String toString(){
		
		StringBuilder str = new StringBuilder();
		String sep = " -- ";
		
		str.append("Structure-Type: ").append(structureType.toString()).append(sep);
		str.append("Main Entity: ").append(mainEntityName);
		
		return str.toString();
	
	}

	
}
