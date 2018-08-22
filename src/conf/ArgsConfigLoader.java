package conf;

import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;
import com.sanityinc.jargs.CmdLineParser.OptionException;

public class ArgsConfigLoader {

	public void loadArgsInConfig(String[] args, Configuration conf){
		
		CmdLineParser parser = new CmdLineParser();

		Option<String> neighbor = parser.addStringOption('n', Configuration.NEIGHBOR);
		//Option<Integer> port = parser.addIntegerOption('p', Configuration.PORT);
		Option<String> self = parser.addStringOption('s', Configuration.SELF);
		Option<Integer> selfPort = parser.addIntegerOption(Configuration.SELFPORT);
		Option<String> publicKey = parser.addStringOption(Configuration.PUBLICKEY);
		Option<String> privateKey = parser.addStringOption(Configuration.PRIVATEKEY);
		
		
		Option<?>[] options = new Option[]{neighbor, self, selfPort, publicKey, privateKey};
		
		try {
			parser.parse(args);
		} catch (OptionException e) {
			return;
		}
		
		for(Option<?> option : options){
			
			Object o = parser.getOptionValue(option);
			if(o != null){
				conf.put(option.longForm(), o);
			}
			
		}
		
	}
	
}
