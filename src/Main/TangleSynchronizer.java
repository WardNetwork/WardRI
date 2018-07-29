package Main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rpanic.NeighborPool;
import org.rpanic.TCPNeighbor;

import Interfaces.NetworkTangleInterface;
import Interfaces.TangleInterfaceDistributor;
import network.ObjectSerializer;

public class TangleSynchronizer
{
    Tangle tangle;
    TCPNeighbor neighbor;
    NeighborPool pool;
    TangleInterfaceDistributor distributor;
    
    public TangleSynchronizer(Tangle t, TangleInterfaceDistributor distributor, TCPNeighbor neighbor, NeighborPool pool) {
        this.tangle = t;
        this.neighbor = neighbor;
        this.pool = pool;
        this.distributor = distributor;
    }
    
    public boolean synchronize() {
        System.out.println("Synchronizing Tangle...");
        long start = System.currentTimeMillis();
        if (neighbor != null && pool.checkConnection(neighbor)) {
        	System.out.println("Sending reqTngl");
        	
            String res = neighbor.send("reqTngl");
            
            int txSize = 0;
            
            while(!res.contains("endOfTangleSync")) {
            	
                txSize += processResponse(res);
                
                res = neighbor.send("resTnglAck");
                
                System.out.println("res " + res);
                
            }

            boolean validTangle = this.tangle.validateTangle();
            System.out.println(txSize + " transactions synchronized");
            System.out.println("Tangle synchronized. Tangle is valid: " + validTangle);
            System.out.println("Synchronized with TPS: " + ((double)txSize)/((System.currentTimeMillis()-start)/1000D));
            
            return true;
        }
        System.out.println("Tangle can´t be synchronized, Neighbor can´t be reached");
        return false;
    }
    
    public int processResponse(String response) {
    	
    	if(response.endsWith(";")) { //TODO Ordentlich machen im Send() und einheitlich
    		response = response.substring(0, response.length()-1).trim();
    	}
    	
    	if (response.startsWith("resTngl ")) {
    		
    		response = response.substring(8);
            System.out.println("Response recieved");
            System.out.println("Responded Tanglesync with " + response);
            
            String[] txStrs = response.split(",");
            List<Transaction> list = new ArrayList<Transaction>();
            ObjectSerializer serializer = new ObjectSerializer();
            
            for (int i = 0; i < txStrs.length; i++) {
                final String txS = txStrs[i];
                System.out.println(i + " " + txS);
                list.add(serializer.parseTransaction(txS));
            }
            
            this.integrateIntoTangle(list);
            return list.size();
        
    	}
    	return 0;
    	
    }
    
    public void integrateIntoTangle(List<Transaction> list) {
//    	this.tangle.addExistingGenesisTransaction(list.get(0));
    	
        list.stream()/*.skip(1)*/.forEach(t -> 
        	distributor.addTranscation(t));    
    }
}
