package sharded;

import java.util.ArrayList;
import java.util.List;

import org.rpanic.GroupedNeighborPool;
import org.rpanic.NeighborRequestReponse;

import Interfaces.ShardedDAGInsertable;
import model.Hash;
import newMain.RI;
import voting.DistributedVotingManager;
import voting.DistributedVotingSender;

public class ShardingLevel implements Insertable<ShardingLevelTransaction>{

	List<ShardedDAGInsertable> insertables;
	
	RI ri;
	int level;
	GroupedNeighborPool pool;
	NeighborRequestReponse response;
	DistributedVotingManager voting;
	public long id;

	public ShardingLevel(long id, RI ri, List<ShardedDAGInsertable> insertables, int level, GroupedNeighborPool pool) {
		super();
		this.id = id;
		this.insertables = new ArrayList<>(insertables);
		this.ri = ri;
		this.level = level;
		this.pool = pool;
		this.response = new NeighborRequestReponse(pool);
		this.voting = new DistributedVotingManager(new ShardedVotingWeighterBuilder(this));
		this.voting.addListener(new DistributedVotingSender(pool, ri.getPublicKey(), ri));
	}

	public NeighborRequestReponse getResponsers() {
		return response;
	}
	
	public void addInsertable(ShardedDAGInsertable insertable){
		insertables.add(insertable);
	}

	@Override
	public void addTransaction(ShardingLevelTransaction obj) {
		insertables.forEach(x -> x.addTransaction(obj));
	}

	public GroupedNeighborPool getNeighborPool(){
		return pool;
	}
	
	public DistributedVotingManager getVotingManager(){
		return voting;
	}

	@Override
	public ShardingLevelTransaction getTransaction(Hash hash) {
		
		for(ShardedDAGInsertable i : insertables){ //TODO Local DAG representation first, then DB, then Network
			
			ShardingLevelTransaction o = i.getTransaction(hash);
			if(o != null)
				return o;
			
		}
		
		return null;
	}
	
	public ShardingLevelDAG getLocalDAG(){
		for(ShardedDAGInsertable i : insertables){
			if(i instanceof ShardingLevelDAG){
				return (ShardingLevelDAG)i;
			}
		}
		return null;
	}
	
}
