package newMain;

import java.security.KeyPair;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import Main.TangleAlgorithms;
import model.HexString;

public class TxCreator {

	DAG dag;
	RI ri;
	
	public TxCreator(RI ri) {
		super();
		this.dag = ri.getDAG();
		this.ri = ri;
	}

	public Transaction create(HexString reciever, double amount, Object data){
		
		TransactionBuilder builder = new TransactionBuilder(ri.getPublicKey(), reciever, amount, data);
		
		//elect
		
		Set<Transaction> confirmation = electTransactions();
		
		Logger.error("Elected: " + confirmation.stream().map(x -> x.getTxId().getHashString()).reduce((x, y) -> x + ", " + y).orElse("none"));
		
		Set<DAGObjectReference<Transaction>> references = confirmation.stream().map(x -> new DAGObjectReference<Transaction>(x)).collect(Collectors.toSet());
		
		//Create, Sign & Pow
		
		KeyPair keypair = ri.getKeyPair();

		Transaction t = builder.setConfirmed(references).solveProof().sign(keypair).buildTransaction();
		
		Logger.error("TxId: " + t.getTxId());
		
		return t;
		
	}
	
	public Set<Transaction> electTransactions(){
		
		Set<Transaction> elected = new HashSet<>();
		
		if (dag.transactionList.size() <= 2) {
            elected.addAll(dag.getTransactionList());
            return elected;
        }
        Transaction temp = null;
        for (int i = 0; i < 100 && elected.size() < 2; ++i){
        	
            temp = TangleAlgorithms.electConfirmationTx(dag);
            
            if(elected.contains(temp)){
            	continue;
            }
            
            elected.add(temp);
        	
        }

        return elected;
        
	}
	
}
