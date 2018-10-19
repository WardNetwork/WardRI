package voting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.HexString;
import voting.DistributedVotingManager.Status;
import voting.DistributedVotingManager.VotingWeighter;

public class DistributedVoting {
	
	String id;
	List<DistributedVote> list;
	Map<HexString, Double> weights;
	double totalWeight = 0D;
	
	public DistributedVoting(String id, VotingWeighter weighter){
		
		this.id = id;
		
		Map<HexString, Double> weights = weighter.getVotingWeights();
		
		this.weights = weights;
		
		totalWeight = weights.values().stream().mapToDouble(x -> x).sum();
		
		if(totalWeight == 0d){
			this.weights = findNextVoteData(weighter);
			totalWeight = weights.values().stream().mapToDouble(x -> x).sum();
		}

		list = new ArrayList<>();
		
	}
	
	private Map<HexString, Double> findNextVoteData(VotingWeighter previous){
		
		int newEpoch = previous.getEpoch() - 1;
		VotingWeighter clone = previous.getClone(newEpoch);
		Map<HexString, Double> w = clone.getVotingWeights();
		if(!w.isEmpty()){
			return w;
		}else{
			return findNextVoteData(clone);
		}
		
	}
	
	public void addVote(DistributedVote vote){
		list.add(vote);
	}
	
	public DistributedVotingManager.Status validateVoting(DistributedVotingValidator val){
		
		List<Double> positive = weights
				.entrySet().stream()
				.filter(x -> list.stream().anyMatch(y -> y.getVote() && y.pubKey.equals(x.getKey())))
				.map(x -> x.getValue())
				.collect(Collectors.toList());
		
		List<Double> negative = weights
				.entrySet().stream()
				.filter(x -> list.stream().anyMatch(y -> !y.getVote() && y.pubKey.equals(x.getKey())))
				.map(x -> x.getValue())
				.collect(Collectors.toList());
		
		return val.validateVoting(positive, negative, totalWeight);
		
	}
	
	/*private double votingPower(boolean vote){
		return list.stream()
				.filter(x -> x.vote == vote)
				.mapToDouble(DistributedVote::getVoteWeight)
				.reduce((x, y) -> x + y)
				.getAsDouble();
	}*/
	
	/*public static double getVotingWeight(List<Transaction> list){
		
		double temp = list.stream().mapToDouble(Transaction::getNodePowWeight)
				.reduce((x, y) -> x + y)
				.getAsDouble();
		
		//TODO In Whitepaper it is log(num tx + 1)
		//
		
		return Math.log(temp);
		
	}*/
	
	public interface DistributedVotingValidator{
		
		public Status validateVoting(List<Double> positive, List<Double> negative, double totalWeight);
		
	}
	
	public static class DistributedVote{
		
		HexString pubKey;
		double voteWeight = -1D;
		boolean vote;
		
		//TODO signature?
		public DistributedVote(HexString pubKey, double voteWeight, boolean vote){
			this.pubKey = pubKey;
			this.voteWeight = voteWeight;
			this.vote = vote;
		}
		
		public DistributedVote(HexString pubKey, boolean vote){
			
			this.pubKey = pubKey;
			this.vote = vote;
		}
		
		public double getVoteWeight(){
			if(voteWeight == -1D){
				voteWeight = calculateVoteWeight();
			}
			return voteWeight;
		}
		
		private double calculateVoteWeight() {
			throw new UnsupportedOperationException();
		}

		public boolean getVote(){
			return vote;
		}
	}
	
}
