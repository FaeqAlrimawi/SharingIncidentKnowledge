package ie.lero.spare.pattern_instantiation;

import java.util.LinkedList;
import java.util.List;

public interface IncidentPatternInstantiationListener {
	
	public void updateProgress(int progress);
	public void updateLogger(String msg);
	public void updateAssetMapInfo(String msg);
	public void updateAssetSetInfo(LinkedList<String[]> assetSets);
	public void updateResult(int setID, GraphPathsAnalyser graphAnalyser, String outputFile, String timeConsumed);

}
