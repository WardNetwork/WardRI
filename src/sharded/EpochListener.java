package sharded;

import voting.Epoch;

public interface EpochListener {

	public void onEpochComplete(long epochNum, Epoch epoch);
	
}
