package newMain;

import java.util.Comparator;

import model.Hash;

public class TransactionReference {

	Hash hash;
	Transaction t;
	
	public TransactionReference(Hash hash){
		this.hash = hash;
	}
	
	public TransactionReference(Transaction t){
		this.t = t;
	}

	public Hash getTxId() {
		if(hash != null){
			return hash;
		}else if(t != null){
			hash = t.getTxId();
			return hash;
		}else{
			return null;
		}
	}

	public Transaction getTransaction(DAG dag) {
		if(t != null){
			return t;
		}else if(hash != null){
			Transaction tx = dag.findTransaction(hash);
			if(tx != null){
				t = tx;
				return t;
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
    public static Comparator<TransactionReference> getComparator(){
    	return (x, y) -> {
			if(y == null || x == null){
				return 0;
			}
			return x.getTxId().getHashString().compareTo(y.getTxId().getHashString());
		};
    }
	
}
