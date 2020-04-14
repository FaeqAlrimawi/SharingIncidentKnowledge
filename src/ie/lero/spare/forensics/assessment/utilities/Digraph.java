package ie.lero.spare.forensics.assessment.utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Digraph<V> {

    public static class Edge<V>{
        private V vertex;
        private double probability;
        private String label;
        
        public Edge(V v, double prob, String lbl){
            vertex = v; 
            probability = prob;
            label = lbl;
        }
        
        public Edge(V v, double prob){
            vertex = v; 
            probability = prob;
            label = null;
        }

        public V getVertex() {
            return vertex;
        }

        public double getProbability() {
            return probability;
        }

        public String getLabel() {
        	return label;
        }
        
        @Override
        public String toString() {
          
            if(label != null && !label.isEmpty()) {
            	  return "Edge [state=" + vertex + ", probability=" + probability + ", label = "+ label +"]";
            } else {
            	  return "Edge [state=" + vertex + ", probability=" + probability + "]";
            }
        }

    }

    /**
     * A Map is used to map each vertex to its list of adjacent vertices.
     */

    private Map<V, List<Edge<V>>> neighbors = new HashMap<V, List<Edge<V>>>();
    private Map<V, List<V>> neighborNodes = new HashMap<V, List<V>>();
    private int[][] adjacencyMatrix;
    
    private int nr_edges;

    
    public Digraph() {
    	
    }
    
    public Digraph(Digraph<V> digraph) {
    	
    	String label = null;
    	double prob = -1;
    	
    	for(V srcNode : digraph.getNodes()) {
    		for(V desNode : digraph.outboundNeighbors(srcNode)) {
    			label = digraph.getLabel(srcNode, desNode);
    			prob = digraph.getProbability(srcNode, desNode);
    			this.add(srcNode, desNode, prob, label);
    		}
    	}
    }
    
    /**
     * String representation of graph.
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (V v : neighbors.keySet())
            s.append("\n    " + v + " -> " + neighbors.get(v));
        return s.toString();
    }

    /**
     * Add a vertex to the graph. Nothing happens if vertex is already in graph.
     */
    public void add(V vertex) {
        if (neighbors.containsKey(vertex))
            return;
        neighbors.put(vertex, new ArrayList<Edge<V>>());
    }

    public int getNumberOfEdges(){
        int sum = 0;
        for(List<Edge<V>> outBounds : neighbors.values()){
            sum += outBounds.size();
        }
        return sum;
    }

    /**
     * True iff graph contains vertex.
     */
    public boolean contains(V vertex) {
        return neighbors.containsKey(vertex);
    }

    /**
     * Add an edge to the graph; if either vertex does not exist, it's added.
     * This implementation allows the creation of multi-edges and self-loops.
     */
    public void add(V from, V to, double probability) {
        this.add(from);
        this.add(to);

        neighbors.get(from).add(new Edge<V>(to, probability));
    }
    
    public void add(V from, V to, double probability, String label) {
        this.add(from);
        this.add(to);
        neighbors.get(from).add(new Edge<V>(to, probability, label));
    }

    public synchronized int outDegree(int vertex) {
        return neighbors.get(vertex).size();
    }

    public synchronized int inDegree(V vertex) {
       return inboundNeighbors(vertex).size();
    }

    public List<V> outboundNeighbors(V vertex) {
        List<V> list = new ArrayList<V>();
        for(Edge<V> e: neighbors.get(vertex))
            list.add(e.vertex);
        return list;
    }

    public synchronized List<V> outboundNeighborsForTransitionGeneration(V vertex) {
        
    	return neighborNodes.get(vertex);
       
    }
    
    public void deleteNeighborNodesMap() {
        
    	neighborNodes.clear();
       
    }
    
    public void generateNeighborNodesMap() {
    	
    	for(V node : getNodes()) {
    		neighborNodes.put(node, outboundNeighbors(node));
    	}
    }
    
    public List<V> inboundNeighbors(V inboundVertex) {
        List<V> inList = new ArrayList<V>();
        for (V to : neighbors.keySet()) {
            for (Edge<V> e : neighbors.get(to))
                if (e.vertex.equals(inboundVertex))
                    inList.add(to);
        }
        return inList;
    }

    public boolean isEdge(V from, V to) {
      for(Edge<V> e :  neighbors.get(from)){
          if(e.vertex.equals(to))
              return true;
      }
      return false;
    }

    public double getProbability(V from, V to) {
        for(Edge<V> e :  neighbors.get(from)){
            if(e.vertex.equals(to))
                return e.probability;
        }
        return -1;
    }
    
	public List<V> getNodes() {
    	List<V> inList = new ArrayList<V>();
        for (V to : neighbors.keySet()) {
             inList.add(to);
        }
        return inList;
    }
    
	public int getNumberOfNodes() {
    	
		return neighbors.size();
    }
	
    public String getLabel(V from, V to) {
        for(Edge<V> e :  neighbors.get(from)){
            if(e.vertex.equals(to))
                return e.label;
        }
        return null;
    }
    
    public void setLabel(V from, V to, String label) {
        for(Edge<V> e :  neighbors.get(from)){
            if(e.vertex.equals(to))
                e.label = label;
        }
    }
    
    protected void createAdjacencyMatrix() {
    	
    	int numOfNodes = neighbors.size();
    	adjacencyMatrix = new int[numOfNodes][numOfNodes];
    	
    	generateNeighborNodesMap();
    	
    	for(int i=0;i<numOfNodes;i++) {
    		for(int j=0;j<numOfNodes;j++) {
    			if(neighborNodes.get(i).contains(j)) {
    				adjacencyMatrix[i][j] = 1;
    			}
    		}
    	}
    }
    

    /**
     * Returns number of possible walks for all nodes to all nodes
     * @param graph
     * @param u
     * @param v
     * @param k
     * @return
     */
	public int[][][] countwalks(int k) {
		// Table to be filled up using DP. The value count[i][j][e]
		// will/ store count of possible walks from i to j with
		// exactly k edges
		int V = neighbors.size(); //V is number of node;
		
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		System.out.println("creating adjacency matrix...");
		
		if(adjacencyMatrix == null) {
			createAdjacencyMatrix();
		}
		
		
		int count[][][] = new int[V][V][k + 1];
		
		System.out.println("findin all possbile paths...");
		
		// Loop for number of edges from 0 to k
		for (int e = 0; e <= k; e++) {
			for (int i = 0; i < V; i++) // for source
			{
				for (int j = 0; j < V; j++) // for destination
				{
					// initialize value
					count[i][j][e] = 0;

					// from base cases
					if (e == 0 && i == j)
						count[i][j][e] = 1;
					if (e == 1 && adjacencyMatrix[i][j] != 0)
						count[i][j][e] = 1;

					// go to adjacent only when number of edges
					// is more than 1
					if (e > 1) {
						for (int a = 0; a < V; a++) // adjacent of i
							if (adjacencyMatrix[i][a] != 0)
								count[i][j][e] += count[a][j][e - 1];
					}
				}
			}
		}

		long endTime = Calendar.getInstance().getTimeInMillis();
		
		long duration = endTime - startTime;

		int secMils2 = (int) duration % 1000;
		int hours2 = (int) (duration / 3600000) % 60;
		int mins2 = (int) (duration / 60000) % 60;
		int secs2 = (int) (duration / 1000) % 60;

		// execution time
		System.out.println("Time to calculate walks: "
						+ duration + "ms [" + hours2 + "h:" + mins2 + "m:" + secs2 + "s:" + secMils2 + "ms]");

		
//		for (int i = 0; i < count[0][v].length; i++) {
//			System.out.println(count[0][v][i]);
//		}

		return count;
	}
	
	public int[][][] countwalksList(int k) {
		// Table to be filled up using DP. The value count[i][j][e]
		// will/ store count of possible walks from i to j with
		// exactly k edges
		int V = neighbors.size(); //V is number of node;
		
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		
		
//		if(adjacencyMatrix == null) {
//			createAdjacencyMatrix();
//		}
		
		if(neighborNodes == null || neighborNodes.isEmpty()) {
			System.out.println("creating neighbours lists...");
			generateNeighborNodesMap();
		}
		
		int count[][][] = new int[V][V][k + 1];
		
		System.out.println("findin all possbile paths...");
		
		// Loop for number of edges from 0 to k
		for (int e = 0; e <= k; e++) {
			for (int i = 0; i < V; i++) // for source
			{
				for (int j = 0; j < V; j++) // for destination
				{
					// initialize value
					count[i][j][e] = 0;

					// from base cases
					if (e == 0 && i == j)
						count[i][j][e] = 1;
					if (e == 1 && neighborNodes.get(i).contains(j))
						count[i][j][e] = 1;

					// go to adjacent only when number of edges
					// is more than 1
					if (e > 1) {
						for (int a = 0; a < V; a++) // adjacent of i
							if (neighborNodes.get(i).contains(a))
								count[i][j][e] += count[a][j][e - 1];
					}
				}
			}
		}

		long endTime = Calendar.getInstance().getTimeInMillis();
		
		long duration = endTime - startTime;

		int secMils2 = (int) duration % 1000;
		int hours2 = (int) (duration / 3600000) % 60;
		int mins2 = (int) (duration / 60000) % 60;
		int secs2 = (int) (duration / 1000) % 60;

		// execution time
		System.out.println("Time to calculate walks: "
						+ duration + "ms [" + hours2 + "h:" + mins2 + "m:" + secs2 + "s:" + secMils2 + "ms]");

		
//		for (int i = 0; i < count[0][v].length; i++) {
//			System.out.println(count[0][v][i]);
//		}

		return count;
	}
}
