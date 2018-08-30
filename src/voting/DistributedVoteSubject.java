package voting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import voting.DistributedVoting.DistributedVote;

public class DistributedVoteSubject {

	List<DistributedVoting> votings = new ArrayList<>();
	
	private String subject;

	public DistributedVoteSubject(String subject) {
		super();
		this.subject = subject;
	}
	
	public boolean addVote(String voteCat, DistributedVote vote){
		if(votings.stream().anyMatch(x -> x.id.equals(voteCat))){
			
			Optional<DistributedVoting> optional = votings.stream().filter(x -> x.id.equals(voteCat)).findFirst();
			if(optional.isPresent()){
				optional.get().addVote(vote);
				return true;
			}
			
		}else{
			addVoting(new DistributedVoting(voteCat, null)); //TODO DAG entfernen
			return addVote(voteCat, vote);
		}
		return false;
	}
	
	public DistributedVoting getVoting(String voteCat){
		return votings.stream().filter(x -> x.id.equals(voteCat)).findFirst().orElse(null);
	}
	
	public void addVoting(DistributedVoting voting){
		votings.add(voting);
	}
	
	public String getSubject(){
		return subject;
	}
	
}
