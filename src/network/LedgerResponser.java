package network;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.rpanic.Responser;

import Main.Tangle;

public class LedgerResponser implements Responser<String, Socket>{

	Tangle tangle;
	
	public LedgerResponser(Tangle tangle) {
		this.tangle = tangle;
	}
	
	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("ledger");
	}

	@Override
	public void accept(String response, Socket socket) {
		
		String res = new ObjectSerializer().serialize(tangle.createLedger2().getMap());
		
		try {
			
			OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
			
			writer.write(res + " ;");
			
			writer.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
