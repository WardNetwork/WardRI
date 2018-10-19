package voting; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import model.HexString;
import voting.DistributedVoting.DistributedVote;
import voting.DistributedVoting.DistributedVotingValidator;

public class DistributedVotingManager {

	private List<DistributedVoteSubject> runningVotes = new ArrayList<>();
	private Map<String, Map<String, Status>> closedVotes = new HashMap<>();
	private VotingWeighterBuilder weighter;
	private volatile List<VotingListener> listeners = new CopyOnWriteArrayList<>();
	
	public void addVote(String voteSubject, String voteCat, DistributedVote vote){
		
		DistributedVoteSubject subject = null;
		boolean newVote = false;
		if(runningVotes.stream().anyMatch(x -> x.getSubject().equals(voteSubject))){
			Optional<DistributedVoteSubject> optional = runningVotes.stream().filter(x -> x.getSubject().equals(voteSubject)).findFirst();
			if(optional.isPresent()){
				subject = optional.get();
				newVote = false;
			}
		}else{
			if(!closedVotes.containsKey(voteCat)){
				int epochNum = Integer.parseInt(voteSubject.split("-")[1]);
				
				subject = new DistributedVoteSubject(voteSubject, weighter.fromEpoch(epochNum));
				runningVotes.add(subject);
				newVote = true;
			}
		}
		
		if(subject != null){
			subject.addVote(voteCat, vote);
			synchronized (listeners) {
				for(VotingListener listener : listeners){
					listener.onVote(subject, voteCat, vote, newVote);
				}
			}
		} //else: vote already expired
		
	}
	
	public Status getStatus(String subject, String voteCat, DistributedVotingValidator validator){
		
		Optional<DistributedVoteSubject> optional = runningVotes.stream().filter(x -> x.getSubject().equals(subject)).findFirst();
		if(optional.isPresent()){
			DistributedVoting voting = optional.get().getVoting(voteCat);
			if(voting != null){
				return voting.validateVoting(validator);
			}else{
				return statusOfClosed(subject, voteCat);
			}
		}else if(closedVotes.containsKey(voteCat)){
			return statusOfClosed(subject, voteCat);
		}
		return Status.UNDEFINED;
	}
	
	private Status statusOfClosed(String subject, String voteCat){
		
		if(closedVotes.keySet().contains(subject)){
			if(closedVotes.get(subject).containsKey(voteCat)){
				return closedVotes.get(subject).get(voteCat);
			}
		}
		return Status.UNDEFINED;
	}
	
	public void closeVote(String subject, String voteCat){
		
		//TODO implement
		
	}
	
	public DistributedVoteSubject getVoteSubject(String voteSubject){
		return runningVotes.stream().filter(x -> x.getSubject().equals(voteSubject)).findFirst().orElse(null);
	}
	
	public static enum Status{
		RUNNING, ACCEPTED, DECLINED, UNDEFINED
	}
	
	public DistributedVotingManager(VotingWeighterBuilder weighter){
		this.weighter = weighter;
	}
	
	public void addListener(VotingListener listener){
		if(!listeners.contains(listener)){
			listeners.add(listener);
		}
	}
	
	public boolean removeListener(VotingListener listener){
		return listeners.remove(listener);
	}
	
	public interface VotingListener{
		public void onVote(DistributedVoteSubject subject, String voteCat, DistributedVote vote, boolean newVote);
	}
	
	public interface VotingWeighter{
		
		public Map<HexString, Double> getVotingWeights(); 
		
		public VotingWeighter getClone(int epochNum);
		
		public int getEpoch();
		
	}
	
	public interface VotingWeighterBuilder{
		
		public VotingWeighter fromEpoch(int epochNum);
		
	}
	
}
