package sync;

import java.util.ArrayList;
import java.util.List;

import org.rpanic.GroupedNeighborPool;
import org.rpanic.TCPNeighbor;

import network.ObjectSerializer;
import newMain.RI;
import newMain.Transaction;
import newMain.TxInserter;

public class LocalDAGSynchronizer
{
    RI ri;
    TCPNeighbor neighbor;
    GroupedNeighborPool pool;
    
    public LocalDAGSynchronizer(RI ri, TCPNeighbor neighbor, GroupedNeighborPool pool) {
        this.ri = ri;
        this.neighbor = neighbor;
        this.pool = pool;
    }
    
    public boolean synchronize() {
        System.out.println("Synchronizing Tangle...");
        long start = System.currentTimeMillis();
        if (neighbor != null && pool.checkConnection(neighbor)) {
        	System.out.println("Sending reqTngl");
        	
            String res = neighbor.send("reqTngl");
			System.out.println("Recieved: " + res);
            
            List<Transaction> transactions = new ArrayList<>();
            
            while(!res.contains("endOfTangleSync")) {
            	
                transactions.addAll(processResponse(res));
                
                res = neighbor.send("resTnglAck");
				System.out.println("Sending: resTnglAck");
				System.out.println("Recieved: " + res);
                
                
            }
            
            transactions.sort((x, y) -> Long.compare(x.getCreatedTimestamp(), y.getCreatedTimestamp()));
            
            /* TODO necessary? 
            long latestTx = ri.getDAG().getTransactionList().get(last);
            
            transactions.stream().filter(x -> x.getCreatedTimestamp() > latestTx);*/
            
            int success = 0;
            
            TxInserter inserter = new TxInserter(ri);
            for(Transaction t : transactions){
        		success += inserter.insert(t) ? 1 : 0;
            }
           

            System.out.println(transactions.size() + " transactions synchronized (" + success + "/" + (transactions.size()-success + ")"));
            System.out.println("Synchronized with TPS: " + ((double)transactions.size())/((System.currentTimeMillis()-start)/1000D));
            
            return true;
        }
        System.out.println("Tangle can´t be synchronized, Neighbor can´t be reached");
        return false;
    }
    
    public List<Transaction> processResponse(String response) {
    	
    	if (response.startsWith("resTngl ")) {
    		
    		response = response.substring(8);
            
            String[] txStrs = response.split(",");
            List<Transaction> list = new ArrayList<Transaction>();
            ObjectSerializer serializer = new ObjectSerializer();
            
            for (int i = 0; i < txStrs.length; i++) {
                final String txS = txStrs[i];
                System.out.println(i + " " + txS);
                list.add(serializer.parseTransaction(txS));
            }
            
            return list;
        
    	}
    	return null;
    	
    }
}
