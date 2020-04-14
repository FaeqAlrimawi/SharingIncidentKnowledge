package ie.lero.spare.utility;

import java.util.LinkedList;

public class BigraphNode {
	
	private String id; //the name of the asset in the system model
	private String incidentAssetName; //the name of the asset in the incident model
	private LinkedList<OuterName> outerNames;
	private LinkedList<InnerName> innerNames;
	private Site site;
	private LinkedList<BigraphNode> childNodes;
	private String control;
	private BigraphNode parent;
	private int parentRoot;
	private boolean isKnowledgePartial;
	public BigraphNode() {
		
		outerNames = new LinkedList<OuterName>();
		innerNames = new LinkedList<InnerName>();
		site = null;
		childNodes = new LinkedList<BigraphNode>();
		parentRoot = -1;
		parent = null;
		isKnowledgePartial = false;
	}

	public boolean addOuterName(String name, boolean isClosed) {
		
		OuterName tmp = new OuterName(name, isClosed);
		
		if(!outerNames.contains(tmp)) {
			outerNames.add(tmp);
			return true;
		}
		
		return false;
		
	}
	
	
	
	public String getIncidentAssetName() {
		return incidentAssetName;
	}

	public void setIncidentAssetName(String incidentAssetName) {
		this.incidentAssetName = incidentAssetName;
	}

	public boolean isKnowledgePartial() {
		return isKnowledgePartial;
	}

	public void setKnowledgePartial(boolean isKnowledgePartial) {
		this.isKnowledgePartial = isKnowledgePartial;
	}

	public boolean addOuterName(String name) {
		return addOuterName(name, false);
	}
	
	public boolean addOuterNames(LinkedList<String> names) {
		
		if(names != null && !names.isEmpty()){
			for(String n : names) {
				addOuterName(n, false);
			}
			return true;
		}
		
		return false;
	}
	
	public boolean addOuterNames(OuterName name) {
		
		if(name == null) {
			return false;
		}
		
		outerNames.add(name);
		
		return true;
	}
	
	public boolean addInnerName(String name, boolean isClosed) {
		
		InnerName tmp = new InnerName(name, isClosed);
		
		if(!innerNames.contains(tmp)) {
			innerNames.add(tmp);
			return true;
		}
		
		return false;
	}
	
	public boolean addInnerName(String name) {
		
		return addInnerName(name, false);
	}
	
	public boolean addInnerNames(LinkedList<String> names) {
		
		if(names != null && !names.isEmpty()){
			for(String n : names) {
				addInnerName(n, false);
			}
			return true;
		}
		
		return false;
	}
	
	public boolean addInnerNames(InnerName name) {
		
		if(name == null) {
			return false;
		}
		
		innerNames.add(name);
		
		return true;
	}
	
