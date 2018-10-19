package Interfaces;

import model.Hash;
import sharded.Insertable;
import sharded.ShardingLevelTransaction;

public interface ShardedDAGInsertable extends Insertable<ShardingLevelTransaction>{
	
	public void addTransaction(ShardingLevelTransaction obj);
	
	public ShardingLevelTransaction getTransaction(Hash hash);
	
}
