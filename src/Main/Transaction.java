package Main;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import Interfaces.TangleInterface;
import model.Hash;
import model.HexString;
import model.Storeable;
import model.TransactionBase;
import network.ObjectSerializer;

public class Transaction extends TransactionBase implements Storeable
{
   
    private volatile Set<Hash> confirmed;
//    private double nodeWeight;
    List<String> nodesWhichConfirmedMe;
    
    @SuppressWarnings("deprecation")
	public Transaction() {
        Transaction.DEBUGidCount++;
        this.DEBUGId = Transaction.DEBUGidCount;
    }
    
    public Transaction(HexString sender, HexString reciever, int value) {
        this.confirmed = new CopyOnWriteArraySet<>();
        this.nodesWhichConfirmedMe = new ArrayList<String>();
        this.powProof = null;
        this.sender = sender;
        this.reciever = reciever;
        this.value = value;
        this.createdTimestamp = System.nanoTime() - 262430000000000L;
//        this.TxId = this.hashCodeSHA();
        Transaction.DEBUGidCount++;
        this.DEBUGId = Transaction.DEBUGidCount;
    }

    //TODO Remove
    public Transaction(final HexString sender, final HexString reciever, final int value, final boolean isDummy) {
        this(sender, reciever, value);
        if (isDummy) {
            --Transaction.DEBUGidCount;
            this.DEBUGId = Integer.MAX_VALUE;
        }
    }
    
    public Transaction(HexString sender, HexString reciever, int value, long createdTimeStamp) {
        this(sender, reciever, value);
        this.createdTimestamp = createdTimeStamp;
//        this.TxId = this.hashCodeSHA();
    }
    
    public void addConfimationNodeReference(String nodeId) {
        this.nodesWhichConfirmedMe.add(nodeId);
    }
    
    public List<String> getNodeWhichConfirmedThisNode() {
        return this.nodesWhichConfirmedMe;
    }
    
    public Set<Hash> getConfirmed() {
    	if(isSealed()){
    		return Collections.unmodifiableSet(confirmed);
    	}
        return this.confirmed;
    }
    
//    public double getNodePOWWeight() { //TODO Useless, oder?
//        if (this.nodeWeight == 0.0) {
//            this.nodeWeight = this.calculatePOWWeight();
//        }
//        return this.nodeWeight;
//    }
//    
//    private double calculatePOWWeight() {
//        String powResult;
//        int i;
//        for (powResult = this.powProof.getResult(), i = 0; i < powResult.length() && powResult.charAt(i) == '0'; ++i) {}
//        return i * Math.pow(10.0, i);
//    }
//    
//    public double calculateNodeWeight(final Tangle tangle) {
//        final List<Transaction> alreadyChecked = new ArrayList<Transaction>();
//        double weight = 0.0;
//        final Queue<Transaction> q = new LinkedList<Transaction>();
//        q.add(this);
//        alreadyChecked.add(this);
//        while (!q.isEmpty()) {
//            final Transaction n = q.remove();
//            final List<Transaction> children = tangle.findAllTransactions(n.nodesWhichConfirmedMe);
//            for (final Transaction t : children) {
//                weight += t.getNodePOWWeight();
//                if (!alreadyChecked.contains(t)) {
//                    q.add(t);
//                    alreadyChecked.add(t);
//                }
//            }
//        }
//        return weight;
//    }
    
    public void doPoW() {
    	
    	seal();
        final TransactionProof proof = new TransactionProof(this.hashCodeSHA());
        proof.solve();
        this.powProof = proof;
    }
    
    private void seal() {
    	if(!sealed){
	    	sealed = true;  //TODO Sealed umsetzen und wirklich immutable machen
	    	this.TxId = hashCodeSHA();
    	}
	}

	public void confirm(Transaction appended) {
		checkSealed();
        if (appended == null) {
            Thread.dumpStack();
            System.out.println("Transaction couldn´t be confirmed because tx was null");
            return;
        }
//        appended.addConfimationNodeReference(this.getTxHash().getHashString()); TODO removed bec. finish
        this.confirmed.add(appended.getTxHash());
    }
    
//    public void finish() {
//    	
//    	this.TxId = hashCodeSHA();
//    	
//    	for(Hash t : confirmed) {
//    		
//    		if(!t.isFinished()) {
//    			throw new IllegalArgumentException("Confirmed Transaction is not finished, aborting");
//    		}
//    		
//            t.addConfimationNodeReference(this.getTxHash().getHashString());
//    	}
//    	
//    	if(!this.powProof.getHash().equals(TxId)) {
//    		throw new IllegalArgumentException("PowProof was created with invalid Hash");
//    	}
//    	
//    	this.finished = true;
//    	
//    }
    
    
    @Override
    public String toString() {
        String s = "";
        for (Hash t : confirmed) {
            s = String.valueOf(s) + t.toString() + ", ";
        }
        return "Tx" + this.TxId + " confirmed: " + s + " ";
    }
    
    protected String createSHAString() {
    	String s = String.valueOf(this.createdTimestamp) + this.sender + this.reciever + this.value;
        
    	for(Hash h : getConfirmed()){
    		s += h.getHashString();
    	}
    	
    	return s;
    }
    
    public static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    
    public void sign(PrivateKey privateKey, PublicKey publicKey) {
        try {
            Signature dsa = Signature.getInstance(SIGNATURE_ALGORITHM);
            dsa.initSign(privateKey);
            dsa.update(publicKey.getEncoded());
            byte[] signatre = dsa.sign();
            this.signature = new HexString(signatre);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }
    
    public boolean validateSignature(PublicKey publicKey) { //TODO integrieren
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(publicKey.getEncoded());
            boolean verified = sig.verify(this.signature.getHash());
            return verified;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Transaction) {
			Transaction t = (Transaction)obj;
			return t.hashCodeSHA().equals(this.hashCodeSHA());
		}
		return super.equals(obj);
	}

	@Override
	public String store() {
		if(!isSealed()) {
			throw new IllegalStateException("Transaction is not finished!");
		}
		
		return new ObjectSerializer().serialize(this);  //TODO evtl Objectserializer ganz entfernen
	}
	
	public Transaction readStore(String s, Tangle tangle) {
		
        String[] tokenized = s.split(" ");
        
        if(tokenized[2].startsWith("0x")){
        	System.out.println(s);
        }
        
        this.sender = HexString.fromHashString(tokenized[0]);
        this.reciever = HexString.fromHashString(tokenized[1]);
        this.value = Integer.parseInt(tokenized[2]);
        this.createdTimestamp = Long.parseLong(tokenized[3]);
        
        this.setPoWSolution(tokenized[4]);
        this.setSignature(HexString.fromHashString(tokenized[5]));
        for (int i = 6; i < tokenized.length; ++i) {
            String confirmation = tokenized[i];
            this.getConfirmed().add(Hash.fromHashString(confirmation));
        }
	       
        return this;
	}
	
	public Transaction readStore(String s) {
		throw new UnsupportedOperationException("Use readStore(String, Tangle)");
	}
}
