package sharded;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.rpanic.GroupedNeighborPool;

import model.HexString;
import newMain.RI;
import voting.DistributedVoteSubject;
import voting.DistributedVoting.DistributedVote;
import voting.DistributedVoting.DistributedVotingValidator;
import voting.DistributedVotingManager;
import voting.DistributedVotingManager.Status;
import voting.DistributedVotingManager.VotingListener;

public class ShardSplitter implements VotingListener {

	RI ri;
	int level;
	int epochNum;
	ShardingLevel sl;
	String sub;
	List<String> cats;
	DistributedVotingManager manager;
	DistributedVotingValidator validator;
	List<HexString> senders;
	
	public ShardSplitter(RI ri, int level, int epochNum) {
		this.ri = ri;
		this.level = level;
		this.sl = ri.getShardingLevel(level);
		this.manager = sl.getVotingManager();
		this.validator = new ShardSplitterVoteValidator();
		this.epochNum = epochNum;
	}
	
	public void requestSplitting(){
		
		sub = "split-" + sl.id;
		
		//Get Distinction PK
		List<HexString> list = ri.getDAG().getTransactionsInEpoch(ri.getEpoch(), epochNum).stream()
				.map(x -> x.getSender()).distinct().collect(Collectors.toList());
		HexString distinction = list.get(list.size() / 2);
		this.senders = list;
		
		cats = new ArrayList<>();
		String cat = epochNum + "," + distinction.getHashString();
		cats.add(cat);
		
		manager.addVote(sub, cat, new DistributedVote(ri.getPublicKey(), true));
		manager.addListener(this);
		
		checkVote(cat);
		
	}
	
	@Override
	public void onVote(DistributedVoteSubject subject, String voteCat, DistributedVote vote, boolean newVote) {
		
		if(!newVote && vote.getVote() && subject.equals(sub)){
			if(cats.contains(voteCat)){
				
				checkVote(voteCat);
				
			}
		}else if(newVote && subject.equals(sub)){
			
			//Other distinction keys
			
			String[] arr = voteCat.split(",");
			int epochNum = Integer.parseInt(arr[0]);
			if(epochNum == this.epochNum){
				
				HexString distinction = HexString.fromHashString(arr[1]);
				int index = senders.indexOf(distinction);
				int diff = (senders.size() / 2) - index;
				if(diff <= (senders.size() * 0.05d)){
					
					cats.add(voteCat);
					manager.addVote(subject.getSubject(), voteCat, new DistributedVote(ri.getPublicKey(), true));
					checkVote(voteCat);
					
				}
				
			}
			
		}
	}
	
	public void checkVote(String cat){
		Status status = manager.getStatus(sub, cat, validator);
		if(status == Status.ACCEPTED){
			split(cat);
			manager.removeListener(this);
		}else if(status == Status.DECLINED){
			manager.removeListener(this);
		}
	}
	
	private void split(String cat) {
		
		//Start of splitting precedure
		
		HexString distinction = HexString.fromHashString(cat.split(",")[1]);
		
		//Filter Digits (should be only digits normally
		//String s = sl.id.chars().filter(x -> Character.isDigit(x)).map(x -> (Character)x).reduce((x, y) -> x + "" + y).orElse(0);
		long old = sl.id;
		long new1 = old << 1;
		long new2 = new1 + 1;
		
		long ownShard = isNodeInFirstHalf(distinction, ri.getPublicKey()) ? new1 : new2;
		
		GroupedNeighborPool pool = sl.getNeighborPool();
		pool.switchToNewShardId(ownShard);
		
		ShardingLevel newLevel = new ShardingLevel(ownShard, ri, sl.insertables, level, pool);
		
		ri.replaceShardingLevel(level, newLevel);
		
		//TODO
	}
	
	
	
	public static boolean isNodeInFirstHalf(HexString distinction, HexString node){
		
		return HexString.compare(node, distinction) <= 0 ? true : false;
		
	}

	protected class ShardSplitterVoteValidator implements DistributedVotingValidator{
		
		@Override
		public Status validateVoting(List<Double> positive, List<Double> negative, double totalWeight) {
			double weight = positive.stream().mapToDouble(x -> x).sum();
			if(totalWeight * 0.8 <= weight){ //80% needed
				return Status.ACCEPTED;
			}else{
				double not = negative.stream().mapToDouble(x -> x).sum();
				if(totalWeight * 0.2 <= not){
					return Status.DECLINED;
				}else{
					return Status.RUNNING;
				}
			}
		}
		
	}

}
