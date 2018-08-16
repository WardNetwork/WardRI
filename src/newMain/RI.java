package newMain;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rpanic.GroupedNeighborPool;
import org.rpanic.ListenerThread;
import org.rpanic.NeighborRequestReponse;
import org.rpanic.TCPNeighbor;

import Interfaces.DAGInsertable;
import conf.Configuration;
import database.DatabaseDAG;
import keys.KeyStore;
import model.HexString;
import network.LedgerResponser;
import network.TangleSyncResponser;

public class RI {

	Configuration conf;
	DAG dag;
	DatabaseDAG dbdag;
	List<DAGInsertable> tangleInterfaces;
	
	public GroupedNeighborPool shardedPool;
	boolean isGenesis = false;
	
	public RI(){
		tangleInterfaces = new ArrayList<>();
	}
	
	public RI(boolean isGenesis){
		this();
		this.isGenesis = isGenesis;
	}
	
	public void init(Configuration conf){
		this.conf = conf;
		
		dag = new DAG();
		//dbdag = new DatabaseDAG();
		//Network Relay (Weiterleitung)
		
		tangleInterfaces.addAll(Arrays.asList(dag/*, dbdag*/));

		initNetwork();
		initKeys();
	}
	
	public void initKeys(){
		
		//TODO to rework
		KeyStore.PATH = KeyStore.PATH + "/" + conf.getInt(Configuration.SELFPORT);
        if (conf.getHexString(Configuration.PRIVATEKEY) != null && conf.getHexString(Configuration.PUBLICKEY) != null) {
        	//if both Keys are entered
            KeyStore.importNewKeypair(conf.getHexString(Configuration.PUBLICKEY).getHashString(), conf.getHexString(Configuration.PRIVATEKEY).getHashString());
        }
        else {
            if (conf.getHexString(Configuration.PRIVATEKEY) == null && conf.getHexString(Configuration.PUBLICKEY) == null) {
                conf.put(Configuration.PRIVATEKEY, KeyStore.getPrivateString());
                conf.put(Configuration.PUBLICKEY, KeyStore.getPublicString());
            }else {
            	System.out.println("You can´t input only one part of the key");
            	//TODO Public key aus Privatekey erzeugen
            }
        }
	}
	
	public void initNetwork(){

		TCPNeighbor entry = null;
		if(!isGenesis){
			entry = new TCPNeighbor(conf.getInetAddress(Configuration.NEIGHBOR));
	        entry.setPort(conf.getInt(Configuration.PORT));
		}
		
        TCPNeighbor selfN = new TCPNeighbor(conf.getInetAddress(Configuration.SELF));
        selfN.setPort(conf.getInt(Configuration.SELFPORT));
        
        GroupedNeighborPool shardPool = new GroupedNeighborPool(entry, selfN, selfN.getPort(), "shard");
        //GroupedNeighborPool rootPool = new GroupedNeighborPool(entry, selfN, selfN.getPort(), "root");

        NeighborRequestReponse response = new NeighborRequestReponse(shardPool);
		
		//TODO response.addResponser(new TangleSyncResponser(tangle));
		response.addResponser(new TxResponder(this));
		//TODO response.addResponser(new LedgerResponser(tangle));
		
        ListenerThread.startListeningThreadTcp(selfN.getPort(), response);
        shardPool.init();
        
        this.shardedPool = shardPool;
        
	}
	
	public void synchronizeDAGState(){
		//TODO
	}
	
	public List<DAGInsertable> getInsertables(){
		return tangleInterfaces;
	}
	
	public DAG getDAG(){
		return dag;
	}
	
	public GroupedNeighborPool getShardedPool(){
		return shardedPool;
	}
	
	public HexString getPublicKey(){
		return HexString.fromHashString(KeyStore.getPublicString());
	}
	
	public KeyPair getKeyPair(){
		return new KeyPair(KeyStore.getPublicKey(), KeyStore.getPrivateKey());
	}
}
