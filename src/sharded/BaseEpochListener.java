package sharded;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import model.Hash;
import model.HexString;
import newMain.CryptoUtil;
import newMain.RI;
import newMain.Transaction;
import representative.RepresentativeVoteObserver;
import voting.DistributedRandom;
import voting.DistributedVoteSubject;
import voting.DistributedVoting.DistributedVote;
import voting.DistributedVoting.DistributedVotingValidator;
import voting.DistributedVotingManager;
import voting.DistributedVotingManager.Status;
import voting.DistributedVotingManager.VotingListener;
import voting.Epoch;

public class BaseEpochListener implements EpochListener, VotingListener, Closeable{

	RI ri;
	String voteSubject;
	
	Map<HexString, Map<HexString, Double>> map = new HashMap<>();
	Map<HexString, Long> epochNums = new HashMap<>();
	DistributedVotingManager manager;
	
	public BaseEpochListener(RI ri) {
		this.ri = ri;
		this.manager = ri.votingManager;
	}
	
	@Override
	public void onEpochComplete(long epochNum, Epoch epoch) {

		//Preparations (Registering for Votes)
		voteSubject = "data-" + epochNum;
		manager.addListener(this);
		
		//Pack
		List<Transaction> list = ri.getDAG().getTransactionsInEpoch(epoch, epochNum);
		
		list.stream().sorted((x, y) -> {
			
			int c = Long.compare(x.getCreatedTimestamp(), y.getCreatedTimestamp());
			if(c == 0){
				c = HexString.compare(x.getTxId(), x.getTxId());
			}
			return c;
		});
		
		
		String hashData = list.stream().map(x -> x.getTxId().getHashString()).reduce((x, y) -> x + y).orElse("");
		Hash packed = CryptoUtil.hashSHA256(hashData);
		
		//Preelect Weightmap
		
		Map<HexString, Double> weights = electWeights(epoch, epochNum);
		
		map.put(packed, weights);
		epochNums.put(packed, epochNum);

		Logger.debug("BaseEpochListener: " + voteSubject + " added");
		//Vote
		manager.addVote(voteSubject, packed.getHashString(), new DistributedVote(ri.getPublicKey(), true));
		
		
	}
	
	private Map<HexString, Double> electWeights(Epoch epoch, long epochNum){
		
		List<Transaction> list = ri.getDAG().getTransactionsInEpoch(epoch, epochNum);
		
		Map<HexString, Double> weights = list
				.stream()
				.collect(Collectors.groupingBy(Transaction::getSender, Collectors.counting()))
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
		
		if(epochNum < 0){
			return null;
		}
		
		if(weights == null || weights.isEmpty()){
			
			return electWeights(epoch, epochNum - 1);
		}else{
			return weights;
		}
		
		
	}
	
	public boolean agreeToVote(Hash h){
		
		
		return false;
	}

	@Override
	public void onVote(DistributedVoteSubject subject, String voteCat, DistributedVote vote, boolean newVote) {
		
		if(newVote && subject.getSubject().equals(voteSubject) && vote.getVote()){
			
			//TODO
			
		}/*else */if(subject.getSubject().equals(voteSubject) && vote.getVote()){
			
			Status status = manager.getStatus(subject.getSubject(), voteCat, new FiftyPercentValidator()); //TODO validator ersetzen
			if(status == Status.ACCEPTED){
				electRep(voteCat);
			}
			
		}
	}
	
	public void electRep(String hash){
		
		HexString hashObj = HexString.fromHashString(hash);
		
		HexString rep = DistributedRandom.random(Hash.fromHashString(hash), map.get(hashObj));
		
		Logger.debug("Elected Rep: " + rep.toString());
		
		String voteSubject = "rep-" + epochNums.get(hashObj);

		RepresentativeVoteObserver observer = new RepresentativeVoteObserver(ri, epochNums.get(hashObj), hash, rep, 1);
		
		ri.getShardingLevel(1).response.addResponser(observer);
		manager.addListener(observer);
		
		manager.addVote(voteSubject, rep.getHashString(), new DistributedVote(ri.getPublicKey(), true));
		
		map.remove(hashObj);
		epochNums.remove(hashObj);
		
	}
	
	public static class FiftyPercentValidator implements DistributedVotingValidator{

		@Override
		public Status validateVoting(List<Double> positive, List<Double> negative, double totalWeight) {
			double pos = positive.stream().reduce(0D, (x, y) -> x + y);
			if(totalWeight * 0.5D <= pos){
				return Status.ACCEPTED;
			}else{
				double neg = negative.stream().reduce(0D, (x, y) -> x + y);
				if(totalWeight * 0.5D <= neg){
					return Status.DECLINED;
				}else{
					return Status.RUNNING;
				}
			}
		}
		
	}
	
	@Override
	public void close(){
		
		//ri.getShardingLevel(0).getVotingManager().removeListener(this);
		
		
	}

}
