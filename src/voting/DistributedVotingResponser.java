package voting;

import java.net.Socket;

import org.rpanic.Responser;

import model.HexString;
import newMain.CryptoUtil;
import newMain.DAG;
import voting.DistributedVoting.DistributedVote;

public class DistributedVotingResponser implements Responser<String, Socket> {

	DAG dag;
	
	DistributedVotingManager manager = new DistributedVotingManager(dag);
	
	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("voted");
	}

	@Override
	public void accept(String response, Socket socket) {
		
		String[] tokens = response.split(" ");
		//Request syntax: "voted voteSub voteCat true|false pubkey signature"
		
		String pubKey = tokens[4];
		HexString signature = HexString.fromHashString(tokens[5]);
		String signatureData = tokens[1] + " " + tokens[2] + " " + tokens[3] + " " + tokens[4];
		
		boolean valid = CryptoUtil.validateSignature(signature, CryptoUtil.publicKeyFromString(pubKey), signatureData.getBytes());
			
		if(valid){
			DistributedVote vote = new DistributedVote(HexString.fromHashString(pubKey), dag, Boolean.parseBoolean(tokens[3]));
			manager.addVote(tokens[1], tokens[2], vote);
		}
		
	}
	

}
