package ie.lero.spare.pattern_instantiation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SystemsHandler {

	private static Map<Long, SystemInstanceHandler> systemHandlers = new HashMap<Long, SystemInstanceHandler>();
	private static SystemInstanceHandler currentSystemHandler;
	
	public static Map<Long, SystemInstanceHandler> getSystemHandlers() {
		return systemHandlers;
	}
	
	public synchronized static void addSystemHandler(SystemInstanceHandler sysHandler) {
		
		if(sysHandler == null) {
			return;
		}
		
		long id = createID(sysHandler);
		sysHandler.setSysID(id);
		
		systemHandlers.put(id, sysHandler);
		
		if(systemHandlers.size() == 1) {
			setCurrentSystemHandler(sysHandler);
		}
	}
	
	protected static long createID(SystemInstanceHandler sysHandler) {
		
		Random rand = new Random();
		
		long id = rand.nextLong();
		
		int tries = 1000;
		
		while(systemHandlers.containsKey(id) && tries > 0) {
			
			id = rand.nextLong();
			
			tries--;
		}
		
		return id;
	}
	
	public synchronized static void setCurrentSystemHandler(SystemInstanceHandler sysHandler) {
		currentSystemHandler = sysHandler;
	}
	
	public static SystemInstanceHandler getCurrentSystemHandler() {
		return currentSystemHandler;
	}
	
	public synchronized static SystemInstanceHandler getSystemHandler(long id) {
		return systemHandlers.get(id);
	}
	
	public synchronized static SystemInstanceHandler removeSystemHandler(long id) {
		
		return systemHandlers.remove(id);
	}
	
	public static void clearAll() {
		systemHandlers.clear();
		systemHandlers = null;
		currentSystemHandler = null;
	}
	
}
