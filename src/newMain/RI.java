package newMain;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rpanic.ExternalAddress;
import org.rpanic.GroupedNeighborPool;
import org.rpanic.ListenerThread;
import org.rpanic.NeighborRequestReponse;
import org.rpanic.TCPNeighbor;

import Interfaces.DAGInsertable;
import Interfaces.NetworkDAG;
import conf.Configuration;
import database.DatabaseDAG;
import keys.KeyStore;
import model.HexString;
import network.TangleSyncResponser;

public class RI {

	Configuration conf;
	DAG dag;
	DatabaseDAG dbdag;
	NetworkDAG nwdag;
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

		initNetwork();
		initKeys();
		
		dag = new DAG();
		//dbdag = new DatabaseDAG();
		nwdag = new NetworkDAG(shardedPool); //STEHENGEBLIEBEN AM NETWORK SHIT
		
		tangleInterfaces.addAll(Arrays.asList(dag, nwdag/*, dbdag*/));
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
			String confEntry = conf.getString(Configuration.NEIGHBOR);
			if(confEntry.contains(":")){
				try {
					entry = new TCPNeighbor(InetAddress.getByName(confEntry.split(":")[0]));
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return;
				}
		        entry.setPort(Integer.parseInt(confEntry.split(":")[1]));
			}
		}
		
		InetAddress selfAdd = conf.getInetAddress(Configuration.SELF);
		if(selfAdd == null){
			selfAdd = ExternalAddress.getExternalAddress();
		}
        TCPNeighbor selfN = new TCPNeighbor(conf.getInetAddress(Configuration.SELF));
        selfN.setPort(conf.getInt(Configuration.SELFPORT));
        
        GroupedNeighborPool shardPool = new GroupedNeighborPool(entry, selfN, selfN.getPort(), "shard");
        //GroupedNeighborPool rootPool = new GroupedNeighborPool(entry, selfN, selfN.getPort(), "root");

        NeighborRequestReponse response = new NeighborRequestReponse(shardPool);
		
		response.addResponser(new TangleSyncResponser(this));
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
		//TODO evtl. imutable Liste zurückgeben und addInsertable() hinzufügen
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
