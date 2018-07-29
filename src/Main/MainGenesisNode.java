// 
// Decompiled by Procyon v0.5.30
// 

package Main;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.rpanic.ListenerThread;
import org.rpanic.NeighborPool;
import org.rpanic.NeighborRequestReponse;
import org.rpanic.TCPNeighbor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Interfaces.LocalTangleInterface;
import Interfaces.NetworkTangleInterface;
import Interfaces.TangleInterface;
import Interfaces.TangleInterfaceDistributor;
import conf.ArgsConfigLoader;
import conf.Configuration;
import keys.KeyStore;
import model.HexString;
import model.TangleTransaction;
import network.NeighborRequestResponseBuilder;

public class MainGenesisNode
{
    public static final String startReciever = "3032301006072a8648ce3d020106052b81040006031e00044bcf8446fcd64238cfa5cbb10c5c12d2d2a8a0af7831276909542e56";
    
    private static final Logger log = LoggerFactory.getLogger(MainGenesisNode.class);
    
    public static void main(final String[] args) throws Exception {
    	
        final Configuration conf = new Configuration();
        new ArgsConfigLoader().loadArgsInConfig(args, conf);
        final int selfPortI = conf.getInt("selfport");
        log.info("Starting GenesisNode on Port " + selfPortI);
        final TCPNeighbor selfN = new TCPNeighbor(InetAddress.getByName(conf.getString("self")));
        selfN.setPort(selfPortI);
        
        //Keys
        //KeyStore.PATH = KeyStore.PATH + conf.getInt("selfport");
        if (conf.getHexString(Configuration.PRIVATEKEY) != null && conf.getHexString(Configuration.PUBLICKEY) != null) {
            KeyStore.importNewKeypair(conf.getHexString(Configuration.PUBLICKEY).getHashString(), conf.getHexString(Configuration.PRIVATEKEY).getHashString());
        }
        else {
            if (conf.getHexString(Configuration.PRIVATEKEY) == null && conf.getHexString(Configuration.PUBLICKEY) == null) {
                conf.put(Configuration.PRIVATEKEY, KeyStore.getPrivateString());
                conf.put(Configuration.PUBLICKEY, KeyStore.getPublicString());
            }else {
            	log.error("You can´t input only one part of the key");
            }
        }
        
        final Tangle tangle = new Tangle(conf);
        t = tangle;
        final TangleVisualizer visualizer = new TangleVisualizer(tangle);
        final NeighborPool pool = new NeighborPool(null, selfN, selfN.getPort());
        
    	TangleInterfaceDistributor distr = new TangleInterfaceDistributor(tangle);
        distr.addTangleInterface(new LocalTangleInterface(tangle));
        distr.addTangleInterface(new NetworkTangleInterface(tangle, pool));
        
        TransactionIntegrater integrater = new TransactionIntegrater(tangle, distr);
        
        final NeighborRequestReponse responser = NeighborRequestResponseBuilder.buildNewDefault(tangle, integrater, pool);
        ListenerThread.startListeningThreadTcp(selfN.getPort(), responser);
        pool.init();
        log.info("Genesisnode successfully initialized");
        log.info("Seeding...");
        genesisTranscation(tangle, pool, HexString.fromHashString("3032301006072a8648ce3d020106052b81040006031e00044bcf8446fcd64238cfa5cbb10c5c12d2d2a8a0af7831276909542e56"), conf.getHexString("publickey"));
        CommandLineWaiter.startCommandLineInput(tangle, distr, visualizer, pool); //TODO DEBUG for SPeedtest
    }
    
    public static void genesisTranscation(final Tangle tangle, final NeighborPool pool, final HexString sender, final HexString reciever) {
        final TangleInterfaceDistributor distributor = new TangleInterfaceDistributor(tangle, new TangleInterface[] { new LocalTangleInterface(tangle), new NetworkTangleInterface(tangle, pool) });
//        
//    	List<TangleInterface> interfs = distributor.getInterfaces();
//    	TangleInterface localInterf = interfs.stream().filter(x -> x.getClass().getName().equals(LocalTangleInterface.class)).collect(Collectors.toList()).get(0);
//    	interfs.remove(localInterf);
        
        Transaction genesis = new Transaction(sender, reciever, 10000000);
        genesis.doPoW();
        genesis.sign(KeyStore.getPrivateKey(), KeyStore.getPublicKey());
        distributor.addTranscation(genesis);
        
        TangleTransaction genesisT = tangle.getGenesisTransaction();
        directZeroTxs(tangle, genesis, genesisT, distributor, sender, reciever);
        seed(tangle, distributor, sender, reciever);
        
//    	interfs.add(localInterf);
    	
        log.info("Seeding complete");
    }
    
    private static void directZeroTxs(Tangle tangle, Transaction genesis, TangleTransaction genesisT, TangleInterfaceDistributor distributor, HexString sender, HexString reciever) {

    	for (int i = 0; i < 4; ++i) {
            Transaction t = new Transaction(reciever, sender, 1);
            t.getConfirmed().add(genesis.getTxHash());
            
            t.doPoW();
            t.sign(KeyStore.getPrivateKey(), KeyStore.getPublicKey());
            
            try{
            	genesis.addConfimationNodeReference(t.getTxHash().getHashString());
            }catch(Error e){
            	
            }
            
//            TangleTransaction tt = tangle.addOwnTransaction(t);
//            tt.getConfirmed().clear();
//            tt.getConfirmed().add(genesisT);
//            genesisT.getNodesWhichConfirmedMe().add(tt);
//            tangle.transactions.put(t.getTxHash().getHashString(), t);
//            tangle.confirmTransaction(t, genesis);
//            tangle.currentLedgerState.addTransaction(t);;
//            TangleAlgorithms.addTransactionToCache(t);
//            t.doPoW();
            
            distributor.addTranscation(t);
            try {
                Thread.sleep(1L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    	
    }
    
    private static void seed(Tangle tangle, TangleInterfaceDistributor distributor, HexString sender, HexString reciever){
    	
    	int count = 200;
    	TransactionIssuer issuer = new TransactionIssuer(tangle, distributor, KeyStore.getPrivateKey(), KeyStore.getPublicKey());
    	
    	for( ; count > 0 ; count--){
    		Transaction t = new Transaction(reciever, sender, 1);
    		issuer.issue(t);
    	}
    	
    }
    
    public static Tangle t; //TODO DEV
}
