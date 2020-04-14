package ie.lero.spare.utility;

import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import cyberPhysical_Incident.CyberPhysicalIncidentPackage;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.impl.IncidentDiagramImpl;

public class IncidentModelHandler {
	
	private static final String EXTENSION = "cpi";
	private static IncidentDiagram incidentModel;
	private static String filePath;
	
	/**
	 * Load an incident model from the given file name
	 * @param fileName the XMI file of the incident model
	 * @return an IncidentDigram object containing the model information
	 */
	public static IncidentDiagram loadIncidentFromFile(String fileName) {
	
	IncidentDiagram incidentDiagram = null;
	
	// generate EPackages from schemas	
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				    "ecore", new EcoreResourceFactoryImpl());

				ResourceSet rs = new ResourceSetImpl();
				// enable extended metadata
				final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
				rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,
				    extendedMetaData);

				EPackage.Registry.INSTANCE.put(CyberPhysicalIncidentPackage.eNS_URI, CyberPhysicalIncidentPackage.eINSTANCE);
				
				Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(EXTENSION, new XMIResourceFactoryImpl() {
			    	
			    	public Resource createResource(URI uri) {
			    		
			    		XMIResource xmiResource = new XMIResourceImpl(uri);
			 
			    		return xmiResource;			
			    	}
			    });
	
		try {
			
			Resource r = rs.getResource(URI.createFileURI(fileName), true);
				
			EObject eObject = r.getContents().get(0);
			
			if (eObject instanceof EPackage) {
				  EPackage p = (EPackage)eObject;
				  rs.getPackageRegistry().put(p.getNsURI(), p);
			}

			if(IncidentDiagram.class.isInstance(eObject)) {
				incidentDiagram = (IncidentDiagramImpl) eObject;	
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return incidentDiagram;
		
	}
	
	/**
	 * Save an incident model (given by the IncidentDiagram object) to the given file name
	 * @param incidentDiagram the object holding the incident model information
	 * @param fileName the target file to save the model to
	 * @return True if saving is successful. False otherwise.
	 */
	public static boolean SaveIncidentToFile(IncidentDiagram incidentDiagram, String fileName) {
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
			    "ecore", new EcoreResourceFactoryImpl());

			ResourceSet rs = new ResourceSetImpl();
			// enable extended metadata
			final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
			rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,
			    extendedMetaData);

			EPackage.Registry.INSTANCE.put(CyberPhysicalIncidentPackage.eNS_URI, CyberPhysicalIncidentPackage.eINSTANCE);
			
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(EXTENSION, new XMIResourceFactoryImpl() {
		    	
		    	public Resource createResource(URI uri) {
		    		
		    		XMIResource xmiResource = new XMIResourceImpl(uri);
		 
		    		return xmiResource;			
		    	}
		    });
			
			try {
				Resource r = rs.createResource(URI.createFileURI(fileName));
					
				r.getContents().add(incidentDiagram);
				
				r.save(Collections.EMPTY_MAP);
				
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
			
	}
	
	public static void main(String[]args) {
	
		IncidentDiagram incidentDiagram = loadIncidentFromFile("etc/example/interruption_incident-pattern.cpi");
		
		if (incidentDiagram != null) {
			System.out.println(incidentDiagram.getActivity().get(0).getName());
			
			
			SaveIncidentToFile(incidentDiagram, "etc/example/inc.cpi");
		}
		
		//re-reading saved model
		IncidentDiagram inci = loadIncidentFromFile("etc/example/inc.cpi");
		
		System.out.println(inci.getActivity().get(0).getName());
	}

	public static IncidentDiagram getIncidentModel() {
		
		if(incidentModel == null) {
		loadIncidentFromFile(filePath);	
		}
		
		return incidentModel;
	}

	public static IncidentDiagram getIncidentModel(String filePath) {
		
		setFilePath(filePath);
		
		return getIncidentModel();
	}

	public static void setIncidentModel(IncidentDiagram incidentModel) {
		IncidentModelHandler.incidentModel = incidentModel;
	}

	public static String getFilePath() {
		return filePath;
	}

	public static void setFilePath(String filePath) {
		IncidentModelHandler.filePath = filePath;
	}
	
	
}
