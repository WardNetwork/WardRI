package newMain;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import Main.TangleAlgorithms;
import Main.TransactionQueue;
import model.Hash;

/**
 * Inserts Transactions coming from the network into the DAG bzw. Database
 */
public class TxInserter {
	
	TransactionQueue queue;
	
	Predicate<Transaction> pred;
	Predicate<Hash> retrieve;
	
	private RI ri;
	
	public TxInserter(RI _ri){
		this.ri = _ri;
		this.pred = (t) -> {
			return this.insert(t);
		};
		this.retrieve = (h) -> {
			Transaction t = new NetworkObjectGetter().resolveTransaction(h, _ri);
			if(t != null){
				System.out.println("resolve tried - succeed");
				return this.insert(t);
			}else{
				System.out.println("resolve tried - failed");
				return false;
			}
		};
		queue = new TransactionQueue(pred, retrieve);
	}
	
	public boolean insert(Transaction transaction){
		
		DAG dag = ri.getDAG();
		
		//   VALIDATE
		boolean valid = dag.createLedger().validTransaction(transaction);
		
		if(!valid){
			Logger.warn("Tx Value not valid for sender " + transaction.getSender().getHashString());
			return valid;
		}
		
		valid = dag.getTransactionList().containsAll(transaction.getConfirmed().stream().map(x -> x.getTransaction(dag)).collect(Collectors.toList())) && valid;  //MApping because of TransactionReference
		
		if(!valid){
			List<Hash> conflicts = transaction.getConfirmed().stream().filter(x -> !dag.getTransactionList().contains(x.getTransaction(dag))).map(x -> x.getTxId()).collect(Collectors.toList());
			queue.add(transaction, conflicts);
			Logger.warn("Confirmation Error in Tx - Added to queue (" + conflicts.size() + ")");
			return valid;
		}
		
		valid = transaction.getPowProof().validateProof() && valid;
		
		if(!valid){
			Logger.warn("Wrong PoW Proof");
			return valid;
		}
				
		valid = transaction.getCreatedTimestamp() < System.currentTimeMillis() && valid;
		
		if(!valid){
			Logger.warn("Tx has been created in the future!");
			return valid;
		}
		
		//Check if the Transaction is younger than the Confirmations
		valid = transaction.getConfirmed().stream().allMatch(x -> transaction.getCreatedTimestamp() > x.getTransaction(dag).getCreatedTimestamp()) && valid;
		
		if(!valid){
			Logger.warn("Tx is younger than the ones it confirmes");
			return valid;
		}
		
		valid = CryptoUtil.validateSignature(transaction.getSignature(), CryptoUtil.publicKeyFromString(transaction.getSender().getHashString()), transaction.getTxId().getHash()) && valid;
		
		if(!valid){
			Logger.info(transaction.createHashString());
			Logger.warn("Tx not signed properly");
			return valid;
		}
		
		if(!valid){
			return valid;
		}
		
		//  Calculate - kp was ich damit gemeint habe
		
		//  INSERT
		
		ri.getInsertables().forEach(x -> x.addTransaction(transaction));
		
		queue.notify(transaction);
		TangleAlgorithms.addTransactionToCache(transaction);
		
		return valid;
		
	}
	
}
