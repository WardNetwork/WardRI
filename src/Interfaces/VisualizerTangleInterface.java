package Interfaces;

import Main.TangleVisualizer;
import model.HexString;
import model.Ledger;
import newMain.Transaction;

public class VisualizerTangleInterface implements DAGInsertable
{
    public TangleVisualizer visualizer;
    
    public VisualizerTangleInterface(final TangleVisualizer visualizer) {
        this.visualizer = visualizer;
    }
    
    @Override
    public void addTranscation(final Transaction t) {
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
}
