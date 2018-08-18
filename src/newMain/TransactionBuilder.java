package newMain;

import java.security.KeyPair;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Main.TransactionProof;
import model.Hash;
import model.HexString;

public class TransactionBuilder {

	protected static Logger log = LoggerFactory.getLogger(TransactionBuilder.class);
	
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
    
    private byte fieldFilled = 0;
    private static byte CONFIRMED_FIELD = 1, SIGNATURE_FIELD = 2, PROOF_FIELD = 4;
	
	public TransactionBuilder(HexString sender, HexString reciever, double value, Object data){
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.value = value;
		this.data = data;
		this.createdTimestamp = System.currentTimeMillis();
	}
	
	public TransactionBuilder setConfirmed(Set<TransactionReference> ref){
		this.confirmed = ref;
		this.TxId = CryptoUtil.hashSHA256(createHashString());
		fieldFilled |= CONFIRMED_FIELD;
		return this;
	}
	
	public TransactionBuilder setSignature(HexString signature){
		this.signature = signature;
		fieldFilled |= SIGNATURE_FIELD;
		return this;
	}
	
	public TransactionBuilder sign(KeyPair keypair){
		HexString signature = CryptoUtil.sign(keypair.getPrivate(), keypair.getPublic(), createHashString().getBytes());
		System.out.println(createHashString());
		return setSignature(signature);
	}
	
	public TransactionBuilder solveProof(){
		
		if((fieldFilled & CONFIRMED_FIELD) == CONFIRMED_FIELD){
			
			this.powProof = new TransactionProof(this.TxId);
			this.powProof.solve();
			fieldFilled |= PROOF_FIELD;
			
		}else{
			throw new UnsupportedOperationException("You have to set the Confirmations first!!");
		}
		
		return this;
	}
	
	
	public Transaction buildTransaction(){
		
		if(fieldFilled == 7){
			Transaction t = new Transaction(sender, reciever, value, data, createdTimestamp, powProof.getSolution(), signature, confirmed);
			return t;
		}else{
			throw new UnsupportedOperationException("You have to set all fields first!!");
		}
		
	}
	
	
	public String createHashString(){
		String s = String.valueOf(this.createdTimestamp) + this.sender + this.reciever + this.value;
	    
		for(TransactionReference h : confirmed.stream().sorted(TransactionReference.getComparator()).collect(Collectors.toList())){
			s += h.getTxId().getHashString();
		}
		
		return s;
	}
	
}
