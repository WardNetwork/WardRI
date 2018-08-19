package snapshot;

import model.Ledger;
import newMain.DAG;
import newMain.Transaction;

public class Snapshot
{
    public Ledger state;
    DAG dag;
    public long createdTimeStamp;
    
    public static Snapshot createSnapshot(DAG dag) {
    	
        Ledger ledger = dag.createLedger();
        
        //for (Transaction t : dag.getTips()) {
        	//ledger.addTransaction(t);
        //}
        
        Snapshot s = new Snapshot(dag, ledger);
        
        return s;
    }
    
    public Snapshot(DAG dag, Ledger state) {
        this.state = state;
        this.dag = dag;
        this.createdTimeStamp = System.currentTimeMillis();
    }
    
    public void reHangTips(DAG dag) {
    	
        /*for (Transaction t : dag.getTips()) {
            t.getConfirmed().clear();
            t.getConfirmed().add(t);
        }*/
        
    }
}
