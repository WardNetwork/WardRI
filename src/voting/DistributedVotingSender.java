package voting;

import org.rpanic.GroupedNeighborPool;

import model.HexString;
import newMain.CryptoUtil;
import newMain.DAG;
import newMain.RI;
import voting.DistributedVoting.DistributedVote;

public class DistributedVotingSender {
	
	GroupedNeighborPool pool;
	HexString pubKey;
	DAG dag;
	RI ri;
	
	public DistributedVotingSender(GroupedNeighborPool pool, HexString pubKey, RI ri){
		this.pool = pool;
		this.pubKey = pubKey;
		this.ri = ri;
		this.dag = ri.getDAG();
	}
	
	/** voteCat = hash(ledgerChanges)
	 */
	public void voteFor(String voteCat, boolean vote) {
		
		String request = buildRequest(voteCat, vote);
		
		pool.broadcast(request);
		
		DistributedVotingManager.getInstance(dag).addVote(voteCat, new DistributedVote(pubKey, dag, vote));
		
	}
	
	public String buildRequest(String voteCat, boolean vote){
		
		String req = "voted " + voteCat + " " + vote;

		String signatureData = req;
		
		HexString signature = CryptoUtil.sign(ri.getKeyPair().getPrivate(), ri.getKeyPair().getPublic(), signatureData.getBytes()); 
		
		req += pubKey.getHashString() + " " + signature;
		
		return req;
	}
	
}
