package voting;

import org.rpanic.GroupedNeighborPool;

import model.HexString;
import newMain.CryptoUtil;
import newMain.DAG;
import newMain.RI;
import voting.DistributedVoting.DistributedVote;
import voting.DistributedVotingManager.VotingListener;

public class DistributedVotingSender implements VotingListener{
	
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
	public void sendVoteFor(DistributedVoteSubject subject, String voteCat, boolean vote) {
		
		String request = buildRequest(subject, voteCat, vote);
		
		pool.broadcast(request);
		
	}
	
	public String buildRequest(DistributedVoteSubject subject, String voteCat, boolean vote){
		
		String req = "voted " + subject + " " + voteCat + " " + vote;

		String signatureData = req;
		
		HexString signature = CryptoUtil.sign(ri.getKeyPair().getPrivate(), ri.getKeyPair().getPublic(), signatureData.getBytes()); 
		
		req += pubKey.getHashString() + " " + signature;
		
		return req;
	}

	@Override
	public void onVote(DistributedVoteSubject subject, String voteCat, DistributedVote vote, boolean newVote) {
		
		if(vote.pubKey.equals(pubKey)){
			
			sendVoteFor(subject, voteCat, vote.vote);
			
		}
	}

	
}
