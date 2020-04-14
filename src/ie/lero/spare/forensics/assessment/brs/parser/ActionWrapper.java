package ie.lero.spare.forensics.assessment.brs.parser;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cyberPhysical_Incident.Entity;
import it.uniud.mads.jlibbig.core.std.Signature;

public class ActionWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5906901715923235705L;

	private String actionName;

	private String reactName;

	private BigraphWrapper precondition;

	private BigraphWrapper postcondition;

	// list of entities and their occurrence in the action PRE
	private List<Map.Entry<String, Long>> preEntities;

	// list of entities and their occurrence in the action POST
	private List<Map.Entry<String, Long>> postEntities;

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public BigraphWrapper getPrecondition() {
		return precondition;
	}

	public void setPrecondition(BigraphWrapper precondition) {
		this.precondition = precondition;
	}

	public BigraphWrapper getPostcondition() {
		return postcondition;
	}

	public void setPostcondition(BigraphWrapper postcondition) {
		this.postcondition = postcondition;
	}

	public String getReactName() {
		return reactName;
	}

	public void setReactName(String reactName) {
		this.reactName = reactName;
	}

	public List<Map.Entry<String, Long>> getPreEntities() {

		return preEntities;
	}

	/**
	 * Finds all entities in the action with their occurrences(count) with the
	 * action
	 * 
	 * @param excluding
	 * @param topK
	 * @param isAscending
	 * @return
	 */
	public List<Map.Entry<String, Long>> findAllEntities(List<String> excluding, int topK, boolean isAscending) {

		List<Map.Entry<String, Long>> allEntities = new LinkedList<Map.Entry<String, Long>>();

		allEntities.addAll(findPreEntities(excluding, topK, isAscending));
		allEntities.addAll(findPostEntities(excluding, topK, isAscending));

		return allEntities;

	}

	/**
	 * Finds all entities in the precondition and their count. It can exclude
	 * entities specified in the excluding list. It can retun the topK entities
	 * 
	 * @param excluding
	 * @param topK
	 * @return
	 */
	public List<Map.Entry<String, Long>> findPreEntities(List<String> excluding, int topK, boolean isAscending) {

		if (preEntities == null) {
			preEntities = new LinkedList<Map.Entry<String, Long>>();
		} else {
			preEntities.clear();
		}

		List<String> entities = new LinkedList<String>();

		if (precondition != null) {
			Set<Entity> ents = precondition.getControlMap().keySet();

			for (Entity ent : ents) {

				String name = ent.getName();

				// if it is a term to exclude, then continue
				if (excluding != null && excluding.contains(name)) {
					continue;
				}

				// if it is a reaction name that is used to identify the
				// action (i.e. an extra)
				if (name.equalsIgnoreCase(actionName)) {
					continue;
				}

				entities.add(name);
			}
		}

		Map<String, Long> map = entities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		if (topK > 0) {
			if (isAscending) {
				preEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
						.limit(topK).collect(Collectors.toList());
			} else {
				preEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.limit(topK).collect(Collectors.toList());
			}

		} else {

			if (isAscending) {
				preEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
						.collect(Collectors.toList());
			} else {
				preEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.collect(Collectors.toList());
			}
		}

		return preEntities;

	}

	public List<Map.Entry<String, Long>> getPostEntities() {

		return postEntities;
	}

	/**
	 * Finds all entities in the postcondition and their count. It can exclude
	 * entities specified in the excluding list. It can retun the topK entities
	 * 
	 * @param excluding
	 * @param topK
	 * @return
	 */
	public List<Map.Entry<String, Long>> findPostEntities(List<String> excluding, int topK, boolean isAscending) {

		if (postEntities == null) {
			postEntities = new LinkedList<Map.Entry<String, Long>>();
		} else {
			postEntities.clear();
		}

		List<String> entities = new LinkedList<String>();

		if (postcondition != null) {
			Set<Entity> ents = postcondition.getControlMap().keySet();

			for (Entity ent : ents) {

				String name = ent.getName();

				// if it is a term to exclude, then continue
				if (excluding != null && excluding.contains(name)) {
					continue;
				}

				// if it is a reaction name that is used to identify the
				// action (i.e. an extra)
				if (name.equalsIgnoreCase(actionName)) {
					continue;
				}

				entities.add(name);
			}
		}

		Map<String, Long> map = entities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		if (topK > 0) {
			if (isAscending) {
				postEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
						.limit(topK).collect(Collectors.toList());
			} else {
				postEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.limit(topK).collect(Collectors.toList());
			}

		} else {

			if (isAscending) {
				postEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
						.collect(Collectors.toList());
			} else {
				postEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.collect(Collectors.toList());
			}
		}

		return postEntities;

	}

	public Map<String, List<ActionChangeHolder>> findChanges() {

		Map<String, List<ActionChangeHolder>> changes = new HashMap<String, List<ActionChangeHolder>>();

		if (precondition == null || postcondition == null) {
			return changes;
		}

		List<String> allEntities = new LinkedList<String>();
		List<String> addedEntities = new LinkedList<String>();

		/// ==== NEEDS FIXING.... entities used are modified (numbered)
		// == need to track entities from pre to post
		//== not easy
		
		// add all pre entities
		// precondition.getControl(entityName)
		allEntities.addAll(precondition.getEntities());

		// add all post entities which are not added
		for (String postEntity : postcondition.getEntities()) {
			if (!allEntities.contains(postEntity)) {
				allEntities.add(postEntity);
				addedEntities.add(postEntity);
			}
		}

		// for each entity in the precondition find what changes (containment or
		// connectivity) it had in the post
		for (String entity : allEntities) {
			List<ActionChangeHolder> changesInContainment = findContainmentChanges(entity);

			if (changes.containsKey(entity)) {
				changes.get(entity).addAll(changesInContainment);
			} else {
				changes.put(entity, changesInContainment);
			}
		}

		return changes;
	}

	public List<ActionChangeHolder> findContainmentChanges(String entity) {
		// finds containment changes for the given entity
		// looks in the pre and post for changes of contained entities for that
		// entity

		List<ActionChangeHolder> containmentChanges = new LinkedList<ActionChangeHolder>();

		if (precondition == null || postcondition == null) {
			return containmentChanges;
		}

		List<String> preContainedEntities = precondition.getContainedEntitiesMap().get(entity);
		List<String> postContainedEntities = postcondition.getContainedEntitiesMap().get(entity);

		// if no change
		if (preContainedEntities == null || preContainedEntities.isEmpty()) {
			if (postContainedEntities == null || postContainedEntities.isEmpty()) {
				return containmentChanges;
			}
		}

		// if the entity didn't have any in the pre then in the post they are
		// added
		if (preContainedEntities == null || preContainedEntities.isEmpty()) {
			if (postContainedEntities != null && !postContainedEntities.isEmpty()) {
				ActionChangeHolder addedContainmentChange = new ActionChangeHolder();

				// set change to containment
				addedContainmentChange.setChangeType(BigraphRelationType.CONTAINMENT);

				// set operation to add
				addedContainmentChange.setChangeOperation(BigraphChangeOperation.ADD);

				// set changed entities to post contained entities
				addedContainmentChange.setChangedEntities(new LinkedList<String>(postContainedEntities));

				containmentChanges.add(addedContainmentChange);

				return containmentChanges;
			}
		}

		// if the entity did have any in the pre then in the post they are
		// removed
		if (postContainedEntities == null || postContainedEntities.isEmpty()) {
			if (preContainedEntities != null && !preContainedEntities.isEmpty()) {
				ActionChangeHolder removedContainmentChange = new ActionChangeHolder();

				// set change to containment
				removedContainmentChange.setChangeType(BigraphRelationType.CONTAINMENT);

				// set operation to add
				removedContainmentChange.setChangeOperation(BigraphChangeOperation.REMOVE);

				// set changed entities to post contained entities
				removedContainmentChange.setChangedEntities(new LinkedList<String>(preContainedEntities));

				containmentChanges.add(removedContainmentChange);

				return containmentChanges;
			}
		}

		// if both pre and post have contained entities, then check each

		// check pre
		for (String preEntity : preContainedEntities) {

			// if not contained in the post then there's a remove
			if (!postContainedEntities.contains(preEntity)) {
				ActionChangeHolder remove = new ActionChangeHolder();

				remove.setChangeType(BigraphRelationType.CONTAINMENT);
				remove.setChangeOperation(BigraphChangeOperation.REMOVE);
				List<String> change = new LinkedList<String>();
				change.add(preEntity);
				remove.setChangedEntities(change);

				containmentChanges.add(remove);
			}
		}

		// check post
		for (String postEntity : postContainedEntities) {

			// if not contained in the post then there's a remove
			if (!preContainedEntities.contains(postEntity)) {
				ActionChangeHolder addition = new ActionChangeHolder();

				addition.setChangeType(BigraphRelationType.CONTAINMENT);
				addition.setChangeOperation(BigraphChangeOperation.ADD);
				List<String> change = new LinkedList<String>();
				change.add(postEntity);
				addition.setChangedEntities(change);

				containmentChanges.add(addition);
			}
		}

		return containmentChanges;
	}
}
