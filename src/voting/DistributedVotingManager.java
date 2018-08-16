package voting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import newMain.DAG;
import voting.DistributedVoting.DistributedVote;

public class DistributedVotingManager {

	private final List<DistributedVoting> runningVotes = new ArrayList<>();
	private Map<String, Status> closedVotes = new HashMap<>();
	private DAG dag = null;
	
	public void addVote(String voteCat, DistributedVote vote){
		
		DistributedVoting voting = null;
		if(runningVotes.stream().anyMatch(x -> x.id.equals(voteCat))){
			Optional<DistributedVoting> optional = runningVotes.stream().filter(x -> x.id.equals(voteCat)).findFirst();
			if(optional.isPresent()){
				voting = optional.get();
			}
		}else{
			if(!closedVotes.containsKey(voteCat)){
				voting = new DistributedVoting(voteCat, dag);
				runningVotes.add(voting);
			}
		}
		voting.addVote(vote);
		
	}
	
	public Status getStatus(String voteCat){
		
		Optional<DistributedVoting> optional = runningVotes.stream().filter(x -> x.id.equals(voteCat)).findFirst();
		if(optional.isPresent()){
			return optional.get().validateVoting();
		}else if(closedVotes.containsKey(voteCat)){
			return closedVotes.get(voteCat);
		}
		return null;
	}
	
	public DistributedVoting getVoting(String voteCat){
		return runningVotes.stream().filter(x -> x.id.equals(voteCat)).findFirst().get();
	}
	
	public static enum Status{
		RUNNING, ACCEPTED, DECLINED
	}
	
	private static DistributedVotingManager instance = new DistributedVotingManager();
	public static DistributedVotingManager getInstance(DAG tangle){
		if(instance.dag == null && tangle != null){
			instance.dag = tangle;
		}
		return instance;
	}
	
}
