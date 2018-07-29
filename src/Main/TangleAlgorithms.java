package Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import model.Hash;
import model.TangleTransaction;
import model.TransactionBase;

public class TangleAlgorithms {
	
	/** Number of Transactions which should be considered in the electConfimation algorithm, sorted by date
	 */
	public static final int numOfLatestTx = 20; //20; //TODO Dynamisch berechnen
	public static final double timeDiffMultiplicator = 0.5d;
	
	public static List<Transaction> latestTxs = new ArrayList<>();
	
	//2nd try
	public static Transaction electConfirmationTx(Tangle tangle, Transaction transaction){
		
//		int subListStart = tangle.transactions.size() - numOfLatestTx - 1;
//		if(subListStart < 0){
//			subListStart = 0;
//		}
		if(tangle.performantTransactions.size() == 0 ){
			addTransactionToCache(transaction);
			return null;
		}
		
//		List<Transaction> list = new ArrayList<>(tangle.transactions.values());
		
//		list.remove(transaction);
		
//		Collections.sort(list, new Comparator<Transaction>() {
//			@Override
//			public int compare(Transaction o1, Transaction o2) {
//				int i = Long.compare(o1.getCreatedTimestamp(), o2.getCreatedTimestamp());
////				if(i != 0)
////					System.out.println("Switched");
//				return i;
//			}
//		});
		
//		list = list.subList(subListStart, tangle.transactions.size()-1);
		
//		long sumTimeDiff = list.stream().mapToLong(x -> calculateTimeDiff(transaction, x)).sum();
		long sumTimeDiff = latestTxs.stream().mapToLong(x -> calculateTimeDiff(transaction, x)).sum();
		
		Map<Transaction, Double> map = new HashMap<>();
		double totalProbability = 0D;
		
		for(Transaction t : latestTxs){
			double probability = getTxSelectionProbability(transaction, t, sumTimeDiff);
			
			map.put(t, probability);
			
			totalProbability += probability;
		}
		
		double random = new Random().nextDouble() * totalProbability;
		
		Iterator<Transaction> iterator = map.keySet().iterator();
		
		Transaction tempTrans = iterator.next();
		
		for(double d = map.get(tempTrans) ; d <= totalProbability; d += map.get(tempTrans)){
			
			if(d >= random){
				
				return tempTrans;
			}
			
			if(!iterator.hasNext()){
				System.out.println("asd");
			}
			tempTrans = iterator.next();
		}
		
		//Add to Tx Cache
		addTransactionToCache(transaction);
		
		
		if(map.get(tempTrans) > totalProbability - random){ //Falls die Random zahl auf den Letzten f�llt, wird in der Schleife nix getan
			return tempTrans;
		}
		
		return null;
		
	}
	
	public static void addTransactionToCache(Transaction transaction){
		if(!latestTxs.contains(transaction) && (latestTxs.size() == 0 || transaction.getCreatedTimestamp() > latestTxs.get(0).getCreatedTimestamp())){
			if(latestTxs.size() < numOfLatestTx + 1){
				latestTxs.add(transaction);
			}else{
				latestTxs.remove(0);
				latestTxs.add(transaction);
			}
			Collections.sort(latestTxs, (o1, o2)-> Long.compare(o1.getCreatedTimestamp(), o2.getCreatedTimestamp()));
		}
	}
	
	public static long calculateTimeDiff(TransactionBase t1, TransactionBase t2){
		
		return t1.getCreatedTimestamp() - t2.getCreatedTimestamp();
		
	}
	
	public static double getTxSelectionProbability(Transaction base, Transaction candidate, long sumTimeDiff){
		
//		if(candidate.nodesWhichConfirmedMe.size() <= 1) {
//			System.err.println();
//		}
		
		double timeDiff = ((double)sumTimeDiff) / ((double)calculateTimeDiff(base, candidate));
		
		double sizeweight = candidate.getNodeWhichConfirmedThisNode().size();
		
//		double result = 4.13D * Math.pow(Math.E, (-0.92) * sizeweight);  //Calculated with Geogebra
//		double result = 6.22D * Math.pow(Math.E, (-0.85) * sizeweight);  //Calculated with Geogebra //0.85
		double result = 29D * Math.pow(Math.E, (-1.03) * sizeweight);
		
//		if(sizeweight == 0)
//			return 1000000D;
		if(candidate.getNodeWhichConfirmedThisNode().size() == 0)// && Tangle.getTangleInstance().validateTransaction(candidate))
			return 1000D;
//			result += 10 + timeDiff * timeDiffMultiplicator * 100;  //DEV Probiern
		
		return result + (timeDiff * timeDiffMultiplicator);
		
	}
	
}