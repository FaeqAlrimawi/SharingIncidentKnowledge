package ie.lero.spare.franalyser.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntFunction;

import environment.EnvironmentDiagram;
import environment.Mobility;
import ie.lero.spare.pattern_instantiation.AssetMap;

public class CartesianIterator<T> implements Iterator<String[]> {

	private final String[][] sets;
	private final IntFunction<String[]> arrayConstructor;
	private int count = 0;
	private String[] next = null;
	private String previous = null;
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	private List<int[]> entitiesRules = AssetMap.rulesList;
	private EnvironmentDiagram systemModel;
	
	// used to refine the matching to the incident entities
	public static boolean isStrict = false;

	public CartesianIterator(String[][] sets, IntFunction<String[]> arrayConstructor) {
		Objects.requireNonNull(sets);
		Objects.requireNonNull(arrayConstructor);

		this.sets = copySets(sets);
		this.arrayConstructor = arrayConstructor;
	}

	public CartesianIterator(String[][] sets, IntFunction<String[]> arrayConstructor, EnvironmentDiagram sysModel) {
		Objects.requireNonNull(sets);
		Objects.requireNonNull(arrayConstructor);

		this.sets = copySets(sets);
		this.arrayConstructor = arrayConstructor;

		systemModel = sysModel;
	}

	private static <T> String[][] copySets(String[][] sets) {
		// If any of the arrays are empty, then the entire iterator is empty.
		// This prevents division by zero in `hasNext`.
		for (String[] set : sets) {
			if (set.length == 0) {
				return Arrays.copyOf(sets, 0);

			}
		}
		return sets.clone();
	}

	@Override
	public boolean hasNext() {
		if (next != null) {
			return true;
		}

		int tmp = count;
		String[] value = arrayConstructor.apply(sets.length);
		for (int i = 0; i < value.length; i++) {
			String[] set = sets[i];

			int radix = set.length;
			int index = tmp % radix;

			value[i] = set[index];

			tmp /= radix;
		}

		if (tmp != 0) {
			// Overflow.
			return false;
		}

		next = value;
		count++;

		return true;
	}

	@Override
	public String[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException();

		}

		String[] tmp = next;
		next = null;
		return tmp;
	}

	public LinkedList<LinkedList<String>> iterateElements() {

		int tmp = count;
		boolean isDuplicate = false;
		LinkedList<String> value;
		int num = calculateNumberOfElements();
		LinkedList<LinkedList<String>> results = new LinkedList<LinkedList<String>>();

		for (; count < num; count++) {
			isDuplicate = false;
			tmp = count;
			value = new LinkedList<String>();
			// T[] value = arrayConstructor.apply(sets.length);
			for (int i = 0; i < sets.length; i++) {// sets.length is number of
													// incident entities
				String[] set = sets[i];

				int radix = set.length;
				int index = tmp % radix;

				if (value.contains(set[index])) {
					isDuplicate = true;
					break;
				}
				value.add(set[index]);
				tmp /= radix;
			}

			if (!isDuplicate) {

				if (isStrict) {
					if (checkSetBasedOnEntities(value)) {
						results.add(value);
					}
				} else {
					results.add(value);
				}

			}
		}

		return results;
	}

	private int calculateNumberOfElements() {

		int num = 1;

		for (int i = 0; i < sets.length; i++) {
			num *= sets[i].length;
		}

		return num;
		// return (int) Math.pow(sets[0].length, sets.length);
	}

	protected boolean checkSetBasedOnEntities(LinkedList<String> set) {

		List<int[]> assetsRules = createIncidentEntitiesRules(set);

		for (int i = 0; i < entitiesRules.size(); i++) {

			for (int j = 0; j < entitiesRules.get(i).length; j++) {
				if (entitiesRules.get(i)[j] == 1 && assetsRules.get(i)[j] == 0) {
					return false;
				}
			}
		}

		return true;
	}

	protected void checkSetBasedOnSystem(LinkedList<String> set) {

		// TBD
	}

	protected List<int[]> createIncidentEntitiesRules(List<String> assetSet) {

//		EnvironmentDiagram systemModel = ModelsHandler.getCurrentSystemModel();

		List<environment.Asset> assets = new LinkedList<environment.Asset>();
		int rulesNum = AssetMap.rulesNum;

		for (String assetName : assetSet) {
			assets.add(systemModel.getAsset(assetName));
		}

		// rules for each asset are 4 currently: isSame? 0 or 1,
		// hasConnectivity? 0 or 1, isParent? 0 or 1, isChild? 0 or 1
		// the size of the rules array = rulesNum*neighbourhood*size of
		// generalAsset array (i.e. number of general assets)
		int numOfEntities = assets.size();

		environment.Asset src, des;

		List<int[]> rulesList = new LinkedList<int[]>();

		for (int i = 0; i < numOfEntities - 1; i++) {
			rulesList.add(new int[(numOfEntities - 1 - i) * rulesNum]);
		}

		int index = 0;

		// determine properties
		for (int i = 0; i < rulesList.size(); i++) {
			src = assets.get(i);
			index = i + 1;
			int[] tmpAry = rulesList.get(i);
			for (int j = 0; j < tmpAry.length;) {
				des = assets.get(index);

				// [1] isConnected
				for (environment.Connection con : src.getConnections()) {

					environment.Asset ast1 = con.getAsset1();
					environment.Asset ast2 = con.getAsset1();

					if ((ast1 != null && ast1.getName().equals(des.getName()))
							|| (ast2 != null && ast2.getName().equals(des.getName()))) {
						tmpAry[j] = 1;// con.getType().ordinal();
						break;
					}
				}

				if (tmpAry[j] != 1) {
					tmpAry[j] = 0;
				}

				// next property
				j++;

				// [2] is the destination parent of source
				environment.Asset srcParent = src.getParentAsset();

				if (srcParent != null && srcParent.getName().equals(des.getName())) {
					tmpAry[j] = 1;
				} else {
					tmpAry[j] = 0;
				}

				// next property
				j++;

				// [3] is destination a child in source
				if (src.getContainedAssets().contains(des)) {
					tmpAry[j] = 1;
				} else {
					tmpAry[j] = 0;
				}

				index++;

				// next property
				j++;
			}
		}

		return rulesList;
	}

	public static void main(String[] args) {

		// represents number of system assets that match each incident asset
		// assuming
		int rows = 4;
		// represents number of incident assets
		int columns = 3;
		// String [] a = {"a", "b", "c"};
		// System.out.println(Arrays.toString(a));
		String[][] tst = new String[rows][columns];
		int cnt = 0;
		// generate dummy array assuming they are all unique
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				tst[i][j] = "" + j;// +""+j;//cnt;//dummy[rand.nextInt(dummy.length)];
				cnt++;
			}
		}

		for (String[] a : tst) {
			System.out.println(Arrays.toString(a));
		}
		CartesianIterator<String> car = new CartesianIterator<String>(tst, String[]::new);

		System.out.println("Testing [The generation of unqiue sequences WITHOUT threads] using a " + rows + "" + "*"
				+ columns + "\nstatring time [" + dtf.format(LocalDateTime.now()) + "]");

		LinkedList<LinkedList<String>> res = car.iterateElements();

		for (LinkedList<String> lst : res) {
			System.out.println(lst);
		}
		System.out.println("End time [" + dtf.format(LocalDateTime.now()) + "]");
		System.out.println(res.size());
	}
}