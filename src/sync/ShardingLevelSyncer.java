package sync;

import java.util.ArrayList;
import java.util.List;

import org.rpanic.GroupedNeighborPool;
import org.rpanic.ListenerThread;
import org.rpanic.NeighborRequestReponse;
import org.rpanic.TCPNeighbor;

import Interfaces.ShardedDAGInsertable;
import network.ObjectSerializer;
import newMain.RI;
import sharded.EpochSyncResponder;
import sharded.ShardingLevel;
import sharded.ShardingLevelDAG;
import sharded.ShardingLevelTransaction;
import voting.DistributedVotingResponser;

public class ShardingLevelSyncer {

	public static List<ShardingLevel> syncShardingLevels(RI ri, TCPNeighbor n, TCPNeighbor self){
		
		System.out.println("syncShardingLevels Port: " + n.getPort());
		
		String res = n.send("howmanylevels");
		
		System.out.println("Recieved:" + res);
		if(res.startsWith("dnn")){
			res = n.send("howmanylevels");
			System.out.println("Recieved2:" + res);
		}
		
		if(res.startsWith("levels ")){
			String[] tokens = res.split(" ");
			int levels = Integer.parseInt(tokens[1]);
			
			if(levels >= 0 && tokens.length >= levels){
				
				List<ShardingLevel> list = new ArrayList<>(levels);
				
				for(int i = 1 ; i <= levels ; i++){ // 1 weil 0 ausgelassen wird
					
					TCPNeighbor n2 = new TCPNeighbor(n.getAddress());
					n2.setPort(n.getPort() + i);
					
					GroupedNeighborPool pool = new GroupedNeighborPool(n2, self, self.getPort() + i, i); //TODO Wegen Ports noch überlegen
					
					pool.init();
					
					pool.broadcast("testbroadcast");
					
					List<ShardedDAGInsertable> insertables = new ArrayList<>();
					
					insertables.add(new ShardingLevelDAG());
					
					ShardingLevel l = new ShardingLevel(Long.parseLong(tokens[i]), ri, insertables, i, pool); //TODO Responder
					
					list.add(l);
					
					syncLevel(l, n);
					
					addResponsers(ri, l, pool);
					
				}
				
				System.out.println("Synchronized " + list.size() + " Shardinglevels");
				
				return list;
			}
		}
		return null;
	}
	
	public static void addResponsers(RI ri, ShardingLevel level, GroupedNeighborPool pool){

    	NeighborRequestReponse response = level.getResponsers();
		
    	response.addResponser(new EpochSyncResponder(ri.getEpoch()));
    	response.addResponser(new DistributedVotingResponser(ri.getDAG(), level.getVotingManager()));
		
        ListenerThread.startListeningThreadTcp(pool.getPort(), response);
	}
	
	private static boolean syncLevel(ShardingLevel level, TCPNeighbor n){
		
		String s = n.send("sync " + level.id);
		
		ObjectSerializer serializer = new ObjectSerializer();
		
		if(s.startsWith("sync ")){
			
			s = s.substring(5); //Remove sync
			String[] txs = s.split(",");
			for(String tx : txs){
				ShardingLevelTransaction t = serializer.parseShardedTransaction(tx);
				
				level.addTransaction(t);
				
			}
			
			return true;
			
		}
		
		return false;
	}
	
}
