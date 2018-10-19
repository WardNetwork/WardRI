package representative;

import java.util.List;

import sharded.ShardingLevel;
import voting.DistributedVoteSubject;
import voting.DistributedVoting.DistributedVote;
import voting.DistributedVoting.DistributedVotingValidator;
import voting.DistributedVotingManager.Status;
import voting.DistributedVotingManager.VotingListener;

public class RepresentativChallengeVoteListener implements VotingListener{

	String subject, voteCat;
	ShardingLevel level;
	
	public RepresentativChallengeVoteListener(String subject, String voteCat, ShardingLevel level) {
		super();
		this.subject = subject;
		this.voteCat = voteCat;
		this.level = level;
	}

	@Override
	public void onVote(DistributedVoteSubject subject, String voteCat, DistributedVote vote, boolean newVote) {
		
		if(subject.equals(this.subject) && voteCat.equals(this.voteCat) && vote.getVote()){
			
			Status status = level.getVotingManager().getStatus(subject.getSubject(), voteCat, new RepresentativeChallengeVoteValidator());
			
			if(status == Status.ACCEPTED){
				
				//TODO
				
				level.getVotingManager().removeListener(this);
				
			}else if(status == Status.DECLINED){
				
				level.getVotingManager().removeListener(this);
				
			}
			
		}
		
	}
	
	private class RepresentativeChallengeVoteValidator implements DistributedVotingValidator{
		
		@Override
		public Status validateVoting(List<Double> positive, List<Double> negative,  double totalWeight) {
			double weight = positive.stream().reduce((x, y) -> x + y).orElse(0D);
			if(totalWeight * 0.4 <= weight){ //40% needed
				return Status.ACCEPTED;
			}else{
				double not = negative.stream().reduce((x, y) -> x + y).orElse(0D);
				if(totalWeight * 0.6 <= not){
					return Status.DECLINED;
				}else{
					return Status.RUNNING;
				}
			}
		}
		
	}

}
