package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Main.MainGenesisNode;
import Main.Tangle;
import Main.Transaction;
import conf.Configuration;
import model.Hash;
import model.HexString;

public class DatastructureSpeedTest {

	public static void main(String[] args) {
		
		int size = 1000000;
		
		System.out.println(System.getProperty("os.name"));
		
		ArrayList<Transaction> list = new ArrayList<>();
		
		long start = System.currentTimeMillis();
		
		for(int i = 0 ; i < size ; i++) {
			
			list.add(new Transaction(null, null, i));
			
		}
		
		long diff = System.currentTimeMillis() - start;
		
		System.out.println("Insert ArrayList: " + diff/1000D);
		
		Map<Hash, Transaction> map = new HashMap<>();
		
		start = System.currentTimeMillis();
		for(int i = 0 ; i < size ; i++) {
			
			list.add(new Transaction(null, null, 2));
			
		}
		diff = System.currentTimeMillis() - start;
		
		System.out.println("Insert HashMap: " + diff/1000D);
		
		
		start = System.currentTimeMillis();
		for(int i = 0 ; i < size ; i++) {
			
			for(Transaction t : list)
			{
				if(t.getValue() == i) {
					break;
				}
			}
		}
		diff = System.currentTimeMillis() - start;
		System.out.println("Find ArrayList: " + diff/1000D);
		
		
		start = System.currentTimeMillis();
		for(Hash h : map.keySet()) {

			map.get(h);
		}
		diff = System.currentTimeMillis() - start;
		System.out.println("Find HashMap: " + diff/1000D);
		
	}
	
}
