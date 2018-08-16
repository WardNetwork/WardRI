package voting;

import org.rpanic.GroupedNeighborPool;

import model.HexString;
import newMain.DAG;
import voting.DistributedVoting.DistributedVote;

public class DistributedVotingSender {
	
	GroupedNeighborPool pool;
	HexString pubKey;
	DAG dag;
	
	public DistributedVotingSender(GroupedNeighborPool pool, HexString pubKey, DAG dag){
		this.pool = pool;
		this.pubKey = pubKey;
		this.dag = dag;
	}
	
	public void voteFor(String voteCat, boolean vote) {
		
		String request = buildRequest(voteCat, vote);
		
		pool.broadcast(request);
		
		DistributedVotingManager.getInstance(dag).addVote(voteCat, new DistributedVote(pubKey, dag, vote));
		
	}
	
	public String buildRequest(String voteCat, boolean vote){
		
		String req = "voted " + voteCat + " " + vote;
		
		String signature = ""; //TODO implement
		
		req += pubKey.getHashString() + " " + signature;
		
		return req;
	}
	
}
