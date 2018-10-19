package Main;

import java.net.InetAddress;
import java.security.KeyPair;

import org.pmw.tinylog.Logger;
import org.rpanic.GroupedNeighborPool;
import org.rpanic.TCPNeighbor;

import Interfaces.DAGInsertable;
import conf.ArgsConfigLoader;
import conf.Configuration;
import model.HexString;
import newMain.CryptoUtil;
import newMain.DAG;
import newMain.RI;
import newMain.Transaction;
import newMain.TxCreator;
import newMain.TxInserter;

public class MainGenesisNode
{
    public static final String startReciever = "0x3032301006072a8648ce3d020106052b81040006031e00048ea17408f617101709e18abaebf773fa3f9722f3b67e6da6f357b3f6";
	public static final String startRecieverPK = "0x302c020100301006072a8648ce3d020106052b8104000604153013020101040eb2868bdae8cb85caf8e8a10cca42";
    
    public static void main(final String[] args) throws Exception {
    	
        final Configuration conf = new Configuration();
        
        new ArgsConfigLoader().loadArgsInConfig(args, conf);
        
        final int selfPortI = conf.getInt("selfport");
        Logger.info("Starting GenesisNode on Port " + selfPortI);
        final TCPNeighbor selfN = new TCPNeighbor(InetAddress.getByName(conf.getString("self")));
        selfN.setPort(selfPortI);
        
        RI ri = new RI(true);
        ri.init(conf);
        
        DAG dag = ri.getDAG();
        
        //Thread.sleep(5000L);
        
        Logger.info("Genesisnode successfully initialized");
        Logger.info("Seeding...");
        genesisTranscation(ri, ri.getShardedPool(), HexString.fromHashString(startReciever), conf.getHexString("publickey"));
        CommandLineWaiter.startCommandLineInput(ri, ri.getShardedPool()); //TODO DEBUG for SPeedtest
    }
    
    public static void genesisTranscation(RI ri, GroupedNeighborPool pool, HexString sender, HexString reciever) {
    	
    	TxCreator creator = new TxCreator(ri);
        
    	Transaction genesis = creator.create(reciever, 100000, null);
    	genesis.setSenderGenesis(sender);
    	//genesis.genesisRecalculateTxId();
    	
    	KeyPair keypair = CryptoUtil.keypairFromStrings(startReciever, startRecieverPK);
    	
    	HexString signature = CryptoUtil.sign(keypair.getPrivate(), keypair.getPublic(), genesis.getTxId().getHash());
    	genesis.signature = signature;
        
    	String TxId = CryptoUtil.hashSHA256(genesis.createHashString()).getHashString();
    	
    	Logger.debug(TxId + " !=? " + genesis.getTxId());
    	
    	Logger.debug("Signature Valid: " + CryptoUtil.validateSignature(signature, keypair.getPublic(), genesis.getTxId().getHash()));
    	
    	Transaction finalGenesis = genesis;
    	
    	for(DAGInsertable i : ri.getInsertables()){
    		i.addTransaction(finalGenesis);
    	}
		TangleAlgorithms.addTransactionToCache(genesis);
		
        directZeroTxs(ri, genesis, sender, creator);
        //seed(tangle, distributor, sender, reciever);
    	
        Logger.info("Seeding complete");
    }
    
    private static void directZeroTxs(RI ri, Transaction genesis, HexString reciever, TxCreator creator) {

    	TxInserter inserter = new TxInserter(ri);
    	
    	for (int i = 0; i < 4; ++i) {
            Transaction t = creator.create(reciever, 1, null);
            
            boolean success = inserter.insert(t);
            
            try {
                Thread.sleep(1L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    	
    }
    
    /*private static void seed(Tangle tangle, TangleInterfaceDistributor distributor, HexString sender, HexString reciever){
    	
    	int count = 200;
    	TransactionIssuer issuer = new TransactionIssuer(tangle, distributor, KeyStore.getPrivateKey(), KeyStore.getPublicKey());
    	
    	for( ; count > 0 ; count--){
    		Transaction t = new Transaction(reciever, sender, 1);
    		issuer.issue(t);
    	}
    	
    }*/
}
