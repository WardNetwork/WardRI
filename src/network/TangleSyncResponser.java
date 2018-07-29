package network;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.rpanic.Responser;

import Main.Tangle;
import model.TangleTransaction;

public class TangleSyncResponser implements Responser<String, Socket>{

	Tangle tangle;
	
	public static final int requestTxSize = 200;
	
	public TangleSyncResponser(Tangle tangle) {
		super();
		this.tangle = tangle;
	}

	@Override
	public boolean acceptable(String responseType) {
		return responseType.equals("reqTngl");
	}

	@Override
	public void accept(String response, Socket socket) {
		
		System.out.println("Sync request recieved");
		
		List<TangleTransaction> list = tangle.getTransactions();
		
		Collections.sort(list, (x1, x2) -> Long.compare(x1.getCreatedTimestamp(), x2.getCreatedTimestamp()));
		
		List<List<String>> txStrs = getTxStrs(list);

		try {
			
			Scanner scanner = new Scanner(socket.getInputStream()).useDelimiter(";");
			
			OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
			
			String res;
			
			for(List<String> txStrsSub : txStrs) {
				
				String ret = "resTngl ";
			
				ret += String.join(",", txStrsSub);
				
				writer.write(ret + " ;");
				
				writer.flush();
			
				res = scanner.next();
				
				if(res.startsWith("check")) {
					System.out.println("res:" + res);
					res = scanner.next();
				}
				System.out.println("res:" + res);
				
				if(!res.contains("resTnglAck")) {
					System.out.println("Not Acknoledged by Partner, aborting");
					return;
				}
				
			}
			
			writer.write("endOfTangleSync;");
			writer.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private List<List<String>> getTxStrs(List<TangleTransaction> list) {
		
		//TxStrs
		
		List<List<String>> txStrs = new ArrayList<>();
		
		ObjectSerializer serializer = new ObjectSerializer();
		
		//values for Splitting
		
		int numLists = (int) (list.size() / (double)requestTxSize);
		
		if(list.size() != requestTxSize) {  // +1 because 201 / 200 = 1 -> we need 2 requests
			numLists++;
		}
		
		for(int i = 0 ; i < numLists ; i++) {
			txStrs.add(new ArrayList<>());
		}
		
		int index = 0;
		
		System.out.println("Numlists: " + numLists + " for " + list.size() + " elements ");
		
		for(int i = 0 ; i < list.size() ; i++) {
			
			TangleTransaction t = list.get(i);
			
			String serialized = serializer.serialize(t);
			
			System.out.println(i + " " + serialized);
			
			txStrs.get(index).add(serialized);
			
			if(i % 200 == 0 && i != 0) {
				index++;
			}
			
		}
		
		return txStrs;
		
	}

}
