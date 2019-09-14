package ie.lero.spare.pattern_extraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

public class TestChoco {

	public static void main(String[] args) {

		// example1();
		// ex2();
		// patternBasedExample();
		// test2();

		System.out.println("=======================================");
		Map<Integer, List<int[]>> maps = new HashMap<Integer, List<int[]>>();

		int[][] allPossiblePatternsMapsInt = new int[5][];

		allPossiblePatternsMapsInt[0] = new int[] { 1, 2, 3,4,5,6 }; // sequence should
															// be ordered in //
															// // ascending
															// order
		allPossiblePatternsMapsInt[1] = new int[] { 3, 4,5,6 };
		allPossiblePatternsMapsInt[2] = new int[] { 6, 8,9 };
		allPossiblePatternsMapsInt[3] = new int[] { 6,7,8, 9 };
		allPossiblePatternsMapsInt[4] = new int[] { 9,10, 12, 13, 14 };

		int numOfPatterns = 2;
		LinkedList<int[]> pattern_1_maps = new LinkedList<int[]>();
		pattern_1_maps.add(allPossiblePatternsMapsInt[0]);
		pattern_1_maps.add(allPossiblePatternsMapsInt[1]);

		LinkedList<int[]> pattern_2_maps = new LinkedList<int[]>();
		pattern_2_maps.add(allPossiblePatternsMapsInt[2]);
		pattern_2_maps.add(allPossiblePatternsMapsInt[3]);
		pattern_2_maps.add(allPossiblePatternsMapsInt[4]);

		maps.put(0, pattern_1_maps);
		maps.put(1, pattern_2_maps);

		List<Solution> solutions = findSolutions4(maps, 20);

		int cnt = 0;

		for (Solution sol : solutions) {
			System.out.println(cnt + ": " + sol);
			cnt++;

			if (cnt == 100) {
				break;
			}
		}

		System.out.println(solutions.size());

	}

	protected static void example1() {
		int N = 100;
		// 1. Modelling part
		Model model = new Model("all-interval series of size " + N);
		// 1.a declare the variables
		IntVar[] S = model.intVarArray("s", N, 0, N - 1, false);
		IntVar[] V = model.intVarArray("V", N - 1, 1, N - 1, false);
		// 1.b post the constraints
		for (int i = 0; i < N - 1; i++) {
			model.distance(S[i + 1], S[i], "=", V[i]).post();
		}
		model.allDifferent(S).post();
		model.allDifferent(V).post();

		S[1].gt(S[0]).post();
		V[1].gt(V[N - 2]).post();

		// 2. Solving part
		Solver solver = model.getSolver();
		// 2.a define a search strategy
		solver.setSearch(Search.minDomLBSearch(S));
		if (solver.solve()) {
			System.out.printf("All interval series of size %d%n", N);
			for (int i = 0; i < N - 1; i++) {
				System.out.printf("%d <%d> ", S[i].getValue(), V[i].getValue());
			}
			System.out.printf("%d", S[N - 1].getValue());
		}

	}

	static void ex2() {

		Model mod = new Model("Ex2 model");

		IntVar num1 = mod.intVar("num1", 0, 10, false);
		IntVar num2 = mod.intVar("num2", 5, 10, false);
		IntVar num3 = mod.intVar("num3", 11, 20, false);
		IntVar[] nms = mod.intVarArray(5, new int[] { 1, 2, 3, 4, 5 });

		// my own constraint
		// Constraint testConstraint = new Constraint("TestConstraint", new
		// TestPropagator(nms, 5));

		mod.arithm(num1, "<", num2).post();
		mod.arithm(num1, "=", num3).post();

		Solver solver = mod.getSolver();
		int cnt = 100;
		int i = 1;

		List<Solution> solutions = solver.findAllSolutions();

		System.out.println(solutions.get(0));

		/*
		 * while(solver.solve() && cnt > 0) { System.out.println(i+": "+num1 +
		 * " " + num2); // System.out.println(num2); cnt--;i++; }
		 */
		System.out.println(solver.getSolutionCount());
	}

