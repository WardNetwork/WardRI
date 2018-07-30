package voting;

import org.rpanic.NeighborPool;

import Main.Tangle;
import model.HexString;
import voting.DistributedVoting.DistributedVote;

public class DistributedVotingSender {
	
	NeighborPool pool;
	HexString pubKey;
	Tangle tangle;
	
	public DistributedVotingSender(NeighborPool pool, HexString pubKey, Tangle tangle){
		this.pool = pool;
		this.pubKey = pubKey;
		this.tangle = tangle;
	}
	
	public void voteFor(String voteCat, boolean vote) {
		
		String request = buildRequest(voteCat, vote);
		
		pool.broadcast(request);
		
		DistributedVotingManager.getInstance(tangle).addVote(voteCat, new DistributedVote(pubKey, tangle, vote));
		
	}
	
	public String buildRequest(String voteCat, boolean vote){
		
		String req = "voted " + voteCat + " " + vote;
		
		String signature = ""; //TODO implement
		
		req += pubKey.getHashString() + " " + signature;
		
		return req;
	}
	
}
