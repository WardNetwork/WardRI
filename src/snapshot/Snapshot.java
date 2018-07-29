// 
// Decompiled by Procyon v0.5.30
// 

package snapshot;

import Main.Tangle;
import Main.Transaction;
import model.Ledger;
import model.TangleTransaction;

public class Snapshot
{
    public Ledger state;
    Tangle tangle;
    public long createdTimeStamp;
    
    public static Snapshot createSnapshot(Tangle tangle) {
    	
        Ledger ledger = tangle.createLedger2();
        
        for (TangleTransaction t : tangle.getTips()) {
        	ledger.addTransaction(t);
        }
        
        Snapshot s = new Snapshot(tangle, ledger);
        
        return s;
    }
    
    public Snapshot(Tangle tangle, Ledger state) {
        this.state = state;
        this.tangle = tangle;
        this.createdTimeStamp = System.currentTimeMillis();
    }
    
    public void reHangTips(Tangle tangle) {
    	
        for (TangleTransaction t : tangle.getTips()) {
            t.getConfirmed().clear();
            t.getConfirmed().add(t);
        }
        
    }
}
