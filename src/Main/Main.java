package Main;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conf.ArgsConfigLoader;
import conf.Configuration;
import conf.FileConfigLoader;
import keys.KeyStore;
import newMain.DAG;
import newMain.RI;

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
        
        RI ri = new RI();
        ri.init(conf);
        
        DAG dag = ri.getDAG();
        TangleVisualizer visualizer = new TangleVisualizer(dag);
        log.info(KeyStore.getPublicString());
        log.info(KeyStore.getPrivateString());
        
        Thread.sleep(5000L);
        
        new TangleSynchronizer(ri, ri.getShardedPool().getRandomNeighbor(), ri.getShardedPool()).synchronize();
        CommandLineWaiter.startCommandLineInput(ri, visualizer, ri.getShardedPool());
    }
}
