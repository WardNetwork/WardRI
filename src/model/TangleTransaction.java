package model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import Main.Tangle;
import Main.Transaction;

public class TangleTransaction extends TransactionBase{
	
	Tangle tangle;
    
    private volatile Set<TangleTransaction> confirmed;
    private double nodeWeight;
    List<TangleTransaction> nodesWhichConfirmedMe;

    public static TangleTransaction fromTransaction(Transaction t, Tangle tangle){
    	return fromTransaction(t, tangle, true);
    }
    
    /**
     * 
     * @param t
     * @param tangle
     * @param updateReferences Indicates weather the tangle should be searched for all Confirmation Transactions and update their references to Tx t
     * @return
     */
    public static TangleTransaction fromTransaction(Transaction t, Tangle tangle, boolean updateReferences){
    	if(!t.isSealed()){
    		System.out.println("Transaction not sealed!!!");
    		Thread.dumpStack();
    		return null;
    	}
    	return new TangleTransaction(t, tangle, updateReferences);
    }
    
    //TODO Algorithmen implementieren
    
    public TangleTransaction(Transaction t, Tangle tangle, boolean updateReferences){
    	
    	this.tangle = tangle;
    	this.confirmed = new CopyOnWriteArraySet<>();
    	this.nodesWhichConfirmedMe = new ArrayList<>();
    	
    	if(updateReferences){
    		for(Hash h : t.getConfirmed()){
    			TangleTransaction conf = tangle.findTangleTransaction(h);
	    		this.confirmed.add(conf);
    		}
    		for(String h : t.getNodeWhichConfirmedThisNode()){
    			TangleTransaction conf = tangle.findTangleTransaction(Hash.fromHashString(h));
	    		this.nodesWhichConfirmedMe.add(conf);
    		}
    		
    	}
	    	
        this.powProof = t.getTransactionProof();
        this.sender = t.getSender();
        this.reciever = t.getReciever();
        this.value = t.getValue();
        this.createdTimestamp = t.getCreatedTimestamp();
        this.TxId = t.getTxHash();
        this.DEBUGId = t.DEBUGgetDEBUGId();
        this.data = null; //TODO DATA
        this.setSignature(t.getSignature());
        
    	
    }

	public Set<TangleTransaction> getConfirmed() {
		if(isSealed()){
    		return Collections.unmodifiableSet(confirmed);
    	}
		return confirmed;
	}

	public double getNodeWeight() {
		return nodeWeight;
	}

	public List<TangleTransaction> getNodesWhichConfirmedMe() {
		return nodesWhichConfirmedMe;
	}
	
	public double getNodePOWWeight() { //TODO Useless, oder?
		if (this.nodeWeight == 0.0) {
		    this.nodeWeight = this.calculatePOWWeight();
		}
		return this.nodeWeight;
	}
  
	private double calculatePOWWeight() {
		String powResult;
		int i;
		for (powResult = this.powProof.getResult(), i = 0; i < powResult.length() && powResult.charAt(i) == '0'; ++i) {
		}
		return i * Math.pow(10.0, i);
	}
	
	public Transaction toTransaction(){
		Transaction t = new Transaction(sender, reciever, value, createdTimestamp);
		
		t.data = data;
		t.powProof = this.powProof;
		t.DEBUGId = this.DEBUGId;
		t.sealed = sealed;
		t.signature = signature;
		
		return t;
	}

	public double calculateCumulativeNodeWeight(final Tangle tangle) {
		final List<TangleTransaction> alreadyChecked = new ArrayList<TangleTransaction>();
		double weight = 0.0;
		final Queue<TangleTransaction> q = new LinkedList<TangleTransaction>();
		q.add(this);
		alreadyChecked.add(this);
		while (!q.isEmpty()) {
			TangleTransaction n = q.remove();
			List<TangleTransaction> children = n.getNodesWhichConfirmedMe();
			for (TangleTransaction t : children) {
				weight += t.getNodePOWWeight();
				if (!alreadyChecked.contains(t)) {
					q.add(t);
					alreadyChecked.add(t);
				}
			}
		}
		return weight;
	}

	protected String createSHAString() {
		String s = this.createdTimestamp + "" + this.sender + this.reciever + this.value;

		for (TangleTransaction h : getConfirmed()) {
			s += h.getTxHash().getHashString();
		}

		return s;
	}

}