	protected static void patternBasedExample() {

		/*
		 * //pattern 0 maps (3 maps) pattern1MapsArray[0][0] = new int[]{1,2};
		 * pattern1MapsArray[0][1] = new int[]{1,2,3}; pattern1MapsArray[0][2] =
		 * new int[]{1,2,3,4};
		 * 
		 * //pattern 1 maps (2 maps pattern1MapsArray[1][0] = new int[]{3,5};
		 * pattern1MapsArray[1][1] = new int[]{5,7};
		 */
		// IntVar [] patternMaps = new IntVar[actualNumberOfPatterns];

		// variables for pattern maps
		/*
		 * pattern1Maps[0] = model.setVar("ptr1-1", 1,2); pattern1Maps[1] =
		 * model.setVar("ptr1-2", 1,3); pattern1Maps[2] = model.setVar("ptr1-3",
		 * 2,5);
		 */
		/*
		 * int patternIndex = 0; int cntMap = 0;
		 */
		// variables for all patterns
		// patternMaps[cntMap] = model.intVar("ptr0", patternIndex,
		// patternIndex+numOfpattern0Maps-1,false);

		// int numOfpattern1Maps = 1;
		// int numOfpattern0Maps = 2;
		// SetVar[] patterns = new SetVar[actualNumberOfPatterns];

		// SetVar[] pattern1Maps = new SetVar[numOfpattern1Maps];

		// int[][][] pattern1MapsArray = new
		// int[numOfpatterns][numOfpattern1Maps][];

		// int numOfpatterns = 3;
		/*
		 * patterns[patternIndex] = model.intVar("ptr0-0", 0,
		 * numOfPossiblePatterns-1); patternIndex++; patterns[patternIndex] =
		 * model.intVar("ptr0-1", 0, numOfPossiblePatterns-1);
		 * 
		 * patternIndex++; cntMap++;
		 * 
		 * patternMaps[cntMap] = model.intVar("ptr1", patternIndex,
		 * patternIndex+numOfpattern1Maps-1,false);
		 * 
		 * patterns[patternIndex] = model.setVar("ptr1-1", 4,5);
		 */

		// patterns[0] = model.setVar("ptr1-1", 1, 2); // actions 1, 2, 3
		// patterns[1] = model.setVar("ptr1-2", 1, 2, 3); // actions 1, 2, 3
		//
		// patterns[2] = model.setVar("ptr2-1", 4, 5); // actions 3, 4, 5
		// patterns[3] = model.setVar("ptr2-2", 3, 4, 5); // actions 3, 4, 5
		//
		// patterns[4] = model.setVar("ptr3-1", 6, 7); // assuming it maps to
		// actions 7, 8

		// define constraints
		// 1st constraint: all patterns should be in a solution
		// 2nd constraint: no overlapping between patterns

		// model.or(model.element(model.intVar(0), patterns, patterns[0]),
		// model.element(model.intVar(1), patterns, patterns[1])).post();

		// patterns[0].

		/*
		 * for(int i = 0;i<numOfpatterns-1;i++) { //Constraint samePattern =
		 * model. model.disjoint(patterns[i], patterns[i+1]).post(); }
		 */

		Model model = new Model("Pattern model");

		int actualNumberOfPatterns = 2;// numer of patterns matched
		int numOfPossiblePatterns = 5; // all found matches of patterns
		IntVar[] patterns = new IntVar[actualNumberOfPatterns];

		// represents which possible patterns are mapped to the same pattern
		// (i.e. same number same pattern)
		/*
		 * IntVar[] possiblePatternsAssociation = new
		 * IntVar[numOfPossiblePatterns];
		 * 
		 * possiblePatternsAssociation[0] = model.intVar(0); //pattern 0
		 * possiblePatternsAssociation[1] = model.intVar(0); //pattern 0
		 * possiblePatternsAssociation[2] = model.intVar(1); //pattern 1
		 * possiblePatternsAssociation[3] = model.intVar(1); //pattern 1
		 * possiblePatternsAssociation[4] = model.intVar(1); //pattern 1
		 */
		// =========Variables=====================

		// represents the actions that map to a pattern. Actions are represented
		// as numbers in the sequence
		/*
		 * SetVar[] possiblePatternsSets = new SetVar[numOfPossiblePatterns];
		 * 
		 * possiblePatternsSets[0] = model.setVar("ptr0",1,2); //pattern 0
		 * possiblePatternsSets[1] = model.setVar("ptr1",2,3); //pattern 0
		 * possiblePatternsSets[2] = model.setVar("ptr2",80,100); //pattern 1
		 * possiblePatternsSets[3] = model.setVar("ptr3",4,6); //pattern 1
		 * possiblePatternsSets[4] = model.setVar("ptr4",7,8); //pattern 1
		 */
		// int[] tst = new int[] {1,2};
		// model.setVar(tst);
		// create pattern variables
		for (int i = 0; i < patterns.length; i++) {
			patterns[i] = model.intVar("pattern-" + i, 0, numOfPossiblePatterns - 1);
		}

		// =========Constraints=====================
		// each pattern should be different
		model.allDifferent(patterns).post();
		// Constraint myCons = new Constraint("Cons1", new
		// TestPropagator(patterns,4));
		//
		// model.and(myCons).post();

		// no overlapping
		for (int i = 0; i < actualNumberOfPatterns - 1; i++) {

			// for(int j=i+1; j<actualNumberOfPatterns;j++) {
			// Constraint con = new Constraint("Cons1", new
			// TestPropagator(patterns[i], patterns[j],4));
			// model.and(con).post();
			// }
		}
		//// model.allDisjoint(pattern1Maps).post(); //no overlap constraint
		// //model.addClauses(LogOp.or(null));

		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();

		for (Solution so : solutions) {
			System.out.println(so);
		}

		System.out.println(solutions.size());

	}

