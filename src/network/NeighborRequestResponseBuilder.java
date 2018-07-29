package network;

import org.rpanic.NeighborPool;
import org.rpanic.NeighborRequestReponse;

import Main.Tangle;
import Main.TransactionIntegrater;

public class NeighborRequestResponseBuilder {
	
	public static NeighborRequestReponse buildNewDefault(Tangle tangle, TransactionIntegrater integrater, NeighborPool pool){
		
		NeighborRequestReponse response = new NeighborRequestReponse(pool);
		
		response.addResponser(new TangleSyncResponser(tangle));
		response.addResponser(new TxResponser(tangle, integrater));
		response.addResponser(new LedgerResponser(tangle));
		
		return response;
		
	}
	
}
