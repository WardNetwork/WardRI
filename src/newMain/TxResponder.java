package newMain;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.rpanic.Responser;

import model.Hash;
import model.HexString;
import network.ObjectSerializer;

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
        
        Transaction t = new ObjectSerializer().parseTransaction(response);
        
        //Give it to the TxInserter
        new TxInserter(ri).insert(t);
        
	}

}
