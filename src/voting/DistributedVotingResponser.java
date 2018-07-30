package voting;

import java.net.Socket;

import org.rpanic.Responser;

import Main.Tangle;
import model.HexString;
import voting.DistributedVoting.DistributedVote;

public class DistributedVotingResponser implements Responser<String, Socket> {

	Tangle tangle;
	
	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("voted");
	}

	@Override
	public void accept(String response, Socket socket) {
		
		//TODO Check if reponse actually starts with "voted"
		
		String[] tokens = response.split(" ");
		//Request syntax: "voted voteCat true|false pubkey signature"
		
		HexString pubKey = HexString.fromHashString(tokens[3]);
		HexString signature = HexString.fromHashString(tokens[4]);
		
		//TODO validate Signature
			
		DistributedVote vote = new DistributedVote(pubKey, tangle, Boolean.parseBoolean(tokens[2]));
		DistributedVotingManager.getInstance(tangle).addVote(tokens[1], vote);
		
		
		
	}
	

}
