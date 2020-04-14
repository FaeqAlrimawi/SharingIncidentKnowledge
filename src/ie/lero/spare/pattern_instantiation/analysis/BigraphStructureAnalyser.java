package ie.lero.spare.pattern_instantiation.analysis;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//import core.brs.parser.BigraphRelationType;
//import core.brs.parser.BigraphWrapper;
//import core.brs.parser.utilities.JSONTerms;

import cyberPhysical_Incident.CyberPhysicalIncidentFactory;
import cyberPhysical_Incident.Entity;
import ie.lero.spare.forensics.assessment.brs.parser.BigraphRelationType;
import ie.lero.spare.forensics.assessment.brs.parser.BigraphWrapper;
import ie.lero.spare.forensics.assessment.brs.parser.utilities.JSONTerms;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Control;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;

/**
 * The aim of this class is to identify common and different structure(s)
 * between given Bigraph objects
 * 
 * @author Faeq
 *
 */
public class BigraphStructureAnalyser {

	// defines how much matching should there be between given bigraphs ALL
	// matching, most, some, etc.
	private Commonality commonality;

	/**
	 * Finds the commonalities between the given BigraphWarppers
	 * 
	 * @param bigraphWrappers
	 *            List of bigraphWrapper objects to search for commonalities
	 *            within
	 * @param commonality
	 *            Level of commonality (ALL, Most, etc.)
	 * @return
	 */
	public List<BigraphStructure> findCommonalities(List<BigraphWrapper> bigraphWrappers, Commonality commonality) {

		if (bigraphWrappers == null || bigraphWrappers.isEmpty()) {
			return null;
		}

		CyberPhysicalIncidentFactory instance = CyberPhysicalIncidentFactory.eINSTANCE;

		BigraphWrapper bigWrapper1 = bigraphWrappers.get(0);
		BigraphWrapper bigWrapper2 = bigraphWrappers.get(1);

		Matcher matcher = new Matcher();

		// create a joint sig
		Signature jointSig = createJointSignature(bigWrapper1, bigWrapper2);

		Bigraph big2 = bigWrapper2.createBigraph(true, jointSig);

		System.out.println("Big2 bigraph::\n" + big2);

		BigraphWrapper bigTemp = new BigraphWrapper();

		// ====focus on whatever I can find... so commonality at the moment is
		// not important
		// one (could be slow way) is to create bigraphs from initial elements
		// which grow to be the largest common

		List<BigraphStructure> result = new LinkedList<BigraphStructure>();

		// ==start from root of one bigraph wrapper and start building
		// == building another bigraph starts by adding a root, then start
		// adding an entity contained in that root, then connectivity

		for (String root : bigWrapper1.getRoots()) {
			System.out.println("checking root: " + root);

			// get control of root
			String rootControl = bigWrapper1.getControl(root);

			// ignore any of the irrelevant terms (e.g., KeyWords)
			if (JSONTerms.BIG_IRRELEVANT_TERMS.contains(rootControl)) {
				continue;

			}
			// add root
			bigTemp.addRoot(root);

			//add to entities
//			bigTemp.addEntity(root);
			
//			Entity entRoot = instance.createEntity();
//
//			entRoot.setName(rootControl);

			// add entity and its control
//			bigTemp.addControl(entRoot, root);
			
			// get root entities
			List<String> rootEntities = bigWrapper1.getContainedEntitiesMap().get(root);

			if (rootEntities == null) {
				continue;
			}

			for (String entityName : rootEntities) {
				System.out.println("checking entity: " + entityName);
				// for each entity increment the wrapper by that entity
				String control = bigWrapper1.getControl(entityName);

				Entity ent = instance.createEntity();

				ent.setName(control);

				// add entity and its control
				bigTemp.addControl(ent, entityName);

				// add site
				boolean hasSite = bigWrapper1.getEntitySiteMap().get(entityName) != null ? true : false;
				bigTemp.addSite(entityName, hasSite);

				//add to root
				bigTemp.addContainedEntity(root, entityName);
				
				// do basic matching with the other bigwrapper to see if it
				// exists
				// no bigraph matching required just check if the entity control
				// exists in the other bigraphWrapper
				if (!bigWrapper2.hasControl(control)) {
					// create a bigraph structure
					BigraphStructure struct = new BigraphStructure();
					struct.setMainEntityName(control);
					struct.setStructureType(BigraphRelationType.ENTITY);
					result.add(struct);
					continue;
				}

				// if the other bigraphwrapper has the entity
				// then add other relations of that entity
				// decremently for containment
				List<String> entityContainedChildren1 = bigWrapper1.getContainedEntitiesMap().get(entityName);
				List<String> entityContainedChildren2 = bigWrapper2.getContainedEntitiesMap().get(entityName);
				List<String> entityContainedChildrenControls2 = new LinkedList<String>();
				
				//get controls of 2
				for(String entity2 : entityContainedChildren2) {
					String cntrl = bigWrapper2.getControl(entity2);
					
					if(cntrl!=null) {
						entityContainedChildrenControls2.add(cntrl);
					}
				}
				
				if (entityContainedChildren1 != null) {

					// add all contained entities in big1 [level-1]
					for (int i = 0; i > entityContainedChildren1.size(); i++) {
						// add child to entity in the bigraph temp
						String childEntityName = entityContainedChildren1.get(i);
						
						String childControl = bigWrapper1.getControl(childEntityName);

						//check if the control exists in the second
						
						Entity entChild = instance.createEntity();

						entChild.setName(childControl);

						// add entity and its control
						bigTemp.addControl(entChild, childEntityName);

						// add site
						boolean childHasSite = bigWrapper1.getEntitySiteMap().get(childEntityName);
						bigTemp.addSite(entityName, childHasSite);

					}

					//add all connections of entities in big1
					
					// check if matching
					Bigraph bigRedex = bigTemp.createBigraph(false, jointSig);

					System.out.println(bigRedex);
					
					if(matcher.match(big2, bigRedex).iterator().hasNext()) {
						System.out.println("partial big is matched to the big2");
					} else{
						System.out.println("partial big is NOT matched to the big2");
					}

				}

				// incremently for connectivity

			}
		}

		return result;
	}

	protected Signature createJointSignature(BigraphWrapper big1, BigraphWrapper big2) {

		SignatureBuilder bldr = new SignatureBuilder();

		Signature sig1 = big1.createBigraphSignature();
		Signature sig2 = big2.createBigraphSignature();

		Iterator<Control> it1 = sig1.iterator();
		Iterator<Control> it2 = sig2.iterator();

		while (it1.hasNext()) {

			Control ctrl = it1.next();

			if (!bldr.contains(ctrl.getName())) {
				bldr.add(ctrl);
			}
		}

		while (it2.hasNext()) {

			Control ctrl = it2.next();

			if (!bldr.contains(ctrl.getName())) {
				bldr.add(ctrl);
			}
		}

		return bldr.makeSignature();
	}

	
	public Commonality getCommonality() {
		return commonality;
	}

	public void setCommonality(Commonality commonality) {
		this.commonality = commonality;
	}

}
