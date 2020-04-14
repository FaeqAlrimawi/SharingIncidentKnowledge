package ie.lero.spare.forensics.assessment.brs.parser;

import java.util.List;

public class ActionChangeHolder {
	
	private BigraphRelationType changeType;
	private BigraphChangeOperation changeOperation;

	//holds the names of entities that the change applies to
	private List<String> changedEntities;

	public BigraphRelationType getChangeType() {
		return changeType;
	}

	public void setChangeType(BigraphRelationType changeType) {
		this.changeType = changeType;
	}

	public BigraphChangeOperation getChangeOperation() {
		return changeOperation;
	}

	public void setChangeOperation(BigraphChangeOperation changeOperation) {
		this.changeOperation = changeOperation;
	}

	public List<String> getChangedEntities() {
		return changedEntities;
	}

	public void setChangedEntities(List<String> changedEntities) {
		this.changedEntities = changedEntities;
	}
	
	
	public String toString() {
		
		StringBuilder bldr = new StringBuilder();
		
		bldr.append("-Change Type: ").append(changeType)
		.append("\n-Change Operation: ").append(changeOperation)
		.append("\n-changed Entities: ").append(changedEntities);
		
		return bldr.toString();
		
	}
}
