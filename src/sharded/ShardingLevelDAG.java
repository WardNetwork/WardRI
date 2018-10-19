package sharded;

import Interfaces.ShardedDAGInsertable;
import model.Hash;
import newMain.GenericDAG;

public class ShardingLevelDAG implements ShardedDAGInsertable{

	GenericDAG<ShardingLevelTransaction> dag = new GenericDAG<>();

	@Override
	public void addTransaction(ShardingLevelTransaction obj) {
		dag.addTransaction(obj);
	}
	@Override
	public ShardingLevelTransaction getTransaction(Hash hash) {
		return dag.findTransaction(hash);
	}
	
	public GenericDAG<ShardingLevelTransaction> getGenericDAG(){
		return dag;
	}

}
