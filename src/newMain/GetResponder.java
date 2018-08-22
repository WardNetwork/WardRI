package newMain;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.rpanic.Responser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Hash;
import network.ObjectSerializer;

public class GetResponder implements Responser<String, Socket> {

	Logger log = LoggerFactory.getLogger(GetResponder.class);
	RI ri;
	
	public GetResponder(RI ri) {
		super();
		this.ri = ri;
	}

	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("get");
	}

	@Override
	public void accept(String response, Socket socket) {
		String[] tokens = response.split(" ");
		
		if(tokens.length < 3){
			log.debug("Request " + response + " not valid!");
			return;
		}
		
		String type = tokens[1];

		try{
			
			OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
		
			switch(type){
				case "tx":
					
					getTx(tokens, writer);
					
					break;
				default: 
					log.debug("Type " + type + " not valid!");
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void getTx(String[] tokens, OutputStreamWriter writer) throws IOException{
		
		Hash hash = Hash.fromHashString(tokens[2]);
		
		Transaction t = ri.getDAG().findTransaction(hash);
		
		String serialized = new ObjectSerializer().serialize(t);
		
		writer.write(serialized + ";");
		
	}

}