	private static void test2() {

		int actualNumberOfPatterns = 2;// numer of patterns matched
		int numOfAllMaps = 5; // all found matches of patterns
		int numOfActions = 20; // could be defined as the max number of the maps

		int[] actionsArray = new int[numOfActions];

		int[][] allPossiblePatternsMapsInt = new int[numOfAllMaps][];

		allPossiblePatternsMapsInt[0] = new int[] { 4, 6 };
		allPossiblePatternsMapsInt[1] = new int[] { 4, 5 };
		allPossiblePatternsMapsInt[2] = new int[] { 5, 6 };
		allPossiblePatternsMapsInt[3] = new int[] { 8, 10 };
		allPossiblePatternsMapsInt[4] = new int[] { 15, 17 };

		// indicates how many maps for each pattern
		int[] numOfMapsPerPattern = new int[actualNumberOfPatterns];
		numOfMapsPerPattern[0] = 2;
		numOfMapsPerPattern[1] = 3;

		// ===Derived variables
		// represents pattern maps found from matching all patterns to an
		// incident model
		int[][][] possiblePatternsMapsInt = new int[actualNumberOfPatterns][][];

		int index = 0;
		for (int i = 0; i < possiblePatternsMapsInt.length; i++) {
			possiblePatternsMapsInt[i] = new int[numOfMapsPerPattern[i]][];
			for (int j = 0; j < possiblePatternsMapsInt[i].length; j++) {
				possiblePatternsMapsInt[i][j] = allPossiblePatternsMapsInt[index];
				index++;
			}
		}
		/*
		 * possiblePatternsMapsInt[0][0] = allPossiblePatternsMapsInt[0];
		 * possiblePatternsMapsInt[0][1] = allPossiblePatternsMapsInt[1];
		 * 
		 * possiblePatternsMapsInt[1][0] = allPossiblePatternsMapsInt[2];
		 * possiblePatternsMapsInt[1][1] = allPossiblePatternsMapsInt[3];
		 * possiblePatternsMapsInt[1][2] = allPossiblePatternsMapsInt[4];
		 */

		// represents which possible patterns are mapped to the same pattern
		// (i.e. same number same pattern)
		// int [] possiblePatternsAssociationInt = new int[]{0,0,1,1,1};///also
		// depends on num of maps

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		// ============Creating model========================//

		Model model = new Model("Pattern-Map Model");

		// ============Defining Variables======================//

		SetVar[] patterns = new SetVar[actualNumberOfPatterns];
		// IntVar[] possiblePatternsAssociation = new
		// IntVar[possiblePatternsMapsInt.length];

		// variables which determines which maps belong to the same pattern
		// for(int i=0;i<possiblePatternsMapsInt.length;i++) {
		// possiblePatternsAssociation[i] =
		// model.intVar("associ"+i,possiblePatternsAssociationInt[i]);
		// }

		SetVar[][] possiblePatternsMaps = new SetVar[possiblePatternsMapsInt.length][];

		// each pattern has as domain values the range from {} to
		// {0,1,2,..,N-1}, where N is number of actions
		for (int i = 0; i < patterns.length; i++) {
			patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
		}

		for (int i = 0; i < patterns.length; i++) {
			// variables which represent the sets that a generated set by a
			// pattern should belong to
			possiblePatternsMaps[i] = new SetVar[possiblePatternsMapsInt[i].length];
			for (int j = 0; j < possiblePatternsMapsInt[i].length; j++) {
				possiblePatternsMaps[i][j] = model.setVar("map" + i + "" + j, possiblePatternsMapsInt[i][j]);
			}
		}

		// ============Defining Constraints======================//
		// ===1-No overlapping between maps
		// ===2-A map should be one of the defined maps by the variable
		// possiblePatternMaps
		// ===3-at least 1 map for each pattern
		// 1-no overlapping
		model.allDisjoint(patterns).post();

		for (int i = 0; i < patterns.length; i++) {
			// 2 & 3- a map should belong to one of the identified maps and each
			// pattern should have
			// a map in the solution
			model.member(possiblePatternsMaps[i], patterns[i]).post();

		}

		// ============Finding solutions======================//
		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();

		int cnt = 0;

		for (Solution so : solutions) {
			System.out.println(cnt + ":" + so);
			cnt++;

			if (cnt == 100) {
				break;
			}
		}

		System.out.println(solutions.size());
	}

