package Main;

import java.io.File;

import org.pmw.tinylog.Logger;

import conf.ArgsConfigLoader;
import conf.Configuration;
import conf.FileConfigLoader;
import keys.KeyStore;
import newMain.DAG;
import newMain.RI;

public class Main
{
    public static final String DEFAULT_CONF_FILE = "ward.conf";
    
    public static void main(final String[] args) throws Exception {
    	
        Configuration conf = new Configuration();
        
        new ArgsConfigLoader().loadArgsInConfig(args, conf);
        
        String confFilePath = conf.getString(Configuration.CONFIGFILE);
        if (confFilePath == null && new File(DEFAULT_CONF_FILE).exists()) {
            confFilePath = DEFAULT_CONF_FILE;
        }
        if (confFilePath != null) {
            new FileConfigLoader().loadInConfig(new File(confFilePath), conf);
        }
        Logger.info("Starting Node on Port " + conf.getInt(Configuration.SELFPORT));
        
        RI ri = new RI();
        ri.init(conf);
        
        DAG dag = ri.getDAG();
        Logger.info(KeyStore.getPublicString());
        Logger.info(KeyStore.getPrivateString());
        
        //Thread.sleep(5000L);
        
        CommandLineWaiter.startCommandLineInput(ri, ri.getShardedPool());
    }
}
