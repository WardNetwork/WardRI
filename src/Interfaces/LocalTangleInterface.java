// 
// Decompiled by Procyon v0.5.30
// 

package Interfaces;

import Main.Tangle;
import Main.Transaction;
import model.HexString;
import model.Ledger;

public class LocalTangleInterface implements TangleInterface
{
    public Tangle tangle;
    
    public LocalTangleInterface(final Tangle tangle) {
        this.tangle = tangle;
    }
    
    @Override
    public synchronized void addTranscation(Transaction t) {
    	tangle.addOwnTransaction(t);
    }
    
    @Override
    public Ledger createLedger() {
        return this.tangle.createLedger2();
    }
    
    @Override
    public Double getBalance(final HexString account) {
        return this.tangle.createLedger2().getBalance(account);
    }
}