	public static List<Solution> findSolutions(Map<Integer, List<int[]>> patternMaps, int numberOfActions) {

		// numberOfActions could be defined as the max number of the maps

		int[] actionsArray = new int[numberOfActions];

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		// ============Creating model========================//

		Model model = new Model("Pattern-Map Model");

		// ============Defining Variables======================//

		int numOfAllMaps = 0;
		int numOfPatterns = patternMaps.keySet().size();
		for (List<int[]> list : patternMaps.values()) {
			numOfAllMaps += list.size();
		}

		SetVar[] patterns = new SetVar[numOfPatterns];
		SetVar[] extraPatterns = new SetVar[numOfAllMaps - numOfPatterns];

		SetVar[][] possiblePatternsMaps = new SetVar[patterns.length][];

		// each pattern has as domain values the range from {} to
		// {0,1,2,..,N-1}, where N is number of actions
		for (int i = 0; i < patterns.length; i++) {
			patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
		}

		// create extra patterns (which can be satisfied or not)
		for (int i = 0; i < extraPatterns.length; i++) {
			extraPatterns[i] = model.setVar("ExtraPattern-" + (numOfPatterns + i), new int[] {}, actionsArray);
		}

		// All maps
		List<SetVar> allMaps = new LinkedList<SetVar>();

		for (int i = 0; i < patterns.length; i++) {

			// variables which represent the sets that a generated set by a
			// pattern should belong to
			possiblePatternsMaps[i] = new SetVar[patternMaps.get(i).size()];

			for (int j = 0; j < possiblePatternsMaps[i].length; j++) {
				possiblePatternsMaps[i][j] = model.setVar("map" + i + "" + j, patternMaps.get(i).get(j));
				allMaps.add(possiblePatternsMaps[i][j]);
			}

		}

		// ============Defining Constraints======================//
		// ===1-No overlapping between maps
		// ===2-A map should be one of the defined maps by the variable
		// possiblePatternMaps
		// ===3-at least 1 map for each pattern

		// 1-no overlapping
		model.allDisjoint(patterns).post();

		// List<Constraint> constraints = new LinkedList<Constraint>();

		// essential: at least 1 map for each pattern
		for (int i = 0; i < patterns.length; i++) {
			model.member(possiblePatternsMaps[i], patterns[i]).post();
		}

		model.allDisjoint(extraPatterns).reify();

		// for extras
		for (int i = 0; i < extraPatterns.length; i++) {

			// needs to belong to one of the maps
			model.member(allMaps.toArray(new SetVar[0]), extraPatterns[i]).post();

			// no overlapping with the other essential patterns
			for (int j = 0; j < patterns.length; j++) {
				model.disjoint(patterns[j], extraPatterns[i]).post();
			}

		}

		// ============Finding solutions======================//
		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();

		return solutions;
	}

