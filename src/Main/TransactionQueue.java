package Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import model.Hash;
import newMain.Transaction;

public class TransactionQueue {

	//TODO Use it in Code
	
	HashMap<Transaction, List<Hash>> map = new HashMap<>();
	HashMap<Transaction, List<Predicate<Transaction>>> actions = new HashMap<>();
	
	public void add(Transaction t, List<Hash> conflict, Predicate<Transaction> action) {
		
		if(map.containsKey(t)) {
			map.get(t).addAll(conflict);
			
		}else{
			List<Hash> list = new ArrayList<>();
			list.addAll(conflict);
			map.put(t, list);
		}
		
	}
	
	/**
	 * Is called, when a new Transaction got added to the Local Tangle, 
	 * so that the Transcationqueue can resolve issues with earlier transactions
	 */
	public void notify(Transaction t) {
		
		List<Hash> conflicts;
		
		for(Transaction candidate : map.keySet()) {
			
			conflicts = map.get(candidate);
			int i;
			if((i = conflicts.indexOf(t.getTxId())) >= 0) {
				
				if(conflicts.size() == 1) {
					Predicate<Transaction> predicate = actions.get(candidate).get(i);
					boolean success = predicate.test(candidate);
					if(success) {
						map.remove(candidate);
					}
				}else {
					conflicts.remove(i);
				}
				
				System.out.println("Tx resolved and added");
				
			}
			
		}
		
	}

	public int size() {
		return map.size();
	}
	
}
