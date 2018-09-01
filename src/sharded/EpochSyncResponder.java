package sharded;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.rpanic.Responser;

import voting.Epoch;

public class EpochSyncResponder implements Responser<String, Socket>{

	private Epoch e;
	public EpochSyncResponder(Epoch e) {
		this.e = e;
	}
	
	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("epochreq");
	}

	@Override
	public void accept(String response, Socket socket) {
		
		try(BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))){
			
			String res = "epochres " + e.getBeginningTime();
			
			w.write(res + " ;");
			w.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