	protected static int findMaxNumber(Map<Integer, List<int[]>> patternMaps) {

		// finds the maximum action number specified in the given map
		int result = 0;

		for (List<int[]> list : patternMaps.values()) {
			for (int[] ary : list) {
				if (ary[ary.length - 1] > result) {
					result = ary[ary.length - 1];
				}
			}
		}

		return result;
	}

	public static List<Solution> findSolutions2(Map<Integer, List<int[]>> patternMaps, int numberOfActions) {

		// numberOfActions could be defined as the max number of the maps

		int[] actionsArray = new int[numberOfActions];

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		int numOfAllMaps = 0;

		for (List<int[]> list : patternMaps.values()) {
			numOfAllMaps += list.size();
		}

		int currentNumOfPatterns = numOfAllMaps;
		Model model;
		List<Solution> solutions = null;
		Solver solver = null;

		while (currentNumOfPatterns > 0) {

			model = new Model("Pattern-Map Model");

			// ============Defining Variables======================//
			SetVar[] patterns = new SetVar[currentNumOfPatterns];
			SetVar[] possiblePatternsMaps = new SetVar[numOfAllMaps];

			// each pattern has as domain values the range from {} to
			// {0,1,2,..,N-1}, where N is number of actions
			for (int i = 0; i < currentNumOfPatterns; i++) {
				patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
			}

			int index = 0;

			for (List<int[]> list : patternMaps.values()) {
				for (int[] ary : list) {
					possiblePatternsMaps[index] = model.setVar("map" + index, ary);
					index++;
				}
			}

			// ============Defining Constraints======================//
			// ===1-No overlapping between maps
			// ===2-A map should be one of the defined maps by the variable
			// possiblePatternMaps
			// ===3-at least 1 map for each pattern

			// 1-no overlapping
			model.allDisjoint(patterns).post();

			// essential: at least 1 map for each pattern
			for (int i = 0; i < patterns.length; i++) {
				model.member(possiblePatternsMaps, patterns[i]).post();
			}

			// ============Finding solutions======================//
			solver = model.getSolver();

			// if a solution found then break

			if (solver.solve()) {
				System.out.println("a solution is found for" + "\nNumber of patterns used = " + currentNumOfPatterns);
				break;
			}

			System.out.println("No solution found for patterns # = " + currentNumOfPatterns
					+ "... decreasing the number of patterns and looking for a solution again");
			currentNumOfPatterns--;
		}

		if (currentNumOfPatterns > 0) {
			solutions = solver.findAllSolutions();
		}

		return solutions;
	}

