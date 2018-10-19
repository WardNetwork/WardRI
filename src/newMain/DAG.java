package newMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Interfaces.DAGInsertable;
import model.Hash;
import model.HexString;
import model.Ledger;
import voting.Epoch;

public class DAG implements DAGInsertable{
	
	Map<Hash, Transaction> transactions = new HashMap<>();
	List<Transaction> transactionList = new ArrayList<>();
	
	Ledger currentLedger = new Ledger();

	@Override
	public void addTransaction(Transaction t) {
		if(transactions.containsKey(t.getTxId())){  //Wenn die Tx schon vorhanden ist
			return;
		}
		transactions.put(t.getTxId(), t);
		transactionList.add(t);
		currentLedger.addTransaction(t);
		
		//Update References
		t.getConfirmed().stream().forEach(x -> transactions.get(x.getTxId()).confirmedBy.add(t));
	}
	
	public Transaction findTransaction(Hash tx){
		return transactions.get(tx);
	}
	
	public Map<Hash, Transaction> getTransactions(){
		return transactions;
	}

	public List<Transaction> getTransactionList() {
		return transactionList;
	}
	
	@Override
	public Ledger createLedger() {
		return currentLedger;
	}

	@Override
	public Double getBalance(HexString p0) {
		return currentLedger.getBalance(p0);
	}
	
	public List<Transaction> getTransactionsInEpoch(Epoch epoch, long epochNum){
		long endtime = epoch.getEndTime(epochNum);
		long beginTime = epoch.getBeginningTime(epochNum);
		
		return transactionList.stream()
		.filter(x -> x.getCreatedTimestamp() < endtime && x.getCreatedTimestamp() >= beginTime)
		.collect(Collectors.toList());
	}

	@Override
	public Transaction getTransaction(Hash hash) {
		return findTransaction(hash);
	}
	
}
