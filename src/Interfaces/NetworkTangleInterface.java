package Interfaces;

import java.util.Map;

import org.rpanic.GroupedNeighborPool;

import model.HexString;
import model.Ledger;
import network.ObjectSerializer;
import newMain.Transaction;

public class NetworkTangleInterface implements DAGInsertable
{
    GroupedNeighborPool pool;
    
    public NetworkTangleInterface(final GroupedNeighborPool pool) {
        this.pool = pool;
    }
    
    @Override
    public synchronized void addTranscation(final Transaction t) {
        String request = new ObjectSerializer().serialize(t);
        request = "tx " + request;
        this.pool.broadcast(request);
    }
    
    @Override
    public Ledger createLedger() {
    	String request = "ledger";
        String response = this.pool.getRandomNeighbor().send(request);  //TODO Was, wenn der Neighbor nicht responded etc...
        Map<HexString, Double> ledger = new ObjectSerializer().parseMap(response, x -> HexString.fromHashString(x), x -> Double.parseDouble(x));
        return new Ledger(ledger);
    }
    
    @Override
    public Double getBalance(final HexString account) {
        return this.createLedger().getBalance(account);
    }
}
