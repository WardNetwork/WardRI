package test;

import java.util.ArrayList;
import java.util.Arrays;

import Main.TangleAlgorithms;
import newMain.Transaction;

public class ElectNodeTest {

	public static void main(String[] args) {
		
		Transaction appended = null;//new Transaction("as2d", "2as", 1);
		
		Transaction t3 = null;//new Transaction("asd", "aas", 1);
		
		Transaction t = null;//new Transaction("asd", "as", 1);
		t.addConfimationNodeReference("12");
		t.addConfimationNodeReference("12");
		t.addConfimationNodeReference("12");
		
		Transaction t2 = null;//new Transaction("asd", "as", 1);
		t2.addConfimationNodeReference("12");

		long sumTimeDiff = new ArrayList<>(Arrays.asList(t,t2,t3)).stream().mapToLong(x -> TangleAlgorithms.calculateTimeDiff(appended, x)).sum();
		
		System.out.println("t: " + TangleAlgorithms.getTxSelectionProbability(appended, t, sumTimeDiff));
		
		System.out.println("t2: " + TangleAlgorithms.getTxSelectionProbability(appended, t2, sumTimeDiff));
		System.out.println("t3: " + TangleAlgorithms.getTxSelectionProbability(appended, t3, sumTimeDiff));
		
	}
	
}
