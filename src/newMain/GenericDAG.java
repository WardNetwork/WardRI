package newMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import model.Hash;
import model.HexString;
import model.Ledger;
import sharded.DAGObject;
import voting.Epoch;

public class GenericDAG<E extends DAGObject<E>>{
	
	Map<Hash, E> transactions = new HashMap<>();
	List<E> transactionList = new ArrayList<>();
	
	Ledger currentLedger = new Ledger();

	public void addTransaction(E t) {
		if(transactions.containsKey(t.getTxId())){  //Wenn die Tx schon vorhanden ist
			return;
		}
		
		Logger.warn("New Level 1 Transaction: " + t.getTxId().getHashString());
		transactions.put(t.getTxId(), t);
		transactionList.add(t);
		
		//Update References
		t.getConfirmed().stream().forEach(x -> {
			Hash txid = x.getTxId();
			E e = transactions.get(txid);
			if(e == null) {
				//TODO Try to get it
				Logger.warn("E NULL: " + txid.getHashString());
			}
			Set<E> set = e.getConfirmedBy();
			set.add(t);
			}
		);
	}
	
	public <T extends DAGObject> void helper(List<T> list, T t){
		List<T> s = new ArrayList<>();
		s.add(t);
		list.add(t);
		//t.getConfirmed().stream().forEach(x -> transactions.get(x.getTxId()).getConfirmedBy().add(t));
	}
	
	public E findTransaction(Hash tx){
		return transactions.get(tx);
	}
	
	public Map<Hash, E> getTransactions(){
		return transactions;
	}

	public List<E> getTransactionList() {
		return transactionList;
	}
	
	public Ledger createLedger() {
		return currentLedger;
	}
	
	public Double getBalance(HexString p0) {
		return currentLedger.getBalance(p0);
	}
	
	public List<E> getTransactionsInEpoch(Epoch epoch, long epochNum){
		long endtime = epoch.getEndTime(epochNum);
		return transactionList.stream()
		.filter(x -> x.getCreatedTimestamp() < endtime && x.getCreatedTimestamp() > epoch.getBeginningTime(epochNum))
		.collect(Collectors.toList());
	}
	
}
