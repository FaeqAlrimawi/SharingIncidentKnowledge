package ie.lero.spare.pattern_instantiation.analysis.utilities;

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
import environment.CyberPhysicalSystemPackage;
import environment.EnvironmentDiagram;
import environment.impl.EnvironmentDiagramImpl;

public class SystemModelHandler {
	
	private static final String EXTENSION = "cps";
	private static final EPackage MODEL_PACKAGE_EINSTANCE = CyberPhysicalSystemPackage.eINSTANCE;
	private static final String MODEL_PACKAGE_ENS_URI = CyberPhysicalSystemPackage.eNS_URI;
	
	/**
	 * Load a system model from the given file name
	 * @param fileName the XMI file of the system model
	 * @return an EnvironmentDiagram object containing the model information
	 */
	public static EnvironmentDiagram loadSystemFromFile(String fileName) {
	
		EnvironmentDiagram environmentDiagram = null;
	
	// generate EPackages from schemas	
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				    "ecore", new EcoreResourceFactoryImpl());

				ResourceSet rs = new ResourceSetImpl();
				// enable extended metadata
				final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
				rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,
				    extendedMetaData);

				EPackage.Registry.INSTANCE.put(MODEL_PACKAGE_ENS_URI, MODEL_PACKAGE_EINSTANCE);
				
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

			environmentDiagram = (EnvironmentDiagramImpl) eObject;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return environmentDiagram;
		
	}
	
	/**
	 * Save an incident model (given by the IncidentDiagram object) to the given file name
	 * @param incidentDiagram the object holding the incident model information
	 * @param fileName the target file to save the model to
	 * @return True if saving is successful. False otherwise.
	 */
	public static boolean SaveSystemToFile(EnvironmentDiagram environmentDiagram, String fileName) {
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
			    "ecore", new EcoreResourceFactoryImpl());

			ResourceSet rs = new ResourceSetImpl();
			// enable extended metadata
			final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
			rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,
			    extendedMetaData);

			EPackage.Registry.INSTANCE.put(MODEL_PACKAGE_ENS_URI, MODEL_PACKAGE_EINSTANCE);
			
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(EXTENSION, new XMIResourceFactoryImpl() {
		    	
		    	public Resource createResource(URI uri) {
		    		
		    		XMIResource xmiResource = new XMIResourceImpl(uri);
		 
		    		return xmiResource;			
		    	}
		    });
			
			try {
				Resource r = rs.createResource(URI.createFileURI(fileName));
					
				r.getContents().add(environmentDiagram);
				
				r.save(Collections.EMPTY_MAP);
				
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
			
	}
	
/*	public static void main(String[]args) {
	
		EnvironmentDiagram environmentDiagram = loadSystemFromFile("etc/example/research_centre_model.cps");
		
		if (environmentDiagram != null) {
			System.out.println(environmentDiagram);

		}
		
		SaveSystemToFile(environmentDiagram, "etc/example/sys.cps");
		
		//re-reading saved model
		EnvironmentDiagram inci = loadSystemFromFile("etc/example/sys.cps");
		
		System.out.println(inci);
	}*/
	
}
