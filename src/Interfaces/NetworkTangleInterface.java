// 
// Decompiled by Procyon v0.5.30
// 

package Interfaces;

import java.util.Map;

import org.rpanic.NeighborPool;

import Main.Tangle;
import Main.Transaction;
import model.HexString;
import model.Ledger;
import network.ObjectSerializer;

public class NetworkTangleInterface implements TangleInterface
{
    Tangle tangle;
    NeighborPool pool;
    
    public NetworkTangleInterface(final Tangle tangle, final NeighborPool pool) {
        this.tangle = tangle;
        this.pool = pool;
    }
    
    @Override
    public synchronized void addTranscation(final Transaction t) {
        String request = t.store();
        request = "tx " + request;
        this.pool.broadcast(request);
    }
    
    @Override
    public Ledger createLedger() {
    	String request = "ledger";
        String response = this.pool.getRandomNeighbor().send(request);
        Map<HexString, Double> ledger = new ObjectSerializer().parseMap(response, x -> HexString.fromHashString(x), x -> Double.parseDouble(x));
        return new Ledger(ledger);
    }
    
    @Override
    public Double getBalance(final HexString account) {
        return this.createLedger().getBalance(account);
    }
}
