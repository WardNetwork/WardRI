package newMain;

import java.security.KeyPair;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import Main.TangleAlgorithms;
import newMain.Transaction;
import model.HexString;

public class TxCreator {

	DAG dag;
	RI ri;
	
	public TxCreator(RI ri) {
		super();
		this.dag = ri.getDAG();
		this.ri = ri;
	}
	
	public void issue(Transaction t){
		
		ri.tangleInterfaces.forEach(x -> x.addTranscation(t));
		
	}

	public Transaction create(HexString reciever, double amount, Object data){
		
		TransactionBuilder builder = new TransactionBuilder(ri.getPublicKey(), reciever, amount, data);
		
		//elect
		
		Set<Transaction> confirmation = electTransactions();
		Set<TransactionReference> references = confirmation.stream().map(x -> new TransactionReference(x)).collect(Collectors.toSet());
		
		//Create, Sign & Pow
		
		KeyPair keypair = ri.getKeyPair();

		Transaction t = builder.setConfirmed(references).solveProof().sign(keypair).buildTransaction();
		
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
        	
            temp = TangleAlgorithms.electConfirmationTx2(dag);
            
            if(elected.contains(temp)){
            	continue;
            }
            
            elected.add(temp);
        	
        }

        return elected;
        
	}
	
}