	public void addChildNode(BigraphNode node) {
		if(!childNodes.contains(node)) {
			childNodes.add(node);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!BigraphNode.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final BigraphNode other = (BigraphNode) obj;
	    
	    //if both nodes have the same id they are equal
	    if(other.getId().equals(this.id)) {
	    	return true;
	    }
	    
	    return false;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LinkedList<String> getOuterNames() {
		LinkedList<String> names = new LinkedList<String>();
		
		if(outerNames != null) {
			for(OuterName n : outerNames) {
				names.add(n.getName());
			}
		}
		
		return names;
	}
	
	public LinkedList<OuterName> getOuterNamesObjects() {
		return outerNames;
	}

	public void setOuterNames(LinkedList<String> outerNames) {
		this.outerNames.clear();
		addOuterNames(outerNames);
	}

	public LinkedList<String> getInnerNames() {
		LinkedList<String> names = new LinkedList<String>();
		
		if(innerNames != null) {
			for(InnerName n : innerNames) {
				names.add(n.getName());
			}
		}
		
		return names;
	}
	
	public LinkedList<InnerName> getInnerNamesObjects() {
		return innerNames;
	}

	public void setInnerNames(LinkedList<String> innerNames) {
		innerNames.clear();
		addInnerNames(innerNames);
	}

	public boolean hasSite() {
		if(site != null) {
			return true;
		}
		
		return false;
	}

	public void setSite(boolean setSite) {
		
		if(setSite) {
			if(site == null) {
				site = new Site();
			}
		} else {
			site = null;
		}
		
	}

	public void setSite(String name, boolean isClosed) {
		
		if(site == null) {
			site = new Site(name, isClosed);	
		}
		
		site.setName(name);
		site.setClosed(isClosed);
	}
	
	public LinkedList<BigraphNode> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(LinkedList<BigraphNode> nodes) {
		this.childNodes = nodes;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public BigraphNode getParent() {
		return parent;
	}

	public void setParent(BigraphNode parent) {
		this.parent = parent;
	}

	public int getParentRoot() {
		return parentRoot;
	}

	public void setParentRoot(int parentRoot) {
		this.parentRoot = parentRoot;
	}
	
	public boolean hasParent() {
		
		if(parent != null) {
			return true;
		}
		
		return false;
	}

	public boolean isParentRoot() {
		if(parent == null && parentRoot != -1) {
			return true;
		}
		
		return false;
	}
	
	public boolean isLeaf() {
		if(childNodes == null || childNodes.size() == 0) {
			return true;
		}
		
		return false;
	}
	public String toString() {
		StringBuilder res = new StringBuilder();
		
		res.append("id:").append(getId()).append(",")
			.append("control:").append(getControl()).append(",");
		
		if(parent == null) {
			res.append("parentRoot:").append(getParentRoot());
		} else {
			res.append("parent:").append(getParent().getId());
		}
		
		//append outer names
		if(outerNames != null && outerNames.size()>0) {
		res.append(",outerNames:{");
		for(int i=0;i<outerNames.size();i++) {
			res.append(outerNames.get(i));
			if(i != outerNames.size()-1) {
				res.append(",");
			}	
		}
		res.append("}");
		}
		
		//append inner names
				if(innerNames != null && innerNames.size()>0) {
				res.append(",innerNames:{");
				for(int i=0;i<innerNames.size();i++) {
					res.append(innerNames.get(i));
					if(i != innerNames.size()-1) {
						res.append(",");
					}	
				}
				res.append("}");
				}
		
		//append site
		res.append("site:"+site);
		
		res.append("\n");
		
		return res.toString();
	}
	
	class BRSName {
		
		protected String name;
		protected boolean isClosed;
		
		public BRSName(){
			name ="";
			isClosed = false;
		}
		
		public BRSName(String name){
			this.name = name;
			isClosed = false;
		}
		
		public BRSName(String name, boolean isClosed){
			this.name = name;
			this.isClosed = isClosed;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isClosed() {
			return isClosed;
		}

		public void setClosed(boolean isClosed) {
			this.isClosed = isClosed;
		}
		
		@Override
		public boolean equals(Object other){
		    boolean result;
		    
		    if((other == null) || (!BRSName.class.isAssignableFrom(other.getClass()))){
		        return false;
		    } 

		     BRSName otherName = (BRSName)other;
		     
		     //equality based on name matching
		     result = name.equals(otherName.name);

		    return result;
		} // end equals
	
	}
	
	public class OuterName extends BRSName {
		
		public OuterName() {
			super();
		}
		
		public OuterName(String name){
			super(name);
		}
		
		public OuterName(String name, boolean isClosed) {
			super(name, isClosed);
		}
		
	}
	
	public class InnerName extends BRSName {
		
		public InnerName() {
			super();
		}
		
		public InnerName(String name){
			super(name);
		}
		
		public InnerName(String name, boolean isClosed) {
			super(name, isClosed);
		}
	}
	
	public class Site extends BRSName {
		
		public Site() {
			super();
		}
		
		public Site(String name){
			super(name);
		}
		
		public Site(String name, boolean isClosed) {
			super(name, isClosed);
		}
	}

}
