package Interfaces;

import java.util.Map;

import newMain.Transaction;
import model.HexString;
import model.Ledger;

public interface DAGInsertable
{
    void addTranscation(final Transaction p0);  //TODO Spelling mistake
    
    Ledger createLedger();
    
    Double getBalance(final HexString p0);
}
