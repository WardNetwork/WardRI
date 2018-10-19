package sharded;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.rpanic.Responser;

import network.ObjectSerializer;
import newMain.GenericDAG;
import newMain.RI;

public class ShardingLevelSyncResponder implements Responser<String, Socket> {

	RI ri;
	
	public ShardingLevelSyncResponder(RI ri) {
		super();
		this.ri = ri;
	}

	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("sync");
	}

	@Override
	public void accept(String response, Socket socket) {
		String[] token = response.split(" ");
		if(token.length > 1){
			
			long shardId = Long.parseLong(token[1]);
			
			ShardingLevel level = ri.getShardingLevels().stream().filter(x -> x.id == shardId).findFirst().orElse(null);
			
			ShardingLevelDAG sldag = (ShardingLevelDAG) level.insertables.stream().filter(x -> x.getClass().getName().equals(ShardingLevelDAG.class.getName())).findFirst().orElse(null);

			GenericDAG<ShardingLevelTransaction> dag = sldag.getGenericDAG();
			
			ObjectSerializer s = new ObjectSerializer();
			
			List<String> strings = new ArrayList<>();
			
			for(ShardingLevelTransaction t : dag.getTransactionList()){
				
				String serialized = s.serialize(t);
				strings.add(serialized);
				
			}
			
			String res = "sync ";
			res += String.join(",", strings) + " ;";
			
			try{
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				w.write(res);
				w.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}
