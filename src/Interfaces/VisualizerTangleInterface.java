package Interfaces;

import Main.TangleVisualizer;
import newMain.Transaction;
import model.HexString;
import model.Ledger;
import model.TangleTransaction;

public class VisualizerTangleInterface implements TangleInterface
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
