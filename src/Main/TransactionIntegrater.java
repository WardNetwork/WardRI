package Main;

import Interfaces.NetworkTangleInterface;
import Interfaces.TangleInterface;
import Interfaces.TangleInterfaceDistributor;

public class TransactionIntegrater {

	TangleInterfaceDistributor distributor;
	Tangle tangle;
	
	public TransactionIntegrater(Tangle tangle, TangleInterfaceDistributor distributor){
		
		//IF Distributor has NetworkInterface, remove it without damaging the original distributor
		if(distributor.getInterface(NetworkTangleInterface.class) != null){
			
			this.distributor = new TangleInterfaceDistributor(tangle);
			
			for(TangleInterface in : distributor.getInterfaces()){
				
				if(!in.getClass().getName().equals(NetworkTangleInterface.class.getName())){
					this.distributor.addTangleInterface(in);
				}
			}
			
		}else{
			this.distributor = distributor;
		}
		this.tangle = tangle;
	}
	
	public void integrate(Transaction t){
		
		if(t.isSealed() && t.getTransactionProof().validateProof()){
			
			//if(tangle.validateTransaction(t, tangle.currentLedgerState)){
			if(tangle.validateAndFixTangle(t)){
				
				distributor.addTranscation(t);
				
			}
		}
		
	}
	
}
