package sharded;

import java.util.Set;

import Main.TransactionProof;
import model.Hash;
import model.HexString;
import newMain.DAGObjectReference;

public interface DAGObject<E extends DAGObject<E>> {

	public Hash getTxId();
	
	public Set<DAGObjectReference<E>> getConfirmed();
	
	public Set<E> getConfirmedBy();
	
	public long getCreatedTimestamp();

	public TransactionProof getPowProof();
	
	public HexString getSignature();
	
	public HexString getSender();
	
}
