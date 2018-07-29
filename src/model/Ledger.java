package model;

import java.util.HashMap;
import java.util.Map;

public class Ledger {

	Map<HexString, Double> ledger = new HashMap<>();
	
	boolean genesisTransaction = false;
	
	public Ledger(TransactionBase genesis){
		
		ledger.put(genesis.getReciever(), (double)genesis.getValue());
		genesisTransaction = true;
		
	}
	
	public Ledger(Map<HexString, Double> ledger) {
		this.ledger = ledger;
		genesisTransaction = ledger.size() > 0;
	}
	
	public Ledger(){}
	
	public void addTransaction(TransactionBase t){
		
		if(genesisTransaction){  //IF Genesistransaction was already issued
			ledger.put(t.getSender(), doubleValue(ledger.get(t.getSender())) - t.getValue());
		}else {
			genesisTransaction = true; //When no ledger was created in the constructor
		}
		
		ledger.put(t.getReciever(), doubleValue(ledger.get(t.getReciever())) + t.getValue());
	}
	
	public void correctTransaction(TransactionBase t){
		
		if(genesisTransaction){
			
			Double sender = ledger.get(t.getSender());
			double reciever = doubleValue(ledger.get(t.getReciever()));
			
			if(reciever < t.getValue() || sender == null){
				throw new IllegalArgumentException("Illegal state, transaction cannot be reversed!! \n " + t.toString());
			}
			
			ledger.put(t.getSender(), sender + t.getValue());
			ledger.put(t.getReciever(), reciever - t.getValue());
			
		}else{
			System.err.println("ERORR 12321 - no genesis transaction issued. Who programmed this shit?");
		}
		
	}
	
	public boolean validTransaction(TransactionBase t){
		
		Double d = ledger.get(t.getSender());
		
		d = d == null ? 0D : d;
		
		return d >= t.getValue();
		
	}
	
	private double doubleValue(Double d){
		return d == null ? 0 : d;
	}
	
	public Double getBalance(HexString account){
		return ledger.get(account);
	}
	
	public Map<HexString, Double> getMap(){
		return ledger;
	}
	
	public Map<HexString, Double> toMap(){
		return new HashMap<>(ledger);
	}
	
	@Override
	public String toString() {
		return ledger.toString();
	}
	
}
