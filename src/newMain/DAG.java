package newMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Interfaces.DAGInsertable;
import model.Hash;
import model.HexString;
import model.Ledger;

public class DAG implements Insertable, DAGInsertable{
	
	Map<Hash, Transaction> transactions = new HashMap<>();
	List<Transaction> transactionList = new ArrayList<>();
	
	Ledger currentLedger = new Ledger();
	
	public void insertTransaction(Transaction t){
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

	public Ledger getCurrentLedger() {
		return currentLedger;
	}

	@Override
	public void addTranscation(Transaction p0) {
		insertTransaction(p0);  //TODO only because of insertable rendundancy
	}

	@Override
	public Ledger createLedger() {
		return currentLedger;  //TODO same as getCurrentLedger
	}

	@Override
	public Double getBalance(HexString p0) {
		return currentLedger.getBalance(p0);
	}
	
}
