package ie.lero.spare.pattern_instantiation.analysis;

public interface TraceMinerListener {
	
	//number of traces is read from files
	public void onNumberOfTracesRead(int numOfTraces);
	
	//a single trace is loaded from file
	public void onTracesLoaded(int numOfTracesLoaded);
	
	public void onLoadingJSONFile();

	
	public void onSavingFilteredTracesComplete(boolean isSuccessful);
}
