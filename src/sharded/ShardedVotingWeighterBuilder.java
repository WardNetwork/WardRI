package sharded;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.HexString;
import voting.DistributedVotingManager.VotingWeighter;
import voting.DistributedVotingManager.VotingWeighterBuilder;

public class ShardedVotingWeighterBuilder implements VotingWeighterBuilder{

	ShardingLevel level;
	
	public ShardedVotingWeighterBuilder(ShardingLevel level) {
		super();
		this.level = level;
	}

	@Override
	public VotingWeighter fromEpoch(int epochNum) {
		return new ShardedVotingWeighter(level, epochNum);
	}

	public class ShardedVotingWeighter implements VotingWeighter{

		ShardingLevel level;
		int epochNum;
		
		public ShardedVotingWeighter(ShardingLevel level, int epochNum) {
			super();
			this.level = level;
			this.epochNum = epochNum;
		}


		@Override
		public Map<HexString, Double> getVotingWeights() {
			
			List<ShardingLevelTransaction> transactions = level.getLocalDAG().getGenericDAG().getTransactionsInEpoch(level.ri.getEpoch(), epochNum);
			
			Map<HexString, Double> map = transactions.stream().collect(Collectors.toMap(ShardingLevelTransaction::getSender, x -> 1D));
			
			return map;
			
		}

		@Override
		public VotingWeighter getClone(int epochNum) {
			return new ShardedVotingWeighter(level, epochNum);
		}

		@Override
		public int getEpoch() {
			return epochNum;
		}
		
	}
	
}
