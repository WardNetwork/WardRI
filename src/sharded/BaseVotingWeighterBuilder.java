package sharded;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import model.HexString;
import newMain.RI;
import newMain.Transaction;
import voting.DistributedVotingManager.VotingWeighter;
import voting.DistributedVotingManager.VotingWeighterBuilder;

public class BaseVotingWeighterBuilder implements VotingWeighterBuilder{

	RI ri;
	
	public BaseVotingWeighterBuilder(RI ri) {
		this.ri = ri;
	}

	@Override
	public VotingWeighter fromEpoch(int epochNum) {
		return new BaseVotingWeighter(ri, epochNum);
	}
	
	public class BaseVotingWeighter implements VotingWeighter{
	
		RI ri;
		int epochNum;
		
		private BaseVotingWeighter(RI ri, int epochNum) {
			super();
			this.ri = ri;
			this.epochNum = epochNum;
		}
	
		@Override
		public Map<HexString, Double> getVotingWeights() {
			
			List<Transaction> list = ri.getDAG().getTransactionsInEpoch(ri.getEpoch(), epochNum);
			
			Map<HexString, Double> map = new HashMap<>();
			
			Map<HexString, List<Transaction>> map2 = list.stream().collect(Collectors.groupingBy(x -> x.getSender()));
			
			for(Entry<HexString, List<Transaction>> entry : map2.entrySet()){
				
				double value = 1 + Math.log(entry.getValue().size());
				map.put(entry.getKey(), value);
				
			}
			
			return map;
		}

		@Override
		public VotingWeighter getClone(int epochNum) {
			return new BaseVotingWeighter(ri, epochNum);
		}

		@Override
		public int getEpoch() {
			return epochNum;
		}
		
	}

}
