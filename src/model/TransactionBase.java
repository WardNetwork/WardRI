package model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Main.Transaction;
import Main.TransactionProof;

public abstract class TransactionBase {
	
	protected static Logger log = LoggerFactory.getLogger(TransactionBase.class);
	protected static int DEBUGidCount = 0;
	
	protected int DEBUGId;
	protected Hash TxId;
	protected HexString sender;
	protected HexString reciever;
    public int value;
    
    protected Object data;  //TODO alles implementieren
    
    protected long createdTimestamp;
    protected TransactionProof powProof;

    //Signature ist hier, weil die Daten behalten werden sollten
    protected HexString signature = null;
    
    protected boolean sealed = false;
    
    protected TransactionBase() {
    	
    }
    
    protected TransactionBase(HexString sender, HexString reciever, int value, long createdTimeStamp) {
    	
        this.powProof = null;
        this.sender = sender;
        this.reciever = reciever;
        this.value = value;
        TransactionBase.DEBUGidCount++;
        this.DEBUGId = TransactionBase.DEBUGidCount;
        this.createdTimestamp = createdTimeStamp;
        
    }
    
    public TransactionProof getTransactionProof() {
        if (powProof == null) {
            log.error("Proof not solved!!");
            return null;
        }
        return powProof;
    }
    
    public void setPoWSolution(final String solution) {
        powProof = new TransactionProof(this.TxId, solution);
        if(!powProof.validateProof()) {
        	log.error("The PoW Solution which was set is not correct!");
        }
    }
    
    public int DEBUGgetDEBUGId() {
    	return DEBUGId;
    }
    
    public Hash getTxHash() {
        return this.TxId;
    }
    
    public HexString getSender() {
        return this.sender;
    }
    
    public HexString getReciever() {
        return this.reciever;
    }
    
    public int getValue() {
        return this.value;
    }
    
    public long getCreatedTimestamp() {
        return this.createdTimestamp;
    }
    
    public boolean isSealed() {
    	return sealed;
    }
    
    protected void checkSealed() {
    	if(isSealed()) {
    		throw new IllegalAccessError("Transaction is sealed");
    	}
    }
    
    @Override
	public boolean equals(Object obj) {
		if(obj instanceof TransactionBase) {
			TransactionBase t = (TransactionBase)obj;
			
			if(!t.getClass().getName().equals(this.getClass().getName())){
				log.error("Not equal Types!!!");
				Thread.dumpStack();
			}
			
			if(t.isSealed()) {
				return t.hashCodeSHA().equals(this.hashCodeSHA());
			}else {
				Function<TransactionBase, String> s = x -> x.getSender().getHashString() + "" + x.getReciever().getHashString() +
						"" + x.getValue() + x.getCreatedTimestamp();
				
				return s.apply(t).equals(s.apply(this));
			}
			
		}
		return super.equals(obj);
	}

	public boolean isSigned() {
		return this.signature != null;
	}

    public HexString getSignature() {
        return this.signature;
    }
    
    public void setSignature(HexString signature) {
        this.signature = signature;
    }
    
    public Hash hashCodeSHA() {
    	try {
            
        	MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
        	String s = createSHAString();
        	
            digest.update(s.getBytes());
            byte[] arr = digest.digest();
            Hash hash = new Hash(arr);
            return hash;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    protected abstract String createSHAString();
}
