package sharded;

import org.rpanic.TCPNeighbor;

import voting.Epoch;

public class EpochSyncer {

	public static Epoch syncAndCreate(TCPNeighbor n){
		
		String res = n.send("epochreq");
		System.out.println("Response: " + res);
		if(res.startsWith("epochres ")){
			String[] arr = res.split(" ");
			Long start = Long.parseLong(arr[1]);
			Epoch e = new Epoch(start, Epoch.DEFAULT_DURATION);
			return e;
		}
		return null;
		
	}
	
}
