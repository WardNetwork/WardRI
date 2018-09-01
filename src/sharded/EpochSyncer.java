package sharded;

import org.rpanic.TCPNeighbor;

import voting.Epoch;

public class EpochSyncer {

	public static Epoch syncAndCreate(TCPNeighbor n){
		
		String res = n.send("epochreq");
		if(res.startsWith("epochres ")){
			String[] arr = res.split(" ");
			Long l = Long.parseLong(arr[1]);
			Epoch e = new Epoch(l, Epoch.DEFAULT_DURATION);
			return e;
		}
		return null;
		
	}
	
}
