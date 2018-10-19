package representative;

import java.net.Socket;

import org.pmw.tinylog.Logger;
import org.rpanic.Responser;

import model.Hash;
import model.HexString;
import network.ObjectSerializer;
import newMain.CryptoUtil;
import newMain.RI;
import sharded.BaseEpochListener;
import sharded.ShardingLevel;
import sharded.ShardingLevelTransaction;
import voting.DistributedVoteSubject;
import voting.DistributedVoting.DistributedVote;
import voting.DistributedVotingManager;
import voting.DistributedVotingManager.Status;
import voting.DistributedVotingManager.VotingListener;

public class RepresentativeVoteObserver implements Responser<String, Socket>, VotingListener{

	long epochNum;
	Hash currentHash;
	int level;
	RI ri;
	HexString votedRep;
	
	public RepresentativeVoteObserver(RI ri, long epochNum, String currentHash, HexString votedRep, int level) {
		super();
		this.epochNum = epochNum;
		this.currentHash = Hash.fromHashString(currentHash);
		this.level = level;
		this.ri = ri;
		this.votedRep = votedRep;
		Logger.debug("RepVoteObserver init");
	}

	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("tx");
	}

	@Override
	public void accept(String response, Socket socket) {
		
		String[] tokens = response.split(" ");
		
		Logger.debug("RepVoteObserver trigger");
		
		ShardingLevel l = ri.getShardingLevel(level);
		
		// ! tx hash shardId epochNum pkRep signature (+ PoW)
		// tx shardId serializedTx
		
		if(tokens[1].equals(l.id + "")){// && parsedEpochNum == epochNum){ //Same Shard Id && same EpochNum

			/*HexString rep = HexString.fromHashString(tokens[4]);
			HexString signature = HexString.fromHashString(tokens[5]);
			Hash hash = Hash.fromHashString(tokens[1]);
			boolean valid = CryptoUtil.validateSignature(signature, CryptoUtil.publicKeyFromString(rep.getHashString()), hash.getHash());
			*/
			
			Logger.warn("ShardTransaction recieved for Epoch " + epochNum);
			
			String tx = response.replaceAll(tokens[0] + " " + tokens[1] + " ", "");
			ShardingLevelTransaction t = new ObjectSerializer().parseShardedTransaction(tx);
			
			Logger.warn("ShardTransaction parsed for Epoch " + epochNum);
			
			long parsedEpochNum = t.getEpoch();
			
			Hash hash = Hash.fromHashString(t.getData().toString());
			
			
			if(parsedEpochNum == epochNum){
				if(CryptoUtil.validateSignature(t.getSignature(), CryptoUtil.publicKeyFromString(t.getSender().getHashString()), t.getTxId().getHash())){
					if(t.getSender().equals(votedRep)){
						
						
						if(currentHash.equals(hash)){
							
							//TODO TxInserter pendant
							
							l.addTransaction(t); 
							if(!l.getResponsers().removeResponser(this)) {
								Logger.warn("Removing of Responser not functional");
							}
							
						}else{
							challenge(hash, parsedEpochNum);
						}
						
					}else{
						challenge(hash, parsedEpochNum);
					}
				}//No else because no one will accept the message
			}
			
		}
		
	}
	
	public void challenge(Hash wrongHash, long epochNum){

		Logger.warn("Challenge for Epoch " + epochNum);
		
		DistributedVotingManager manager = ri.getShardingLevel(level).getVotingManager();
		
		String voteSub = "challenge-" + epochNum;
		
		manager.addVote(voteSub, wrongHash.getHashString(), new DistributedVote(ri.getPublicKey(), true));
		manager.addListener(new RepresentativChallengeVoteListener(voteSub, wrongHash.getHashString(), ri.getShardingLevel(level)));
		
	}

	@Override
	public void onVote(DistributedVoteSubject subject, String voteCat, DistributedVote vote, boolean newVote) {
		
		String subjectStr = "rep-" + epochNum;
		
		if(subject.getSubject().equals(subjectStr)){
			
			if(voteCat.equals(votedRep.getHashString())){
				
				if(votedRep.equals(ri.getPublicKey())){
					
					DistributedVotingManager manager;
					
					if(level - 1 > 0){
						manager = ri.getShardingLevel(level - 1).getVotingManager();
					}else{
						manager = ri.votingManager;
					}
					
					Status status = manager.getStatus(subject.getSubject(), voteCat, new BaseEpochListener.FiftyPercentValidator());
					
					
					if(status == Status.ACCEPTED){
						RepresentativeHashCommitter committer = new RepresentativeHashCommitter(level, ri, (int)epochNum);
						committer.commit(currentHash.getHashString());
					}
					
				}
				
			}
			
		}
		
	}
	
}
