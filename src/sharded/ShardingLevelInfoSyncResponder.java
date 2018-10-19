package sharded;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.rpanic.Responser;

import newMain.RI;

public class ShardingLevelInfoSyncResponder implements Responser<String, Socket>{

	RI ri;
	
	public ShardingLevelInfoSyncResponder(RI ri) {
		super();
		this.ri = ri;
	}

	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("howmanylevels");
	}

	@Override
	public void accept(String response, Socket socket) {
		System.out.println("Got it skrra");
		
		String res = "levels " + ri.getShardingLevels().size() + " ";
		
		for(ShardingLevel l : ri.getShardingLevels()){
			res += l.id + " ";
		}
		
		res += ";";
		try{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					
			bw.write(res);
			bw.flush();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}

}
