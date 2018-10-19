package newMain;

import java.util.ArrayList;
import java.util.List;

public class DAGStatistic {

	private int totalTransactions = 0;
	List<Long> timestamps = new ArrayList<>();
	private static final int maxListSize = 50000;
	
	public void addedTransaction(){
		
		totalTransactions++;
		timestamps.add(System.currentTimeMillis());
		if(totalTransactions > maxListSize){
			timestamps.remove(0);
		}
		
	}
	
	public long transactionRate(){
		
		long duration = 1000L;
		long limit = System.currentTimeMillis() - duration;
		long txs = timestamps.stream().filter(x -> x >= limit).count() / (duration / 1000L);
		
		return txs;
		
	}
	
}
