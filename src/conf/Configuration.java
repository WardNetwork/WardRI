package conf;

import java.util.HashMap;
import java.util.Map;

import model.HexString;

public class Configuration {
	
	Map<String, Object> props = new HashMap<>();
	
	public void put(String s, Object o){
		props.put(s, o);
	}
	
	public Object get(String s){
		return props.get(s);
	}
	
	public int getInt(String s){
		Object o = get(s);
		if(o instanceof String){
			try{
				return Integer.parseInt((String)o);		
			}catch(NumberFormatException e){};
		}else{
			return ((Integer)o).intValue();
		}
		return 0;
	}
	
	public String getString(String s){
		Object o = get(s);
		if(o == null || !(o instanceof String)){
			return null;
		}
		return (String)o;
	}
	
    public HexString getHexString(String s) {
    	Object o = get(s);
    	if(o == null)
    		return null;
    	if(o instanceof HexString) {
    		return (HexString)o;
    	}
    	else if(o instanceof String) {
    		return HexString.fromHashString(o.toString());
    	}
        return null;
    }
	
	public static final String NEIGHBOR = "neighbor";
	public static final String PORT = "port";
	public static final String SELF = "self";
	public static final String SELFPORT = "selfport";
	public static final String PUBLICKEY = "publickey";
	public static final String PRIVATEKEY = "privatekey";
	
}
