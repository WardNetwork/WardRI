package newMain;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.rpanic.Responser;

import model.Hash;
import model.HexString;

public class TxResponder implements Responser<String, Socket>{

	RI ri;
	
	public TxResponder(RI ri){
		this.ri = ri;
	}
	
	@Override
	public boolean acceptable(String responseType) {
        return responseType.equals("tx");
	}

	@Override
	public void accept(String response, Socket socket) {

        if(response.startsWith("tx ")){
        	response = response.replaceAll("tx ", "");
        }
        
		String[] tokenized = response.split(" ");
        
        if(tokenized[2].startsWith("0x")){
        	System.out.println(response);  
        }
        
        //TODO Replace with ObjectSerializer.parse(response);
        
        HexString sender = HexString.fromHashString(tokenized[0]);
        HexString reciever = HexString.fromHashString(tokenized[1]);
        double value = Double.parseDouble(tokenized[2]);
        long createdTimestamp = Long.parseLong(tokenized[3]);
        
        String powSolution = tokenized[4];
        HexString signature = HexString.fromHashString(tokenized[5]);
        Set<TransactionReference> confirmed = new HashSet<>();
        for (int i = 6; i < tokenized.length; ++i) {
            String confirmation = tokenized[i];
            confirmed.add(new TransactionReference(Hash.fromHashString(confirmation)));
        }
        
        Transaction t = new Transaction(sender, reciever, value, null /* TODO */, createdTimestamp, powSolution, signature, confirmed);
        
        //Give it to the TxInserter
        new TxInserter(ri).insert(t);
        
	}

}
