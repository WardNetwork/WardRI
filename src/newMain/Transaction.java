package newMain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Main.Tangle;
import Main.TransactionProof;
import model.Hash;
import model.HexString;

public class Transaction implements Hashable{
	
	protected static Logger log = LoggerFactory.getLogger(Transaction.class);
	
	private Hash TxId;
	private HexString sender;
	private HexString reciever;
	
	private double value;
    
	private Object data;
    
	private long createdTimestamp;
	protected TransactionProof powProof;
    
    protected HexString signature;
    
    protected Set<TransactionReference> confirmed;
    protected Set<Transaction> confirmedBy;
    
    public Transaction(HexString sender, HexString reciever, double value, Object data, long createdTimestamp,
			String powProof, HexString signature, Set<TransactionReference> confirmed) {
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.value = value;
		this.data = data;
		this.createdTimestamp = createdTimestamp;
		this.signature = signature;
		this.confirmed = confirmed;
		this.TxId = CryptoUtil.hashSHA256(createHashString());
		this.powProof = new TransactionProof(this.TxId, powProof);
	}

	public double getNodePowWeight(){
    	String powResult;
		int i;
		for (powResult = this.powProof.getResult(), i = 0; i < powResult.length() && powResult.charAt(i) == '0'; ++i) {}
		return i * Math.pow(10.0, i);
    }
    
    public double calculateCumulativeNodeWeight(final Tangle tangle) {
		final List<Transaction> alreadyChecked = new ArrayList<Transaction>();
		double weight = 0.0;
		final Queue<Transaction> q = new LinkedList<Transaction>();
		q.add(this);
		alreadyChecked.add(this);
		while (!q.isEmpty()) {
			Transaction n = q.remove();
			Set<Transaction> children = n.getConfirmedBy();
			for (Transaction t : children) {
				weight += t.getNodePowWeight();
				if (!alreadyChecked.contains(t)) {
					q.add(t);
					alreadyChecked.add(t);
				}
			}
		}
		return weight;
	}

	public Set<TransactionReference> getConfirmed() {
		return confirmed;
	}

	public Set<Transaction> getConfirmedBy() {
		return confirmedBy;
	}

	public HexString getSender() {
		return sender;
	}

	public HexString getReciever() {
		return reciever;
	}

	public Hash getTxId() {
		return TxId;
	}

	public double getValue() {
		return value;
	}

	public long getCreatedTimestamp() {
		return createdTimestamp;
	}

	public TransactionProof getPowProof() {
		return powProof;
	}

	public HexString getSignature() {
		return signature;
	}

	@Override
	public String createHashString() {
		String s = String.valueOf(this.createdTimestamp) + this.sender + this.reciever + this.value;
        
    	for(TransactionReference h : getConfirmed()){
    		s += h.getTxId().getHashString();
    	}
    	
    	return s;
	}
	
}
