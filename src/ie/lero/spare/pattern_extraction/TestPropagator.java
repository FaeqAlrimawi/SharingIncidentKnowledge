package ie.lero.spare.pattern_extraction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

public class TestPropagator extends Propagator<IntVar> {

	/**
	 * The constant the sum cannot be greater than
	 */
	final int b;
	int[] possiblePatternsAssociation;
	int[][] possiblePatternsSets;
	int numOfPossiblePatterns = 5;
	IntVar pattern1;
	IntVar pattern2;
	SetVar pattern;
	IntVar dummy;
	/**
	 * Constructor of the specific sum propagator : x1 + x2 + ... + xn <= b
	 * 
	 * @param x
	 *            array of integer variables
	 * @param b
	 *            a constant
	 */
//	public TestPropagator(IntVar pattern1, IntVar pattern2, int b) {
//		super(new IntVar[]{pattern1, pattern2}, PropagatorPriority.LINEAR, false);
//		this.pattern1 = pattern1;
//		this.pattern2 = pattern2;
//		this.b = b;
//		initialise();
//	}

	public TestPropagator(SetVar pattern, int b, IntVar dummy) {
		super(new IntVar[]{dummy}, PropagatorPriority.LINEAR, false);
		this.pattern1 = pattern1;
		this.pattern2 = pattern2;
		this.pattern = pattern;
		this.b = b;
		this.dummy = dummy;
		initialise();
	}

	
	protected void initialise() {

		possiblePatternsAssociation = new int[numOfPossiblePatterns];

		possiblePatternsAssociation[0] = 0; // pattern 0
		possiblePatternsAssociation[1] = 0; // pattern 0
		possiblePatternsAssociation[2] = 1; // pattern 1
		possiblePatternsAssociation[3] = 1; // pattern 1
		possiblePatternsAssociation[4] = 1; // pattern 1

		// represents the actions that map to a pattern. Actions are represented
		// as numbers in the sequence
		possiblePatternsSets = new int[numOfPossiblePatterns][];

		possiblePatternsSets[0] = new int[] { 1, 2 }; // pattern 0
		possiblePatternsSets[1] = new int[] { 2, 3 }; // pattern 0
		possiblePatternsSets[2] = new int[] { 5, 6 }; // pattern 1
		possiblePatternsSets[3] = new int[] { 4, 6 }; // pattern 1
		possiblePatternsSets[4] = new int[] { 9, 11 }; // pattern 1
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.INCLOW);
	}

	/*
	 * @Override public void propagate(int evtmask) throws
	 * ContradictionException { int sumLB = 0; for (IntVar var : vars) { sumLB
	 * += var.getLB(); } int F = b - sumLB; if (F < 0) { fails(); } for (IntVar
	 * var : vars) { int lb = var.getLB(); int ub = var.getUB(); if (ub - lb >
	 * F) { var.updateUpperBound(F + lb, this); } } }
	 */

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int sumLB = 0;
		boolean isFailed = false;
		
//		System.out.println("=========Propag=============");
//		
//		for (IntVar var : vars) {
//			// sumLB += var.getLB();
//			int possiblePatternIndex = var.getValue();
//			System.out.println("val: " + possiblePatternIndex);
//			if (possiblePatternIndex == 3) {
//				isFailed = true;
//				break;
//			}
//
//		}
		int[] vals = pattern.getValue().toArray();
		
		System.out.println(Arrays.toString(vals));
		
		if(!isACombination(vals)) {
			dummy.updateLowerBound(0, this);
			
		}
		
//		pattern1.updateLowerBound(pattern2.getUB(), this);
//		pattern2.updateUpperBound(pattern1.getLB(), this);
		
//		if (isFailed) { //stopping condition??
//			System.out.println("yaay");
//			fails();
//		}

		/*for (IntVar var : vars) {
			System.out.println(vars.length+" "+var.getUB());
			var.updateUpperBound(var.getUB()-1, this);
			}*/

	}
	
	protected boolean isOverLapping(int index1, int index2) {

		for(int e : possiblePatternsSets[index1]) {
			for(int e2 : possiblePatternsSets[index2]) {
				if(e == e2) {
					return true;
				}
			}
		}
		
		 
		 return false;
                
		
	}
	
	protected boolean isACombination(int patternArray[]) {

		for(int[] array : possiblePatternsSets) {
			if(Arrays.equals(patternArray, array)) {
				return true;
			}
		}
		
		 return false;
                
		
	}
	//
	// @Override
	// public ESat isEntailed() {
	// int sumUB = 0, sumLB = 0;
	// for (IntVar var : vars) {
	// sumLB += var.getLB();
	// sumUB += var.getUB();
	// }
	// if (sumUB <= b) {
	// return ESat.TRUE;
	// }
	// if (sumLB > b) {
	// return ESat.FALSE;
	// }
	// return ESat.UNDEFINED;
	// }

	@Override
//	public ESat isEntailed() {
////		int sumUB = 0, sumLB = 0;
////		boolean isFailed = false;
////		System.out.println("=========Entailed=============");
//		int domSum = 0;
//		boolean isOverlapping = false;
//		
//		isOverlapping = isOverLapping(pattern1.getValue(), pattern2.getValue());
//		
//		if (!isOverlapping) {
//			return ESat.TRUE;
//		} else if(isOverlapping) {
//			return ESat.FALSE;
//		} else {
//			return ESat.UNDEFINED;
//		}
//	
//	}
	
	public ESat isEntailed() {
//		int sumUB = 0, sumLB = 0;
//		boolean isFailed = false;
//		System.out.println("=========Entailed=============");
		
		boolean isACombination = dummy.getLB()!=0?true:false;
		
		if (isACombination) {
			return ESat.TRUE;
		} else if(!isACombination) {
			return ESat.FALSE;
		} else {
			return ESat.UNDEFINED;
		}
	
	}
}