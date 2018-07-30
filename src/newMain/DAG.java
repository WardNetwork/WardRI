package newMain;

import java.util.List;
import java.util.Map;

import model.HexString;

public class DAG {
	
	Map<String, Transaction> transactions;
	List<Transaction> transactionList;
	
	public void insertTransaction(Transaction t){
		
	}
	
	public Transaction findTransaction(HexString tx){
		return null;
	}
	
	public Map<String, Transaction> getTransactions(){
		return transactions;
	}

	public List<Transaction> getTransactionList() {
		return transactionList;
	}
	
}
