package Interfaces;

import model.HexString;
import model.Ledger;
import newMain.Transaction;

public interface DAGInsertable
{
    void addTransaction(final Transaction p0);
    
    Ledger createLedger();
    
    Double getBalance(final HexString p0);
}
