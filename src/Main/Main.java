// 
// Decompiled by Procyon v0.5.30
// 

package Main;

import java.io.File;
import java.net.InetAddress;

import org.rpanic.ListenerThread;
import org.rpanic.NeighborPool;
import org.rpanic.NeighborRequestReponse;
import org.rpanic.TCPNeighbor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Interfaces.LocalTangleInterface;
import Interfaces.NetworkTangleInterface;
import Interfaces.TangleInterfaceDistributor;
import conf.ArgsConfigLoader;
import conf.Configuration;
import conf.FileConfigLoader;
import database.DatabaseTangleInterface;
import keys.KeyStore;
import network.NeighborRequestResponseBuilder;

public class Main
{
    public static final String DEFAULT_CONF_FILE = "panicoin.conf";
    
    private static final Logger log = LoggerFactory.getLogger(MainGenesisNode.class);
    
    public static void main(final String[] args) throws Exception {
    	
        Configuration conf = new Configuration();
        
        new ArgsConfigLoader().loadArgsInConfig(args, conf);
        
        String confFilePath = conf.getString("configFile");
        if (confFilePath == null && new File(DEFAULT_CONF_FILE).exists()) {
            confFilePath = DEFAULT_CONF_FILE;
        }
        if (confFilePath != null) {
            new FileConfigLoader().loadInConfig(new File(confFilePath), conf);
        }
        log.info("Starting Node on Port " + conf.getInt(Configuration.SELFPORT));
        
        final TCPNeighbor entry = new TCPNeighbor(InetAddress.getByName(conf.getString(Configuration.NEIGHBOR)));
        entry.setPort(conf.getInt(Configuration.PORT));
        final TCPNeighbor selfN = new TCPNeighbor(InetAddress.getByName(conf.getString(Configuration.SELF)));
        selfN.setPort(conf.getInt(Configuration.SELFPORT));
        
        
        KeyStore.PATH = KeyStore.PATH + "/" + conf.getInt(Configuration.SELFPORT);
        if (conf.getHexString(Configuration.PRIVATEKEY) != null && conf.getHexString(Configuration.PUBLICKEY) != null) {
            KeyStore.importNewKeypair(conf.getHexString(Configuration.PUBLICKEY).getHashString(), conf.getHexString(Configuration.PRIVATEKEY).getHashString());
        }
        else {
            if (conf.getHexString(Configuration.PRIVATEKEY) == null && conf.getHexString(Configuration.PUBLICKEY) == null) {
                conf.put(Configuration.PRIVATEKEY, KeyStore.getPrivateString());
                conf.put(Configuration.PUBLICKEY, KeyStore.getPublicString());
            }else {
            	log.error("You can´t input only one part of the key");
            	//TODO Public key aus Privatekey erzeugen
            }
        }
        
        
        Tangle tangle = new Tangle(conf);
        TangleVisualizer visualizer = new TangleVisualizer(tangle);
        NeighborPool pool = new NeighborPool(entry, selfN, selfN.getPort());
        

    	TangleInterfaceDistributor distr = new TangleInterfaceDistributor(tangle);
        distr.addTangleInterface(new LocalTangleInterface(tangle));
        distr.addTangleInterface(new NetworkTangleInterface(tangle, pool));
        
        TransactionIntegrater integrater = new TransactionIntegrater(tangle, distr);
        
        NeighborRequestReponse responser = NeighborRequestResponseBuilder.buildNewDefault(tangle, integrater, pool);
        ListenerThread.startListeningThreadTcp(selfN.getPort(), responser);
        pool.init();
        
        Thread.sleep(5000L);
        
        TangleInterfaceDistributor syncDistributor = new TangleInterfaceDistributor(tangle, new LocalTangleInterface(tangle), new DatabaseTangleInterface());
        
        new TangleSynchronizer(tangle, syncDistributor, entry, pool).synchronize();
        CommandLineWaiter.startCommandLineInput(tangle, distr, visualizer, pool);
    }
}
