package voting;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Main.Tangle;
import model.HexString;
import model.TangleTransaction;
import voting.DistributedVotingManager.Status;

public class DistributedVoting {
	
	String id;
	List<DistributedVote> list;
	double totalWeight;
	
	public DistributedVoting(String id, Tangle tangle){
		
		this.id = id;
		
		Map<HexString, List<TangleTransaction>> map = tangle.performantTransactions.stream()
				.collect(Collectors.groupingBy(TangleTransaction::getSender));
		
		for(List<TangleTransaction> values : map.values()){
			
			totalWeight += getVotingWeight(values);
			
		}
		
		if(totalWeight == 0d){
			throw new IllegalArgumentException("Tangle epoch has no Transactions");
		}
		
	}
	
	public void addVote(DistributedVote vote){
		list.add(vote);
	}
	
	public DistributedVotingManager.Status validateVoting(){
		
		if(totalWeight < votingPower(true) * 2d){
			return Status.ACCEPTED;
		}else if(totalWeight > votingPower(false) * 2d){
			return Status.DECLINED;
		}
		return Status.RUNNING;
		
	}
	
	private double votingPower(boolean vote){
		return list.stream()
				.filter(x -> x.vote == vote)
				.mapToDouble(DistributedVote::getVoteWeight)
				.reduce((x, y) -> x + y)
				.getAsDouble();
	}
	
	public static double getVotingWeight(List<TangleTransaction> list){
		
		double temp = list.stream().mapToDouble(TangleTransaction::getNodeWeight)
				.reduce((x, y) -> x + y)
				.getAsDouble();
		
		//TODO In Whitepaper it is log(num tx + 1)
		//
		
		return Math.log(temp);
		
	}
	
	public static class DistributedVote{
		
		HexString pubKey;
		double voteWeight;
		boolean vote;
		
		//TODO signature?
		public DistributedVote(HexString pubKey, double voteWeight, boolean vote){
			this.pubKey = pubKey;
			this.voteWeight = voteWeight;
			this.vote = vote;
		}
		
		public DistributedVote(HexString pubKey, Tangle tangle, boolean vote){
			double totalweight = DistributedVoting.getVotingWeight(
				tangle.performantTransactions
				.stream()
				.filter(tx -> tx.getSender().equals(pubKey))
				.collect(Collectors.toList())
			);
			
			this.pubKey = pubKey;
			this.voteWeight = totalweight;
			this.vote = vote;
		}
		
		public double getVoteWeight(){
			return voteWeight;
		}
		
		public boolean getVote(){
			return vote;
		}
	}
	
}
