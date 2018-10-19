package Interfaces;

import model.HexString;
import model.Ledger;
import newMain.Transaction;
import sharded.Insertable;

public interface DAGInsertable extends Insertable<Transaction>
{
    void addTransaction(final Transaction t);
    
    Ledger createLedger();
    
    Double getBalance(final HexString p0);
}
