package newMain;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Main.TangleAlgorithms;
import Main.TransactionQueue;
import model.Hash;

/**
 * Inserts Transactions coming from the network into the DAG bzw. Database
 */
public class TxInserter {
	
	private static Logger log = LoggerFactory.getLogger(TxInserter.class); 
	
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
				return this.insert(t);
			}else{
				return false;
			}
		};
		queue = new TransactionQueue(pred, retrieve);
	}
	
	public boolean insert(Transaction transaction){
		
		DAG dag = ri.getDAG();
		
		//   VALIDATE
		boolean valid = dag.getCurrentLedger().validTransaction(transaction);
		
		if(!valid){
			log.debug("Tx Value not valid for sender " + transaction.getSender().getHashString());
			return valid;
		}
		
		valid = dag.getTransactionList().containsAll(transaction.getConfirmed().stream().map(x -> x.getTransaction(dag)).collect(Collectors.toList())) && valid;  //MApping because of TransactionReference
		
		if(!valid){
			List<Hash> conflicts = transaction.getConfirmed().stream().filter(x -> !dag.getTransactionList().contains(x.getTransaction(dag))).map(x -> x.getTxId()).collect(Collectors.toList());
			queue.add(transaction, conflicts);
			log.debug("Confirmation Error in Tx - Added to queue (" + conflicts.size() + ")");
			return valid;
		}
		
		valid = transaction.getPowProof().validateProof() && valid;
		
		if(!valid){
			log.debug("Wrong PoW Proof");
			return valid;
		}
				
		valid = transaction.getCreatedTimestamp() < System.currentTimeMillis() && valid;
		
		if(!valid){
			log.debug("Tx has been created in the future!");
			return valid;
		}
		
		//Check if the Transaction is younger than the Confirmations
		valid = transaction.getConfirmed().stream().allMatch(x -> transaction.getCreatedTimestamp() > x.getTransaction(dag).getCreatedTimestamp()) && valid;
		
		if(!valid){
			log.debug("Tx is younger than the ones it confirmes");
			return valid;
		}
		
		valid = CryptoUtil.validateSignature(transaction.getSignature(), CryptoUtil.publicKeyFromString(transaction.getSender().getHashString()), transaction.createHashString().getBytes()) && valid;
		
		if(!valid){
			System.out.println(transaction.createHashString());
			log.debug("Tx not signed properly");
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
