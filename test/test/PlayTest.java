package test;

import java.net.InetAddress;

import org.pmw.tinylog.Logger;
import org.rpanic.ExternalAddress;

public class PlayTest {

	public static void main(String[] args) {
		
		InetAddress add = ExternalAddress.getExternalAddress();
		Logger.info(add.toString());
		
		for(int i = 1 ; i < 100 ; i++){
		int old = i;
		int new1 = old << 1;
		int new2 = new1 + 1;
		
		System.out.println(old + " " + new1 + " " + new2);
		}
		
		//SecureRandom r = new SecureRandom(Hash.fromHashString("0xc1c3b9792b3b99b56e41746055d3250fc597e93a3024cb104bee7b76ee0ae007").getHash());
		//System.out.println(r.nextInt());
		
		/*byte f = 0;
		
		byte field = 2;
		
		f = 2;
		System.out.println((f & field));
		f = 0;
		System.out.println((f & field));*/
		
		
	}
	
}
