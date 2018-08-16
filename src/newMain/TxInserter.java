package newMain;

/**
 * Inserts Transactions coming from the network into the DAG bzw. Database
 */
public class TxInserter {
	
	public boolean insert(Transaction transaction, RI ri){
		
		DAG dag = ri.getDAG();
		
		//   VALIDATE
		boolean valid = dag.getCurrentLedger().validTransaction(transaction);
		
		valid = dag.getTransactionList().containsAll(transaction.getConfirmed()) && valid;
		
		valid = transaction.getPowProof().validateProof() && valid;
				
		valid = transaction.getCreatedTimestamp() < System.currentTimeMillis() && valid;
		
		valid = CryptoUtil.validateSignature(transaction.getSignature(), CryptoUtil.publicKeyFromString(transaction.getSender().getHashString()), transaction.createHashString().getBytes()) && valid;
		
		if(!valid){
			return valid;
		}
		
		//  Calculate - kp was ich damit gemeint habe
		
		//  INSERT
		
		ri.getInsertables().forEach(x -> x.addTranscation(transaction));
		
		return valid;
		
	}
	
}
