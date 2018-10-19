package Interfaces;

import Main.TangleVisualizer;
import model.Hash;
import sharded.ShardingLevelTransaction;

public class ShardedVisualizerInterface implements ShardedDAGInsertable{
	
	public TangleVisualizer<ShardingLevelTransaction> visualizer;
    
    public ShardedVisualizerInterface(final TangleVisualizer<ShardingLevelTransaction> visualizer) {
        this.visualizer = visualizer;
    }

	@Override
	public void addTransaction(ShardingLevelTransaction obj) {
		visualizer.addTransaction(obj);
	}

	@Override
	public ShardingLevelTransaction getTransaction(Hash hash) {
		// TODO Auto-generated method stub
		return null;
	}
}
