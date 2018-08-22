package Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import newMain.DAG;
import newMain.Transaction;

public class TangleAlgorithms {
	
	/** Number of Transactions which should be considered in the electConfimation algorithm, sorted by date
	 */
	public static final int numOfLatestTx = 200; //20; //TODO Dynamisch berechnen
	public static final double timeDiffMultiplicator = 0.5d;
	
	public static volatile List<Transaction> latestTxs = new ArrayList<>();
	
	public static Transaction electConfirmationTx(DAG dag){
		
		Map<Transaction, Double> map = new HashMap<>();
		double totalProbability = 0D;
		
		synchronized (latestTxs) {
			
			long sumTimeDiff = latestTxs.stream().mapToLong(x -> calculateTimeDiff(System.currentTimeMillis(), x)).sum();
			
			for(Transaction t : latestTxs){
				double probability = getTxSelectionProbability(System.currentTimeMillis(), t, sumTimeDiff);
				
				map.put(t, probability);
				
				totalProbability += probability;
			}
		}
		
		double random = new Random().nextDouble() * totalProbability;
		
		Iterator<Transaction> iterator = map.keySet().iterator();
		
		Transaction tempTrans = iterator.next();
		
		for(double d = map.get(tempTrans) ; d <= totalProbability + map.get(tempTrans); d += map.get(tempTrans)){
			
			if(d >= random){
				
				return tempTrans;
			}
			
			if(!iterator.hasNext()){
				System.out.println("asd");
			}
			tempTrans = iterator.next();
		}
		return null;
	}
	
	public static void addTransactionToCache(Transaction transaction){
		synchronized (latestTxs) {
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
	}
	
	public static long calculateTimeDiff(Transaction t1, Transaction t2){
		
		return t1.getCreatedTimestamp() - t2.getCreatedTimestamp();
		
	}
	
	public static long calculateTimeDiff(long t1, Transaction t2){
		return t1 - t2.getCreatedTimestamp();
	}
	
	public static double getTxSelectionProbability(long now, Transaction candidate, long sumTimeDiff){
		
//		if(candidate.nodesWhichConfirmedMe.size() <= 1) {
//			System.err.println();
//		}
		
		double timeDiff = ((double)sumTimeDiff) / ((double)calculateTimeDiff(now, candidate));
		
		double sizeweight = candidate.getConfirmedBy().size();
		
//		double result = 4.13D * Math.pow(Math.E, (-0.92) * sizeweight);  //Calculated with Geogebra
//		double result = 6.22D * Math.pow(Math.E, (-0.85) * sizeweight);  //Calculated with Geogebra //0.85
		double result = 29D * Math.pow(Math.E, (-1.03) * sizeweight);
		
//		if(sizeweight == 0)
//			return 1000000D;
		if(candidate.getConfirmedBy().size() == 0)// && Tangle.getTangleInstance().validateTransaction(candidate))
			return 1000D;
//			result += 10 + timeDiff * timeDiffMultiplicator * 100;  //DEV Probiern
		
		return result + (timeDiff * timeDiffMultiplicator);
		
	}
	
}
