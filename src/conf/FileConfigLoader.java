package conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileConfigLoader {

	public void loadInConfig(File f, Configuration conf){
		
		try {
			List<String> lines = Files.readAllLines(f.toPath());
			
			for(String s : lines){
				
				if(!s.startsWith("#") && s.contains("=")){
					
					String[] arr = s.split("=");
					conf.put(arr[0].trim(), arr[1].trim());
					
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void saveIntoConfigFile(Configuration conf, String key, File f){
		
		//TODO implement
		
	}
	
}
