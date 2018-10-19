package newMain;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
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
import sharded.BaseEpochListener;
import sharded.BaseVotingWeighterBuilder;
import sharded.EpochSyncResponder;
import sharded.EpochSyncer;
import sharded.ShardingLevel;
import sharded.ShardingLevelDAG;
import sharded.ShardingLevelInfoSyncResponder;
import sharded.ShardingLevelSyncResponder;
import sync.LocalDAGSynchronizer;
import sync.ShardingLevelSyncer;
import voting.DistributedVotingManager;
import voting.DistributedVotingResponser;
import voting.Epoch;

public class RI {

	Configuration conf;
	DAG dag;
	DatabaseDAG dbdag;
	NetworkDAG nwdag;
	List<DAGInsertable> tangleInterfaces;
	List<ShardingLevel> levels;
	Epoch epoch;
	public DistributedVotingManager votingManager;
	
	public GroupedNeighborPool shardedPool;
	boolean isGenesis = false;
	NeighborRequestReponse response;
	TCPNeighbor entry;
	TCPNeighbor self;
	
	public RI(){
		tangleInterfaces = new ArrayList<>();
	}
	
	public RI(boolean isGenesis){
		this();
		this.isGenesis = isGenesis;
	}
	
	public void init(Configuration conf){
		Logger.getConfiguration()
			.level(Level.DEBUG)
			.formatPattern("{date:mm:ss.SSS} {class_name}: {message}")
			.activate();
		
		this.conf = conf;
		
		initNetwork();
		initKeys();
		
		dag = new DAG();
		//dbdag = new DatabaseDAG();
		nwdag = new NetworkDAG(shardedPool); //STEHENGEBLIEBEN AM NETWORK SHIT
		
		tangleInterfaces.addAll(Arrays.asList(dag, nwdag/*, dbdag*/));
		
		synchronize(entry, self);
		
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
        
		this.votingManager = new DistributedVotingManager(new BaseVotingWeighterBuilder(this));
		
        GroupedNeighborPool shardPool = new GroupedNeighborPool(entry, selfN, selfN.getPort(), 1);
        //GroupedNeighborPool rootPool = new GroupedNeighborPool(entry, selfN, selfN.getPort(), "root");

        response = new NeighborRequestReponse(shardPool);
		
		response.addResponser(new TangleSyncResponser(this));
		response.addResponser(new TxResponder(this));
		response.addResponser(new GetResponder(this));
		response.addResponser(new DistributedVotingResponser(dag, votingManager));
    	response.addResponser(new ShardingLevelInfoSyncResponder(this));
    	response.addResponser(new ShardingLevelSyncResponder(this));
		//TODO response.addResponser(new LedgerResponser(tangle));
		
        ListenerThread.startListeningThreadTcp(selfN.getPort(), response);
        shardPool.init();
        
        try {
			Thread.sleep(1L);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        
        this.entry = entry;
        this.self = selfN;
        
        this.shardedPool = shardPool;
        
	}
	
	
	public void synchronize(TCPNeighbor entry, TCPNeighbor selfN){
        
        if(!isGenesis){
        	List<ShardingLevel> levels = ShardingLevelSyncer.syncShardingLevels(this, entry, selfN);
        	this.levels = levels;
        	
            new LocalDAGSynchronizer(this, entry, this.getShardedPool()).synchronize();
        	
			Epoch e = EpochSyncer.syncAndCreate(entry);
			e.start();
			this.epoch = e;
        	
        }else{
        	
        	//System.out.println("Entrynode Genesis: " + entry.getAddress().getHostAddress() + ":" + entry.getPort());
        	System.out.println("Entrynode Genesis: " + (entry == null));
        	
        	this.levels = new ArrayList<>();
        	TCPNeighbor self2 = new TCPNeighbor(selfN.getAddress());
        	self2.setPort(selfN.getPort()+1);
        	GroupedNeighborPool pool = new GroupedNeighborPool(entry, self2, selfN.getPort() + 1, 1);
        	pool.init();
        	ShardingLevel level = new ShardingLevel(1, this, Arrays.asList(new ShardingLevelDAG()), 1, pool);
        	ShardingLevelSyncer.addResponsers(this, level, pool);
        	this.levels.add(level);
        	
        	this.epoch = new Epoch(System.currentTimeMillis(), Epoch.DEFAULT_DURATION);
        	this.epoch.start();
        }
    	response.addResponser(new EpochSyncResponder(getEpoch()));
        
        this.epoch.addListener(new BaseEpochListener(this));
		
	}
	
	public List<DAGInsertable> getInsertables(){
		return Collections.unmodifiableList(tangleInterfaces);
	}
	
	public boolean addInsertable(DAGInsertable insertable){
		if(!tangleInterfaces.contains(insertable)){
			tangleInterfaces.add(insertable);
			return true;
		}else{
			return false;
		}
	}
	
	public ShardingLevel getShardingLevel(int level){
		
		if(level == 0){
			throw new UnsupportedOperationException("Retrieve Level 0 as Level 1!");
		}
		level -= 1;
		return levels.get(level);
	}
	
	public List<ShardingLevel> getShardingLevels(){
		return Collections.unmodifiableList(levels);
	}
	
	public boolean replaceShardingLevel(int level, ShardingLevel sl){
		
		level -= 1;
		if(levels.size() >= level){
			return levels.set(level, sl) != null;
		}
		return false;
		
	}
	
	public DAG getDAG(){
		return dag;
	}
	
	public GroupedNeighborPool getShardedPool(){
		return shardedPool;
	}
	
	public Epoch getEpoch() {
		return epoch;
	}

	public HexString getPublicKey(){
		return HexString.fromHashString(KeyStore.getPublicString());
	}
	
	public KeyPair getKeyPair(){
		return new KeyPair(KeyStore.getPublicKey(), KeyStore.getPrivateKey());
	}
}
