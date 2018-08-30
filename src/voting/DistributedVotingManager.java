package voting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import newMain.DAG;
import voting.DistributedVoting.DistributedVote;
import voting.DistributedVoting.DistributedVotingValidator;

public class DistributedVotingManager {

	private final List<DistributedVoteSubject> runningVotes = new ArrayList<>();
	private Map<String, Map<String, Status>> closedVotes = new HashMap<>();
	private DAG dag = null;
	private List<VotingListener> listeners = new ArrayList<>(2);
	
	public void addVote(String voteSubject, String voteCat, DistributedVote vote){
		
		DistributedVoteSubject subject = null;
		if(runningVotes.stream().anyMatch(x -> x.getSubject().equals(voteSubject))){
			Optional<DistributedVoteSubject> optional = runningVotes.stream().filter(x -> x.getSubject().equals(voteSubject)).findFirst();
			if(optional.isPresent()){
				subject = optional.get();
			}
		}else{
			if(!closedVotes.containsKey(voteCat)){
				subject = new DistributedVoteSubject(voteSubject);
				runningVotes.add(subject);
			}
		}
		if(subject != null){
			subject.addVote(voteCat, vote);
			for(VotingListener listener : listeners){
				listener.onVote(subject, voteCat, vote);
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
	
	public DistributedVotingManager(DAG dag){
		this.dag = dag;
	}
	
	public void addListener(VotingListener listener){
		if(!listeners.contains(listener)){
			listeners.add(listener);
		}
	}
	
	interface VotingListener{
		public void onVote(DistributedVoteSubject subject, String voteCat, DistributedVote vote);
	}
	
}
