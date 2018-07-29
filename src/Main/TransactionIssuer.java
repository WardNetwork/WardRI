package Main;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Interfaces.TangleInterfaceDistributor;
import model.Ledger;

/**
 * Issues a new Transaction created by yourself and then broadcasts it to all recievers
 */
public class TransactionIssuer {
	
	TangleInterfaceDistributor distributor;
	Tangle tangle;
	PublicKey publicKey;
	PrivateKey privateKey;
	
	private static Logger log = LoggerFactory.getLogger(TransactionIssuer.class);
	
	public TransactionIssuer(Tangle tangle, TangleInterfaceDistributor distributor, PrivateKey privateKey, PublicKey publicKey){
		this.distributor = distributor;
		this.tangle = tangle;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	public void issue(Transaction t){
		
		Ledger ledger = tangle.currentLedgerState; //TODO getter
		
		Transaction elected = tangle.electValidTransaction(t, ledger);
		
		if (elected != null) {
        	
	        t.confirm(elected);
//	        log.debug("Tx" + t.getTxHash() + " confirmed Tx" + elected.getTxHash());
	        
            Transaction elected2 = tangle.electValidTransaction(t, ledger, elected);
            if (elected2 != null) {
                t.confirm(elected2);
//    	        log.debug("Tx" + t.getTxHash() + " confirmed Tx" + elected.getTxHash());
                
                //Successful appended
                
            }
        }
		
		t.doPoW();
		
		//TODO In Successful apended verschieben? Jetzt nur wegen Genesisshit
		log.info("Tx Id: " + t.getTxHash());
		
		t.sign(privateKey, publicKey);
		
		distributor.addTranscation(t);
		
		
		
		//TODO Workflow integrieren
		
	}
	
}
