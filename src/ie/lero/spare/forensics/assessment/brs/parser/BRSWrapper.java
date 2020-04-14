package ie.lero.spare.forensics.assessment.brs.parser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import it.uniud.mads.jlibbig.core.std.SignatureBuilder;
import it.uniud.mads.jlibbig.core.std.Control;
import it.uniud.mads.jlibbig.core.std.Signature;

public class BRSWrapper {

	// name, if any
	private String name;

	// type (BRS, PBRS, SBRS)
	private BigraphType type;

	// file path if any
	private String filePath;

	// controls defined
	// key is control name, value is the object representation
	private Map<String, Control> controls;

	// actions (or reactions) of the brs
	// key is react name, value is an ActionWrapper object
	private Map<String, ActionWrapper> actions;

	//signature for Bigraph object representation
	private Signature signature;
	
	public static final String BIGRAPH_FILE_EXTENSION = "big";

	public BRSWrapper() {
		controls = new HashMap<String, Control>();
		actions = new HashMap<String, ActionWrapper>();
	}

	public Map<String, ActionWrapper> getActions() {
		return actions;
	}

	public void setActions(Map<String, ActionWrapper> actions) {
		this.actions = actions;
	}

	public Map<String, Control> getControls() {
		return controls;
	}

	public void setControls(Map<String, Control> controls) {
		this.controls = controls;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigraphType getType() {
		return type;
	}

	public void setType(BigraphType type) {
		this.type = type;
	}

	public Signature getSignature() {
		
		if(signature == null) {
			createSignature();
		}
		
		return signature;
	}

	public void setSignature(Signature signature) {
		this.signature = signature;
	}

	public String getFilePath() {
		return filePath;
	}

	public Signature createSignature() {
	
		SignatureBuilder sigBldr = new SignatureBuilder();
		
		for(Control ctrl : controls.values()) {
			sigBldr.add(ctrl);
		}
		
		signature = sigBldr.makeSignature();
		
		return signature;
	
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;

		// set name to be the file name without the extension
		if (filePath.contains(File.separator)) {
			String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length());

			// remove .big
			if (fileName.endsWith(BIGRAPH_FILE_EXTENSION)) {
				fileName = fileName.replace("." + BIGRAPH_FILE_EXTENSION, "");
				setName(fileName);
			}
			
		} else {
			//figure out the separator
			String sep = "/";

			if (filePath.contains("\\")) {
				sep = "\\";
			}

			if(filePath.contains(sep)) {
				String fileName = filePath.substring(filePath.lastIndexOf(sep) + 1, filePath.length());

				// remove .big
				if (fileName.endsWith(BIGRAPH_FILE_EXTENSION)) {
					fileName = fileName.replace("." + BIGRAPH_FILE_EXTENSION, "");
					setName(fileName);
				}
			}
			
		}
	}

	public void addControl(Control ctrl) {

		
		controls.put(ctrl.getName(), ctrl);
	}

	public void addControl(String name, int arity) {
		// by defualt the control is active (i.e. can be part of reaction rules)
		addControl(name, true, arity);

	}

	public void addControl(String name, boolean isActive, int arity) {
		// by defualt the control is active (i.e. can be part of reaction rules)
		Control ctrl = new Control(name, isActive, arity);
		controls.put(name, ctrl);
	}

}
