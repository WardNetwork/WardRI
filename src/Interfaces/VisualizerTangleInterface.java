package Interfaces;

import Main.TangleVisualizer;
import model.Hash;
import model.HexString;
import model.Ledger;
import newMain.Transaction;

public class VisualizerTangleInterface implements DAGInsertable
{
    public TangleVisualizer<Transaction> visualizer;
    
    public VisualizerTangleInterface(final TangleVisualizer<Transaction> visualizer) {
        this.visualizer = visualizer;
    }
    
    @Override
    public void addTransaction(final Transaction t) {
        this.visualizer.addTransaction(t);
    }
    
    @Override
    public Ledger createLedger() {
        return null;
    }
    
    @Override
    public Double getBalance(final HexString account) {
        return null;
    }

	@Override
	public Transaction getTransaction(Hash hash) {
		return null;
	}
}
