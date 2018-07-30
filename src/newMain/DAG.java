package newMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Hash;
import model.Ledger;

public class DAG implements Insertable{
	
	Map<Hash, Transaction> transactions = new HashMap<>();
	List<Transaction> transactionList = new ArrayList<>();
	
	Ledger currentLedger = new Ledger();
	
	public void insertTransaction(Transaction t){
		transactions.put(t.getTxId(), t);
		transactionList.add(t);
		currentLedger.addTransaction(t);
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
	
}
