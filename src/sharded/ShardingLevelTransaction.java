package sharded;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import Main.TransactionProof;
import model.Hash;
import model.HexString;
import newMain.CryptoUtil;
import newMain.DAGObjectReference;
import newMain.Hashable;

public class ShardingLevelTransaction implements DAGObject<ShardingLevelTransaction>, Hashable{
	
	private Hash TxId;
	private HexString sender;
    
	private Object data;
    
	private long createdTimestamp;
	private long epochNum;
	protected TransactionProof powProof;
    
    public HexString signature;//TODO private
    
    private Set<DAGObjectReference<ShardingLevelTransaction>> confirmed;
    private Set<ShardingLevelTransaction> confirmedBy;
    
	public ShardingLevelTransaction(HexString sender, Object data, long createdTimestamp, long epochNum,
			String powProof, HexString signature, Set<DAGObjectReference<ShardingLevelTransaction>> confirmed) {
		super();
		this.sender = sender;
		this.data = data;
		this.createdTimestamp = createdTimestamp;
		this.signature = signature;
		this.confirmed = confirmed;
		this.confirmedBy = new HashSet<>();
		this.epochNum = epochNum;
		this.TxId = CryptoUtil.hashSHA256(createHashString());
		this.powProof = new TransactionProof(this.TxId, powProof);
		if(!this.powProof.validateProof()){
			throw new UnsupportedOperationException("PoW Proof wrong!");
		}
	}

	@Override
	public Hash getTxId() {
		return TxId;
	}
	
	@Override
	public Set<DAGObjectReference<ShardingLevelTransaction>> getConfirmed() {
		return confirmed;
	}

	@Override
	public Set<ShardingLevelTransaction> getConfirmedBy() {
		return confirmedBy;
	}

	@Override
	public long getCreatedTimestamp() {
		return createdTimestamp;
	}

	@Override
	public TransactionProof getPowProof() {
		return powProof;
	}

	@Override
	public HexString getSignature() {
		return signature;
	}

	@Override
	public HexString getSender() {
		return sender;
	}
	
	public long getEpoch(){
		return epochNum;
	}
	
	public Object getData(){
		return data;
	}

	@Override
	public String createHashString() {
		String s = String.valueOf(this.createdTimestamp) + this.sender + this.data;
        
    	for(DAGObjectReference<ShardingLevelTransaction> h : getConfirmed().stream().sorted(DAGObjectReference.getComparator()).collect(Collectors.toList())){
    		s += h.getTxId().getHashString();
    	}
    	
    	return s;
	}

}