	public static List<Solution> findSolutions3(Map<Integer, List<int[]>> patternMaps, int numberOfActions) {

		// numberOfActions could be defined as the max number of the maps

		int[] actionsArray = new int[numberOfActions];

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		int numOfAllMaps = 0;

		for (List<int[]> list : patternMaps.values()) {
			numOfAllMaps += list.size();
		}

		int currentNumOfPatterns = numOfAllMaps;
		Model model;
		List<Solution> solutions = null;
		Solver solver = null;
		IntVar severitySum = null;
		int maxSeverity = 20;
		SetVar[] possiblePatternsMaps = new SetVar[numOfAllMaps];

		// actual severity array, assuming its embedded in the argument
		// variable
		int[] severityValuesForMaps = new int[numOfAllMaps];

		for (int i = 0; i < numOfAllMaps; i++) {
			severityValuesForMaps[i] = ((i + 1) * 2) % maxSeverity;
		}

		// =============look for
		// solution==========================================
		while (currentNumOfPatterns > 0) {

			model = new Model("Pattern-Map Model");

			// ============Defining Variables======================//
			SetVar[] patterns = new SetVar[currentNumOfPatterns];
			IntVar[] patternseverity = new IntVar[currentNumOfPatterns];

			// used to update severity values
			int[] coeffs = new int[currentNumOfPatterns];
			Arrays.fill(coeffs, 1); // coeff is 1

			// defines the maximum number of patterns
			// IntVar numberOfPatterns = model.intVar("max_patterns_num",
			// patternMaps.keySet().size(), numOfAllMaps);

			// defines severity. Currently it is considered from 1 to 10
			severitySum = model.intVar("max_severity", 0, 99999);

			// each pattern has as domain values the range from {} to
			// {0,1,2,..,N-1}, where N is number of actions
			for (int i = 0; i < currentNumOfPatterns; i++) {
				patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
				patternseverity[i] = model.intVar("pattern_" + i + "_severity", 0, maxSeverity - 1);
			}

			int index = 0;
			for (List<int[]> list : patternMaps.values()) {
				for (int[] ary : list) {
					possiblePatternsMaps[index] = model.setVar("map" + index, ary);
					index++;
				}
			}

			// ============Defining Constraints======================//
			// ===1-No overlapping between maps
			// ===2-A map should be one of the defined maps by the variable
			// possiblePatternMaps
			// ===3-at least 1 map for each pattern

			// 1-no overlapping
			model.allDisjoint(patterns).post();

			// essential: at least 1 map for each pattern
			for (int i = 0; i < patterns.length; i++) {
				model.member(possiblePatternsMaps, patterns[i]).post();
			}

			// create constraints over the value of severity to be one in the
			// defined pattern severity array
			for (int i = 0; i < patterns.length; i++) {
				for (int j = 0; j < numOfAllMaps; j++) {
					model.ifThen(model.allEqual(patterns[i], possiblePatternsMaps[j]),
							model.element(patternseverity[i], severityValuesForMaps, model.intVar(j)));
				}
			}

			// defines the maximum severity for a solution
//			model.scalar(patternseverity, coeffs, "=", severitySum).post();
//			model.setObjective(Model.MAXIMIZE, severitySum);

			// ============Finding solutions======================//
			solver = model.getSolver();

			if (solver.solve()) {
				break;
			}

			currentNumOfPatterns--;
		}

		solutions = solver.findAllOptimalSolutions(severitySum, Model.MAXIMIZE, null);

		return solutions;
	}

