package newMain;

import model.Hash;
import network.ObjectSerializer;

public class NetworkObjectGetter {
	
	public Transaction resolveTransaction(Hash h, RI ri){
		
		String request = "get tx " + h.getHashString();
		String s = send(ri, request);
		
		return new ObjectSerializer().parseTransaction(s);
		
	}
	
	public String send(RI ri, String s){
		//TODO this process needs to be implemented so that we get an answer if one exists in the network
		return ri.getShardedPool().getRandomNeighbor().send(s);
	}
	
}