	public static List<Solution> findSolutions4(Map<Integer, List<int[]>> patternMaps, int numberOfActions) {

		// numberOfActions could be defined as the max number of the maps

		int[] actionsArray = new int[numberOfActions];

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		int numOfAllMaps = 0;

		for (List<int[]> list : patternMaps.values()) {
			numOfAllMaps += list.size();
		}

		int currentNumOfPatterns = numOfAllMaps;
		Model model = null;
		List<Solution> solutions = null;
		Solver solver = null;
		IntVar severitySum = null;
		int maxSeverity = 20;
		int minSeverity = 1;
		SetVar[] possiblePatternsMaps = new SetVar[numOfAllMaps];
		SetVar[] patterns = null;
		boolean isSolutionfound = false;

		// actual severity array, assuming its embedded in the argument
		// variable
		int[] severityValuesForMaps = new int[numOfAllMaps];

		for (int i = 0; i < numOfAllMaps; i++) {
			severityValuesForMaps[i] = maxSeverity-minSeverity;
		}

		// =============look for
		// solution==========================================
		while (currentNumOfPatterns > 0) {

			model = new Model("Pattern-Map Model");

			// ============Defining Variables======================//
			patterns = new SetVar[currentNumOfPatterns];
			IntVar[] patternseverity = new IntVar[currentNumOfPatterns];

			// used to update severity values
			int[] coeffs = new int[currentNumOfPatterns];
			Arrays.fill(coeffs, 1); // coeff is 1

			// defines severity. Currently it is considered from 1 to 10
			severitySum = model.intVar("max_severity", 0, 99999);

			// each pattern has as domain values the range from {} to
			// {0,1,2,..,N-1}, where N is number of actions
			for (int i = 0; i < currentNumOfPatterns; i++) {
				patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
				patternseverity[i] = model.intVar("pattern_" + i + "_severity", minSeverity, maxSeverity);
			}

			int index = 0;
			for (List<int[]> list : patternMaps.values()) {
				for (int[] ary : list) {
					possiblePatternsMaps[index] = model.setVar("map" + index, ary);
					index++;
				}
			}

			// ============Defining Constraints======================//
			// ===1-No overlapping between maps
			// ===2-A map should be one of the defined maps by the variable
			// possiblePatternMaps
			// ===3-at least 1 map for each pattern

			// 1-no overlapping
			model.allDisjoint(patterns).post();

			// essential: at least 1 map for each pattern
			for (int i = 0; i < patterns.length; i++) {
				model.member(possiblePatternsMaps, patterns[i]).post();
			}

			// create constraints over the value of severity to be one in the
			// defined pattern severity array
			for (int i = 0; i < patterns.length; i++) {
				for (int j = 0; j < numOfAllMaps; j++) {
					model.ifThen(model.allEqual(patterns[i], possiblePatternsMaps[j]),
							model.element(patternseverity[i], severityValuesForMaps, model.intVar(j)));
				}
			}

			// defines the maximum severity for a solution
			model.scalar(patternseverity, coeffs, "=", severitySum).post();
//			model.setObjective(Model.MAXIMIZE, severitySum);

			// ============Finding solutions======================//
			solver = model.getSolver();
			SetVar uniq;
			solutions = new LinkedList<Solution>();
			List<Integer> vals = new LinkedList<Integer>();

			while (solver.solve()) {
				
				vals.clear();
				
				// get the new solution
				for (int i = 0; i < currentNumOfPatterns; i++) {
					vals.addAll(Arrays.stream(patterns[i].getValue().toArray()).boxed().collect(Collectors.toList()));
				}

				//create a setVar of the new solution
				uniq = model.setVar(vals.stream().mapToInt(i -> i).toArray());
				
				//add a constraint that next solution should be different from this
				model.not(model.union(patterns, uniq)).post();
				
				//add a constraint that next solution should have equal or more actions
				//could be implemented..?
			
				//add the current solution to the solutions list
				solutions.add(new Solution(model).record());

				isSolutionfound = true;
				// break;
			}

			if (isSolutionfound) {
				break;
			}

			currentNumOfPatterns--;
		}


		return solutions;
	}

}
